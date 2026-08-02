import { useState, useMemo, useRef, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { NotePencil, Flag } from '@phosphor-icons/react'
import ScoringOverviewCards from '../../../../../components/coordinator/events/ScoringOverviewCards'
import ScoringFilterBar from '../../../../../components/coordinator/events/ScoringFilterBar'
import ScoreTable from '../../../../../components/coordinator/events/ScoreTable'
import ModalShell from '../../../../../components/shared/ModalShell'
import RequestsSection from '../../../../../components/coordinator/roundResults/RequestsSection'
import ViolationHandlingModal from '../../../../../components/coordinator/roundResults/ViolationHandlingModal'
import ScoreDistributionModal from '../../../../../components/coordinator/roundResults/ScoreDistributionModal'
import styles from './ScoringTab.module.css'
import axiosClient from '../../../../../api/axiosClient'

// -- Tab Chấm điểm trong SpecificEventPage (đã kết nối API thật) --

function ScoringTab() {
  const { eventId } = useParams()

  // ── Filter state ──
  const [roundId, setRoundId] = useState(null)   // null = chưa chọn (đợi fetch)
  const [trackId, setTrackId] = useState('all')
  const [judgeId, setJudgeId] = useState('all')
  const [search, setSearch] = useState('')

  // ── Dữ liệu từ API ──
  const [rounds, setRounds] = useState([])
  const [tracks, setTracks] = useState([{ id: 'all', name: 'Tất cả' }])
  const [criteria, setCriteria] = useState([])
  const [entries, setEntries] = useState([])     // đã map sang shape ScoreTable
  const [judges, setJudges] = useState([])       // JudgeSummaryDTO đã map
  const [violations, setViolations] = useState([])
  const [pendingScoreEdits, setPendingScoreEdits] = useState(0)

  const [loadingRounds, setLoadingRounds] = useState(true)
  const [loadingResult, setLoadingResult] = useState(false)

  // ── Modal state ──
  const [isScoreEditModalOpen, setIsScoreEditModalOpen] = useState(false)
  const [isViolationModalOpen, setIsViolationModalOpen] = useState(false)
  const [directViolationTeam, setDirectViolationTeam] = useState(null)
  const [scoreDistributionTeam, setScoreDistributionTeam] = useState(null)
  const [selectedTeamId, setSelectedTeamId] = useState(null)
  const [highlightJudgeId, setHighlightJudgeId] = useState(null)

  const tableRef = useRef(null)

  // ── Fetch danh sách vòng và track khi mount ──
  useEffect(() => {
    if (!eventId) return
    const fetchMeta = async () => {
      setLoadingRounds(true)
      try {
        const [roundsRes, tracksRes] = await Promise.all([
          axiosClient.get(`/round?eventId=${eventId}`),
          axiosClient.get(`/track?eventId=${eventId}`)
        ])

        const mappedRounds = roundsRes.data.map((r) => ({
          id: r.roundId,
          name: r.roundName,
          isCurrent: r.status === 'IN_PROGRESS',
        }))
        setRounds(mappedRounds)

        // Lấy criteria từ vòng đầu tiên (dùng chung cho toàn bộ tab)
        const firstRoundCriteria = roundsRes.data[0]?.criteria || []
        setCriteria(firstRoundCriteria.map((c) => ({
          id: String(c.id),
          realId: String(c.id),   // dùng để map điểm từ scores["1"], scores["2"]
          name: c.name,
          weight: c.weight,
        })))

        // Chọn vòng đang diễn ra làm mặc định
        const activeRound = mappedRounds.find(r => r.isCurrent) || mappedRounds[0]
        if (activeRound) setRoundId(activeRound.id)

        setTracks([
          { id: 'all', name: 'Tất cả' },
          ...tracksRes.data.map((t) => ({ id: String(t.id), name: t.name }))
        ])
      } catch (err) {
        console.error('Lỗi fetch meta ScoringTab:', err)
      } finally {
        setLoadingRounds(false)
      }
    }
    fetchMeta()
  }, [eventId])

  // ── Fetch kết quả chấm theo vòng + track ──
  useEffect(() => {
    if (!roundId) return
    const fetchResult = async () => {
      setLoadingResult(true)
      try {
        const trackParam = trackId && trackId !== 'all' ? `&trackId=${trackId}` : ''
        const { data } = await axiosClient.get(`/round/${roundId}/results?${trackParam.slice(1)}`)

        // Map entries: EntryDTO → shape ScoreTable
        // - trackId: khi xem 'tất cả', gán sentinel '__all__' để ScoreTable có thể group được
        // - judgeId: dùng tên làm key (API không trả về id BGK)
        const effectiveTrackId = (trackId && trackId !== 'all') ? trackId : '__all__'
        const mappedEntries = (data.entries || []).map((e) => ({
          teamId: String(e.team.id),
          teamName: e.team.name,
          roundId: String(roundId),
          trackId: effectiveTrackId,
          discrepancy: e.discrepancy ? { stdDev: e.discrepancy.stdDev } : null,
          violation: e.violation ? { reason: e.violation.reason || '' } : null,
          perJudge: (e.perJudge || []).map((pj, idx) => ({
            // Dùng tên BGK làm judgeId → phải đồng bộ với judges[].id bên dưới
            judgeId: pj.judge || `judge_${idx}`,
            judgeName: pj.judge,
            submitted: pj.submitted,
            total: pj.total,
            scores: pj.scores || {},
          })),
        }))
        setEntries(mappedEntries)

        // Map judges: dùng tên làm id để đồng bộ với entries[].perJudge[].judgeId
        const mappedJudges = (data.judges || []).map((j) => ({
          id: j.name,      // Dùng tên thống nhất với perJudge.judgeId
          name: j.name,
          assigned: j.assigned,
          scored: j.scored,
        }))
        setJudges(mappedJudges)

      } catch (err) {
        console.error('Lỗi fetch result ScoringTab:', err)
        setEntries([])
        setJudges([])
      } finally {
        setLoadingResult(false)
      }
    }
    fetchResult()
  }, [roundId, trackId])

  // ── Fetch violations + score-edit requests ──
  useEffect(() => {
    const fetchRequests = async () => {
      try {
        const vioRes = await axiosClient.get('/system-requests/violations')
        setViolations(Array.isArray(vioRes.data) ? vioRes.data : [])
      } catch { /* bỏ qua */ }

      try {
        const editRes = await axiosClient.get('/system-requests/score-edits')
        const editData = Array.isArray(editRes.data) ? editRes.data : []
        setPendingScoreEdits(editData.filter(r => r.status === 'PENDING').length)
      } catch { /* bỏ qua */ }
    }
    fetchRequests()
  }, [])

  // ── Khi filter vòng thay đổi → reset các filter con ──
  const handleRoundChange = (id) => {
    setRoundId(id)
    setTrackId('all')
    setJudgeId('all')
    setSelectedTeamId(null)
  }

  // ── Lọc entries theo judge (phía FE, vì API không hỗ trợ) ──
  const filteredEntries = useMemo(() => {
    if (judgeId === 'all') return entries
    return entries.filter(e =>
      (e.perJudge || []).some(j => j.judgeId === judgeId && j.submitted)
    )
  }, [entries, judgeId])

  // ── Tính tổng quan từ data thật ──
  const overview = useMemo(() => {
    const totalTeams = entries.length
    const totalScored = entries.filter(e => {
      const js = e.perJudge || []
      return js.length > 0 && js.every(j => j.submitted)
    }).length
    const totalPending = totalTeams - totalScored
    const pendingViolations = violations.filter(v => !v.resolved).length
    return { totalTeams, totalScored, totalPending, pendingScoreEdits, pendingViolations }
  }, [entries, violations, pendingScoreEdits])

  // ── Judges hiển thị theo filter ──
  const visibleJudges = useMemo(() => {
    if (judgeId === 'all') return judges
    return judges.filter(j => j.id === judgeId)
  }, [judges, judgeId])

  if (loadingRounds) {
    return <div className={styles.tab}><p className={styles.pageDesc}>Đang tải dữ liệu...</p></div>
  }

  return (
    <div className={styles.tab}>
      {/* Header */}
      <header className={styles.header}>
        <h2 className={styles.pageTitle}>Chấm điểm</h2>
        <p className={styles.pageDesc}>
          Theo dõi tiến độ chấm, phân tích phân bố điểm và phát hiện bất đồng giữa BGK.
        </p>
      </header>

      {/* ① Tổng quan tính từ data thật */}
      <div className={styles.topSection}>
        <div className={styles.overviewWrap}>
          <ScoringOverviewCards
            overview={overview}
            onOpenScoreEditRequests={() => setIsScoreEditModalOpen(true)}
            onOpenViolationRequests={() => setIsViolationModalOpen(true)}
          />
        </div>
      </div>

      {/* ② Filter bar */}
      <ScoringFilterBar
        rounds={rounds}
        tracks={tracks}
        judges={judges}
        roundId={roundId}
        trackId={trackId}
        judgeId={judgeId}
        search={search}
        onRoundChange={handleRoundChange}
        onTrackChange={(id) => { setTrackId(id); setSelectedTeamId(null) }}
        onJudgeChange={setJudgeId}
        onSearch={setSearch}
      />

      {/* Loading state */}
      {loadingResult && (
        <p className={styles.pageDesc} style={{ padding: '1em 0' }}>Đang tải điểm...</p>
      )}

      {/* ③ Bảng tổng hợp */}
      {!loadingResult && (
        <div ref={tableRef}>
          <ScoreTable
            entries={filteredEntries}
            judges={visibleJudges}
            tracks={
              // ScoreTable groupByTrack dùng track.id để match với entry.trackId
              // Khi 'all': entries có trackId='__all__' → cần track tương ứng
              // Khi cụ thể: entries có trackId=trackId → dùng tracks thật (bỏ item 'all')
              trackId === 'all'
                ? [{ id: '__all__', name: 'Tất cả hạng mục' }]
                : tracks.filter(t => t.id !== 'all')
            }
            criteria={criteria}
            search={search}
            highlightJudgeId={highlightJudgeId}
            onHighlightJudge={setHighlightJudgeId}
            onSelectTeam={setSelectedTeamId}
            selectedTeamId={selectedTeamId}
            onOpenScoreEditRequests={() => setIsScoreEditModalOpen(true)}
            onOpenViolationRequests={(teamId) => {
              if (teamId) {
                const entry = entries.find(e => String(e.teamId) === String(teamId))
                const v = violations.find(vio => vio.teamName === entry?.teamName)
                if (v) setDirectViolationTeam(v)
                else setIsViolationModalOpen(true)
              } else {
                setIsViolationModalOpen(true)
              }
            }}
            onOpenScoreDistribution={(teamId) => {
              const entry = entries.find(e => String(e.teamId) === String(teamId))
              if (entry) setScoreDistributionTeam(entry)
            }}
          />
        </div>
      )}


      {/* Modal yêu cầu chỉnh điểm */}
      {isScoreEditModalOpen && (
        <ModalShell
          onClose={() => setIsScoreEditModalOpen(false)}
          size="md"
          title={`Yêu cầu chỉnh điểm (${overview.pendingScoreEdits})`}
          subtitle="Danh sách các yêu cầu điều chỉnh lại điểm từ Ban giám khảo."
          titleColor="var(--color-primary-green)"
          subtitleColor="var(--color-text-primary)"
          icon={<NotePencil size={24} weight="fill" color="var(--color-primary-green)" />}
        >
          <div style={{ paddingTop: '1em' }}>
            <RequestsSection type="scoreEdit" hideHeader={true} />
          </div>
        </ModalShell>
      )}

      {/* Modal vi phạm tổng hợp */}
      {isViolationModalOpen && (
        <ModalShell
          onClose={() => setIsViolationModalOpen(false)}
          size="md"
          title={`Xử lí vi phạm (${overview.pendingViolations})`}
          subtitle="Danh sách các đội thi bị báo cáo vi phạm cần Ban tổ chức xem xét."
          titleColor="var(--color-primary-orange)"
          subtitleColor="var(--color-text-primary)"
          icon={<Flag size={24} weight="fill" color="var(--color-primary-orange)" />}
        >
          <div style={{ paddingTop: '1em' }}>
            <RequestsSection type="violation" hideHeader={true} />
          </div>
        </ModalShell>
      )}

      {/* Modal xử lý vi phạm cụ thể */}
      {directViolationTeam && (
        <ViolationHandlingModal
          isOpen={true}
          onClose={() => setDirectViolationTeam(null)}
          onHandled={() => setDirectViolationTeam(null)}
          data={directViolationTeam}
          onOpenTeam={() => {}}
          onOpenSubmission={() => {}}
        />
      )}

      {/* Modal phân bố điểm */}
      {scoreDistributionTeam && (
        <ScoreDistributionModal
          isOpen={true}
          onClose={() => setScoreDistributionTeam(null)}
          data={{
            teamName: scoreDistributionTeam.teamName,
            criteria,
            judges: (scoreDistributionTeam.perJudge || []).map(pj => ({
              id: pj.judgeId,
              name: pj.judgeName,
              scores: pj.scores || {}
            }))
          }}
        />
      )}
    </div>
  )
}

export default ScoringTab

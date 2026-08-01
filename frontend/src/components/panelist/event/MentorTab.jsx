import { useState, useMemo, useRef, useEffect } from 'react'
import { ChatCircleDots, CheckCircle } from '@phosphor-icons/react'
import Badge from '../../shared/Badge'
import Button from '../../shared/Button'
import MentorTeamTable from './MentorTeamTable'
import MentorRequestModal from './MentorRequestModal'
import styles from './MentorTab.module.css'
import axiosClient from '../../../api/axiosClient'

// "Vừa xong" / "x phút trước" / "x giờ trước" / "x ngày trước"
function timeAgo(iso) {
  if (!iso) return ''
  const diff = Date.now() - new Date(iso).getTime()
  const min = Math.floor(diff / 60000)
  if (min < 1) return 'Vừa xong'
  if (min < 60) return `${min} phút trước`
  const hour = Math.floor(min / 60)
  if (hour < 24) return `${hour} giờ trước`
  return `${Math.floor(hour / 24)} ngày trước`
}

/**
 * MentorTab — nội dung chính tab Mentor.
 * Gồm: box câu hỏi chờ xử lí (cuộn ngang) ở trên cùng + bảng đội phụ trách.
 * Popup câu hỏi theo từng đội được quản lý tại đây.
 *
 * @param {object} event
 */
function MentorTab({ event }) {
  const mentor = event.assignment?.mentor ?? {}
  const rawTeams = mentor.teams ?? []
  // Milestones = danh sách các vòng thi (theo thứ tự vòng)
  const milestones = mentor.milestones ?? []
  const eventId = event.id

  // State lưu điểm & hạng từng team theo vòng
  // Shape: { [teamId]: { score: number|null, rank: number|null, roundName: string|null } }
  const [teamScoresMap, setTeamScoresMap] = useState({})

  // Gọi API song song để lấy điểm của tất cả team khi component mount
  useEffect(() => {
    if (!eventId || rawTeams.length === 0) return

    const fetchAll = async () => {
      const results = await Promise.allSettled(
        rawTeams.map((team) =>
          axiosClient
            .get(`/team-results/events/${eventId}/teams/${team.id}/results`)
            .then((res) => ({ teamId: team.id, data: res.data }))
        )
      )

      const map = {}
      results.forEach((result) => {
        if (result.status === 'fulfilled') {
          const { teamId, data } = result.value
          if (!Array.isArray(data) || data.length === 0) return

          // Chỉ hiển thị điểm khi publishStage đã ở giai đoạn 3 (công bố chính thức)
          // Backend trả về teamTotalScore = null nếu chưa publish ở stage >= 3.
          // Tìm kết quả vòng mới nhất có điểm (không null).
          const withScore = data.filter((r) => r.teamTotalScore != null)
          if (withScore.length === 0) return

          // Sắp xếp theo ordinalNumber giảm dần, lấy vòng mới nhất có điểm
          withScore.sort((a, b) => (b.ordinalNumber ?? 0) - (a.ordinalNumber ?? 0))
          const latest = withScore[0]

          map[teamId] = {
            score: latest.teamTotalScore,
            rank: latest.teamRank?.rank ?? null,
            roundName: latest.roundName ?? null,
          }
        }
      })

      setTeamScoresMap(map)
    }

    fetchAll()
  }, [eventId, rawTeams.length])

  // Tính danh sách teams với đầy đủ status, currentRound, stoppedRound, score, rank
  const teams = useMemo(() => {
    // Tìm số vòng tối đa mà bất kỳ team nào đã hoàn thành trong nhóm
    // Dùng làm heuristic: team nào done < maxDone → đã bị loại (dừng lại)
    const maxDone = rawTeams.reduce(
      (max, t) => Math.max(max, t.progress?.done ?? 0),
      0
    )

    return rawTeams.map((team) => {
      const done = team.progress?.done ?? 0
      const total = team.progress?.total ?? milestones.length

      // Team bị loại khi có team khác đã tiến xa hơn (done < maxDone)
      const isStopped = done < maxDone
      const derivedStatus = isStopped ? 'stopped' : 'competing'

      // Badge hiển thị vòng mới nhất mà team đã tham gia (theo done của team đó)
      // - Nếu done = 0: vòng 1 (chưa nộp bài vòng nào)
      // - Nếu done = 1: milestones[0] (vòng 1 là vòng cuối của team)
      // - Nếu done = 2: milestones[1] (vòng 2 là vòng đang/đã ở)
      const teamRoundIndex = Math.max(1, done)
      const teamRoundLabel =
        milestones[teamRoundIndex - 1]?.title ?? `Vòng ${teamRoundIndex}`

      // Tên vòng dừng — chỉ dùng khi isStopped: là vòng cuối team còn tham gia
      const stoppedRoundName = isStopped
        ? (done > 0 ? milestones[done - 1]?.title ?? `Vòng ${done}` : milestones[0]?.title ?? 'Vòng 1')
        : null

      // Lấy điểm từ API đã fetch (chỉ hiển thị khi đã công bố stage 3)
      const scoreData = teamScoresMap[team.id] ?? null
      const finalScore = scoreData?.score ?? null
      const finalRank = scoreData?.rank ?? 0

      return {
        ...team,
        status: derivedStatus,
        currentRound: teamRoundLabel,   // Vòng mới nhất của chính team đó (không phải vòng sự kiện)
        stoppedRound: stoppedRoundName,
        score: finalScore,
        rank: finalRank,
      }
    })
  }, [rawTeams, milestones, teamScoresMap])


  const requests = mentor.requests ?? []

  const [activeTeam, setActiveTeam] = useState(null)

  // Câu hỏi đang chờ trả lời (cho box cuộn ngang trên cùng).
  const pendingRequests = useMemo(
    () => requests.filter((r) => !r.answer),
    [requests],
  )

  // Câu hỏi của đội đang mở popup.
  const activeRequests = useMemo(
    () => (activeTeam ? requests.filter((r) => r.teamId === activeTeam.id) : []),
    [activeTeam, requests],
  )

  // ── Overlay fade khi còn cuộn được sang trái/phải ──
  const scrollRef = useRef(null)
  const [fade, setFade] = useState({ left: false, right: false })

  function updateFade() {
    const el = scrollRef.current
    if (!el) return
    const { scrollLeft, scrollWidth, clientWidth } = el
    setFade({
      left: scrollLeft > 4,
      right: scrollLeft + clientWidth < scrollWidth - 4,
    })
  }

  useEffect(() => {
    updateFade()
    window.addEventListener('resize', updateFade)
    return () => window.removeEventListener('resize', updateFade)
  }, [pendingRequests.length])

  function handleReply(requestId, text) {
    // TODO: gọi API gửi phản hồi. Hiện mock chỉ log.
    console.log('reply', requestId, text)
  }

  return (
    <div className={styles.stack}>
      {/* Box câu hỏi chờ xử lí — cuộn ngang */}
      <section className={styles.card}>
        <div className={styles.cardHead}>
          <span className={styles.cardTitle}>
            <ChatCircleDots size={19} weight="fill" className={styles.titleIcon} />
            Câu hỏi chờ xử lí
          </span>
          <Badge variant="blueSolid" size="sm" dot={false} label={`${pendingRequests.length} câu hỏi`} />
        </div>

        {pendingRequests.length === 0 ? (
          <div className={styles.empty}>
            <CheckCircle size={18} weight="fill" />
            Không có câu hỏi nào đang chờ
          </div>
        ) : (
          <div className={styles.scrollShell}>
            {fade.left && <div className={`${styles.fade} ${styles.fadeLeft}`} />}
            {fade.right && <div className={`${styles.fade} ${styles.fadeRight}`} />}

            <div className={`${styles.hscroll} scrollbar`} ref={scrollRef} onScroll={updateFade}>
              {pendingRequests.map((r) => {
                const team = teams.find((t) => t.id === r.teamId)
                return (
                  <article key={r.id} className={styles.qCard}>
                    <div className={styles.qCardMain}>
                      <div className={styles.qCardHead}>
                        <span className={styles.qTeam}>{r.teamName}</span>
                        <span className={styles.qTime}>{timeAgo(r.createdAt)}</span>
                      </div>
                      <p className={styles.qText}>{r.question}</p>
                    </div>
                    <Button
                      className={styles.replyBtn}
                      label="Trả lời"
                      variant="outline"
                      color="blue"
                      labelSize={13}
                      onClick={() => setActiveTeam(team ?? { id: r.teamId, name: r.teamName })}
                    />
                  </article>
                )
              })}
            </div>
          </div>
        )}
      </section>

      {/* Bảng đội phụ trách */}
      <MentorTeamTable teams={teams} onOpenRequests={setActiveTeam} />

      {/* Popup câu hỏi của đội */}
      <MentorRequestModal
        open={!!activeTeam}
        team={activeTeam}
        requests={activeRequests}
        onClose={() => setActiveTeam(null)}
        onSubmitReply={handleReply}
      />
    </div>
  )
}

export default MentorTab

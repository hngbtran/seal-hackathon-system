import { useState, useEffect } from 'react'
import { NotePencil, Flag } from '@phosphor-icons/react'
import Button from '../../shared/Button'
import styles from './RequestsSection.module.css'
import ViolationHandlingModal from './ViolationHandlingModal'
import ScoreEditModal from './ScoreEditModal'
import { mockScoreEditData } from './scoreEditMock'
import axiosClient from '../../../api/axiosClient'
import { format, parseISO } from 'date-fns'

function RequestsSection({ onOpenTeam, onOpenSubmission, type = 'all', hideHeader = false, onRefresh, refreshTrigger }) {
  const [violationModalOpen, setViolationModalOpen] = useState(false)
  const [selectedViolation, setSelectedViolation] = useState(null)
  
  const [scoreEditModalOpen, setScoreEditModalOpen] = useState(false)
  const [selectedScoreEdit, setSelectedScoreEdit] = useState(null)

  const [violations, setViolations] = useState([])
  const [scoreEdits, setScoreEdits] = useState([])
  const [loadingViolations, setLoadingViolations] = useState(true)
  const [loadingScoreEdits, setLoadingScoreEdits] = useState(true)

  // Hàm Helper định dạng thời gian hiển thị
  const formatTime = (timeStr) => {
    if (!timeStr) return ''
    try {
      const date = parseISO(timeStr)
      return format(date, 'HH:mm dd/MM/yyyy')
    } catch {
      return timeStr
    }
  }

  // 1. Fetch danh sách báo cáo vi phạm
  const fetchViolations = () => {
    setLoadingViolations(true)
    axiosClient.get('/system-requests/violations')
      .then((res) => {
        const mappedData = (res.data || []).map(item => ({
          id: item.id || item.requestId,
          submissionId: item.submissionId,
          teamName: item.teamName || 'N/A',
          round: item.round || 'N/A',
          judgeName: item.judgeName || 'N/A',
          time: formatTime(item.time),
          reason: item.reason
        }))
        setViolations(mappedData)
      })
      .catch((err) => console.error("Lỗi khi tải danh sách báo cáo vi phạm:", err))
      .finally(() => setLoadingViolations(false))
  }

  // 2. Fetch danh sách yêu cầu chỉnh sửa điểm (Mới bổ sung)
  const fetchScoreEdits = () => {
    setLoadingScoreEdits(true)
    axiosClient.get('/system-requests/score-edits')
      .then((res) => {
        const mappedData = (res.data || []).map(item => ({
          id: item.requestId || item.id,
          teamName: item.teamName || 'N/A',
          judgeName: item.judgeName || 'N/A',
          time: formatTime(item.time),
          reason: item.reason
        }))
        setScoreEdits(mappedData)
      })
      .catch((err) => console.error("Lỗi khi tải danh sách yêu cầu chỉnh sửa điểm:", err))
      .finally(() => setLoadingScoreEdits(false))
  }

  useEffect(() => {
    fetchViolations()
    fetchScoreEdits()
  }, [refreshTrigger])

  const openViolation = (v) => {
    setSelectedViolation(v)
    setViolationModalOpen(true)
  }

 const openScoreEdit = (req) => {
  axiosClient.get(`/system-requests/score-edits/${req.id}`)
    .then((res) => {
      setSelectedScoreEdit(res.data)
      setScoreEditModalOpen(true)
    })
    .catch((err) => {
      console.error("Lỗi khi tải chi tiết yêu cầu sửa điểm:", err)
    })
}

  return (
    <div className={styles.section}>
      {/* Box Yêu cầu chỉnh sửa điểm */}
      {(type === 'all' || type === 'scoreEdit') && (
        <div className={styles.boxGreen}>
          {!hideHeader && (
            <div className={styles.header}>
              <div className={styles.titleRow}>
                <NotePencil size={24} color="var(--color-primary-green)" weight="fill" />
                <h3 className={styles.titleGreen}>Yêu cầu chỉnh sửa</h3>
                <span className={styles.countBadgeGreen}>{scoreEdits.length}</span>
              </div>
            </div>
          )}

          <div className={styles.list}>
            {loadingScoreEdits ? (
              <div className={styles.emptyGreen}>Đang tải...</div>
            ) : scoreEdits.length === 0 ? (
              <div className={styles.emptyGreen}>Chưa có yêu cầu nào</div>
            ) : (
              scoreEdits.map(req => (
                <div key={req.id} className={styles.cardGreen}>
                  <div className={styles.cardInfo}>
                    <strong className={styles.senderName}>{req.judgeName}</strong>
                    <span className={styles.cardTeam}>Về đội: <span className={styles.teamGreen}>{req.teamName}</span></span>
                  </div>
                  <div className={styles.cardActionGroup}>
                    <span className={styles.time}>{req.time}</span>
                    <Button 
                      label="Chi tiết" 
                      labelSize="0.8rem" 
                      size="xs" 
                      variant="outline" 
                      color="green" 
                      onClick={() => openScoreEdit(req)} 
                    />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Box Xử lý vi phạm */}
      {(type === 'all' || type === 'violation') && (
        <div className={styles.boxOrange}>
          {!hideHeader && (
            <div className={styles.header}>
              <div className={styles.titleRow}>
                <Flag size={24} color="var(--color-primary-orange)" weight="fill" />
                <h3 className={styles.titleOrange}>Xử lý vi phạm</h3>
                <span className={styles.countBadgeOrange}>{violations.length}</span>
              </div>
            </div>
          )}

          <div className={styles.list}>
            {loadingViolations ? (
              <div className={styles.emptyOrange}>Đang tải...</div>
            ) : violations.length === 0 ? (
              <div className={styles.emptyOrange}>Chưa có đội bị cắm cờ</div>
            ) : (
              violations.map(v => (
                <div key={v.id} className={styles.cardOrange}>
                  <div className={styles.cardInfo}>
                    <strong className={styles.senderName}>{v.judgeName}</strong>
                    <span className={styles.cardTeam}>Về đội: <span className={styles.teamOrange}>{v.teamName}</span></span>
                  </div>
                  <div className={styles.cardActionGroup}>
                    <span className={styles.time}>{v.time}</span>
                    <Button 
                      label="Chi tiết" 
                      labelSize="0.8rem" 
                      size="xs" 
                      variant="outline" 
                      color="orange" 
                      onClick={() => openViolation(v)} 
                    />
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {/* Modal xử lý vi phạm */}
      {violationModalOpen && (
        <ViolationHandlingModal 
          isOpen={violationModalOpen} 
          onClose={() => setViolationModalOpen(false)}
          data={selectedViolation}
          onOpenTeam={onOpenTeam}
          onOpenSubmission={onOpenSubmission}
          onHandled={() => {
            fetchViolations()
            if (onRefresh) onRefresh()
          }}
        />
      )}

      {/* Modal xử lý sửa điểm */}
      {scoreEditModalOpen && (
        <ScoreEditModal
          isOpen={scoreEditModalOpen}
          onClose={() => setScoreEditModalOpen(false)}
          data={selectedScoreEdit}
          onOpenTeam={onOpenTeam}
          onOpenSubmission={onOpenSubmission}
          onHandled={() => {
            fetchScoreEdits()
            if (onRefresh) onRefresh()
          }}
        />
      )}
    </div>
  )
}

export default RequestsSection
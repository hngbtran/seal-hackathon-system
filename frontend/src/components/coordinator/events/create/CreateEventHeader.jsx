import { FlagBanner, RocketLaunch, ArrowLeft, Eye } from '@phosphor-icons/react'
import Button from '../../../../components/shared/Button'
import Tooltip from '../../../../components/shared/Tooltip'
import StatusBadge from '../../StatusBadge'
import styles from './CreateEventHeader.module.css'
import { useNavigate } from 'react-router-dom'
// status: 'draft' | 'live' | 'upcoming' | 'ended' | 'cancelled' | 'archived'
// Công bố chỉ enabled khi status === 'draft'
function CreateEventHeader({ title, status = 'draft', onBack, onPublish, onPreview , isPublishDisabled, publishDisabledReason }) {
  const navigate=useNavigate();

  let tooltipContent = null;
  if (status !== 'draft') {
    tooltipContent = "Sự kiện đã được công bố";
  } else if (publishDisabledReason) {
    tooltipContent = publishDisabledReason;
  }

  return (
    <div className={styles.outer}>

      {/* ── Header banner ── */}
      <div className={styles.wrapper}>

        {/* ── Trái: icon + title + badge ── */}
        <div className={styles.titleArea}>
          <FlagBanner size={28} weight="fill" className={styles.flagIcon} />
          <h1 className={styles.title}>{title || 'Sự kiện mới'}</h1>
          <StatusBadge status={status} />
        </div>

        {/* ── Phải: actions ── */}
        <div className={styles.actions}>
          <Button
            label="Xem trước trang"
            icon={Eye}
            iconPosition="right"
            variant="outline"
            onClick={onPreview}
          />
          <Tooltip 
            content={tooltipContent} 
            position="bottom" 
            bgColor="white"
            textColor="blueTxt"
          >
            <Button
              label="Công bố"
              icon={RocketLaunch}
              iconPosition="left"
              variant="primary"
              color="green"
              onClick={onPublish}
              disabled={status !== 'draft' || isPublishDisabled}
            />
          </Tooltip>
        </div>

      </div>
    </div>
  )
}

export default CreateEventHeader
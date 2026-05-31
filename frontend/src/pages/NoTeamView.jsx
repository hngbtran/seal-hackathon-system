import { useState } from 'react'
import EventLayout from '../layouts/EventLayout'
import NoTeamHeader from '../components/noTeamView/NoTeamHeader'
import FindTeamSection from '../components/noTeamView/FindTeamSection'
import InviteTeamCard from '../components/noTeamView/InviteTeamCard'
import RequestTeamCard from '../components/noTeamView/RequestTeamCard'
import styles from './LeaderView.module.css'

// Data tạm — sau này thay bằng API


const FAKE_TEAMS = Array.from({ length: 6 }, (_, i) => ({
    id: i + 1,
    name: 'Tên đội',
    description: 'Giới thiệu ngắn về đội của bạn và định hướng giải quyết bài toán.',
    maxSlots: 4,
    members: [
        { id: 10, name: 'Nguyễn Thành Thái', school: 'Đại học FPT' },
        { id: 11, name: 'Hồ Ngọc Bảo Trân', school: 'Đại học FPT' },
    ],
    isRequested: false,
}))

const FAKE_INVITES = [
  { id: 1, teamName: 'Tên đội', memberCount: 2, maxSlots: 4, message: 'Xin chào...' },
]
const FAKE_REQUESTS = [
  { id: 1, teamName: 'Tên đội', memberCount: 2, maxSlots: 4 },
  { id: 2, teamName: 'Tên đội', memberCount: 3, maxSlots: 4 },
]


function NoTeamView() {
    const [teamStatus, setTeamStatus] = useState('pending')

    return (
        <EventLayout>
            <div className={styles.page}>

                <NoTeamHeader />
                
                <div className={styles.content}>

                    <div className={styles.main}>

                        <FindTeamSection teams={FAKE_TEAMS} />

                    </div>

                    <div className={styles.side}>
                        <InviteTeamCard
                            invites={FAKE_INVITES}
                            onAccept={(id) => console.log('đồng ý', id)}
                            onReject={(id) => console.log('từ chối', id)}
                        />

                        <RequestTeamCard
                            requests={FAKE_REQUESTS}
                            onCancel={(id) => console.log('hủy', id)}
                        />
                    </div>

                </div>
            </div>
        </EventLayout>
    )
}

export default NoTeamView
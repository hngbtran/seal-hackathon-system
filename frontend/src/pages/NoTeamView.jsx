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
    {
        id: 1,
        teamName: 'Pixel Pioneers',
        memberCount: 3,
        maxSlots: 4,
        description: 'Team mình thiên về UI/UX và frontend, mong muốn tìm hướng tiếp cận bài toán từ góc độ trải nghiệm người dùng.',
        message: 'Xin chào! Mình thấy profile của bạn rất phù hợp với định hướng của team. Rất mong bạn cân nhắc tham gia cùng tụi mình nhé!',
    },
    {
        id: 2,
        teamName: 'Code Breakers',
        memberCount: 2,
        maxSlots: 4,
        description: 'Nhóm mình chủ yếu làm backend, đang cần thêm người có thể đảm nhận phần giao diện.',
        message: 'Hi bạn! Team mình đang thiếu một frontend developer. Bạn có muốn join không?',
    },
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
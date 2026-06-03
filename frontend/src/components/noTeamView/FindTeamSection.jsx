import { useState } from 'react'
import TeamCard from './TeamCard'
import CardSearchBase from '../shared/CardSearchBase'
import styles from '../shared/CardSearchBase.module.css'



function FindTeamSection({ teams = [] }) {
  const [search, setSearch] = useState('')
  const [fptOnly, setFptOnly] = useState(false)
  const [requestedIds, setRequestedIds] = useState([])
  const [currentPage, setCurrentPage] = useState(1)

  const processedTeams = teams.map(t => ({
    ...t,
    isRequested: requestedIds.includes(t.id),
  }))


  return (
    <div className={styles.wrapper}>
      <CardSearchBase
        items={teams}
        renderCard={(team) => (
          <TeamCard
            key={team.id}
            team={team}
            onRequest={(id) => setRequestedIds(prev => [...prev, id])}
            onCancel={(id) => setRequestedIds(prev => prev.filter(i => i !== id))}
          />
        )}
        searchPlaceholder="Tìm kiếm tên đội"
        search={search}
        onSearchChange={setSearch}
        fptOnly={fptOnly}
        onFptChange={setFptOnly}
        currentPage={currentPage}
        totalPages={8}
        onPageChange={setCurrentPage}
      />
    </div>
  )
}

export default FindTeamSection
import Navbar from '../components/Navbar/Navbar'
import styles from './UserLayout.module.css'

const FAKE_USER = {
  name: localStorage.getItem('userInfo')? JSON.parse(localStorage.getItem('userInfo')).name : 'Nguyen Van A',
  avatarUrl: 'https://upload.wikimedia.org/wikipedia/en/thumb/b/bd/Doraemon_character.png/250px-Doraemon_character.png',
}

function UserLayout({ children }) {
  return (
    <div className={styles.page}>
      <Navbar isLoggedIn={true} user={FAKE_USER} />
      <main className={styles.main}>
        <div className={styles.card}>
          {children}
        </div>
      </main>
    </div>
  )
}

export default UserLayout
import Button from '../shared/Button';
import { UsersThree, Copy, MagnifyingGlass } from "@phosphor-icons/react";
import styles from './TeamInfoHeader.module.css'

function TeamInfoHeader({ name, description, teamCode }) {
    function handleCopyCode() {
        navigator.clipboard.writeText(teamCode)
    }

    return (
        <div className={styles.wrapper}>

            <div className={styles.teamInfo}>
                <div className="icon-label">
                    <UsersThree size={32} weight="fill" color={'white'}></UsersThree>
                    <h1>{name}</h1>
                </div>

                <p>{description}</p>
            </div>

            <div>
                <div className={styles.codeBox}>
                    <span>Mã đội:</span>
                    <Button className={styles.btn} icon={Copy} label={teamCode} variant="outline" color='blue' onClick={handleCopyCode}/>
                </div>
                <Button className={styles.btn} icon={MagnifyingGlass} label="Tìm thành viên" variant="outline" color='blue' />
            </div>

        </div>
    )
}

export default TeamInfoHeader
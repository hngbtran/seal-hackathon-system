import { useState } from 'react'
import styles from './Tooltip.module.css'

function Tooltip({ 
    children, 
    content, 
    contentSize = 14,
    bgColor = 'blue', // "blue", "green", "orange", "white"
    textColor = 'white', // "blue", "green", "orange", "white"
    position = 'top' // "top", "bottom", "right", "left"
}) {
    const [visible, setVisible] = useState(false)

    if (!content) return children

    return (
        <div
            className={styles.wrapper}
            onMouseEnter={() => setVisible(true)}
            onMouseLeave={() => setVisible(false)}
        >
            {children}

            {visible && (
                <div className={`${styles.tooltip} ${styles[position]} ${styles[bgColor]} `}>
                    <p className={styles[textColor]} style={{ fontSize: `${contentSize}px`}}>{content}</p>
                    <span className={styles.arrow} />
                </div>
            )}
        </div>
    )
}

export default Tooltip
import { useState } from 'react'
import DisclaimerStep  from './DisclaimerStep'
import ChooseOptionStep from './ChooseOptionStep'

function JoinTeamFlow({ onClose }) {
  const [step, setStep]     = useState(1)
  const [option, setOption] = useState(null)  // 'create' | 'code' | 'search'

  if (step === 1) return (
    <DisclaimerStep
      onClose={onClose}
      onNext={() => setStep(2)}
    />
  )

  if (step === 2) return (
    <ChooseOptionStep
      onClose={onClose}
      onBack={() => setStep(1)}
      onNext={(selected) => { setOption(selected); setStep(3) }}
    />
  )

  // Step 3 — sẽ viết tiếp
  return null
}

export default JoinTeamFlow
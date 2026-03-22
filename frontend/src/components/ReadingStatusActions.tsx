import { BookHalf, ClockHistory, EyeFill } from 'react-bootstrap-icons'
import Button from 'react-bootstrap/Button'
import ButtonGroup from 'react-bootstrap/ButtonGroup'
import type { ReadingStatusValue } from '../types/userPage'

interface Props {
  currentStatus?: ReadingStatusValue
  isSaving?: boolean
  onSetStatus: (status: ReadingStatusValue) => void
}

export default function ReadingStatusActions({ currentStatus, isSaving, onSetStatus }: Props) {
  return (
    <ButtonGroup size="sm">
      <Button
        variant={currentStatus === 'WANT_TO_READ' ? 'warning' : 'outline-warning'}
        disabled={isSaving}
        onClick={() => onSetStatus('WANT_TO_READ')}
        title="Want to Read"
      >
        <ClockHistory className="me-1" />
        Want to Read
      </Button>
      <Button
        variant={currentStatus === 'CURRENTLY_READING' ? 'primary' : 'outline-primary'}
        disabled={isSaving}
        onClick={() => onSetStatus('CURRENTLY_READING')}
        title="Currently Reading"
      >
        <BookHalf className="me-1" />
        Reading
      </Button>
      <Button
        variant={currentStatus === 'READ' ? 'success' : 'outline-success'}
        disabled={isSaving}
        onClick={() => onSetStatus('READ')}
        title="Read"
      >
        <EyeFill className="me-1" />
        Read
      </Button>
    </ButtonGroup>
  )
}

import { useCallback, useEffect, useRef, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Form from 'react-bootstrap/Form'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import { Link, useParams } from 'react-router-dom'
import { getBookById, getCurrentUser } from '../api/search'
import {
  createReadProgress,
  getReadProgressForUserBook,
  updateReadProgress,
  type ReadProgressResponse,
} from '../api/readProgress'
import { createReadingSession } from '../api/readingSessions'
import type { BookDetail, UserProfileItem } from '../types/search'
import fallbackCover from '../assets/fallback_cover.png'

type Phase = 'loading' | 'setup' | 'ready' | 'reading' | 'finish' | 'saved'
type TimerMode = 'stopwatch' | 'countdown'

function formatTime(totalSeconds: number): string {
  const hrs = Math.floor(totalSeconds / 3600)
  const mins = Math.floor((totalSeconds % 3600) / 60)
  const secs = totalSeconds % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  return hrs > 0 ? `${pad(hrs)}:${pad(mins)}:${pad(secs)}` : `${pad(mins)}:${pad(secs)}`
}

function todayISO(): string {
  return new Date().toISOString().split('T')[0]
}

function ReadingSessionPage() {
  const { bookId } = useParams()
  const parsedBookId = Number(bookId)

  // Data state
  const [book, setBook] = useState<BookDetail | null>(null)
  const [currentUser, setCurrentUser] = useState<UserProfileItem | null>(null)
  const [readProgress, setReadProgress] = useState<ReadProgressResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Phase state
  const [phase, setPhase] = useState<Phase>('loading')

  // Setup form (new progress)
  const [setupTotalPages, setSetupTotalPages] = useState('')
  const [setupCurrentPosition, setSetupCurrentPosition] = useState('')
  const [setupSaving, setSetupSaving] = useState(false)

  // Ready phase (adjust start position)
  const [startPosition, setStartPosition] = useState(1)

  // Timer
  const [timerMode, setTimerMode] = useState<TimerMode>('stopwatch')
  const [countdownMinutes, setCountdownMinutes] = useState('25')
  const [elapsedSeconds, setElapsedSeconds] = useState(0)
  const [countdownRemaining, setCountdownRemaining] = useState(0)
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  // Finish phase
  const [endPosition, setEndPosition] = useState('')
  const [notes, setNotes] = useState('')
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  // Load book + user + existing progress
  useEffect(() => {
    if (!bookId || Number.isNaN(parsedBookId)) {
      setError('Invalid book id.')
      setLoading(false)
      return
    }

    const load = async () => {
      try {
        const [bookResult, userResult] = await Promise.all([
          getBookById(parsedBookId),
          getCurrentUser(),
        ])
        setBook(bookResult)
        setCurrentUser(userResult)

        // Try to find existing read progress
        const progressList = await getReadProgressForUserBook(userResult.id, parsedBookId)
        if (progressList.length > 0) {
          // Use the most recent one
          const latest = progressList.reduce((a, b) =>
            new Date(a.createdAt) > new Date(b.createdAt) ? a : b,
          )
          setReadProgress(latest)
          setStartPosition(latest.currentPosition ?? 1)
          setPhase('ready')
        } else {
          setPhase('setup')
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load data.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [bookId, parsedBookId])

  // Setup: create new ReadProgress
  const handleSetupSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!currentUser || !book) return

    const totalPages = Number(setupTotalPages)
    const currentPos = Number(setupCurrentPosition) || 1

    if (!totalPages || totalPages <= 0) {
      setError('Please enter a valid total page count.')
      return
    }

    setSetupSaving(true)
    setError(null)
    try {
      const created = await createReadProgress({
        userId: currentUser.id,
        bookId: parsedBookId,
        totalPages,
        currentPosition: currentPos,
        status: 'CURRENTLY_READING',
        startedAt: todayISO(),
      })
      setReadProgress(created)
      setStartPosition(created.currentPosition ?? currentPos)
      setPhase('ready')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to create reading progress.'
      setError(message)
    } finally {
      setSetupSaving(false)
    }
  }

  // Start reading
  const handleStartReading = useCallback(() => {
    setElapsedSeconds(0)
    if (timerMode === 'countdown') {
      const totalSec = Math.max(1, Number(countdownMinutes)) * 60
      setCountdownRemaining(totalSec)
    }
    setPhase('reading')
  }, [timerMode, countdownMinutes])

  // Timer tick
  useEffect(() => {
    if (phase !== 'reading') {
      if (intervalRef.current) {
        clearInterval(intervalRef.current)
        intervalRef.current = null
      }
      return
    }

    intervalRef.current = setInterval(() => {
      setElapsedSeconds((prev) => prev + 1)

      if (timerMode === 'countdown') {
        setCountdownRemaining((prev) => {
          if (prev <= 1) {
            // Time's up — auto-stop
            setPhase('finish')
            return 0
          }
          return prev - 1
        })
      }
    }, 1000)

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current)
        intervalRef.current = null
      }
    }
  }, [phase, timerMode])

  // Stop reading
  const handleStopReading = () => {
    setPhase('finish')
  }

  // Save session
  const handleSaveSession = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!currentUser || !readProgress) return

    const ep = Number(endPosition)
    if (!ep || ep < startPosition) {
      setSaveError('End position must be greater than or equal to start position.')
      return
    }

    const durationMinutes = Math.max(1, Math.ceil(elapsedSeconds / 60))

    setSaving(true)
    setSaveError(null)
    try {
      await createReadingSession({
        userId: currentUser.id,
        readProgressId: readProgress.id,
        durationMinutes,
        startPosition,
        endPosition: ep,
        sessionDate: todayISO(),
        notes: notes.trim() || undefined,
      })

      // Also update the read progress current position
      const updated = await updateReadProgress(readProgress.id, {
        currentPosition: ep,
      })
      setReadProgress(updated)

      setPhase('saved')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to save session.'
      setSaveError(message)
    } finally {
      setSaving(false)
    }
  }

  // Start a new session after saving
  const handleNewSession = () => {
    setEndPosition('')
    setNotes('')
    setElapsedSeconds(0)
    setCountdownRemaining(0)
    setSaveError(null)
    setStartPosition(readProgress?.currentPosition ?? 1)
    setPhase('ready')
  }

  // ── Render ──

  if (loading) {
    return (
      <div className="reading-session-page d-flex align-items-center justify-content-center">
        <Spinner animation="border" /> <span className="ms-2">Loading...</span>
      </div>
    )
  }

  if (error && phase === 'loading') {
    return (
      <div className="reading-session-page">
        <div className="rs-container">
          <Alert variant="danger">{error}</Alert>
          <Link to={`/books/${bookId}`} className="btn btn-outline-secondary btn-sm">
            ← Back to Book
          </Link>
        </div>
      </div>
    )
  }

  const progressPercent =
    readProgress && readProgress.totalPages > 0
      ? Math.round(((readProgress.currentPosition ?? 0) / readProgress.totalPages) * 100)
      : 0

  return (
    <div className="reading-session-page">
      <div className="rs-container">
        {/* Header */}
        <div className="rs-header">
          <Link to={`/books/${bookId}`} className="rs-back-link">
            ← Back to Book
          </Link>
          {book && (
            <div className="rs-book-info">
              <img
                src={book.coverImageUrl || fallbackCover}
                alt={book.title}
                className="rs-book-cover"
              />
              <div>
                <h4 className="mb-1">{book.title}</h4>
                <span className="text-muted">Reading Session</span>
              </div>
            </div>
          )}
        </div>

        {error && <Alert variant="danger" className="mt-3">{error}</Alert>}

        {/* SETUP PHASE */}
        {phase === 'setup' && (
          <Card className="rs-phase-card">
            <Card.Body>
              <h5 className="rs-phase-title">Set Up Your Reading</h5>
              <p className="text-muted">
                No reading progress found for this book. Let's set things up first.
              </p>
              <Form onSubmit={handleSetupSubmit}>
                <Row className="g-3">
                  <Col sm={6}>
                    <Form.Group controlId="setup-total-pages">
                      <Form.Label>Total Pages</Form.Label>
                      <Form.Control
                        type="number"
                        min={1}
                        value={setupTotalPages}
                        onChange={(e) => setSetupTotalPages(e.target.value)}
                        placeholder="e.g. 350"
                        className="bg-dark text-light border-secondary"
                        required
                      />
                    </Form.Group>
                  </Col>
                  <Col sm={6}>
                    <Form.Group controlId="setup-current-position">
                      <Form.Label>Current Page</Form.Label>
                      <Form.Control
                        type="number"
                        min={0}
                        value={setupCurrentPosition}
                        onChange={(e) => setSetupCurrentPosition(e.target.value)}
                        placeholder="e.g. 1"
                        className="bg-dark text-light border-secondary"
                      />
                      <Form.Text className="text-muted">Leave blank to start from page 1</Form.Text>
                    </Form.Group>
                  </Col>
                </Row>
                <Button
                  type="submit"
                  variant="success"
                  className="mt-4 w-100"
                  disabled={setupSaving}
                >
                  {setupSaving ? 'Creating…' : 'Start Tracking'}
                </Button>
              </Form>
            </Card.Body>
          </Card>
        )}

        {/* READY PHASE */}
        {phase === 'ready' && readProgress && (
          <Card className="rs-phase-card">
            <Card.Body>
              <h5 className="rs-phase-title">Ready to Read</h5>

              {/* Progress indicator */}
              <div className="rs-progress-bar-wrapper mb-4">
                <div className="d-flex justify-content-between small text-muted mb-1">
                  <span>Page {readProgress.currentPosition ?? 0}</span>
                  <span>{readProgress.totalPages} pages</span>
                </div>
                <div className="rs-progress-track">
                  <div className="rs-progress-fill" style={{ width: `${progressPercent}%` }} />
                </div>
                <div className="text-center small text-muted mt-1">{progressPercent}% complete</div>
              </div>

              <Form.Group className="mb-4" controlId="start-position">
                <Form.Label>Start reading from page</Form.Label>
                <Form.Control
                  type="number"
                  min={0}
                  max={readProgress.totalPages}
                  value={startPosition}
                  onChange={(e) => setStartPosition(Number(e.target.value))}
                  className="bg-dark text-light border-secondary"
                />
              </Form.Group>

              {/* Timer mode selection */}
              <div className="rs-mode-selector mb-4">
                <div className="small text-uppercase text-muted fw-bold mb-2">Timer Mode</div>
                <div className="d-flex gap-2">
                  <Button
                    variant={timerMode === 'stopwatch' ? 'success' : 'outline-secondary'}
                    size="sm"
                    onClick={() => setTimerMode('stopwatch')}
                    className="flex-fill"
                  >
                    ⏱ Stopwatch
                  </Button>
                  <Button
                    variant={timerMode === 'countdown' ? 'success' : 'outline-secondary'}
                    size="sm"
                    onClick={() => setTimerMode('countdown')}
                    className="flex-fill"
                  >
                    ⏳ Countdown
                  </Button>
                </div>
              </div>

              {timerMode === 'countdown' && (
                <Form.Group className="mb-4" controlId="countdown-minutes">
                  <Form.Label>Reading time (minutes)</Form.Label>
                  <Form.Control
                    type="number"
                    min={1}
                    max={480}
                    value={countdownMinutes}
                    onChange={(e) => setCountdownMinutes(e.target.value)}
                    className="bg-dark text-light border-secondary"
                  />
                </Form.Group>
              )}

              <Button
                variant="success"
                size="lg"
                className="w-100 rs-start-btn"
                onClick={handleStartReading}
              >
                ▶ Start Reading
              </Button>
            </Card.Body>
          </Card>
        )}

        {/* READING PHASE */}
        {phase === 'reading' && (
          <div className="rs-reading-phase">
            <div className="rs-timer-display">
              <div className="rs-timer-label">
                {timerMode === 'stopwatch' ? 'Elapsed' : 'Remaining'}
              </div>
              <div className="rs-timer-value">
                {timerMode === 'stopwatch'
                  ? formatTime(elapsedSeconds)
                  : formatTime(countdownRemaining)}
              </div>
              {timerMode === 'countdown' && (
                <div className="rs-timer-sub text-muted">
                  Total elapsed: {formatTime(elapsedSeconds)}
                </div>
              )}
            </div>
            <Button
              variant="danger"
              size="lg"
              className="rs-stop-btn"
              onClick={handleStopReading}
            >
              ■ Stop Reading
            </Button>
          </div>
        )}

        {/* FINISH PHASE */}
        {phase === 'finish' && (
          <Card className="rs-phase-card">
            <Card.Body>
              <h5 className="rs-phase-title">Session Complete!</h5>
              <div className="rs-session-summary mb-4">
                <div className="rs-summary-item">
                  <span className="rs-summary-label">Duration</span>
                  <span className="rs-summary-value">{formatTime(elapsedSeconds)}</span>
                </div>
                <div className="rs-summary-item">
                  <span className="rs-summary-label">Started at page</span>
                  <span className="rs-summary-value">{startPosition}</span>
                </div>
              </div>

              {saveError && <Alert variant="danger">{saveError}</Alert>}

              <Form onSubmit={handleSaveSession}>
                <Form.Group className="mb-3" controlId="end-position">
                  <Form.Label>Ended at page</Form.Label>
                  <Form.Control
                    type="number"
                    min={startPosition}
                    max={readProgress?.totalPages}
                    value={endPosition}
                    onChange={(e) => setEndPosition(e.target.value)}
                    placeholder={`e.g. ${startPosition + 30}`}
                    className="bg-dark text-light border-secondary"
                    required
                  />
                </Form.Group>

                <Form.Group className="mb-4" controlId="session-notes">
                  <Form.Label>Notes <span className="text-muted">(optional)</span></Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    placeholder="What happened in this session? Any thoughts?"
                    className="bg-dark text-light border-secondary"
                  />
                </Form.Group>

                <Button
                  type="submit"
                  variant="success"
                  size="lg"
                  className="w-100"
                  disabled={saving}
                >
                  {saving ? 'Saving…' : '✓ Save Session'}
                </Button>
              </Form>
            </Card.Body>
          </Card>
        )}

        {/* SAVED PHASE */}
        {phase === 'saved' && readProgress && (
          <Card className="rs-phase-card rs-saved-card">
            <Card.Body className="text-center">
              <div className="rs-saved-icon">✓</div>
              <h5 className="rs-phase-title">Session Saved!</h5>
              <p className="text-muted">
                You're now on page {readProgress.currentPosition} of {readProgress.totalPages}.
              </p>

              <div className="rs-progress-bar-wrapper mb-4">
                <div className="rs-progress-track">
                  <div
                    className="rs-progress-fill"
                    style={{
                      width: `${Math.round(((readProgress.currentPosition ?? 0) / readProgress.totalPages) * 100)}%`,
                    }}
                  />
                </div>
                <div className="text-center small text-muted mt-1">
                  {Math.round(((readProgress.currentPosition ?? 0) / readProgress.totalPages) * 100)}%
                  complete
                </div>
              </div>

              <div className="d-flex flex-column gap-2">
                <Button variant="success" onClick={handleNewSession}>
                  ▶ Start Another Session
                </Button>
                <Link to={`/books/${bookId}`} className="btn btn-outline-secondary">
                  ← Back to Book
                </Link>
              </div>
            </Card.Body>
          </Card>
        )}
      </div>
    </div>
  )
}

export default ReadingSessionPage

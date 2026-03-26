import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Badge from 'react-bootstrap/Badge'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Container from 'react-bootstrap/Container'
import Form from 'react-bootstrap/Form'
import Pagination from 'react-bootstrap/Pagination'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import Tab from 'react-bootstrap/Tab'
import Tabs from 'react-bootstrap/Tabs'
import { Link } from 'react-router-dom'
import { getFeed, getMyActivities } from '../api/activities'
import type { ActivityItem, ActivityType } from '../types/activities'

const PAGE_SIZE = 15

const ACTIVITY_TYPE_LABELS: Record<ActivityType, string> = {
  BOOK_LIKED: 'Book Liked',
  REVIEW_LIKED: 'Review Liked',
  LIST_LIKED: 'List Liked',
  REVIEW_CREATED: 'Review Written',
  LIST_CREATED: 'List Created',
  READING_STATUS_UPDATED: 'Reading Status',
}

const ACTIVITY_TYPE_BADGE: Record<ActivityType, string> = {
  BOOK_LIKED: 'danger',
  REVIEW_LIKED: 'warning',
  LIST_LIKED: 'info',
  REVIEW_CREATED: 'success',
  LIST_CREATED: 'primary',
  READING_STATUS_UPDATED: 'secondary',
}

const READING_STATUS_LABELS: Record<string, string> = {
  WANT_TO_READ: 'Want to Read',
  CURRENTLY_READING: 'Currently Reading',
  READ: 'Read',
  DID_NOT_FINISH: 'Did Not Finish',
}

function formatDate(value: string): string {
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

function ActivityDescription({ item }: { item: ActivityItem }) {
  const m = item.metadata

  switch (item.activityType) {
    case 'BOOK_LIKED':
      return (
        <>
          liked the book{' '}
          {m?.bookId ? (
            <Link to={`/books/${m.bookId}`}>{m.bookTitle ?? 'a book'}</Link>
          ) : (
            <span>{m?.bookTitle ?? 'a book'}</span>
          )}
        </>
      )
    case 'REVIEW_LIKED':
      return (
        <>
          liked a review of{' '}
          {m?.bookId ? (
            <Link to={`/books/${m.bookId}`}>{m.bookTitle ?? 'a book'}</Link>
          ) : (
            <span>{m?.bookTitle ?? 'a book'}</span>
          )}
          {m?.reviewerName && <> by {m.reviewerName}</>}
        </>
      )
    case 'LIST_LIKED':
      return (
        <>
          liked the list{' '}
          {m?.listId ? (
            <Link to={`/lists/${m.listId}`}>{m.listName ?? 'a list'}</Link>
          ) : (
            <span>{m?.listName ?? 'a list'}</span>
          )}
        </>
      )
    case 'REVIEW_CREATED':
      return (
        <>
          wrote a review of{' '}
          {m?.bookId ? (
            <Link to={`/books/${m.bookId}`}>{m.bookTitle ?? 'a book'}</Link>
          ) : (
            <span>{m?.bookTitle ?? 'a book'}</span>
          )}
        </>
      )
    case 'LIST_CREATED':
      return (
        <>
          created the list{' '}
          {m?.listId ? (
            <Link to={`/lists/${m.listId}`}>{m.listName ?? 'a list'}</Link>
          ) : (
            <span>{m?.listName ?? 'a list'}</span>
          )}
        </>
      )
    case 'READING_STATUS_UPDATED': {
      const from = m?.oldStatus ? READING_STATUS_LABELS[m.oldStatus] ?? m.oldStatus : null
      const to = m?.newStatus ? READING_STATUS_LABELS[m.newStatus] ?? m.newStatus : null
      return (
        <>
          marked{' '}
          {m?.bookId ? (
            <Link to={`/books/${m.bookId}`}>{m.bookTitle ?? 'a book'}</Link>
          ) : (
            <span>{m?.bookTitle ?? 'a book'}</span>
          )}{' '}
          as <strong>{to ?? 'unknown'}</strong>
          {from && <> (was {from})</>}
        </>
      )
    }
    default:
      return <span>did something</span>
  }
}

function ActivityCard({ item }: { item: ActivityItem }) {
  return (
    <Card className="mb-2 shadow-sm">
      <Card.Body className="py-2 px-3">
        <div className="d-flex justify-content-between align-items-start gap-2">
          <div className="flex-grow-1">
            <Badge bg={ACTIVITY_TYPE_BADGE[item.activityType]} className="me-2" style={{ fontSize: '0.7rem' }}>
              {ACTIVITY_TYPE_LABELS[item.activityType]}
            </Badge>
            <Link to={`/users/${item.userId}`} className="fw-semibold text-decoration-none">
              {item.userName}
            </Link>{' '}
            <span className="text-muted">
              <ActivityDescription item={item} />
            </span>
          </div>
          <span className="text-muted small text-nowrap">{formatDate(item.createdAt)}</span>
        </div>
      </Card.Body>
    </Card>
  )
}

interface FeedTabProps {
  fetchFn: (page: number, size: number, type?: ActivityType) => Promise<{ content: ActivityItem[]; totalPages: number; number: number }>
  typeFilter: ActivityType | ''
}

function FeedTab({ fetchFn, typeFilter }: FeedTabProps) {
  const [activities, setActivities] = useState<ActivityItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    setPage(0)
  }, [typeFilter])

  useEffect(() => {
    setLoading(true)
    setError(null)
    fetchFn(page, PAGE_SIZE, typeFilter || undefined)
      .then((data) => {
        setActivities(data.content)
        setTotalPages(data.totalPages)
      })
      .catch((err: Error) => setError(err.message))
      .finally(() => setLoading(false))
  }, [fetchFn, page, typeFilter])

  if (loading) {
    return (
      <div className="text-center py-5">
        <Spinner animation="border" />
      </div>
    )
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>
  }

  if (activities.length === 0) {
    return (
      <Alert variant="info" className="mt-3">
        No activities to show yet.
      </Alert>
    )
  }

  return (
    <div className="mt-3">
      {activities.map((item) => (
        <ActivityCard key={item.id} item={item} />
      ))}

      {totalPages > 1 && (
        <Pagination className="mt-3 justify-content-center">
          <Pagination.First onClick={() => setPage(0)} disabled={page === 0} />
          <Pagination.Prev onClick={() => setPage((p) => p - 1)} disabled={page === 0} />
          {Array.from({ length: totalPages }, (_, i) => (
            <Pagination.Item key={i} active={i === page} onClick={() => setPage(i)}>
              {i + 1}
            </Pagination.Item>
          ))}
          <Pagination.Next onClick={() => setPage((p) => p + 1)} disabled={page === totalPages - 1} />
          <Pagination.Last onClick={() => setPage(totalPages - 1)} disabled={page === totalPages - 1} />
        </Pagination>
      )}
    </div>
  )
}

function ActivityFeedPage() {
  const [activeTab, setActiveTab] = useState('feed')
  const [typeFilter, setTypeFilter] = useState<ActivityType | ''>('')

  return (
    <Container className="py-4">
      <Row className="mb-3 align-items-center">
        <Col>
          <h2 className="mb-0">Activity</h2>
        </Col>
        <Col xs="auto">
          <Form.Select
            size="sm"
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value as ActivityType | '')}
            style={{ minWidth: '180px' }}
            aria-label="Filter by activity type"
          >
            <option value="">All types</option>
            {(Object.keys(ACTIVITY_TYPE_LABELS) as ActivityType[]).map((t) => (
              <option key={t} value={t}>
                {ACTIVITY_TYPE_LABELS[t]}
              </option>
            ))}
          </Form.Select>
        </Col>
      </Row>

      <Tabs
        activeKey={activeTab}
        onSelect={(k) => {
          setActiveTab(k ?? 'feed')
          setTypeFilter('')
        }}
        id="activity-tabs"
      >
        <Tab eventKey="feed" title="Following Feed">
          <FeedTab key={`feed-${typeFilter}`} fetchFn={getFeed} typeFilter={typeFilter} />
        </Tab>
        <Tab eventKey="mine" title="My Activity">
          <FeedTab key={`mine-${typeFilter}`} fetchFn={getMyActivities} typeFilter={typeFilter} />
        </Tab>
      </Tabs>
    </Container>
  )
}

export default ActivityFeedPage

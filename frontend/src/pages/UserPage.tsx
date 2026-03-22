import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Badge from 'react-bootstrap/Badge'
import Card from 'react-bootstrap/Card'
import Spinner from 'react-bootstrap/Spinner'
import Tab from 'react-bootstrap/Tab'
import Tabs from 'react-bootstrap/Tabs'
import { Link, useParams } from 'react-router-dom'
import { getListsByUser } from '../api/lists'
import { getBookById, getUserById } from '../api/search'
import { getReadingStatusesByUser, getReviewsByUser } from '../api/userPage'
import BookTile from '../features/books/BookTile'
import ListTile from '../features/lists/ListTile'
import type { BookTileItem } from '../types/books'
import type { ListItem, UserProfileItem } from '../types/search'
import type { ReadingStatusItem, UserReviewItem } from '../types/userPage'

function formatReviewDate(value?: string): string {
  if (!value) {
    return 'Unknown date'
  }

  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) {
    return 'Unknown date'
  }

  return parsed.toLocaleDateString()
}

function UserPage() {
  const { userId } = useParams()

  const [user, setUser] = useState<UserProfileItem | null>(null)
  const [readingStatuses, setReadingStatuses] = useState<ReadingStatusItem[]>([])
  const [lists, setLists] = useState<ListItem[]>([])
  const [reviews, setReviews] = useState<UserReviewItem[]>([])
  const [booksById, setBooksById] = useState<Record<number, BookTileItem>>({})
  const [activeTab, setActiveTab] = useState('read-books')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const parsedUserId = Number(userId)
    if (!userId || Number.isNaN(parsedUserId)) {
      setError('Invalid user id.')
      setLoading(false)
      return
    }

    const loadUserData = async () => {
      setLoading(true)
      setError(null)

      try {
        const [userResult, statusesResult, listsResult, reviewsResult] = await Promise.allSettled([
          getUserById(parsedUserId),
          getReadingStatusesByUser(parsedUserId),
          getListsByUser(parsedUserId),
          getReviewsByUser(parsedUserId),
        ])

        if (userResult.status === 'rejected') {
          throw userResult.reason
        }

        const nextStatuses = statusesResult.status === 'fulfilled' ? statusesResult.value : []
        const nextLists = listsResult.status === 'fulfilled' ? listsResult.value.content : []
        const nextReviews = reviewsResult.status === 'fulfilled' ? reviewsResult.value : []

        setUser(userResult.value)
        setReadingStatuses(nextStatuses)
        setLists(nextLists)
        setReviews(nextReviews)

        const statusBookIds = nextStatuses.map((item) => item.bookId)
        const reviewBookIds = nextReviews.map((item) => item.bookId)
        const uniqueBookIds = Array.from(new Set([...statusBookIds, ...reviewBookIds]))

        if (uniqueBookIds.length > 0) {
          const bookResults = await Promise.allSettled(uniqueBookIds.map((bookId) => getBookById(bookId)))
          const nextBooksById: Record<number, BookTileItem> = {}

          bookResults.forEach((result) => {
            if (result.status === 'fulfilled') {
              nextBooksById[result.value.id] = {
                id: result.value.id,
                title: result.value.title,
                author: result.value.author,
                coverImageUrl: result.value.coverImageUrl,
                description: result.value.description,
              }
            }
          })

          setBooksById(nextBooksById)
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load user page.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadUserData()
  }, [userId])

  const readBooks = useMemo(
    () =>
      readingStatuses
        .filter((item) => item.currentStatus === 'READ')
        .map((item) => booksById[item.bookId])
        .filter((book): book is BookTileItem => Boolean(book)),
    [booksById, readingStatuses],
  )

  const wantToReadBooks = useMemo(
    () =>
      readingStatuses
        .filter((item) => item.currentStatus === 'WANT_TO_READ')
        .map((item) => booksById[item.bookId])
        .filter((book): book is BookTileItem => Boolean(book)),
    [booksById, readingStatuses],
  )

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading user page...</span>
      </div>
    )
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>
  }

  if (!user) {
    return <Alert variant="warning">User not found.</Alert>
  }

  return (
    <div className="user-page-root">
      <section className="mb-4">
        <h2 className="mb-1">{user.userName}</h2>
        {user.bio && <p className="text-muted mb-1">{user.bio}</p>}
      </section>

      <Tabs
        id="user-detail-tabs"
        activeKey={activeTab}
        onSelect={(key) => setActiveTab(key ?? 'read-books')}
        className="mb-3"
      >
        <Tab eventKey="read-books" title={`Read Books (${readBooks.length})`}>
          {readBooks.length === 0 ? (
            <Alert variant="light" className="mt-3">
              No books with READ status yet.
            </Alert>
          ) : (
            <div className="books-grid mt-3">
              {readBooks.map((book) => (
                <BookTile key={book.id} book={book} />
              ))}
            </div>
          )}

          <Card className="mt-4 user-want-card">
            <Card.Body>
              <Card.Title className="d-flex align-items-center justify-content-between">
                <span>Want To Read</span>
                <Badge bg="dark">{wantToReadBooks.length}</Badge>
              </Card.Title>

              {wantToReadBooks.length === 0 ? (
                <Card.Text className="text-muted mb-0">
                  This user has no WANT_TO_READ books yet.
                </Card.Text>
              ) : (
                <div className="books-grid mt-3">
                  {wantToReadBooks.map((book) => (
                    <BookTile key={book.id} book={book} />
                  ))}
                </div>
              )}
            </Card.Body>
          </Card>
        </Tab>

        <Tab eventKey="lists" title={`Lists (${lists.length})`}>
          {lists.length === 0 ? (
            <Alert variant="light" className="mt-3">
              This user has not created any lists yet.
            </Alert>
          ) : (
            <div className="lists-grid mt-3">
              {lists.map((list) => (
                <ListTile key={list.id} list={list} />
              ))}
            </div>
          )}
        </Tab>

        <Tab eventKey="reviews" title={`Reviews (${reviews.length})`}>
          {reviews.length === 0 ? (
            <Alert variant="light" className="mt-3">
              This user has not added any reviews yet.
            </Alert>
          ) : (
            <div className="d-grid gap-3 mt-3">
              {reviews.map((review) => {
                const book = booksById[review.bookId]
                return (
                  <Card key={review.id}>
                    <Card.Body>
                      <div className="d-flex flex-wrap align-items-center gap-2 mb-2">
                        {book ? (
                          <Link to={`/books/${book.id}`} className="fw-semibold text-decoration-none">
                            {book.title}
                          </Link>
                        ) : (
                          <span className="fw-semibold">Book #{review.bookId}</span>
                        )}
                        <Badge bg="warning" text="dark">
                          {review.rating}/5
                        </Badge>
                        <span className="text-muted small">{formatReviewDate(review.createdAt)}</span>
                      </div>
                      <p className="mb-0">{review.reviewText?.trim() || 'No review text provided.'}</p>
                    </Card.Body>
                  </Card>
                )
              })}
            </div>
          )}
        </Tab>
      </Tabs>
    </div>
  )
}

export default UserPage

import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Badge from 'react-bootstrap/Badge'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Spinner from 'react-bootstrap/Spinner'
import Tab from 'react-bootstrap/Tab'
import Tabs from 'react-bootstrap/Tabs'
import { Link, useNavigate } from 'react-router-dom'
import { getListsByUser } from '../api/lists'
import { getBookById, getCurrentUser } from '../api/search'
import { getReadingStatusesByUser, getReviewsByUser } from '../api/userPage'
import { updateCurrentUserProfile } from '../api/profile'
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

const REVIEWS_PER_PAGE = 5

function ProfilePage() {
  const navigate = useNavigate()
  const [user, setUser] = useState<UserProfileItem | null>(null)
  const [readingStatuses, setReadingStatuses] = useState<ReadingStatusItem[]>([])
  const [lists, setLists] = useState<ListItem[]>([])
  const [reviews, setReviews] = useState<UserReviewItem[]>([])
  const [booksById, setBooksById] = useState<Record<number, BookTileItem>>({})
  const [activeTab, setActiveTab] = useState('read-books')
  const [reviewsPage, setReviewsPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [profilePublic, setProfilePublic] = useState(true)
  const [savingPrivacy, setSavingPrivacy] = useState(false)

  useEffect(() => {
    const loadProfileData = async () => {
      setLoading(true)
      setError(null)

      try {
        const currentUser = await getCurrentUser()
        setUser(currentUser)
        setProfilePublic(currentUser.isProfilePublic ?? true)

        const [statusesResult, listsResult, reviewsResult] = await Promise.allSettled([
          getReadingStatusesByUser(currentUser.id),
          getListsByUser(currentUser.id),
          getReviewsByUser(currentUser.id),
        ])

        const nextStatuses = statusesResult.status === 'fulfilled' ? statusesResult.value : []
        const nextLists = listsResult.status === 'fulfilled' ? listsResult.value.content : []
        const nextReviews = reviewsResult.status === 'fulfilled' ? reviewsResult.value : []

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
        const message = err instanceof Error ? err.message : 'Unable to load your page.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadProfileData()
  }, [])

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

  const currentlyReadingBooks = useMemo(
    () =>
      readingStatuses
        .filter((item) => item.currentStatus === 'CURRENTLY_READING')
        .map((item) => booksById[item.bookId])
        .filter((book): book is BookTileItem => Boolean(book)),
    [booksById, readingStatuses],
  )

  const totalReviewPages = reviews.length === 0 ? 1 : Math.ceil(reviews.length / REVIEWS_PER_PAGE)
  const pagedReviews = reviews.slice(
    reviewsPage * REVIEWS_PER_PAGE,
    reviewsPage * REVIEWS_PER_PAGE + REVIEWS_PER_PAGE,
  )

  useEffect(() => {
    setReviewsPage((value) => {
      if (reviews.length === 0) {
        return 0
      }
      const lastPage = Math.max(0, Math.ceil(reviews.length / REVIEWS_PER_PAGE) - 1)
      return Math.min(value, lastPage)
    })
  }, [reviews.length])

  const handlePrivacyToggle = async () => {
    if (!user) return
    const newValue = !profilePublic
    setSavingPrivacy(true)
    setProfilePublic(newValue)
    try {
      await updateCurrentUserProfile(user.id, {
        userName: user.userName,
        emailId: user.emailId,
        bio: user.bio,
        isProfilePublic: newValue,
      })
    } catch {
      setProfilePublic(!newValue)
    } finally {
      setSavingPrivacy(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading your page...</span>
      </div>
    )
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>
  }

  return (
    <div className="user-page-root">
      <section className="mb-4">
        <div className="d-flex justify-content-between align-items-center mb-2">
          <h2 className="mb-0">{user?.userName ?? 'User Page'}</h2>
          <div className="d-flex gap-2">
            <Button
              variant={profilePublic ? 'outline-secondary' : 'secondary'}
              size="sm"
              disabled={savingPrivacy || !user}
              onClick={handlePrivacyToggle}
            >
              {savingPrivacy ? 'Saving...' : profilePublic ? '🌐 Public' : '🔒 Private'}
            </Button>
            <Button
              variant="outline-primary"
              size="sm"
              onClick={() => navigate('/profile/edit')}
            >
              Edit Profile
            </Button>
          </div>
        </div>
        <p className="text-muted mb-0">Track what you read, save what you want next, and revisit your reviews.</p>
      </section>

      <Tabs
        id="user-page-tabs"
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
        </Tab>

        <Tab eventKey="want-to-read" title={`Want To Read (${wantToReadBooks.length})`}>
          <Card className="mt-3 user-want-card">
            <Card.Body>
              <Card.Title className="d-flex align-items-center justify-content-between">
                <span>Want To Read</span>
                <Badge bg="dark">{wantToReadBooks.length}</Badge>
              </Card.Title>

              {wantToReadBooks.length === 0 ? (
                <Card.Text className="text-muted mb-0">
                  Add books to WANT_TO_READ to build your reading queue.
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

        <Tab eventKey="reading" title={`Reading (${currentlyReadingBooks.length})`}>
          {currentlyReadingBooks.length === 0 ? (
            <Alert variant="light" className="mt-3">
              No books with CURRENTLY_READING status yet.
            </Alert>
          ) : (
            <div className="books-grid mt-3">
              {currentlyReadingBooks.map((book) => (
                <BookTile key={book.id} book={book} />
              ))}
            </div>
          )}
        </Tab>

        <Tab eventKey="lists" title={`Lists (${lists.length})`}>
          {lists.length === 0 ? (
            <Alert variant="light" className="mt-3">
              You have not created any lists yet.
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
              You have not added any reviews yet.
            </Alert>
          ) : (
            <>
              <div className="d-grid gap-3 mt-3">
                {pagedReviews.map((review) => {
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

              <div className="d-flex align-items-center justify-content-between mt-3">
                <Button
                  type="button"
                  variant="outline-secondary"
                  size="sm"
                  disabled={reviewsPage <= 0}
                  onClick={() => setReviewsPage((value) => Math.max(0, value - 1))}
                >
                  Previous
                </Button>
                <span className="text-muted small">
                  Page {reviewsPage + 1} of {totalReviewPages}
                </span>
                <Button
                  type="button"
                  variant="outline-dark"
                  size="sm"
                  disabled={reviewsPage >= totalReviewPages - 1}
                  onClick={() => setReviewsPage((value) => Math.min(totalReviewPages - 1, value + 1))}
                >
                  Next
                </Button>
              </div>
            </>
          )}
        </Tab>
      </Tabs>
    </div>
  )
}

export default ProfilePage

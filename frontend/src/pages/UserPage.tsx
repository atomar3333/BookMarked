import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Badge from 'react-bootstrap/Badge'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Spinner from 'react-bootstrap/Spinner'
import Tab from 'react-bootstrap/Tab'
import Tabs from 'react-bootstrap/Tabs'
import { Link, useParams } from 'react-router-dom'
import {
  followUser,
  getFollowerCount,
  getFollowingCount,
  getIsFollowing,
  unfollowUser,
} from '../api/followers'
import { getListsByUser } from '../api/lists'
import { getBookById, getCurrentUser, getUserById } from '../api/search'
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

const REVIEWS_PER_PAGE = 5

function UserPage() {
  const { userId } = useParams()

  const [currentUser, setCurrentUser] = useState<UserProfileItem | null>(null)
  const [user, setUser] = useState<UserProfileItem | null>(null)
  const [readingStatuses, setReadingStatuses] = useState<ReadingStatusItem[]>([])
  const [lists, setLists] = useState<ListItem[]>([])
  const [reviews, setReviews] = useState<UserReviewItem[]>([])
  const [booksById, setBooksById] = useState<Record<number, BookTileItem>>({})
  const [isFollowing, setIsFollowing] = useState(false)
  const [followerCount, setFollowerCount] = useState(0)
  const [followingCount, setFollowingCount] = useState(0)
  const [savingFollow, setSavingFollow] = useState(false)
  const [followError, setFollowError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState('read-books')
  const [reviewsPage, setReviewsPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isPrivateProfile, setIsPrivateProfile] = useState(false)

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
      setFollowError(null)
      setIsPrivateProfile(false)

      try {
        const [
          currentUserResult,
          userResult,
          statusesResult,
          listsResult,
          reviewsResult,
          followerCountResult,
          followingCountResult,
        ] = await Promise.allSettled([
          getCurrentUser(),
          getUserById(parsedUserId),
          getReadingStatusesByUser(parsedUserId),
          getListsByUser(parsedUserId),
          getReviewsByUser(parsedUserId),
          getFollowerCount(parsedUserId),
          getFollowingCount(parsedUserId),
        ])

        if (userResult.status === 'rejected') {
          if (userResult.reason instanceof Error && userResult.reason.message === 'PRIVATE_PROFILE') {
            setIsPrivateProfile(true)
            return
          }
          throw userResult.reason
        }

        const nextStatuses = statusesResult.status === 'fulfilled' ? statusesResult.value : []
        const nextLists = listsResult.status === 'fulfilled' ? listsResult.value.content : []
        const nextReviews = reviewsResult.status === 'fulfilled' ? reviewsResult.value : []
        const viewer = currentUserResult.status === 'fulfilled' ? currentUserResult.value : null

        setCurrentUser(viewer)
        setUser(userResult.value)
        setReadingStatuses(nextStatuses)
        setLists(nextLists)
        setReviews(nextReviews)
        setFollowerCount(followerCountResult.status === 'fulfilled' ? followerCountResult.value : 0)
        setFollowingCount(followingCountResult.status === 'fulfilled' ? followingCountResult.value : 0)

        if (viewer && viewer.id !== parsedUserId) {
          try {
            const nextIsFollowing = await getIsFollowing(viewer.id, parsedUserId)
            setIsFollowing(nextIsFollowing)
          } catch {
            setIsFollowing(false)
          }
        } else {
          setIsFollowing(false)
        }

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

  const canFollow = Boolean(currentUser && user && currentUser.id !== user.id)
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

  const handleFollowToggle = async () => {
    if (!currentUser || !user) {
      return
    }

    const previousFollowing = isFollowing
    setFollowError(null)
    setSavingFollow(true)
    setIsFollowing(!previousFollowing)
    setFollowerCount((value) => Math.max(0, value + (previousFollowing ? -1 : 1)))

    try {
      if (previousFollowing) {
        await unfollowUser(currentUser.id, user.id)
      } else {
        await followUser(currentUser.id, user.id)
      }
    } catch (err) {
      setIsFollowing(previousFollowing)
      setFollowerCount((value) => Math.max(0, value + (previousFollowing ? 1 : -1)))
      const message = err instanceof Error ? err.message : 'Unable to update follow status.'
      setFollowError(message)
    } finally {
      setSavingFollow(false)
    }
  }

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

  if (isPrivateProfile) {
    return (
      <Alert variant="secondary" className="mt-3">
        <strong>Private Profile</strong> — This user has set their profile to private.
      </Alert>
    )
  }

  if (!user) {
    return <Alert variant="warning">User not found.</Alert>
  }

  return (
    <div className="user-page-root">
      <section className="mb-4">
        <div className="d-flex flex-wrap align-items-start justify-content-between gap-3">
          <div>
            <h2 className="mb-1">{user.userName}</h2>
            <div className="d-flex flex-wrap align-items-center gap-3 text-muted small mb-2">
              <span>{followerCount} follower{followerCount === 1 ? '' : 's'}</span>
              <span>{followingCount} following</span>
            </div>
            {user.bio && <p className="text-muted mb-1">{user.bio}</p>}
          </div>

          {canFollow && (
            <Button
              type="button"
              variant={isFollowing ? 'outline-dark' : 'dark'}
              disabled={savingFollow}
              onClick={handleFollowToggle}
            >
              {savingFollow ? 'Saving...' : isFollowing ? 'Unfollow' : 'Follow'}
            </Button>
          )}
        </div>

        {followError && (
          <Alert variant="danger" className="mt-3 mb-0">
            {followError}
          </Alert>
        )}
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

export default UserPage

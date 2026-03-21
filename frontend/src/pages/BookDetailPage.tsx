import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Badge from 'react-bootstrap/Badge'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Form from 'react-bootstrap/Form'
import ListGroup from 'react-bootstrap/ListGroup'
import ProgressBar from 'react-bootstrap/ProgressBar'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import { useParams } from 'react-router-dom'
import {
  createReview,
  getBookAverageRating,
  getBookById,
  getBookReviews,
  getCurrentUser,
  getUserById,
} from '../api/search'
import type { BookDetail, ReviewItem, UserProfileItem } from '../types/search'

interface RatingDistributionItem {
  stars: number
  count: number
  percentage: number
}

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

function buildDistribution(reviews: ReviewItem[]): RatingDistributionItem[] {
  const total = reviews.length
  const counts = new Map<number, number>([
    [1, 0],
    [2, 0],
    [3, 0],
    [4, 0],
    [5, 0],
  ])

  reviews.forEach((review) => {
    const normalized = Math.max(1, Math.min(5, Math.round(review.rating)))
    counts.set(normalized, (counts.get(normalized) ?? 0) + 1)
  })

  return [5, 4, 3, 2, 1].map((stars) => {
    const count = counts.get(stars) ?? 0
    return {
      stars,
      count,
      percentage: total > 0 ? (count / total) * 100 : 0,
    }
  })
}

function BookDetailPage() {
  const { bookId } = useParams()
  const [book, setBook] = useState<BookDetail | null>(null)
  const [averageRating, setAverageRating] = useState<number | null>(null)
  const [reviews, setReviews] = useState<ReviewItem[]>([])
  const [reviewUserNames, setReviewUserNames] = useState<Record<number, string>>({})
  const [currentUser, setCurrentUser] = useState<UserProfileItem | null>(null)
  const [ratingWarning, setRatingWarning] = useState<string | null>(null)
  const [reviewText, setReviewText] = useState('')
  const [selectedRating, setSelectedRating] = useState(0)
  const [reviewError, setReviewError] = useState<string | null>(null)
  const [reviewSuccess, setReviewSuccess] = useState<string | null>(null)
  const [submittingReview, setSubmittingReview] = useState(false)
  const [reloadToken, setReloadToken] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const parsedId = Number(bookId)
    if (!bookId || Number.isNaN(parsedId)) {
      setError('Invalid book id.')
      setLoading(false)
      return
    }

    const loadBook = async () => {
      try {
        setRatingWarning(null)

        const [bookResult, averageResult, reviewsResult, currentUserResult] = await Promise.allSettled([
          getBookById(parsedId),
          getBookAverageRating(parsedId),
          getBookReviews(parsedId),
          getCurrentUser(),
        ])

        if (bookResult.status === 'rejected') {
          throw bookResult.reason
        }

        setBook(bookResult.value)

        if (averageResult.status === 'fulfilled') {
          setAverageRating(averageResult.value)
        } else {
          setRatingWarning('Average rating is unavailable right now.')
        }

        if (currentUserResult.status === 'fulfilled') {
          setCurrentUser(currentUserResult.value)
        } else {
          setCurrentUser(null)
        }

        if (reviewsResult.status === 'fulfilled') {
          const reviewItems = reviewsResult.value
          setReviews(reviewItems)

          const uniqueUserIds = Array.from(new Set(reviewItems.map((item) => item.userId)))
          if (uniqueUserIds.length > 0) {
            const userResults = await Promise.allSettled(
              uniqueUserIds.map((userId) => getUserById(userId)),
            )

            const usersById: Record<number, string> = {}
            let hasUserLookupFailure = false

            userResults.forEach((result, index) => {
              if (result.status === 'fulfilled') {
                usersById[result.value.id] = result.value.userName
              } else {
                hasUserLookupFailure = true
                usersById[uniqueUserIds[index]] = `User #${uniqueUserIds[index]}`
              }
            })

            setReviewUserNames(usersById)

            if (hasUserLookupFailure) {
              setRatingWarning('Some reviewer names are unavailable right now.')
            }
          }
        } else {
          setRatingWarning('Rating distribution is unavailable right now.')
        }
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load book details.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadBook()
  }, [bookId, reloadToken])

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading book...</span>
      </div>
    )
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>
  }

  if (!book) {
    return <Alert variant="warning">Book not found.</Alert>
  }

  const distribution = buildDistribution(reviews)
  const existingReview = currentUser
    ? reviews.find((review) => review.userId === currentUser.id)
    : undefined

  const handleReviewSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setReviewError(null)
    setReviewSuccess(null)

    const parsedId = Number(bookId)
    if (!currentUser) {
      setReviewError('Unable to determine the current user.')
      return
    }

    if (!parsedId || Number.isNaN(parsedId)) {
      setReviewError('Invalid book id.')
      return
    }

    if (selectedRating < 1 || selectedRating > 5) {
      setReviewError('Please select a star rating from 1 to 5.')
      return
    }

    setSubmittingReview(true)
    try {
      await createReview({
        userId: currentUser.id,
        bookId: parsedId,
        reviewText,
        rating: selectedRating,
      })
      setReviewText('')
      setSelectedRating(0)
      setReviewSuccess('Review submitted successfully.')
      setReloadToken((value) => value + 1)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to submit review.'
      setReviewError(message)
    } finally {
      setSubmittingReview(false)
    }
  }

  return (
    <Card className="book-template-card shadow-sm">
      <Card.Body>
        <Row className="g-4 align-items-start">
          <Col md={4}>
            {book.coverImageUrl ? (
              <img
                src={book.coverImageUrl}
                alt={`${book.title} cover`}
                className="book-cover-image"
              />
            ) : (
              <div className="book-cover-placeholder">No Cover</div>
            )}
          </Col>

          <Col md={8}>
            <h2 className="mb-1">{book.title}</h2>
            <p className="text-muted mb-3">by {book.author}</p>

            {ratingWarning && (
              <Alert variant="warning" className="py-2 mb-3">
                {ratingWarning}
              </Alert>
            )}

            <div className="book-rating-summary mb-3">
              <h6 className="mb-1">Average Rating</h6>
              <div className="book-average-rating">
                {averageRating !== null ? averageRating.toFixed(1) : 'N/A'}
                <span className="text-muted small"> / 5</span>
              </div>
              <div className="text-muted small">Based on {reviews.length} review(s)</div>
            </div>

            <div className="book-rating-distribution mb-4">
              <h6 className="mb-2">Rating Distribution</h6>
              {distribution.map((item) => (
                <div key={item.stars} className="d-flex align-items-center gap-2 mb-2">
                  <div className="rating-label">{item.stars}★</div>
                  <ProgressBar now={item.percentage} className="flex-grow-1" />
                  <div className="rating-count text-muted small">{item.count}</div>
                </div>
              ))}
            </div>

            <h6>Description</h6>
            <p className="mb-4">
              {book.description && book.description.trim().length > 0
                ? book.description
                : 'No description available for this book yet.'}
            </p>

            <h6 className="mb-3">Add Your Review</h6>
            {reviewError && <Alert variant="danger">{reviewError}</Alert>}
            {reviewSuccess && <Alert variant="success">{reviewSuccess}</Alert>}
            {existingReview ? (
              <Alert variant="info">
                You have already reviewed this book.
              </Alert>
            ) : (
              <Form onSubmit={handleReviewSubmit} className="mb-4">
                <div className="mb-3">
                  <div className="small text-muted mb-2">Your rating</div>
                  <div className="star-rating-selector">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <button
                        key={star}
                        type="button"
                        className={star <= selectedRating ? 'star-button active' : 'star-button'}
                        onClick={() => setSelectedRating(star)}
                        aria-label={`Set rating to ${star} star${star > 1 ? 's' : ''}`}
                      >
                        ★
                      </button>
                    ))}
                  </div>
                </div>

                <Form.Group className="mb-3" controlId="review-text">
                  <Form.Label>Review</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={4}
                    value={reviewText}
                    onChange={(event) => setReviewText(event.target.value)}
                    placeholder="Write a short review"
                  />
                </Form.Group>

                <Button type="submit" disabled={submittingReview}>
                  {submittingReview ? 'Submitting...' : 'Submit Review'}
                </Button>
              </Form>
            )}

            <h6 className="mb-3">Reviews</h6>
            {reviews.length === 0 ? (
              <Alert variant="light" className="mb-0">
                No reviews yet for this book.
              </Alert>
            ) : (
              <ListGroup variant="flush" className="book-review-list">
                {reviews.map((review) => (
                  <ListGroup.Item key={review.id} className="px-0">
                    <div className="d-flex justify-content-between align-items-center mb-1 gap-2">
                      <div className="d-flex align-items-center gap-2">
                        <Badge bg="dark">{Math.max(1, Math.min(5, Math.round(review.rating)))}★</Badge>
                        <span className="text-muted small">
                          {reviewUserNames[review.userId] ?? `User #${review.userId}`}
                        </span>
                      </div>
                      <span className="text-muted small">{formatReviewDate(review.createdAt)}</span>
                    </div>
                    <div className="mb-0">
                      {review.reviewText && review.reviewText.trim().length > 0
                        ? review.reviewText
                        : 'No review text provided.'}
                    </div>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            )}
          </Col>
        </Row>
      </Card.Body>
    </Card>
  )
}

export default BookDetailPage

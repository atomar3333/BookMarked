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
import { Link, useParams } from 'react-router-dom'
import { addBookToList, createList, getListsByUser } from '../api/lists'
import { getReadingStatusForUserBook, upsertReadingStatus } from '../api/readingStatus'
import {
  createReview,
  deleteReview,
  getBookAverageRating,
  getBookById,
  getBookReviews,
  getCurrentUser,
  getUserById,
  updateReview,
} from '../api/search'
import { getBookLikeStats, likeBook, unlikeBook, getReviewLikeStats, likeReview, unlikeReview } from '../api/likes'
import type { BookDetail, ListItem, ReviewItem, UserProfileItem } from '../types/search'
import type { ReadingStatusValue } from '../types/userPage'
import ReadingStatusActions from '../components/ReadingStatusActions'

interface RatingDistributionItem {
  stars: number
  count: number
  percentage: number
}

function normalizeHalfStepRating(value: number): number {
  const clamped = Math.max(1, Math.min(5, value))
  return Math.round(clamped * 2) / 2
}

function formatRating(value: number): string {
  return Number.isInteger(value) ? `${value}` : value.toFixed(1)
}

function getStarFillPercentage(starNumber: number, rating: number): number {
  const fill = rating - (starNumber - 1)
  if (fill >= 1) {
    return 100
  }
  if (fill <= 0) {
    return 0
  }
  return fill * 100
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

const REVIEWS_PER_PAGE = 5

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
  const [deletingReview, setDeletingReview] = useState(false)
  const [reviewPage, setReviewPage] = useState(0)
  const [lists, setLists] = useState<ListItem[]>([])
  const [listError, setListError] = useState<string | null>(null)
  const [listSuccess, setListSuccess] = useState<string | null>(null)
  const [listLoading, setListLoading] = useState(false)
  const [creatingList, setCreatingList] = useState(false)
  const [activeListId, setActiveListId] = useState<number | null>(null)
  const [newListTitle, setNewListTitle] = useState('')
  const [newListDescription, setNewListDescription] = useState('')
  const [submittingReview, setSubmittingReview] = useState(false)
  const [reloadToken, setReloadToken] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentStatus, setCurrentStatus] = useState<ReadingStatusValue | undefined>(undefined)
  const [savingStatus, setSavingStatus] = useState(false)
  const [bookLikeCount, setBookLikeCount] = useState(0)
  const [bookLikedByCurrentUser, setBookLikedByCurrentUser] = useState(false)
  const [likingBook, setLikingBook] = useState(false)
  const [reviewLikeStates, setReviewLikeStates] = useState<Record<number, { count: number; liked: boolean }>>({})
  const [likingReviewId, setLikingReviewId] = useState<number | null>(null)
  const [likeError, setLikeError] = useState<string | null>(null)

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
        setLikeError(null)

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

        // Load book like stats
        try {
          const bookLikeStats = await getBookLikeStats(parsedId)
          setBookLikeCount(bookLikeStats.likeCount)
          setBookLikedByCurrentUser(bookLikeStats.likedByCurrentUser)
        } catch {
          setBookLikeCount(0)
          setBookLikedByCurrentUser(false)
        }

        if (averageResult.status === 'fulfilled') {
          setAverageRating(averageResult.value)
        } else {
          setRatingWarning('Average rating is unavailable right now.')
        }

        if (currentUserResult.status === 'fulfilled') {
          const user = currentUserResult.value
          setCurrentUser(user)
          setListLoading(true)

          try {
            const listPage = await getListsByUser(user.id)
            setLists(listPage.content)
          } catch (listLoadError) {
            const message =
              listLoadError instanceof Error
                ? listLoadError.message
                : 'Unable to load your lists right now.'
            setListError(message)
          } finally {
            setListLoading(false)
          }

          const existingStatus = await getReadingStatusForUserBook(user.id, parsedId)
          setCurrentStatus(existingStatus?.currentStatus)
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

            // Load like stats for all reviews
            const reviewLikeResults = await Promise.allSettled(
              reviewItems.map((review) => getReviewLikeStats(review.id)),
            )

            const likeStates: Record<number, { count: number; liked: boolean }> = {}
            reviewItems.forEach((review, index) => {
              const result = reviewLikeResults[index]
              if (result.status === 'fulfilled') {
                likeStates[review.id] = {
                  count: result.value.likeCount,
                  liked: result.value.likedByCurrentUser,
                }
              } else {
                likeStates[review.id] = { count: 0, liked: false }
              }
            })

            setReviewLikeStates(likeStates)
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

  const distribution = buildDistribution(reviews)
  const existingReview = currentUser
    ? reviews.find((review) => review.userId === currentUser.id)
    : undefined

  const totalReviewPages = reviews.length === 0 ? 1 : Math.ceil(reviews.length / REVIEWS_PER_PAGE)
  const pagedReviews = reviews.slice(
    reviewPage * REVIEWS_PER_PAGE,
    reviewPage * REVIEWS_PER_PAGE + REVIEWS_PER_PAGE,
  )

  useEffect(() => {
    if (existingReview) {
      setReviewText(existingReview.reviewText ?? '')
      setSelectedRating(normalizeHalfStepRating(existingReview.rating))
      return
    }

    setReviewText('')
    setSelectedRating(0)
  }, [existingReview?.id, existingReview?.reviewText, existingReview?.rating])

  useEffect(() => {
    setReviewPage((value) => {
      if (reviews.length === 0) {
        return 0
      }
      const lastPage = Math.max(0, Math.ceil(reviews.length / REVIEWS_PER_PAGE) - 1)
      return Math.min(value, lastPage)
    })
  }, [reviews.length])

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
      if (existingReview) {
        await updateReview(existingReview.id, {
          reviewText,
          rating: selectedRating,
        })
        setReviewSuccess('Review updated successfully.')
      } else {
        await createReview({
          userId: currentUser.id,
          bookId: parsedId,
          reviewText,
          rating: selectedRating,
        })
        setReviewSuccess('Review submitted successfully.')
      }
      setReloadToken((value) => value + 1)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to save review.'
      setReviewError(message)
    } finally {
      setSubmittingReview(false)
    }
  }

  const handleDeleteReview = async (reviewId: number) => {
    const confirmed = window.confirm('Delete your review for this book?')
    if (!confirmed) {
      return
    }

    setReviewError(null)
    setReviewSuccess(null)
    setDeletingReview(true)
    try {
      await deleteReview(reviewId)
      setReviewText('')
      setSelectedRating(0)
      setReviewSuccess('Review deleted successfully.')
      setReloadToken((value) => value + 1)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to delete review.'
      setReviewError(message)
    } finally {
      setDeletingReview(false)
    }
  }

  const handleEditExistingReview = (review: ReviewItem) => {
    setReviewError(null)
    setReviewSuccess(null)
    setReviewText(review.reviewText ?? '')
    setSelectedRating(normalizeHalfStepRating(review.rating))
  }

  const parsedBookId = Number(bookId)

  const handleSetStatus = async (status: ReadingStatusValue) => {
    if (!currentUser) return
    const previous = currentStatus
    setCurrentStatus(status)
    setSavingStatus(true)
    try {
      await upsertReadingStatus({ userId: currentUser.id, bookId: parsedBookId, currentStatus: status })
    } catch {
      setCurrentStatus(previous)
    } finally {
      setSavingStatus(false)
    }
  }

  const handleAddToExistingList = async (listId: number) => {
    setListError(null)
    setListSuccess(null)

    if (!parsedBookId || Number.isNaN(parsedBookId)) {
      setListError('Invalid book id.')
      return
    }

    setActiveListId(listId)
    try {
      await addBookToList(listId, parsedBookId)
      const addedList = lists.find((item) => item.id === listId)
      setListSuccess(
        addedList ? `Added to "${addedList.title}".` : 'Book added to the selected list.',
      )
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to add book to list.'
      setListError(message)
    } finally {
      setActiveListId(null)
    }
  }

  const handleCreateListAndAdd = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setListError(null)
    setListSuccess(null)

    if (!currentUser) {
      setListError('Unable to determine the current user.')
      return
    }

    if (!parsedBookId || Number.isNaN(parsedBookId)) {
      setListError('Invalid book id.')
      return
    }

    if (!newListTitle.trim()) {
      setListError('List title is required.')
      return
    }

    setCreatingList(true)
    try {
      const createdList = await createList({
        userId: currentUser.id,
        title: newListTitle.trim(),
        description: newListDescription.trim(),
      })

      await addBookToList(createdList.id, parsedBookId)

      setLists((existing) => [createdList, ...existing])
      setNewListTitle('')
      setNewListDescription('')
      setListSuccess(`Created "${createdList.title}" and added this book to it.`)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to create list.'
      setListError(message)
    } finally {
      setCreatingList(false)
    }
  }

  const handleLikeBook = async () => {
    if (!currentUser) {
      setLikeError('You must be logged in to like this book.')
      return
    }

    const wasLiked = bookLikedByCurrentUser

    setLikingBook(true)
    setLikeError(null)
    setBookLikedByCurrentUser(!wasLiked)
    setBookLikeCount((count) => Math.max(0, count + (wasLiked ? -1 : 1)))

    try {
      if (wasLiked) {
        await unlikeBook(parsedBookId)
      } else {
        await likeBook(parsedBookId)
      }
    } catch (err) {
      setBookLikedByCurrentUser(wasLiked)
      setBookLikeCount((count) => Math.max(0, count + (wasLiked ? 1 : -1)))
      const message = err instanceof Error ? err.message : 'Unable to like book.'
      setLikeError(message)
    } finally {
      setLikingBook(false)
    }
  }

  const handleLikeReview = async (reviewId: number) => {
    if (!currentUser) {
      setLikeError('You must be logged in to like this review.')
      return
    }

    const currentState = reviewLikeStates[reviewId] || { count: 0, liked: false }
    const nextState = {
      count: Math.max(0, currentState.count + (currentState.liked ? -1 : 1)),
      liked: !currentState.liked,
    }

    setLikingReviewId(reviewId)
    setLikeError(null)
    setReviewLikeStates((prev) => ({
      ...prev,
      [reviewId]: nextState,
    }))

    try {
      if (currentState.liked) {
        await unlikeReview(reviewId)
      } else {
        await likeReview(reviewId)
      }
    } catch (err) {
      setReviewLikeStates((prev) => ({
        ...prev,
        [reviewId]: currentState,
      }))
      const message = err instanceof Error ? err.message : 'Unable to like review.'
      setLikeError(message)
    } finally {
      setLikingReviewId(null)
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
            <p className="text-muted mb-3">
              {book.authors && book.authors.length > 0 ? (
                <>
                  by{' '}
                  {book.authors.map((author, index) => (
                    <span key={author.id}>
                      {index > 0 ? ', ' : ''}
                      <Link to={`/authors/${author.id}`}>{author.authorName}</Link>
                    </span>
                  ))}
                </>
              ) : (
                <>
                  by <Link to={`/authors?name=${encodeURIComponent(book.author)}`}>{book.author}</Link>
                </>
              )}
            </p>

            {currentUser && (
              <div className="mb-3">
                <ReadingStatusActions
                  currentStatus={currentStatus}
                  isSaving={savingStatus}
                  onSetStatus={handleSetStatus}
                />
              </div>
            )}

            <div className="mb-3">
              <Button
                variant={bookLikedByCurrentUser ? 'dark' : 'outline-dark'}
                size="sm"
                disabled={likingBook || !currentUser}
                onClick={handleLikeBook}
                className="d-flex align-items-center gap-2"
              >
                <span aria-hidden="true">{bookLikedByCurrentUser ? '♥' : '♡'}</span>
                <span>{likingBook ? 'Liking...' : 'Like'}</span>
                {bookLikeCount > 0 && <span className="small">({bookLikeCount})</span>}
              </Button>
              {likeError && <div className="text-danger small mt-2">{likeError}</div>}
            </div>

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

            <h6 className="mb-3">Add To List</h6>
            {listError && <Alert variant="danger">{listError}</Alert>}
            {listSuccess && <Alert variant="success">{listSuccess}</Alert>}

            <div className="book-list-section mb-4">
              <div className="mb-3">
                <div className="small text-muted mb-2">Add to an existing list</div>
                {listLoading ? (
                  <div className="d-flex align-items-center gap-2">
                    <Spinner animation="border" size="sm" />
                    <span>Loading your lists...</span>
                  </div>
                ) : lists.length === 0 ? (
                  <Alert variant="light" className="mb-0">
                    You do not have any lists yet. Create one below.
                  </Alert>
                ) : (
                  <div className="book-list-grid">
                    {lists.map((list) => (
                      <div key={list.id} className="book-list-item">
                        <div>
                          <div className="fw-semibold">{list.title}</div>
                          {list.description && (
                            <div className="text-muted small">{list.description}</div>
                          )}
                        </div>
                        <Button
                          size="sm"
                          variant="outline-dark"
                          disabled={activeListId === list.id || creatingList}
                          onClick={() => handleAddToExistingList(list.id)}
                        >
                          {activeListId === list.id ? 'Adding...' : 'Add'}
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <Form onSubmit={handleCreateListAndAdd}>
                <div className="small text-muted mb-2">Create a new list</div>
                <Form.Group className="mb-3" controlId="new-list-title">
                  <Form.Label>List title</Form.Label>
                  <Form.Control
                    type="text"
                    value={newListTitle}
                    onChange={(event) => setNewListTitle(event.target.value)}
                    placeholder="Favorites, Weekend Reads, Sci-Fi..."
                  />
                </Form.Group>

                <Form.Group className="mb-3" controlId="new-list-description">
                  <Form.Label>Description (optional)</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={3}
                    value={newListDescription}
                    onChange={(event) => setNewListDescription(event.target.value)}
                    placeholder="What is this list for?"
                  />
                </Form.Group>

                <Button type="submit" disabled={creatingList || activeListId !== null}>
                  {creatingList ? 'Creating list...' : 'Create List And Add Book'}
                </Button>
              </Form>
            </div>

            <h6 className="mb-3">{existingReview ? 'Edit Your Review' : 'Add Your Review'}</h6>
            {reviewError && <Alert variant="danger">{reviewError}</Alert>}
            {reviewSuccess && <Alert variant="success">{reviewSuccess}</Alert>}
            <Form onSubmit={handleReviewSubmit} className="mb-4">
              <div className="mb-3">
                <div className="small text-muted mb-2">Your rating</div>
                <div className="star-rating-selector" role="radiogroup" aria-label="Select rating from 1 to 5 in 0.5 steps">
                  {[1, 2, 3, 4, 5].map((starNumber) => {
                    const fillPercentage = getStarFillPercentage(starNumber, selectedRating)
                    const leftHalfValue = Math.max(1, starNumber - 0.5)
                    const rightHalfValue = starNumber

                    return (
                      <div key={starNumber} className="star-split-control">
                        <button
                          type="button"
                          className="star-half-button left"
                          onClick={() => setSelectedRating(leftHalfValue)}
                          aria-label={`Set rating to ${formatRating(leftHalfValue)} stars`}
                        />
                        <button
                          type="button"
                          className="star-half-button right"
                          onClick={() => setSelectedRating(rightHalfValue)}
                          aria-label={`Set rating to ${formatRating(rightHalfValue)} stars`}
                        />
                        <span className="star-icon" aria-hidden="true">
                          <span className="star-icon-base">★</span>
                          <span className="star-icon-fill" style={{ width: `${fillPercentage}%` }}>
                            ★
                          </span>
                        </span>
                      </div>
                    )
                  })}
                </div>
                <div className="small text-muted mt-2">
                  Selected: {selectedRating > 0 ? `${formatRating(selectedRating)} / 5` : 'Not selected'}
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

              <div className="d-flex flex-wrap gap-2">
                <Button type="submit" disabled={submittingReview || deletingReview}>
                  {submittingReview
                    ? existingReview
                      ? 'Saving...'
                      : 'Submitting...'
                    : existingReview
                      ? 'Save Changes'
                      : 'Submit Review'}
                </Button>
                {existingReview && (
                  <Button
                    type="button"
                    variant="outline-danger"
                    disabled={submittingReview || deletingReview}
                    onClick={() => handleDeleteReview(existingReview.id)}
                  >
                    {deletingReview ? 'Deleting...' : 'Delete Review'}
                  </Button>
                )}
              </div>
            </Form>

            <h6 className="mb-3">Reviews</h6>
            {reviews.length === 0 ? (
              <Alert variant="light" className="mb-0">
                No reviews yet for this book.
              </Alert>
            ) : (
              <>
                <ListGroup variant="flush" className="book-review-list">
                  {pagedReviews.map((review) => (
                  <ListGroup.Item key={review.id} className="px-0">
                    <div className="d-flex justify-content-between align-items-center mb-1 gap-2">
                      <div className="d-flex align-items-center gap-2">
                        <Badge bg="dark">{formatRating(normalizeHalfStepRating(review.rating))}★</Badge>
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
                    <div className="d-flex gap-2 align-items-center mt-2">
                      <Button
                        type="button"
                        size="sm"
                        variant={
                          reviewLikeStates[review.id]?.liked
                            ? 'dark'
                            : 'outline-secondary'
                        }
                        disabled={likingReviewId === review.id || !currentUser}
                        onClick={() => handleLikeReview(review.id)}
                        className="d-flex align-items-center gap-1"
                      >
                        <span className="small">
                          {reviewLikeStates[review.id]?.liked ? '♥' : '♡'}
                        </span>
                        <span className="small">
                          {likingReviewId === review.id ? 'Liking...' : 'Like'}
                        </span>
                        {(reviewLikeStates[review.id]?.count ?? 0) > 0 && (
                          <span className="small">
                            ({reviewLikeStates[review.id]?.count})
                          </span>
                        )}
                      </Button>
                    </div>
                    {currentUser && review.userId === currentUser.id && (
                      <div className="d-flex gap-2 mt-3">
                        <Button
                          type="button"
                          size="sm"
                          variant="outline-dark"
                          disabled={submittingReview || deletingReview}
                          onClick={() => handleEditExistingReview(review)}
                        >
                          Edit
                        </Button>
                        <Button
                          type="button"
                          size="sm"
                          variant="outline-danger"
                          disabled={submittingReview || deletingReview}
                          onClick={() => handleDeleteReview(review.id)}
                        >
                          {deletingReview ? 'Deleting...' : 'Delete'}
                        </Button>
                      </div>
                    )}
                  </ListGroup.Item>
                  ))}
                </ListGroup>

                <div className="d-flex align-items-center justify-content-between mt-3">
                  <Button
                    type="button"
                    variant="outline-secondary"
                    size="sm"
                    disabled={reviewPage <= 0}
                    onClick={() => setReviewPage((value) => Math.max(0, value - 1))}
                  >
                    Previous
                  </Button>
                  <span className="text-muted small">
                    Page {reviewPage + 1} of {totalReviewPages}
                  </span>
                  <Button
                    type="button"
                    variant="outline-dark"
                    size="sm"
                    disabled={reviewPage >= totalReviewPages - 1}
                    onClick={() =>
                      setReviewPage((value) => Math.min(totalReviewPages - 1, value + 1))
                    }
                  >
                    Next
                  </Button>
                </div>
              </>
            )}
          </Col>
        </Row>
      </Card.Body>
    </Card>
  )
}

export default BookDetailPage

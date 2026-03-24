import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import { Link, useParams } from 'react-router-dom'
import { getBookById } from '../../api/search'
import { getBooksInList, getListById } from '../../api/listsBrowse'
import { getListLikeStats, likeList, unlikeList } from '../../api/likes'
import { getCurrentUser } from '../../api/search'
import type { BookDetail, UserProfileItem } from '../../types/search'
import type { ListBookEntry, ListTileItem } from '../../types/lists'

const PAGE_SIZE = 20

function ListDetailPage() {
  const { listId } = useParams()

  const [list, setList] = useState<ListTileItem | null>(null)
  const [entries, setEntries] = useState<ListBookEntry[]>([])
  const [booksById, setBooksById] = useState<Record<number, BookDetail>>({})
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentUser, setCurrentUser] = useState<UserProfileItem | null>(null)
  const [listLikeCount, setListLikeCount] = useState(0)
  const [listLikedByCurrentUser, setListLikedByCurrentUser] = useState(false)
  const [likingList, setLikingList] = useState(false)
  const [likeError, setLikeError] = useState<string | null>(null)

  useEffect(() => {
    const parsedId = Number(listId)
    if (!listId || Number.isNaN(parsedId)) {
      setError('Invalid list id.')
      setLoading(false)
      return
    }

    const loadList = async () => {
      setLoading(true)
      setError(null)
      setLikeError(null)
      try {
        const [listResult, entriesResult, userResult] = await Promise.allSettled([
          getListById(parsedId),
          getBooksInList(parsedId, page, PAGE_SIZE),
          getCurrentUser(),
        ])

        if (listResult.status === 'rejected') throw listResult.reason
        if (entriesResult.status === 'rejected') throw entriesResult.reason

        if (userResult.status === 'fulfilled') {
          setCurrentUser(userResult.value)
        }

        const list = listResult.value
        setList(list)

        // Load list like stats
        try {
          const listLikeStats = await getListLikeStats(parsedId)
          setListLikeCount(listLikeStats.likeCount)
          setListLikedByCurrentUser(listLikeStats.likedByCurrentUser)
        } catch {
          setListLikeCount(0)
          setListLikedByCurrentUser(false)
        }

        const entries = entriesResult.value
        setEntries(entries.content)
        setTotalPages(entries.totalPages)

        const uniqueBookIds = Array.from(
          new Set(entries.content.map((item: ListBookEntry) => item.bookId)),
        )
        const bookResults = await Promise.allSettled(
          uniqueBookIds.map((bookId: number) => getBookById(bookId)),
        )

        const nextBooksById: Record<number, BookDetail> = {}
        bookResults.forEach((result) => {
          if (result.status === 'fulfilled') {
            nextBooksById[result.value.id] = result.value
          }
        })
        setBooksById(nextBooksById)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load list.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadList()
  }, [listId, page])

  const listTitle = useMemo(() => list?.title ?? 'List', [list?.title])

  const handleLikeList = async () => {
    if (!currentUser) {
      setLikeError('You must be logged in to like this list.')
      return
    }

    if (!list) return

    const parsedId = Number(listId)
    const wasLiked = listLikedByCurrentUser

    setLikingList(true)
    setLikeError(null)
    setListLikedByCurrentUser(!wasLiked)
    setListLikeCount((count) => Math.max(0, count + (wasLiked ? -1 : 1)))

    try {
      if (wasLiked) {
        await unlikeList(parsedId)
      } else {
        await likeList(parsedId)
      }
    } catch (err) {
      setListLikedByCurrentUser(wasLiked)
      setListLikeCount((count) => Math.max(0, count + (wasLiked ? 1 : -1)))
      const message = err instanceof Error ? err.message : 'Unable to like list.'
      setLikeError(message)
    } finally {
      setLikingList(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading list...</span>
      </div>
    )
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>
  }

  if (!list) {
    return <Alert variant="warning">List not found.</Alert>
  }

  return (
    <div>
      <section className="mb-4">
        <h2 className="mb-1">{listTitle}</h2>
        <p className="text-muted mb-1">Created by User #{list.userId}</p>
        <p className="mb-0">{list.description || 'No description for this list.'}</p>
        <div className="mt-3 d-flex gap-2 align-items-center">
          <Button
            variant={listLikedByCurrentUser ? 'dark' : 'outline-dark'}
            size="sm"
            disabled={likingList || !currentUser}
            onClick={handleLikeList}
            className="d-flex align-items-center gap-2"
          >
            <span aria-hidden="true">{listLikedByCurrentUser ? '♥' : '♡'}</span>
            <span>{likingList ? 'Liking...' : 'Like'}</span>
            {listLikeCount > 0 && <span className="small">({listLikeCount})</span>}
          </Button>
          <Link to={`/lists/${list.id}/edit`} className="btn btn-outline-dark btn-sm">
            Edit This List
          </Link>
        </div>
        {likeError && <Alert variant="danger" className="mt-2 mb-0">{likeError}</Alert>}
      </section>

      {entries.length === 0 ? (
        <Alert variant="light">No books in this list yet.</Alert>
      ) : (
        <Row xs={1} md={2} lg={3} className="g-3">
          {entries.map((entry) => {
            const book = booksById[entry.bookId]

            const cardContent = (
              <Card className="h-100">
                {book?.coverImageUrl ? (
                  <img
                    src={book.coverImageUrl}
                    alt={`${book.title} cover`}
                    className="books-tile-image"
                  />
                ) : (
                  <div className="books-tile-placeholder">No Cover</div>
                )}
                <Card.Body>
                  <Card.Title className="books-tile-title">
                    {book ? book.title : `Book #${entry.bookId}`}
                  </Card.Title>
                  <Card.Text className="text-muted mb-0">
                    {book?.author || 'Author unavailable'}
                  </Card.Text>
                </Card.Body>
              </Card>
            )

            return (
              <Col key={entry.id}>
                {book ? (
                  <Link to={`/books/${book.id}`} className="books-tile-link h-100 d-block">
                    {cardContent}
                  </Link>
                ) : (
                  cardContent
                )}
              </Col>
            )
          })}
        </Row>
      )}

      <div className="d-flex align-items-center justify-content-between mt-3">
        <button
          type="button"
          className="btn btn-outline-secondary"
          onClick={() => setPage((value) => Math.max(0, value - 1))}
          disabled={loading || page <= 0}
        >
          Previous
        </button>
        <div className="text-muted small">
          Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
        </div>
        <button
          type="button"
          className="btn btn-dark"
          onClick={() => setPage((value) => value + 1)}
          disabled={loading || totalPages === 0 || page >= totalPages - 1}
        >
          Next
        </button>
      </div>
    </div>
  )
}

export default ListDetailPage

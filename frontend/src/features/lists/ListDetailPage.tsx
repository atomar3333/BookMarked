import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import { Link, useParams } from 'react-router-dom'
import { getBookById } from '../../api/search'
import { getBooksInList, getListById } from '../../api/listsBrowse'
import type { BookDetail } from '../../types/search'
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
      try {
        const [listResult, entriesResult] = await Promise.all([
          getListById(parsedId),
          getBooksInList(parsedId, page, PAGE_SIZE),
        ])

        setList(listResult)
        setEntries(entriesResult.content)
        setTotalPages(entriesResult.totalPages)

        const uniqueBookIds = Array.from(new Set(entriesResult.content.map((item) => item.bookId)))
        const bookResults = await Promise.allSettled(uniqueBookIds.map((bookId) => getBookById(bookId)))

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
        <div className="mt-3">
          <Link to={`/lists/${list.id}/edit`} className="btn btn-outline-dark btn-sm">
            Edit This List
          </Link>
        </div>
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

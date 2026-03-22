import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Spinner from 'react-bootstrap/Spinner'
import { getBooksPage } from '../../api/books'
import { getCurrentUserReadingStatuses, upsertReadingStatus } from '../../api/readingStatus'
import { getCurrentUser } from '../../api/search'
import type { BookTileItem } from '../../types/books'
import type { ReadingStatusValue } from '../../types/userPage'
import BookGrid from './BookGrid'
import BooksFilters from './BooksFilters'
import BooksPaginator from './BooksPaginator'

const PAGE_SIZE = 20

function BooksPage() {
  const [books, setBooks] = useState<BookTileItem[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [currentUserId, setCurrentUserId] = useState<number | null>(null)
  const [statusByBookId, setStatusByBookId] = useState<Record<number, ReadingStatusValue>>({})
  const [savingByBookId, setSavingByBookId] = useState<Record<number, boolean>>({})

  useEffect(() => {
    const loadCurrentUserAndStatuses = async () => {
      try {
        const me = await getCurrentUser()
        setCurrentUserId(me.id)

        const statuses = await getCurrentUserReadingStatuses(me.id)
        const nextMap: Record<number, ReadingStatusValue> = {}
        statuses.forEach((item) => {
          nextMap[item.bookId] = item.currentStatus
        })
        setStatusByBookId(nextMap)
      } catch {
        setCurrentUserId(null)
        setStatusByBookId({})
      }
    }

    loadCurrentUserAndStatuses()
  }, [])

  useEffect(() => {
    const loadBooks = async () => {
      setLoading(true)
      setError(null)
      try {
        const response = await getBooksPage(page, PAGE_SIZE)
        setBooks(response.content)
        setTotalPages(response.totalPages)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load books.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadBooks()
  }, [page])

  const handleSetStatus = async (bookId: number, status: ReadingStatusValue) => {
    if (!currentUserId) {
      return
    }

    const previous = statusByBookId[bookId]

    setSavingByBookId((existing) => ({ ...existing, [bookId]: true }))
    setStatusByBookId((existing) => ({ ...existing, [bookId]: status }))

    try {
      await upsertReadingStatus({
        userId: currentUserId,
        bookId,
        currentStatus: status,
      })
    } catch {
      setStatusByBookId((existing) => {
        if (previous === undefined) {
          const { [bookId]: _removed, ...rest } = existing
          return rest
        }
        return { ...existing, [bookId]: previous }
      })
    } finally {
      setSavingByBookId((existing) => ({ ...existing, [bookId]: false }))
    }
  }

  return (
    <div>
      <section className="mb-4">
        <h2 className="mb-1">Browse Books</h2>
        <p className="text-muted mb-0">Showing books in a modular catalog layout.</p>
      </section>

      <BooksFilters />

      {loading && (
        <div className="d-flex align-items-center gap-2">
          <Spinner animation="border" size="sm" />
          <span>Loading books...</span>
        </div>
      )}

      {error && <Alert variant="danger">{error}</Alert>}

      {!loading && !error && books.length === 0 && (
        <Alert variant="light">No books found for this page.</Alert>
      )}

      {!loading && !error && books.length > 0 && (
        <>
          <BookGrid
            books={books}
            showStatusActions={Boolean(currentUserId)}
            statusByBookId={statusByBookId}
            savingByBookId={savingByBookId}
            onSetStatus={handleSetStatus}
          />
          <BooksPaginator
            page={page}
            totalPages={totalPages}
            loading={loading}
            onPrevious={() => setPage((value) => Math.max(0, value - 1))}
            onNext={() => setPage((value) => value + 1)}
          />
        </>
      )}
    </div>
  )
}

export default BooksPage

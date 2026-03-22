import type { BookTileItem } from '../../types/books'
import type { ReadingStatusValue } from '../../types/userPage'
import BookTile from './BookTile'

interface BookGridProps {
  books: BookTileItem[]
  showStatusActions?: boolean
  statusByBookId?: Record<number, ReadingStatusValue>
  savingByBookId?: Record<number, boolean>
  onSetStatus?: (bookId: number, status: ReadingStatusValue) => void
}

function BookGrid({
  books,
  showStatusActions,
  statusByBookId,
  savingByBookId,
  onSetStatus,
}: BookGridProps) {
  return (
    <div className="books-grid">
      {books.map((book) => (
        <BookTile
          key={book.id}
          book={book}
          showStatusActions={showStatusActions}
          currentStatus={statusByBookId?.[book.id]}
          isSavingStatus={savingByBookId?.[book.id]}
          onSetStatus={onSetStatus}
        />
      ))}
    </div>
  )
}

export default BookGrid

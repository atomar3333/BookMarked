import type { BookTileItem } from '../../types/books'
import BookTile from './BookTile'

interface BookGridProps {
  books: BookTileItem[]
}

function BookGrid({ books }: BookGridProps) {
  return (
    <div className="books-grid">
      {books.map((book) => (
        <BookTile key={book.id} book={book} />
      ))}
    </div>
  )
}

export default BookGrid

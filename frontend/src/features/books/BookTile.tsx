import Card from 'react-bootstrap/Card'
import { Link } from 'react-router-dom'
import type { BookTileItem } from '../../types/books'

interface BookTileProps {
  book: BookTileItem
}

function BookTile({ book }: BookTileProps) {
  return (
    <Link to={`/books/${book.id}`} className="books-tile-link">
      <Card className="books-tile h-100">
        {book.coverImageUrl ? (
          <img src={book.coverImageUrl} alt={`${book.title} cover`} className="books-tile-image" />
        ) : (
          <div className="books-tile-placeholder">No Cover</div>
        )}
        <Card.Body>
          <Card.Title className="books-tile-title">{book.title}</Card.Title>
          <Card.Text className="text-muted mb-0">{book.author}</Card.Text>
        </Card.Body>
      </Card>
    </Link>
  )
}

export default BookTile

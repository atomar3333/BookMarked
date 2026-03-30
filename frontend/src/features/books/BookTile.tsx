import Card from 'react-bootstrap/Card'
import { Link } from 'react-router-dom'
import type { BookTileItem } from '../../types/books'
import type { ReadingStatusValue } from '../../types/userPage'
import ReadingStatusActions from '../../components/ReadingStatusActions'
import fallbackCover from '../../assets/fallback_cover.png'

interface BookTileProps {
  book: BookTileItem
  showStatusActions?: boolean
  currentStatus?: ReadingStatusValue
  isSavingStatus?: boolean
  onSetStatus?: (bookId: number, status: ReadingStatusValue) => void
}

function BookTile({
  book,
  showStatusActions,
  currentStatus,
  isSavingStatus,
  onSetStatus,
}: BookTileProps) {
  return (
    <Card className="books-tile h-100">
      <Link to={`/books/${book.id}`} className="books-tile-link">
        <img 
          src={book.coverImageUrl || fallbackCover} 
          alt={`${book.title} cover`} 
          className="books-tile-image" 
        />
      </Link>
      <Card.Body>
        <Card.Title className="books-tile-title">
          <Link to={`/books/${book.id}`} className="books-tile-link">
            {book.title}
          </Link>
        </Card.Title>
        <Card.Text className="text-muted mb-3">
          {book.authors && book.authors.length > 0 && (
            <>
              by{' '}
              {book.authors.map((author, index) => (
                <span key={author.id}>
                  {index > 0 ? ', ' : ''}
                  <Link to={`/authors/${author.id}`}>{author.authorName}</Link>
                </span>
              ))}
            </>
          )}
        </Card.Text>

        {showStatusActions && onSetStatus && (
          <ReadingStatusActions
            currentStatus={currentStatus}
            isSaving={isSavingStatus}
            onSetStatus={(status) => onSetStatus(book.id, status)}
          />
        )}
      </Card.Body>
    </Card>
  )
}

export default BookTile

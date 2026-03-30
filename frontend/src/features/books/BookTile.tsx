import Card from 'react-bootstrap/Card'
import { Link } from 'react-router-dom'
import { Dropdown } from 'react-bootstrap'
import { ThreeDots } from 'react-bootstrap-icons'
import type { BookTileItem } from '../../types/books'
import type { ReadingStatusValue } from '../../types/userPage'
import fallbackCover from '../../assets/fallback_cover.png'
import LikeButton from '../../components/LikeButton'
import { likeBook, unlikeBook } from '../../api/likes'

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
      <Card.Body className="d-flex flex-column">
        <Card.Title className="books-tile-title">
          <Link to={`/books/${book.id}`} className="books-tile-link">
            {book.title}
          </Link>
        </Card.Title>
        <Card.Text className="text-muted mb-auto">
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

        <div className="d-flex justify-content-between align-items-center mt-3">
          <LikeButton
            onLike={() => likeBook(book.id)}
            onUnlike={() => unlikeBook(book.id)}
          />

          {showStatusActions && onSetStatus && (
            <Dropdown>
              <Dropdown.Toggle 
                variant="outline-secondary" 
                size="sm" 
                id={`dropdown-${book.id}`}
                disabled={isSavingStatus}
                title="Reading Status Options"
              >
                <ThreeDots />
              </Dropdown.Toggle>

              <Dropdown.Menu align="end">
                <Dropdown.Item 
                  active={currentStatus === 'WANT_TO_READ'}
                  onClick={() => onSetStatus(book.id, 'WANT_TO_READ')}
                >
                  Want to Read
                </Dropdown.Item>
                <Dropdown.Item 
                  active={currentStatus === 'CURRENTLY_READING'}
                  onClick={() => onSetStatus(book.id, 'CURRENTLY_READING')}
                >
                  Reading
                </Dropdown.Item>
                <Dropdown.Item 
                  active={currentStatus === 'READ'}
                  onClick={() => onSetStatus(book.id, 'READ')}
                >
                  Read
                </Dropdown.Item>
              </Dropdown.Menu>
            </Dropdown>
          )}
        </div>
      </Card.Body>
    </Card>
  )
}

export default BookTile

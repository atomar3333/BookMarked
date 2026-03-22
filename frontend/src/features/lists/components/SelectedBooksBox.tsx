import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import type { BookSearchItem } from '../../../types/search'

interface SelectedBooksBoxProps {
  books: BookSearchItem[]
  title?: string
  emptyMessage: string
  removeLabel?: string
  loading?: boolean
  onRemoveBook: (bookId: number) => void
}

function SelectedBooksBox({
  books,
  title = 'Selected Books',
  emptyMessage,
  removeLabel = 'Remove',
  loading,
  onRemoveBook,
}: SelectedBooksBoxProps) {
  return (
    <Card>
      <Card.Body>
        <Card.Title className="d-flex justify-content-between align-items-center mb-3">
          <span>{title}</span>
          <span className="badge text-bg-dark">{books.length}</span>
        </Card.Title>

        {books.length === 0 ? (
          <Alert variant="light" className="mb-0">
            {emptyMessage}
          </Alert>
        ) : (
          <div className="book-list-grid">
            {books.map((book) => (
              <div key={book.id} className="book-list-item">
                <div>
                  <div className="fw-semibold">{book.title}</div>
                  <div className="text-muted small">{book.author}</div>
                </div>
                <Button
                  type="button"
                  size="sm"
                  variant="outline-danger"
                  disabled={loading}
                  onClick={() => onRemoveBook(book.id)}
                >
                  {removeLabel}
                </Button>
              </div>
            ))}
          </div>
        )}
      </Card.Body>
    </Card>
  )
}

export default SelectedBooksBox

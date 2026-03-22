import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Form from 'react-bootstrap/Form'
import Spinner from 'react-bootstrap/Spinner'
import type { BookSearchItem } from '../../../types/search'

interface ListBookSearchPickerProps {
  query: string
  onQueryChange: (value: string) => void
  results: BookSearchItem[]
  selectedBookIds: Set<number>
  loading: boolean
  error: string | null
  onAddBook: (book: BookSearchItem) => void
}

function ListBookSearchPicker({
  query,
  onQueryChange,
  results,
  selectedBookIds,
  loading,
  error,
  onAddBook,
}: ListBookSearchPickerProps) {
  return (
    <Card>
      <Card.Body>
        <Card.Title className="mb-3">Search Books (Title)</Card.Title>

        <Form.Group controlId="list-book-search" className="mb-3">
          <Form.Label>Search by title</Form.Label>
          <Form.Control
            type="text"
            value={query}
            onChange={(event) => onQueryChange(event.target.value)}
            placeholder="Type at least 2 characters"
          />
        </Form.Group>

        {loading && (
          <div className="d-flex align-items-center gap-2 mb-3">
            <Spinner animation="border" size="sm" />
            <span>Searching books...</span>
          </div>
        )}

        {error && (
          <Alert variant="danger" className="mb-3">
            {error}
          </Alert>
        )}

        {query.trim().length > 0 && query.trim().length < 2 && (
          <Alert variant="light" className="mb-0">
            Enter at least 2 characters to search.
          </Alert>
        )}

        {query.trim().length >= 2 && !loading && !error && results.length === 0 && (
          <Alert variant="light" className="mb-0">
            No books matched this title.
          </Alert>
        )}

        {results.length > 0 && (
          <div className="book-list-grid">
            {results.map((book) => {
              const isAdded = selectedBookIds.has(book.id)

              return (
                <div key={book.id} className="book-list-item">
                  <div>
                    <div className="fw-semibold">{book.title}</div>
                    <div className="text-muted small">{book.author}</div>
                  </div>
                  <Button
                    type="button"
                    size="sm"
                    variant={isAdded ? 'outline-secondary' : 'outline-dark'}
                    disabled={isAdded}
                    onClick={() => onAddBook(book)}
                  >
                    {isAdded ? 'Added' : 'Add'}
                  </Button>
                </div>
              )
            })}
          </div>
        )}
      </Card.Body>
    </Card>
  )
}

export default ListBookSearchPicker

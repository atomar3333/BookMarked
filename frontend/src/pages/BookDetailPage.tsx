import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import { useParams } from 'react-router-dom'
import { getBookById } from '../api/search'
import type { BookDetail } from '../types/search'

function BookDetailPage() {
  const { bookId } = useParams()
  const [book, setBook] = useState<BookDetail | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const parsedId = Number(bookId)
    if (!bookId || Number.isNaN(parsedId)) {
      setError('Invalid book id.')
      setLoading(false)
      return
    }

    const loadBook = async () => {
      try {
        const result = await getBookById(parsedId)
        setBook(result)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load book details.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadBook()
  }, [bookId])

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading book...</span>
      </div>
    )
  }

  if (error) {
    return <Alert variant="danger">{error}</Alert>
  }

  if (!book) {
    return <Alert variant="warning">Book not found.</Alert>
  }

  return (
    <Card className="book-template-card shadow-sm">
      <Card.Body>
        <Row className="g-4 align-items-start">
          <Col md={4}>
            {book.coverImageUrl ? (
              <img
                src={book.coverImageUrl}
                alt={`${book.title} cover`}
                className="book-cover-image"
              />
            ) : (
              <div className="book-cover-placeholder">No Cover</div>
            )}
          </Col>

          <Col md={8}>
            <h2 className="mb-1">{book.title}</h2>
            <p className="text-muted mb-3">by {book.author}</p>
            <h6>Description</h6>
            <p className="mb-0">
              {book.description && book.description.trim().length > 0
                ? book.description
                : 'No description available for this book yet.'}
            </p>
          </Col>
        </Row>
      </Card.Body>
    </Card>
  )
}

export default BookDetailPage

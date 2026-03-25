import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import ListGroup from 'react-bootstrap/ListGroup'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import { Link, useParams } from 'react-router-dom'
import { getAuthorById, getBooksByAuthor, type AuthorBookItem, type AuthorItem } from '../api/authors'

function AuthorDetailPage() {
  const { authorId } = useParams()
  const [author, setAuthor] = useState<AuthorItem | null>(null)
  const [books, setBooks] = useState<AuthorBookItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const parsedId = Number(authorId)
    if (!authorId || Number.isNaN(parsedId)) {
      setError('Invalid author id.')
      setLoading(false)
      return
    }

    const load = async () => {
      setLoading(true)
      setError(null)
      try {
        const [authorData, booksData] = await Promise.all([
          getAuthorById(parsedId),
          getBooksByAuthor(parsedId),
        ])
        setAuthor(authorData)
        setBooks(booksData)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load author profile.')
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [authorId])

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading author...</span>
      </div>
    )
  }

  if (error || !author) {
    return <Alert variant="danger">{error ?? 'Author not found.'}</Alert>
  }

  return (
    <Row className="g-4">
      <Col lg={4}>
        <Card className="shadow-sm h-100">
          <Card.Body>
            {author.profilePictureUrl ? (
              <div className="text-center mb-3">
                <img
                  src={author.profilePictureUrl}
                  alt={author.authorName}
                  width={140}
                  height={140}
                  style={{ objectFit: 'cover', borderRadius: '50%' }}
                />
              </div>
            ) : null}
            <h4 className="mb-2">{author.authorName}</h4>
            <div className="text-muted" style={{ whiteSpace: 'pre-line' }}>
              {author.bio || 'No bio added yet.'}
            </div>
          </Card.Body>
        </Card>
      </Col>

      <Col lg={8}>
        <Card className="shadow-sm">
          <Card.Body>
            <h5 className="mb-3">Books by {author.authorName}</h5>
            {books.length === 0 ? (
              <Alert variant="light" className="mb-0">
                No books linked to this author yet.
              </Alert>
            ) : (
              <ListGroup variant="flush">
                {books.map((book) => (
                  <ListGroup.Item key={book.id} className="px-0 py-3">
                    <div className="d-flex justify-content-between align-items-start gap-3">
                      <div className="flex-grow-1">
                        <Link to={`/books/${book.id}`} className="fw-semibold text-decoration-none">
                          {book.title}
                        </Link>
                        {book.description ? (
                          <div
                            className="text-muted small mt-1"
                            style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                          >
                            {book.description}
                          </div>
                        ) : null}
                      </div>
                      {book.coverImageUrl ? (
                        <img
                          src={book.coverImageUrl}
                          alt={book.title}
                          width={52}
                          height={72}
                          style={{ objectFit: 'cover', borderRadius: 4 }}
                        />
                      ) : null}
                    </div>
                  </ListGroup.Item>
                ))}
              </ListGroup>
            )}
          </Card.Body>
        </Card>
      </Col>
    </Row>
  )
}

export default AuthorDetailPage
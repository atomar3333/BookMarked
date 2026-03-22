import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Badge from 'react-bootstrap/Badge'
import Card from 'react-bootstrap/Card'
import Col from 'react-bootstrap/Col'
import Form from 'react-bootstrap/Form'
import Row from 'react-bootstrap/Row'
import Spinner from 'react-bootstrap/Spinner'
import { Link } from 'react-router-dom'
import { unifiedSearch } from '../../api/search'
import type { UnifiedSearchResult } from '../../types/search'

const MIN_QUERY_LENGTH = 2
const SEARCH_DEBOUNCE_MS = 300

const initialResult: UnifiedSearchResult = {
  books: [],
  users: [],
  lists: [],
  warnings: [],
}

function UnifiedSearchPage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<UnifiedSearchResult>(initialResult)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const trimmed = query.trim()

    if (trimmed.length < MIN_QUERY_LENGTH) {
      setResults(initialResult)
      setLoading(false)
      return
    }

    const timer = setTimeout(async () => {
      setLoading(true)
      const nextResults = await unifiedSearch(trimmed)
      setResults(nextResults)
      setLoading(false)
    }, SEARCH_DEBOUNCE_MS)

    return () => clearTimeout(timer)
  }, [query])

  const totalMatches = useMemo(
    () => results.books.length + results.users.length + results.lists.length,
    [results.books.length, results.users.length, results.lists.length],
  )

  return (
    <div>
      <Card className="mb-4 shadow-sm">
        <Card.Body>
          <Card.Title>Unified Search</Card.Title>
          <Card.Text className="text-muted mb-3">
            Search books by title, users by username, and lists by title/description in a
            single query.
          </Card.Text>
          <Form.Control
            type="search"
            placeholder="Search books, users, and lists"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
          <div className="mt-2 text-muted small">
            Enter at least {MIN_QUERY_LENGTH} characters.
          </div>
        </Card.Body>
      </Card>

      {loading && (
        <div className="d-flex align-items-center gap-2 mb-3">
          <Spinner animation="border" size="sm" />
          <span>Searching...</span>
        </div>
      )}

      {results.warnings.map((warning) => (
        <Alert key={warning} variant="warning">
          {warning}
        </Alert>
      ))}

      {query.trim().length >= MIN_QUERY_LENGTH && !loading && (
        <p className="text-muted">Total matches: {totalMatches}</p>
      )}

      {results.books.length > 0 && (
        <section className="mb-4">
          <h5 className="mb-3">
            Books <Badge bg="secondary">{results.books.length}</Badge>
          </h5>
          <Row xs={1} md={2} className="g-3">
            {results.books.map((book) => (
              <Col key={book.id}>
                <Link to={`/books/${book.id}`} className="book-result-link">
                  <Card className="h-100 book-result-card">
                    <Card.Body>
                      <Card.Title>{book.title}</Card.Title>
                      <Card.Subtitle className="mb-2 text-muted">
                        {book.author}
                      </Card.Subtitle>
                      {book.description && (
                        <Card.Text>
                          {book.description.length > 180
                            ? `${book.description.slice(0, 180)}...`
                            : book.description}
                        </Card.Text>
                      )}
                    </Card.Body>
                  </Card>
                </Link>
              </Col>
            ))}
          </Row>
        </section>
      )}

      {results.users.length > 0 && (
        <section className="mb-4">
          <h5 className="mb-3">
            Users <Badge bg="secondary">{results.users.length}</Badge>
          </h5>
          <Row xs={1} md={2} className="g-3">
            {results.users.map((user) => (
              <Col key={user.id}>
                <Card className="h-100">
                  <Card.Body>
                    <Card.Title>{user.userName}</Card.Title>
                    {user.bio && <Card.Text>{user.bio}</Card.Text>}
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </section>
      )}

      {results.lists.length > 0 && (
        <section className="mb-4">
          <h5 className="mb-3">
            Lists <Badge bg="secondary">{results.lists.length}</Badge>
          </h5>
          <Row xs={1} md={2} className="g-3">
            {results.lists.map((list) => (
              <Col key={list.id}>
                <Link to={`/lists/${list.id}`} className="book-result-link">
                  <Card className="h-100 book-result-card">
                    <Card.Body>
                      <Card.Title>{list.title}</Card.Title>
                      <Card.Subtitle className="mb-2 text-muted">
                        User #{list.userId}
                      </Card.Subtitle>
                      {list.description && (
                        <Card.Text>
                          {list.description.length > 180
                            ? `${list.description.slice(0, 180)}...`
                            : list.description}
                        </Card.Text>
                      )}
                    </Card.Body>
                  </Card>
                </Link>
              </Col>
            ))}
          </Row>
        </section>
      )}

      {query.trim().length >= MIN_QUERY_LENGTH && !loading && totalMatches === 0 && (
        <Alert variant="light">No matches found in books, users, or lists.</Alert>
      )}
    </div>
  )
}

export default UnifiedSearchPage

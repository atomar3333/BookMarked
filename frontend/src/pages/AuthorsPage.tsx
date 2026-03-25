import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Form from 'react-bootstrap/Form'
import ListGroup from 'react-bootstrap/ListGroup'
import Spinner from 'react-bootstrap/Spinner'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getAllAuthors, searchAuthors, type AuthorItem } from '../api/authors'

const AUTHORS_PER_PAGE = 10

function AuthorsPage() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const initialSearch = searchParams.get('name') ?? ''
  const [authors, setAuthors] = useState<AuthorItem[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [searchInput, setSearchInput] = useState(initialSearch)
  const [searchTerm, setSearchTerm] = useState(initialSearch)

  useEffect(() => {
    const nextSearch = searchParams.get('name') ?? ''
    setSearchInput(nextSearch)
    setSearchTerm(nextSearch)
    setPage(0)
  }, [searchParams])

  useEffect(() => {
    const loadAuthors = async () => {
      setLoading(true)
      setError(null)
      try {
        if (searchTerm.trim().length > 0) {
          const results = await searchAuthors(searchTerm)
          setAuthors(results)
          setTotalPages(1)
        } else {
          const pageData = await getAllAuthors(page, AUTHORS_PER_PAGE)
          setAuthors(pageData.content)
          setTotalPages(pageData.totalPages)
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load authors.')
      } finally {
        setLoading(false)
      }
    }

    loadAuthors()
  }, [page, searchTerm])

  const handleSearch = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const next = searchInput.trim()
    if (next) {
      setSearchParams({ name: next })
    } else {
      setSearchParams({})
    }
  }

  const clearSearch = () => {
    setSearchParams({})
  }

  return (
    <Card className="shadow-sm">
      <Card.Body>
        <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
          <h4 className="mb-0">Authors</h4>
        </div>

        <Form onSubmit={handleSearch} className="d-flex gap-2 mb-3">
          <Form.Control
            type="text"
            placeholder="Search authors by name"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
          <Button type="submit" variant="dark">
            Search
          </Button>
          {searchTerm && (
            <Button type="button" variant="outline-secondary" onClick={clearSearch}>
              Clear
            </Button>
          )}
        </Form>

        {error && <Alert variant="danger">{error}</Alert>}

        {loading ? (
          <div className="d-flex align-items-center gap-2">
            <Spinner animation="border" size="sm" />
            <span>Loading authors...</span>
          </div>
        ) : authors.length === 0 ? (
          <Alert variant="light">No authors found.</Alert>
        ) : (
          <>
            <ListGroup variant="flush">
              {authors.map((author) => (
                <ListGroup.Item
                  key={author.id}
                  className="px-0 py-3"
                  action
                  onClick={() => navigate(`/authors/${author.id}`)}
                >
                  <div className="d-flex justify-content-between align-items-start gap-3">
                    <div className="flex-grow-1" style={{ minWidth: 0 }}>
                      <div className="fw-semibold">{author.authorName}</div>
                      {author.bio && <div className="text-muted small mt-1">{author.bio}</div>}
                    </div>
                    {author.profilePictureUrl && (
                      <img
                        src={author.profilePictureUrl}
                        alt={author.authorName}
                        width={56}
                        height={56}
                        style={{ objectFit: 'cover', borderRadius: '50%' }}
                      />
                    )}
                  </div>
                </ListGroup.Item>
              ))}
            </ListGroup>

            {!searchTerm && (
              <div className="d-flex align-items-center justify-content-between mt-4">
                <Button
                  variant="outline-secondary"
                  size="sm"
                  disabled={page <= 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                >
                  Previous
                </Button>
                <span className="text-muted small">
                  Page {page + 1} of {totalPages}
                </span>
                <Button
                  variant="outline-dark"
                  size="sm"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                >
                  Next
                </Button>
              </div>
            )}
          </>
        )}
      </Card.Body>
    </Card>
  )
}

export default AuthorsPage
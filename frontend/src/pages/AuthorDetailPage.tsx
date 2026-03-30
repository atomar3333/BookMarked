import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Col from 'react-bootstrap/Col'
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
  const [bioExpanded, setBioExpanded] = useState(false)

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
    <div className="author-detail-page mt-4 mb-5 pb-5">
      <Row className="g-5">
        <Col lg={9}>
          <div className="mb-2 text-uppercase tracking-wider text-muted small fw-semibold">Books Written By</div>
          <h2 className="mb-3 text-white fw-bold" style={{ fontSize: '2.5rem' }}>{author.authorName}</h2>
          
          <div className="director-filter-bar mb-4">
            <span>Author ⌄</span>
            <span>Decade ⌄</span>
            <span>Genre ⌄</span>
            <span>Sort by Popularity ⌄</span>
            <span className="ms-auto border-start border-secondary ps-3">👁 ⌄</span>
          </div>

          {books.length === 0 ? (
            <Alert variant="dark" className="bg-transparent border-secondary text-muted">
              No books linked to this author yet.
            </Alert>
          ) : (
            <div className="director-grid">
              {books.map((book) => (
                <Link to={`/books/${book.id}`} key={book.id} className="director-poster-card" title={book.title}>
                  {book.coverImageUrl ? (
                    <img src={book.coverImageUrl} alt={book.title} />
                  ) : (
                    <div className="d-flex align-items-center justify-content-center h-100 p-2 text-center text-muted small bg-dark" style={{ border: '1px solid var(--app-border)' }}>
                      {book.title}
                    </div>
                  )}
                </Link>
              ))}
            </div>
          )}
        </Col>
        
        <Col lg={3}>
          {author.profilePictureUrl ? (
            <div className="mb-3">
              <img
                src={author.profilePictureUrl}
                alt={author.authorName}
                className="director-profile-image shadow"
              />
            </div>
          ) : (
            <div className="mb-3 director-profile-image d-flex align-items-center justify-content-center bg-dark text-muted" style={{ aspectRatio: '2/3' }}>
              No Image
            </div>
          )}
          
          <div className="text-muted small mb-3" style={{ lineHeight: '1.6', fontSize: '0.85rem' }}>
            {author.bio ? (
              <>
                {!bioExpanded && author.bio.length > 250 ? (
                  <>
                    {author.bio.substring(0, 250)}...{' '}
                    <span 
                      className="text-light fw-semibold" 
                      style={{ cursor: 'pointer' }}
                      onClick={() => setBioExpanded(true)}
                    >
                      more
                    </span>
                  </>
                ) : (
                  <>
                    {author.bio}{' '}
                    {author.bio.length > 250 && (
                      <span 
                        className="text-light fw-semibold" 
                        style={{ cursor: 'pointer' }}
                        onClick={() => setBioExpanded(false)}
                      >
                        less
                      </span>
                    )}
                  </>
                )}
              </>
            ) : (
              'No bio added yet.'
            )}
          </div>
          

        </Col>
      </Row>
    </div>
  )
}

export default AuthorDetailPage
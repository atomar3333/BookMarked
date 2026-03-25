import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Card from 'react-bootstrap/Card'
import ListGroup from 'react-bootstrap/ListGroup'
import Spinner from 'react-bootstrap/Spinner'
import { Link, useParams } from 'react-router-dom'
import { getBooksByGenre, getGenreById, type GenreBookItem, type GenreItem } from '../api/genres'

function GenreDetailPage() {
  const { genreId } = useParams()
  const [genre, setGenre] = useState<GenreItem | null>(null)
  const [books, setBooks] = useState<GenreBookItem[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const parsedId = Number(genreId)
    if (!genreId || Number.isNaN(parsedId)) {
      setError('Invalid genre id.')
      setLoading(false)
      return
    }

    const load = async () => {
      setLoading(true)
      setError(null)
      try {
        const [genreData, booksData] = await Promise.all([
          getGenreById(parsedId),
          getBooksByGenre(parsedId),
        ])
        setGenre(genreData)
        setBooks(booksData)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load genre details.')
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [genreId])

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading genre...</span>
      </div>
    )
  }

  if (error || !genre) {
    return <Alert variant="danger">{error ?? 'Genre not found.'}</Alert>
  }

  return (
    <Card className="shadow-sm">
      <Card.Body>
        <h4 className="mb-3">Genre: {genre.genreName}</h4>
        {books.length === 0 ? (
          <Alert variant="light" className="mb-0">
            No books linked to this genre yet.
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
  )
}

export default GenreDetailPage

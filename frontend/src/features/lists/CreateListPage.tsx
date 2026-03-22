import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Form from 'react-bootstrap/Form'
import Spinner from 'react-bootstrap/Spinner'
import { useNavigate } from 'react-router-dom'
import { addBookToList, createList } from '../../api/lists'
import { getCurrentUser, searchBooksByTitle } from '../../api/search'
import type { BookSearchItem } from '../../types/search'
import ListBookSearchPicker from './components/ListBookSearchPicker'
import SelectedBooksBox from './components/SelectedBooksBox'

function CreateListPage() {
  const navigate = useNavigate()

  const [userId, setUserId] = useState<number | null>(null)
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [query, setQuery] = useState('')
  const [searchResults, setSearchResults] = useState<BookSearchItem[]>([])
  const [selectedBooks, setSelectedBooks] = useState<BookSearchItem[]>([])
  const [loadingUser, setLoadingUser] = useState(true)
  const [searching, setSearching] = useState(false)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [searchError, setSearchError] = useState<string | null>(null)

  useEffect(() => {
    const loadUser = async () => {
      try {
        const user = await getCurrentUser()
        setUserId(user.id)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load current user.'
        setError(message)
      } finally {
        setLoadingUser(false)
      }
    }

    loadUser()
  }, [])

  useEffect(() => {
    const trimmed = query.trim()
    if (trimmed.length < 2) {
      setSearchResults([])
      setSearchError(null)
      return
    }

    const handle = window.setTimeout(async () => {
      setSearching(true)
      setSearchError(null)
      try {
        const results = await searchBooksByTitle(trimmed)
        setSearchResults(results)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Book search failed.'
        setSearchError(message)
      } finally {
        setSearching(false)
      }
    }, 300)

    return () => window.clearTimeout(handle)
  }, [query])

  const selectedBookIds = useMemo(() => new Set(selectedBooks.map((book) => book.id)), [selectedBooks])

  const handleAddBook = (book: BookSearchItem) => {
    setSelectedBooks((existing) => {
      if (existing.some((item) => item.id === book.id)) {
        return existing
      }
      return [...existing, book]
    })
  }

  const handleRemoveBook = (bookId: number) => {
    setSelectedBooks((existing) => existing.filter((book) => book.id !== bookId))
  }

  const handleSave = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)

    if (!userId) {
      setError('Unable to determine current user.')
      return
    }

    if (!title.trim()) {
      setError('List name is required.')
      return
    }

    if (selectedBooks.length === 0) {
      setError('Please add at least one book to create a list.')
      return
    }

    setSaving(true)
    try {
      const created = await createList({
        userId,
        title: title.trim(),
        description: description.trim(),
      })

      for (const book of selectedBooks) {
        await addBookToList(created.id, book.id)
      }

      navigate(`/lists/${created.id}/edit`)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to create list.'
      setError(message)
    } finally {
      setSaving(false)
    }
  }

  if (loadingUser) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading create list page...</span>
      </div>
    )
  }

  return (
    <div className="user-page-root">
      <section className="mb-4">
        <h2 className="mb-1">Create New List</h2>
        <p className="text-muted mb-0">Name your list, add books, and save to start curating.</p>
      </section>

      {error && (
        <Alert variant="danger" className="mb-3">
          {error}
        </Alert>
      )}

      <Form onSubmit={handleSave}>
        <Card className="mb-3">
          <Card.Body>
            <Card.Title className="mb-3">List Details</Card.Title>

            <Form.Group className="mb-3" controlId="create-list-name">
              <Form.Label>Name</Form.Label>
              <Form.Control
                type="text"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
                placeholder="e.g., Weekend Reads"
              />
            </Form.Group>

            <Form.Group controlId="create-list-description">
              <Form.Label>Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={description}
                onChange={(event) => setDescription(event.target.value)}
                placeholder="What is this list about?"
              />
            </Form.Group>
          </Card.Body>
        </Card>

        <div className="d-grid gap-3">
          <ListBookSearchPicker
            query={query}
            onQueryChange={setQuery}
            results={searchResults}
            selectedBookIds={selectedBookIds}
            loading={searching}
            error={searchError}
            onAddBook={handleAddBook}
          />

          <SelectedBooksBox
            books={selectedBooks}
            emptyMessage="Search and add books to build this list."
            onRemoveBook={handleRemoveBook}
          />
        </div>

        <div className="mt-3 d-flex justify-content-end">
          <Button type="submit" variant="dark" disabled={saving}>
            {saving ? 'Saving...' : 'Save List'}
          </Button>
        </div>
      </Form>
    </div>
  )
}

export default CreateListPage

import { useEffect, useMemo, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Form from 'react-bootstrap/Form'
import Spinner from 'react-bootstrap/Spinner'
import { useNavigate, useParams } from 'react-router-dom'
import {
  addBookToList,
  deleteList,
  removeBookFromList,
  updateList,
} from '../../api/lists'
import { getBooksInList, getListById } from '../../api/listsBrowse'
import { getBookById, getCurrentUser, searchBooksByTitle } from '../../api/search'
import type { BookSearchItem } from '../../types/search'
import ListBookSearchPicker from './components/ListBookSearchPicker'
import SelectedBooksBox from './components/SelectedBooksBox'

function EditListPage() {
  const { listId } = useParams()
  const navigate = useNavigate()

  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [isPublic, setIsPublic] = useState(true)
  const [query, setQuery] = useState('')
  const [searchResults, setSearchResults] = useState<BookSearchItem[]>([])
  const [books, setBooks] = useState<BookSearchItem[]>([])
  const [loading, setLoading] = useState(true)
  const [searching, setSearching] = useState(false)
  const [savingDetails, setSavingDetails] = useState(false)
  const [updatingBooks, setUpdatingBooks] = useState(false)
  const [deletingList, setDeletingList] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [searchError, setSearchError] = useState<string | null>(null)
  const [isOwner, setIsOwner] = useState(false)

  useEffect(() => {
    const parsedId = Number(listId)
    if (!listId || Number.isNaN(parsedId)) {
      setError('Invalid list id.')
      setLoading(false)
      return
    }

    const loadEditData = async () => {
      setLoading(true)
      setError(null)

      try {
        const [list, entriesPage, me] = await Promise.all([
          getListById(parsedId),
          getBooksInList(parsedId, 0, 200),
          getCurrentUser(),
        ])

        setTitle(list.title)
        setDescription(list.description ?? '')
        setIsOwner(me.id === list.userId)
        setIsPublic(list.isPublic ?? true)

        const uniqueBookIds = Array.from(new Set(entriesPage.content.map((entry) => entry.bookId)))
        const bookResults = await Promise.allSettled(uniqueBookIds.map((bookId) => getBookById(bookId)))

        const nextBooks: BookSearchItem[] = []
        bookResults.forEach((result) => {
          if (result.status === 'fulfilled') {
            nextBooks.push({
              id: result.value.id,
              title: result.value.title,
              author: result.value.author,
              coverImageUrl: result.value.coverImageUrl,
              description: result.value.description,
            })
          }
        })

        setBooks(nextBooks)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load list editor.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadEditData()
  }, [listId])

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

  const selectedBookIds = useMemo(() => new Set(books.map((book) => book.id)), [books])

  const handleSaveDetails = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    setSuccess(null)

    const parsedId = Number(listId)
    if (!parsedId || Number.isNaN(parsedId)) {
      setError('Invalid list id.')
      return
    }

    if (!title.trim()) {
      setError('List name is required.')
      return
    }

    setSavingDetails(true)
    try {
      await updateList(parsedId, {
        title: title.trim(),
        description: description.trim(),
        isPublic,
      })
      setSuccess('List details updated.')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to update list.'
      setError(message)
    } finally {
      setSavingDetails(false)
    }
  }

  const handleAddBook = async (book: BookSearchItem) => {
    const parsedId = Number(listId)
    if (!parsedId || Number.isNaN(parsedId)) {
      setError('Invalid list id.')
      return
    }

    if (selectedBookIds.has(book.id)) {
      return
    }

    setError(null)
    setSuccess(null)
    setUpdatingBooks(true)
    try {
      await addBookToList(parsedId, book.id)
      setBooks((existing) => [...existing, book])
      setSuccess(`Added "${book.title}".`)
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to add book.'
      setError(message)
    } finally {
      setUpdatingBooks(false)
    }
  }

  const handleRemoveBook = async (bookId: number) => {
    const parsedId = Number(listId)
    if (!parsedId || Number.isNaN(parsedId)) {
      setError('Invalid list id.')
      return
    }

    const target = books.find((book) => book.id === bookId)

    setError(null)
    setSuccess(null)
    setUpdatingBooks(true)
    try {
      await removeBookFromList(parsedId, bookId)
      setBooks((existing) => existing.filter((book) => book.id !== bookId))
      if (target) {
        setSuccess(`Removed "${target.title}".`)
      }
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to remove book.'
      setError(message)
    } finally {
      setUpdatingBooks(false)
    }
  }

  const handleDeleteList = async () => {
    const parsedId = Number(listId)
    if (!parsedId || Number.isNaN(parsedId)) {
      setError('Invalid list id.')
      return
    }

    const shouldDelete = window.confirm(
      'Delete this list? This will remove the list and its book associations.',
    )

    if (!shouldDelete) {
      return
    }

    setError(null)
    setSuccess(null)
    setDeletingList(true)

    try {
      await deleteList(parsedId)
      navigate('/lists')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to delete list.'
      setError(message)
    } finally {
      setDeletingList(false)
    }
  }

  if (loading) {
    return (
      <div className="d-flex align-items-center gap-2">
        <Spinner animation="border" size="sm" />
        <span>Loading list editor...</span>
      </div>
    )
  }

  if (error && !isOwner) {
    return <Alert variant="danger">{error}</Alert>
  }

  if (!isOwner) {
    return <Alert variant="warning">You can only edit your own lists.</Alert>
  }

  return (
    <div className="user-page-root">
      <section className="mb-4">
        <h2 className="mb-1">Edit List</h2>
        <p className="text-muted mb-0">Update details and manage books in this list.</p>
      </section>

      {error && <Alert variant="danger">{error}</Alert>}
      {success && <Alert variant="success">{success}</Alert>}

      <Form onSubmit={handleSaveDetails}>
        <Card className="mb-3">
          <Card.Body>
            <Card.Title className="mb-3">List Details</Card.Title>
            <Form.Group className="mb-3" controlId="edit-list-name">
              <Form.Label>Name</Form.Label>
              <Form.Control
                type="text"
                value={title}
                onChange={(event) => setTitle(event.target.value)}
              />
            </Form.Group>

            <Form.Group controlId="edit-list-description">
              <Form.Label>Description</Form.Label>
              <Form.Control
                as="textarea"
                rows={3}
                value={description}
                onChange={(event) => setDescription(event.target.value)}
              />
            </Form.Group>

            <Form.Group className="mt-3" controlId="edit-list-visibility">
              <Form.Check
                type="switch"
                label={isPublic ? 'Public — visible to everyone' : 'Private — only you can see this'}
                checked={isPublic}
                onChange={(event) => setIsPublic(event.target.checked)}
              />
            </Form.Group>

            <div className="mt-3 d-flex justify-content-between gap-2">
              <Button
                type="button"
                variant="outline-danger"
                disabled={savingDetails || updatingBooks || deletingList}
                onClick={handleDeleteList}
              >
                {deletingList ? 'Deleting...' : 'Delete List'}
              </Button>
              <Button
                type="submit"
                variant="dark"
                disabled={savingDetails || updatingBooks || deletingList}
              >
                {savingDetails ? 'Saving...' : 'Save Changes'}
              </Button>
            </div>
          </Card.Body>
        </Card>
      </Form>

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
          books={books}
          title="Books In This List"
          emptyMessage="No books in this list yet. Search and add one."
          removeLabel={updatingBooks ? 'Working...' : 'Remove'}
          loading={updatingBooks}
          onRemoveBook={handleRemoveBook}
        />
      </div>
    </div>
  )
}

export default EditListPage

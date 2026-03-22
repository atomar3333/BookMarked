import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Spinner from 'react-bootstrap/Spinner'
import { Link } from 'react-router-dom'
import { getListsPage } from '../../api/listsBrowse'
import type { ListTileItem } from '../../types/lists'
import ListGrid from './ListGrid'
import ListsFilters from './ListsFilters'
import ListsPaginator from './ListsPaginator'

const PAGE_SIZE = 20

function ListsPage() {
  const [lists, setLists] = useState<ListTileItem[]>([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const loadLists = async () => {
      setLoading(true)
      setError(null)
      try {
        const response = await getListsPage(page, PAGE_SIZE)
        setLists(response.content)
        setTotalPages(response.totalPages)
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Unable to load lists.'
        setError(message)
      } finally {
        setLoading(false)
      }
    }

    loadLists()
  }, [page])

  return (
    <div>
      <section className="mb-4">
        <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
          <div>
            <h2 className="mb-1">Browse Lists</h2>
            <p className="text-muted mb-0">Explore user-created lists in a modular catalog layout.</p>
          </div>
          <Link to="/lists/create" className="btn btn-dark">
            Create Your List
          </Link>
        </div>
      </section>

      <ListsFilters />

      {loading && (
        <div className="d-flex align-items-center gap-2">
          <Spinner animation="border" size="sm" />
          <span>Loading lists...</span>
        </div>
      )}

      {error && <Alert variant="danger">{error}</Alert>}

      {!loading && !error && lists.length === 0 && (
        <Alert variant="light">No lists found for this page.</Alert>
      )}

      {!loading && !error && lists.length > 0 && (
        <>
          <ListGrid lists={lists} />
          <ListsPaginator
            page={page}
            totalPages={totalPages}
            loading={loading}
            onPrevious={() => setPage((value) => Math.max(0, value - 1))}
            onNext={() => setPage((value) => value + 1)}
          />
        </>
      )}
    </div>
  )
}

export default ListsPage

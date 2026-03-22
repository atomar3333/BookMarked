import Button from 'react-bootstrap/Button'

interface ListsPaginatorProps {
  page: number
  totalPages: number
  loading: boolean
  onPrevious: () => void
  onNext: () => void
}

function ListsPaginator({
  page,
  totalPages,
  loading,
  onPrevious,
  onNext,
}: ListsPaginatorProps) {
  return (
    <div className="d-flex align-items-center justify-content-between mt-3">
      <Button variant="outline-secondary" onClick={onPrevious} disabled={loading || page <= 0}>
        Previous
      </Button>
      <div className="text-muted small">
        Page {totalPages === 0 ? 0 : page + 1} of {totalPages}
      </div>
      <Button
        variant="dark"
        onClick={onNext}
        disabled={loading || totalPages === 0 || page >= totalPages - 1}
      >
        Next
      </Button>
    </div>
  )
}

export default ListsPaginator

import Card from 'react-bootstrap/Card'

function BooksFilters() {
  return (
    <Card className="mb-3">
      <Card.Body>
        <Card.Title className="h6 mb-1">Filters (Coming Soon)</Card.Title>
        <Card.Text className="text-muted mb-0">
          This section is intentionally kept modular for future filters, recommendation
          engine, and trending sections.
        </Card.Text>
      </Card.Body>
    </Card>
  )
}

export default BooksFilters

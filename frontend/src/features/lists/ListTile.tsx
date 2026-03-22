import Card from 'react-bootstrap/Card'
import { Link } from 'react-router-dom'
import type { ListTileItem } from '../../types/lists'

interface ListTileProps {
  list: ListTileItem
}

function ListTile({ list }: ListTileProps) {
  return (
    <Link to={`/lists/${list.id}`} className="lists-tile-link">
      <Card className="lists-tile h-100">
        <Card.Body>
          <Card.Title className="lists-tile-title">{list.title}</Card.Title>
          {list.description ? (
            <Card.Text className="text-muted lists-tile-description">{list.description}</Card.Text>
          ) : (
            <Card.Text className="text-muted lists-tile-description">No description provided.</Card.Text>
          )}
          <div className="small text-muted">User #{list.userId}</div>
        </Card.Body>
      </Card>
    </Link>
  )
}

export default ListTile

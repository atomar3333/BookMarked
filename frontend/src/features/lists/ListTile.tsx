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
        <Card.Body className="d-flex flex-column">
          <div className="flex-grow-1">
            <Card.Title className="lists-tile-title">{list.title}</Card.Title>
            {list.description ? (
              <Card.Text className="text-muted lists-tile-description">{list.description}</Card.Text>
            ) : (
              <Card.Text className="text-muted lists-tile-description">No description provided.</Card.Text>
            )}
          </div>
          <div className="d-flex justify-content-between align-items-center mt-auto pt-2 border-top">
            <div className="small text-muted">User #{list.userId}</div>
            {list.likeStats && (
              <div className="small text-muted d-flex align-items-center gap-1">
                <span>❤️</span>
                <span>{list.likeStats.likeCount}</span>
              </div>
            )}
          </div>
        </Card.Body>
      </Card>
    </Link>
  )
}

export default ListTile

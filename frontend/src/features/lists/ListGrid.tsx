import type { ListTileItem } from '../../types/lists'
import ListTile from './ListTile'

interface ListGridProps {
  lists: ListTileItem[]
}

function ListGrid({ lists }: ListGridProps) {
  return (
    <div className="lists-grid">
      {lists.map((list) => (
        <ListTile key={list.id} list={list} />
      ))}
    </div>
  )
}

export default ListGrid

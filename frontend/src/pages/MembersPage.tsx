import { useEffect, useState } from 'react'
import Alert from 'react-bootstrap/Alert'
import Badge from 'react-bootstrap/Badge'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import ListGroup from 'react-bootstrap/ListGroup'
import Spinner from 'react-bootstrap/Spinner'
import { Link } from 'react-router-dom'
import { followUser, getFollowing, unfollowUser } from '../api/followers'
import { getAllUsers, getCurrentUser } from '../api/search'
import { getReadingStatusesByUser } from '../api/userPage'
import type { UserProfileItem } from '../types/search'

interface MemberStats {
  read: number
  currentlyReading: number
  wantToRead: number
}

const MEMBERS_PER_PAGE = 10

function MembersPage() {
  const [currentUser, setCurrentUser] = useState<UserProfileItem | null>(null)
  const [users, setUsers] = useState<UserProfileItem[]>([])
  const [totalPages, setTotalPages] = useState(1)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [statsByUserId, setStatsByUserId] = useState<Record<number, MemberStats>>({})
  const [followingIds, setFollowingIds] = useState<Set<number>>(new Set())
  const [savingFollowId, setSavingFollowId] = useState<number | null>(null)
  const [followError, setFollowError] = useState<string | null>(null)

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        const cu = await getCurrentUser()
        setCurrentUser(cu)
        const followingPage = await getFollowing(cu.id, 0, 1000)
        setFollowingIds(new Set(followingPage.content.map((f) => f.followingId)))
      } catch {
        // Not logged in or follow fetch failed — follow buttons simply won't show
      }
    }
    loadCurrentUser()
  }, [])

  useEffect(() => {
    const loadUsers = async () => {
      setLoading(true)
      setError(null)
      setStatsByUserId({})
      try {
        const pageData = await getAllUsers(page, MEMBERS_PER_PAGE)
        setUsers(pageData.content)
        setTotalPages(pageData.totalPages)
        setLoading(false)

        const statsResults = await Promise.allSettled(
          pageData.content.map((u) => getReadingStatusesByUser(u.id)),
        )
        const newStats: Record<number, MemberStats> = {}
        statsResults.forEach((result, idx) => {
          const u = pageData.content[idx]
          if (result.status === 'fulfilled') {
            const statuses = result.value
            newStats[u.id] = {
              read: statuses.filter((s) => s.currentStatus === 'READ').length,
              currentlyReading: statuses.filter((s) => s.currentStatus === 'CURRENTLY_READING').length,
              wantToRead: statuses.filter((s) => s.currentStatus === 'WANT_TO_READ').length,
            }
          }
        })
        setStatsByUserId(newStats)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Unable to load members.')
        setLoading(false)
      }
    }
    loadUsers()
  }, [page])

  const handleFollowToggle = async (targetUserId: number) => {
    if (!currentUser) return
    setFollowError(null)
    setSavingFollowId(targetUserId)
    const wasFollowing = followingIds.has(targetUserId)
    setFollowingIds((prev) => {
      const next = new Set(prev)
      if (wasFollowing) next.delete(targetUserId)
      else next.add(targetUserId)
      return next
    })
    try {
      if (wasFollowing) {
        await unfollowUser(currentUser.id, targetUserId)
      } else {
        await followUser(currentUser.id, targetUserId)
      }
    } catch (err) {
      setFollowingIds((prev) => {
        const next = new Set(prev)
        if (wasFollowing) next.add(targetUserId)
        else next.delete(targetUserId)
        return next
      })
      setFollowError(err instanceof Error ? err.message : 'Unable to update follow status.')
    } finally {
      setSavingFollowId(null)
    }
  }

  return (
    <Card className="shadow-sm">
      <Card.Body>
        <h4 className="mb-4">Members</h4>

        {error && <Alert variant="danger">{error}</Alert>}
        {followError && <Alert variant="warning">{followError}</Alert>}

        {loading ? (
          <div className="d-flex align-items-center gap-2">
            <Spinner animation="border" size="sm" />
            <span>Loading members...</span>
          </div>
        ) : users.length === 0 ? (
          <Alert variant="light">No members found.</Alert>
        ) : (
          <>
            <ListGroup variant="flush">
              {users.map((user) => {
                const stats = statsByUserId[user.id]
                const isFollowing = followingIds.has(user.id)
                const saving = savingFollowId === user.id
                const canFollow = currentUser !== null && currentUser.id !== user.id

                return (
                  <ListGroup.Item key={user.id} className="px-0 py-3">
                    <div className="d-flex justify-content-between align-items-start gap-3">
                      <div className="flex-grow-1" style={{ minWidth: 0 }}>
                        <Link
                          to={`/users/${user.id}`}
                          className="fw-semibold text-decoration-none text-dark"
                        >
                          {user.userName}
                        </Link>
                        {user.bio && (
                          <div
                            className="text-muted small mt-1"
                            style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                          >
                            {user.bio}
                          </div>
                        )}
                        <div className="d-flex flex-wrap gap-2 mt-2">
                          {stats !== undefined ? (
                            <>
                              <Badge bg="success" className="fw-normal">
                                {stats.read} read
                              </Badge>
                              <Badge bg="primary" className="fw-normal">
                                {stats.currentlyReading} reading
                              </Badge>
                              <Badge bg="secondary" className="fw-normal">
                                {stats.wantToRead} want to read
                              </Badge>
                            </>
                          ) : (
                            <span className="text-muted small">Loading stats...</span>
                          )}
                        </div>
                      </div>

                      {canFollow && (
                        <Button
                          size="sm"
                          variant={isFollowing ? 'outline-dark' : 'dark'}
                          disabled={saving}
                          onClick={() => handleFollowToggle(user.id)}
                        >
                          {saving ? '...' : isFollowing ? 'Following' : 'Follow'}
                        </Button>
                      )}
                    </div>
                  </ListGroup.Item>
                )
              })}
            </ListGroup>

            <div className="d-flex align-items-center justify-content-between mt-4">
              <Button
                variant="outline-secondary"
                size="sm"
                disabled={page <= 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
              >
                Previous
              </Button>
              <span className="text-muted small">
                Page {page + 1} of {totalPages}
              </span>
              <Button
                variant="outline-dark"
                size="sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
              >
                Next
              </Button>
            </div>
          </>
        )}
      </Card.Body>
    </Card>
  )
}

export default MembersPage

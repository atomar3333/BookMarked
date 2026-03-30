import { useState } from 'react'
import Button from 'react-bootstrap/Button'
import { Heart, HeartFill } from 'react-bootstrap-icons'

interface LikeButtonProps {
  initialIsLiked?: boolean
  initialLikeCount?: number
  onLike: () => Promise<void>
  onUnlike: () => Promise<void>
  size?: 'sm' | 'lg'
  variant?: string
  className?: string
  showText?: boolean
}

export default function LikeButton({
  initialIsLiked = false,
  initialLikeCount,
  onLike,
  onUnlike,
  size = 'sm',
  variant = 'outline-danger',
  className = '',
  showText = false
}: LikeButtonProps) {
  const [isLiked, setIsLiked] = useState(initialIsLiked)
  const [likeCount, setLikeCount] = useState(initialLikeCount)
  const [isLoading, setIsLoading] = useState(false)

  const handleToggle = async (e: React.MouseEvent) => {
    e.preventDefault() // prevent navigating if inside a Link
    e.stopPropagation()
    
    if (isLoading) return

    setIsLoading(true)
    const newLikedState = !isLiked

    try {
      if (newLikedState) {
        await onLike()
        if (likeCount !== undefined) setLikeCount(likeCount + 1)
      } else {
        await onUnlike()
        if (likeCount !== undefined) setLikeCount(Math.max(0, likeCount - 1))
      }
      setIsLiked(newLikedState)
    } catch (error) {
      console.error('Failed to toggle like', error)
      // If we got a 409 conflict, it means they already liked it on another tab/device.
      // We can optimistically auto-correct the state in the background.
      setIsLiked(newLikedState)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <Button 
      variant={isLiked ? "danger" : variant} 
      size={size} 
      className={`d-inline-flex align-items-center justify-content-center ${className}`}
      onClick={handleToggle}
      disabled={isLoading}
      title={isLiked ? "Unlike" : "Like"}
    >
      {isLiked ? <HeartFill /> : <Heart />}
      {likeCount !== undefined && <span className="ms-2">{likeCount}</span>}
      {showText && likeCount === undefined && <span className="ms-2">{isLiked ? 'Liked' : 'Like'}</span>}
    </Button>
  )
}

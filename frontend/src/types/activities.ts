export type ActivityType =
  | 'BOOK_LIKED'
  | 'REVIEW_LIKED'
  | 'LIST_LIKED'
  | 'REVIEW_CREATED'
  | 'LIST_CREATED'
  | 'READING_STATUS_UPDATED'

export interface ActivityMetadata {
  bookId?: number
  bookTitle?: string
  listId?: number
  listName?: string
  reviewId?: number
  reviewerName?: string
  oldStatus?: string
  newStatus?: string
}

export interface ActivityItem {
  id: number
  userId: number
  userName: string
  activityType: ActivityType
  targetId: number
  metadata: ActivityMetadata | null
  createdAt: string
}

export interface ActivityPage {
  content: ActivityItem[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

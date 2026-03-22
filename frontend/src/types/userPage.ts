export type ReadingStatusValue = 'WANT_TO_READ' | 'CURRENTLY_READING' | 'READ'

export interface ReadingStatusItem {
  id: number
  userId: number
  bookId: number
  startedAt?: string
  finishedAt?: string
  currentStatus: ReadingStatusValue
}

export interface UserReviewItem {
  id: number
  userId: number
  bookId: number
  reviewText?: string
  rating: number
  createdAt?: string
}

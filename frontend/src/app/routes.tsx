import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from '../components/ProtectedRoute'
import LoginPage from '../features/auth/LoginPage'
import BooksPage from '../features/books/BooksPage'
import ListDetailPage from '../features/lists/ListDetailPage'
import ListsPage from '../features/lists/ListsPage'
import RegisterPage from '../features/auth/RegisterPage'
import UnifiedSearchPage from '../features/search/UnifiedSearchPage'
import BookDetailPage from '../pages/BookDetailPage'
import HomePage from '../pages/HomePage'
import ProfilePage from '../pages/ProfilePage'
import UserPage from '../pages/UserPage'

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/books" element={<BooksPage />} />
      <Route path="/lists" element={<ListsPage />} />
      <Route path="/lists/:listId" element={<ListDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/users/:userId" element={<UserPage />} />
        <Route path="/search" element={<UnifiedSearchPage />} />
        <Route path="/books/:bookId" element={<BookDetailPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default AppRoutes

import Container from 'react-bootstrap/Container'
import AppNavbar from './components/AppNavbar'
import AppRoutes from './app/routes'

function App() {
  return (
    <div className="app-root">
      <AppNavbar />
      <Container className="py-4">
        <AppRoutes />
      </Container>
    </div>
  )
}

export default App

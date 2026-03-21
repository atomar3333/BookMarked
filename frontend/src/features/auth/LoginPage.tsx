import { useState, type FormEvent } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Form from 'react-bootstrap/Form'
import { loginUser } from '../../api/auth'
import { useAuth } from '../../app/AuthProvider'

interface LocationState {
  from?: string
  message?: string
}

function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const { login } = useAuth()

  const state = (location.state as LocationState | null) ?? null
  const successMessage = state?.message

  const [emailId, setEmailId] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)

    if (!emailId || !password) {
      setError('Email and password are required.')
      return
    }

    setLoading(true)
    try {
      const response = await loginUser({ emailId, password })
      login(response.token)
      const redirectTo = state?.from || '/'
      navigate(redirectTo, { replace: true })
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to login.'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="auth-card shadow-sm">
      <Card.Body>
        <Card.Title className="mb-3">Login</Card.Title>
        {successMessage && <Alert variant="success">{successMessage}</Alert>}
        {error && <Alert variant="danger">{error}</Alert>}
        <Form onSubmit={onSubmit} noValidate>
          <Form.Group className="mb-3" controlId="login-email">
            <Form.Label>Email</Form.Label>
            <Form.Control
              type="email"
              value={emailId}
              onChange={(event) => setEmailId(event.target.value)}
              required
            />
          </Form.Group>

          <Form.Group className="mb-3" controlId="login-password">
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </Form.Group>

          <Button type="submit" disabled={loading} className="w-100">
            {loading ? 'Signing in...' : 'Login'}
          </Button>
        </Form>
        <p className="mt-3 mb-0 text-center">
          No account? <Link to="/register">Create one</Link>
        </p>
      </Card.Body>
    </Card>
  )
}

export default LoginPage

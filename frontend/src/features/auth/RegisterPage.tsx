import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import Alert from 'react-bootstrap/Alert'
import Button from 'react-bootstrap/Button'
import Card from 'react-bootstrap/Card'
import Form from 'react-bootstrap/Form'
import { registerUser } from '../../api/auth'

function RegisterPage() {
  const navigate = useNavigate()
  const [userName, setUserName] = useState('')
  const [emailId, setEmailId] = useState('')
  const [password, setPassword] = useState('')
  const [bio, setBio] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)

    if (!userName || !emailId || !password) {
      setError('Username, email, and password are required.')
      return
    }

    const emailPattern = /\S+@\S+\.\S+/
    if (!emailPattern.test(emailId)) {
      setError('Please enter a valid email address.')
      return
    }

    setLoading(true)
    try {
      await registerUser({ userName, emailId, password, bio })
      navigate('/login', {
        replace: true,
        state: { message: 'Registration successful. Please sign in.' },
      })
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Unable to register.'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card className="auth-card shadow-sm">
      <Card.Body>
        <Card.Title className="mb-3">Create Account</Card.Title>
        {error && <Alert variant="danger">{error}</Alert>}

        <Form onSubmit={onSubmit} noValidate>
          <Form.Group className="mb-3" controlId="register-username">
            <Form.Label>Username</Form.Label>
            <Form.Control
              type="text"
              value={userName}
              onChange={(event) => setUserName(event.target.value)}
              required
            />
          </Form.Group>

          <Form.Group className="mb-3" controlId="register-email">
            <Form.Label>Email</Form.Label>
            <Form.Control
              type="email"
              value={emailId}
              onChange={(event) => setEmailId(event.target.value)}
              required
            />
          </Form.Group>

          <Form.Group className="mb-3" controlId="register-password">
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
          </Form.Group>

          <Form.Group className="mb-3" controlId="register-bio">
            <Form.Label>Bio (optional)</Form.Label>
            <Form.Control
              as="textarea"
              rows={3}
              value={bio}
              onChange={(event) => setBio(event.target.value)}
            />
          </Form.Group>

          <Button type="submit" disabled={loading} className="w-100">
            {loading ? 'Creating account...' : 'Register'}
          </Button>
        </Form>

        <p className="mt-3 mb-0 text-center">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </Card.Body>
    </Card>
  )
}

export default RegisterPage

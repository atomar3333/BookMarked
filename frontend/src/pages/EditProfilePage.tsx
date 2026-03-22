import { useEffect, useState } from 'react'
import { Container, Form, Button, Alert, Spinner } from 'react-bootstrap'
import { useNavigate } from 'react-router-dom'
import { getCurrentUser } from '../api/search'
import { updateCurrentUserProfile } from '../api/profile'
import type { UserProfileItem } from '../types/search'

function EditProfilePage() {
  const navigate = useNavigate()
  const [currentUser, setCurrentUser] = useState<UserProfileItem | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  const [formData, setFormData] = useState({
    userName: '',
    emailId: '',
    bio: '',
  })

  useEffect(() => {
    const loadCurrentUser = async () => {
      try {
        const user = await getCurrentUser()
        setCurrentUser(user)
        setFormData({
          userName: user.userName || '',
          emailId: user.emailId || '',
          bio: user.bio || '',
        })
      } catch (err) {
        setError('Failed to load your profile. Please try again.')
        console.error(err)
      } finally {
        setLoading(false)
      }
    }

    loadCurrentUser()
  }, [])

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)
    setSuccess(false)

    if (!currentUser) {
      setError('User information not loaded.')
      return
    }

    if (!formData.userName.trim()) {
      setError('Username is required.')
      return
    }

    if (!formData.emailId.trim()) {
      setError('Email is required.')
      return
    }

    setSaving(true)
    try {
      await updateCurrentUserProfile(currentUser.id, {
        userName: formData.userName.trim(),
        emailId: formData.emailId.trim(),
        bio: formData.bio.trim(),
      })

      setSuccess(true)
      setTimeout(() => {
        navigate('/profile')
      }, 1500)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update profile.')
      console.error(err)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <Container className="py-5 text-center">
        <Spinner animation="border" />
        <p className="mt-2">Loading your profile...</p>
      </Container>
    )
  }

  return (
    <Container className="py-5">
      <div className="row">
        <div className="col-md-6 mx-auto">
          <h2 className="mb-4">Edit Profile</h2>

          {error && <Alert variant="danger">{error}</Alert>}
          {success && <Alert variant="success">Profile updated successfully! Redirecting...</Alert>}

          <Form onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>Username</Form.Label>
              <Form.Control
                type="text"
                name="userName"
                value={formData.userName}
                onChange={handleChange}
                placeholder="Enter your username"
                required
              />
              <Form.Text className="text-muted">Your unique username for the platform.</Form.Text>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Email</Form.Label>
              <Form.Control
                type="email"
                name="emailId"
                value={formData.emailId}
                onChange={handleChange}
                placeholder="Enter your email"
                required
              />
              <Form.Text className="text-muted">We'll never share your email with anyone else.</Form.Text>
            </Form.Group>

            <Form.Group className="mb-4">
              <Form.Label>Bio</Form.Label>
              <Form.Control
                as="textarea"
                name="bio"
                value={formData.bio}
                onChange={handleChange}
                placeholder="Tell us about yourself..."
                rows={4}
              />
              <Form.Text className="text-muted">A short bio about you ({formData.bio.length}/500 characters)</Form.Text>
            </Form.Group>

            <div className="d-flex gap-2">
              <Button
                variant="primary"
                type="submit"
                disabled={saving}
              >
                {saving ? (
                  <>
                    <Spinner animation="border" size="sm" className="me-2" />
                    Saving...
                  </>
                ) : (
                  'Save Changes'
                )}
              </Button>
              <Button
                variant="outline-secondary"
                onClick={() => navigate('/profile')}
                disabled={saving}
              >
                Cancel
              </Button>
            </div>
          </Form>
        </div>
      </div>
    </Container>
  )
}

export default EditProfilePage

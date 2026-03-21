import Card from 'react-bootstrap/Card'

function ProfilePage() {
  return (
    <Card>
      <Card.Body>
        <Card.Title>Protected Page</Card.Title>
        <Card.Text>You are logged in and can access protected routes.</Card.Text>
      </Card.Body>
    </Card>
  )
}

export default ProfilePage

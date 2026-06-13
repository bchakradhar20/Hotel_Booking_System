import { useEffect, useState } from 'react'
import API from '../api'

export default function Users() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    API.get('/users')
      .then(({ data }) => setUsers(data))
      .catch(() => setError('Failed to load users'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  return (
    <div>
      <h2 className="mb-4">👥 All Users</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="table-responsive">
        <table className="table table-hover table-bordered bg-white shadow-sm">
          <thead className="table-dark">
            <tr><th>ID</th><th>Username</th><th>Email</th><th>Phone</th></tr>
          </thead>
          <tbody>
            {users.map(u => (
              <tr key={u.userId}>
                <td>{u.userId}</td>
                <td>{u.username}</td>
                <td>{u.email}</td>
                <td>{u.phoneNumber || '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

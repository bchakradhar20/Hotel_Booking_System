import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import API from '../api'
import RoomCard from '../components/RoomCard'

export default function Rooms() {
  const navigate = useNavigate()
  const [rooms, setRooms] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [filter, setFilter] = useState('ALL')

  const roles = JSON.parse(localStorage.getItem('roles') || '[]')
  const isAdmin = roles.includes('ROLE_ADMIN')

  useEffect(() => {
    API.get('/rooms')
      .then(({ data }) => setRooms(data))
      .catch(() => setError('Failed to load rooms'))
      .finally(() => setLoading(false))
  }, [])

  const filtered = filter === 'ALL' ? rooms : rooms.filter(r => r.roomType === filter)

  if (loading) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <h2 className="mb-0">🏨 Available Rooms</h2>
        <div className="d-flex align-items-center gap-2 flex-wrap">
          {/* Filter buttons */}
          <div className="btn-group">
            {['ALL', 'STANDARD', 'DELUXE', 'SUITE', 'FAMILY'].map(t => (
              <button key={t}
                className={`btn btn-sm ${filter === t ? 'btn-dark' : 'btn-outline-dark'}`}
                onClick={() => setFilter(t)}>{t}
              </button>
            ))}
          </div>
          {/* Admin shortcut button */}
          {isAdmin && (
            <button
              className="btn btn-sm btn-warning fw-bold"
              onClick={() => navigate('/admin/rooms')}
            >
              ➕ Add / Manage Rooms
            </button>
          )}
        </div>
      </div>

      {/* Admin info bar */}
      {isAdmin && (
        <div className="alert alert-warning py-2 mb-3">
          <small>
            🛠️ You are logged in as <strong>Admin</strong>. Use the
            <strong> Admin</strong> menu or the button above to manage rooms, reservations and users.
          </small>
        </div>
      )}

      {error && <div className="alert alert-danger">{error}</div>}

      {filtered.length === 0
        ? <p className="text-muted">No rooms found.</p>
        : <div className="row row-cols-1 row-cols-md-3 g-4">
            {filtered.map(room => (
              <div className="col" key={room.roomId}>
                <RoomCard room={room} />
              </div>
            ))}
          </div>
      }
    </div>
  )
}

import { useNavigate } from 'react-router-dom'

const badgeStyle = {
  STANDARD: { background: '#4a4a4a', color: '#ccc' },
  DELUXE:   { background: '#7d1a1a', color: '#ffaaaa' },
  SUITE:    { background: '#1a4a2a', color: '#aaffbb' },
  FAMILY:   { background: '#1a3a5a', color: '#aaddff' },
}

export default function RoomCard({ room }) {
  const navigate = useNavigate()
  const token = localStorage.getItem('token')
  const style = badgeStyle[room.roomType] || badgeStyle.STANDARD

  return (
    <div className="card h-100 room-card">
      <div className="card-body d-flex flex-column">
        <div className="d-flex justify-content-between align-items-start mb-2">
          <h5 className="card-title mb-0">Room {room.roomNumber}</h5>
          <span className="badge" style={style}>{room.roomType}</span>
        </div>
        <p className="card-text text-muted flex-grow-1">{room.description || 'No description available.'}</p>
        <ul className="list-unstyled mb-3" style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
          <li>👥 Capacity: <strong style={{ color: 'var(--text)' }}>{room.capacity}</strong></li>
          <li>💰 Price: <strong style={{ color: 'var(--red)' }}>${room.pricePerNight}<span style={{ color: 'var(--text-muted)', fontWeight: 400 }}>/night</span></strong></li>
        </ul>
        <button
          className="btn btn-primary w-100"
          onClick={() => token ? navigate(`/book/${room.roomId}`) : navigate('/login')}
        >
          {token ? '🛏️ Book Now' : '🔐 Login to Book'}
        </button>
      </div>
    </div>
  )
}

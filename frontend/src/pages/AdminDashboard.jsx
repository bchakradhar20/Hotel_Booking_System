import { Link } from 'react-router-dom'

const cards = [
  { to: '/admin/rooms',        icon: '🛏️', title: 'Manage Rooms',        desc: 'Add, edit, or delete hotel rooms' },
  { to: '/admin/reservations', icon: '📋', title: 'Manage Reservations', desc: 'View and delete all reservations' },
  { to: '/admin/users',        icon: '👥', title: 'View Users',           desc: 'View all registered users' },
  { to: '/admin/create-admin', icon: '🛡️', title: 'Create Admin',         desc: 'Register a new admin user' },
]

export default function AdminDashboard() {
  return (
    <div>
      <h2 className="mb-1">🛠️ Admin Dashboard</h2>
      <p className="text-muted mb-4">Manage your hotel system</p>
      <div className="row g-4">
        {cards.map(card => (
          <div className="col-md-3 col-sm-6" key={card.to}>
            <Link to={card.to} className="admin-card">
              <div className="icon">{card.icon}</div>
              <h5>{card.title}</h5>
              <p>{card.desc}</p>
            </Link>
          </div>
        ))}
      </div>
    </div>
  )
}

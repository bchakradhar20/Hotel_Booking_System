import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export default function Navbar() {
  const navigate = useNavigate()
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  const roles = JSON.parse(localStorage.getItem('roles') || '[]')
  const isAdmin = roles.includes('ROLE_ADMIN')
  const [adminOpen, setAdminOpen] = useState(false)
  const [navOpen, setNavOpen] = useState(false)
  const dropdownRef = useRef(null)

  useEffect(() => {
    const handler = e => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) setAdminOpen(false)
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const logout = () => { localStorage.clear(); navigate('/login') }
  const closeAll = () => { setAdminOpen(false); setNavOpen(false) }

  return (
    <nav className="navbar navbar-expand-lg px-4">
      <Link className="navbar-brand" to="/rooms" onClick={closeAll}>🏨 HotelReserve</Link>

      <button className="navbar-toggler" type="button" onClick={() => setNavOpen(o => !o)}
        style={{ borderColor: 'var(--red)' }}>
        <span className="navbar-toggler-icon" style={{ filter: 'invert(1)' }} />
      </button>

      <div className={`collapse navbar-collapse ${navOpen ? 'show' : ''}`}>
        <ul className="navbar-nav me-auto">
          <li className="nav-item">
            <Link className="nav-link" to="/rooms" onClick={closeAll}>Rooms</Link>
          </li>

          {token && (
            <li className="nav-item">
              <Link className="nav-link" to="/my-reservations" onClick={closeAll}>My Reservations</Link>
            </li>
          )}

          {token && (
            <li className="nav-item">
              <Link className="nav-link" to="/profile" onClick={closeAll}>Profile</Link>
            </li>
          )}

          {isAdmin && (
            <li className="nav-item dropdown" ref={dropdownRef}>
              <button
                className="nav-link btn btn-link dropdown-toggle"
                style={{ textDecoration: 'none', color: 'var(--red)', fontWeight: 700 }}
                onClick={() => setAdminOpen(o => !o)}
              >
                🛠️ Admin
              </button>
              {adminOpen && (
                <ul className="dropdown-menu dropdown-menu-dark show"
                  style={{ position: 'absolute', zIndex: 1000 }}>
                  <li><Link className="dropdown-item" to="/admin" onClick={closeAll}>📊 Dashboard</Link></li>
                  <li><hr className="dropdown-divider" /></li>
                  <li><Link className="dropdown-item" to="/admin/rooms" onClick={closeAll}>🛏️ Manage Rooms</Link></li>
                  <li><Link className="dropdown-item" to="/admin/reservations" onClick={closeAll}>📋 Manage Reservations</Link></li>
                  <li><Link className="dropdown-item" to="/admin/users" onClick={closeAll}>👥 View Users</Link></li>
                  <li><hr className="dropdown-divider" /></li>
                  <li><Link className="dropdown-item" to="/admin/create-admin" onClick={closeAll}
                    style={{ color: 'var(--red)' }}>🛡️ Create Admin</Link></li>
                </ul>
              )}
            </li>
          )}
        </ul>

        <ul className="navbar-nav">
          {token ? (
            <>
              <li className="nav-item">
                <span className="nav-link" style={{ color: 'var(--text-muted)' }}>👤 {username}</span>
              </li>
              <li className="nav-item">
                <button className="btn btn-outline-light btn-sm ms-2" onClick={logout}>Logout</button>
              </li>
            </>
          ) : (
            <>
              <li className="nav-item">
                <Link className="nav-link" to="/login" onClick={closeAll}>Login</Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="/register" onClick={closeAll}>Register</Link>
              </li>
            </>
          )}
        </ul>
      </div>
    </nav>
  )
}

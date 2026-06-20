import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import API from '../api'

const EMPTY = { username: '', email: '', password: '', phoneNumber: '' }

export default function Register() {
  const navigate = useNavigate()
  const location = useLocation()
  const [tab, setTab] = useState(location.state?.tab || 'user')
  const [form, setForm] = useState(EMPTY)
  const [adminSecret, setAdminSecret] = useState('')
  const [showSecret, setShowSecret] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const validate = () => {
    if (!form.username || form.username.trim().length < 3) return 'Username must be at least 3 characters'
    if (!form.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) return 'Enter a valid email address'
    if (!form.password || form.password.length < 6) return 'Password must be at least 6 characters'
    if (tab === 'admin' && !adminSecret) return 'Admin secret key is required'
    return ''
  }

  const handleTabSwitch = t => {
    setTab(t)
    setForm(EMPTY)
    setAdminSecret('')
    setError('')
    setSuccess('')
  }

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setSuccess('')
    const validationErr = validate()
    if (validationErr) { setError(validationErr); return }
    setLoading(true)
    try {
      if (tab === 'user') {
        await API.post('/auth/signup', form)
        setSuccess('Registration successful! Redirecting to login...')
        setTimeout(() => navigate('/login'), 1500)
      } else {
        await API.post('/auth/register-admin', { ...form, adminSecret })
        setSuccess('Admin account created! Redirecting to login...')
        setTimeout(() => navigate('/login'), 1500)
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="row justify-content-center mt-4">
      <div className="col-md-5">
        <div className="card shadow">
          <div className="card-body p-4">

            {/* Tab switcher */}
            <ul className="nav nav-tabs mb-4">
              <li className="nav-item w-50 text-center">
                <button
                  className={`nav-link w-100 ${tab === 'user' ? 'active fw-bold' : ''}`}
                  onClick={() => handleTabSwitch('user')}
                >
                  👤 User Register
                </button>
              </li>
              <li className="nav-item w-50 text-center">
                <button
                  className={`nav-link w-100 ${tab === 'admin' ? 'active fw-bold text-danger' : 'text-secondary'}`}
                  onClick={() => handleTabSwitch('admin')}
                >
                  🛡️ Admin Register
                </button>
              </li>
            </ul>

            {/* Admin warning banner */}
            {tab === 'admin' && (
              <div className="alert alert-warning py-2 mb-3">
                <small>
                  ⚠️ Admin accounts have <strong>full system access</strong>.
                  You must provide the admin secret key to register.
                </small>
              </div>
            )}

            <h5 className="text-center mb-3">
              {tab === 'user' ? '📝 Create User Account' : '🛡️ Create Admin Account'}
            </h5>

            {error   && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Username</label>
                <input className="form-control" name="username" value={form.username}
                  onChange={handleChange} required minLength={3}
                  placeholder={tab === 'admin' ? 'e.g. manager01' : 'e.g. johndoe'} />
              </div>

              <div className="mb-3">
                <label className="form-label">Email</label>
                <input className="form-control" type="email" name="email"
                  value={form.email} onChange={handleChange} required />
              </div>

              <div className="mb-3">
                <label className="form-label">Password</label>
                <input className="form-control" type="password" name="password"
                  value={form.password} onChange={handleChange} required minLength={6} />
              </div>

              <div className="mb-3">
                <label className="form-label">Phone Number <span className="text-muted">(optional)</span></label>
                <input className="form-control" name="phoneNumber"
                  value={form.phoneNumber} onChange={handleChange} />
              </div>

              {/* Admin secret key — only shown on admin tab */}
              {tab === 'admin' && (
                <div className="mb-3">
                  <label className="form-label">
                    Admin Secret Key <span className="text-danger">*</span>
                  </label>
                  <div className="input-group">
                    <input
                      className="form-control"
                      type={showSecret ? 'text' : 'password'}
                      value={adminSecret}
                      onChange={e => setAdminSecret(e.target.value)}
                      required
                      placeholder="Enter admin secret key"
                    />
                    <button
                      type="button"
                      className="btn btn-outline-secondary"
                      onClick={() => setShowSecret(s => !s)}
                    >
                      {showSecret ? '🙈' : '👁️'}
                    </button>
                  </div>
                  <div className="form-text">Contact your system administrator for the secret key.</div>
                </div>
              )}

              <button
                className={`btn w-100 mt-1 ${tab === 'admin' ? 'btn-danger' : 'btn-success'}`}
                type="submit"
                disabled={loading}
              >
                {loading
                  ? 'Registering...'
                  : tab === 'admin' ? '🛡️ Create Admin Account' : '✅ Create User Account'}
              </button>
            </form>

            <p className="text-center mt-3 mb-0">
              Already have an account? <Link to="/login">Login</Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

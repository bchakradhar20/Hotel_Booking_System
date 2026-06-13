import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import API from '../api'

export default function CreateAdmin() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', email: '', password: '', phoneNumber: '', adminSecret: '' })
  const [showSecret, setShowSecret] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      await API.post('/auth/register-admin', form)
      setSuccess(`Admin "${form.username}" created successfully!`)
      setForm({ username: '', email: '', password: '', phoneNumber: '', adminSecret: '' })
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create admin')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="row justify-content-center">
      <div className="col-md-5">
        <div className="card shadow">
          <div className="card-body p-4">
            <div className="d-flex align-items-center mb-4">
              <button className="btn btn-sm btn-outline-secondary me-3" onClick={() => navigate('/admin')}>
                ← Back
              </button>
              <h3 className="mb-0">🛡️ Create Admin User</h3>
            </div>

            <div className="alert alert-warning py-2">
              <small>⚠️ This account will have full <strong>ADMIN</strong> access to the system.</small>
            </div>

            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Username</label>
                <input className="form-control" name="username" value={form.username}
                  onChange={handleChange} required minLength={3} placeholder="e.g. admin2" />
              </div>
              <div className="mb-3">
                <label className="form-label">Email</label>
                <input className="form-control" type="email" name="email"
                  value={form.email} onChange={handleChange} required placeholder="admin@hotel.com" />
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
              <div className="mb-3">
                <label className="form-label">Admin Secret Key <span className="text-danger">*</span></label>
                <div className="input-group">
                  <input
                    className="form-control"
                    type={showSecret ? 'text' : 'password'}
                    name="adminSecret"
                    value={form.adminSecret}
                    onChange={handleChange}
                    required
                    placeholder="Enter admin secret key"
                  />
                  <button type="button" className="btn btn-outline-secondary"
                    onClick={() => setShowSecret(s => !s)}>
                    {showSecret ? '🙈' : '👁️'}
                  </button>
                </div>
                <div className="form-text">Default: HotelAdmin@2024</div>
              </div>
              <button className="btn btn-danger w-100" type="submit" disabled={loading}>
                {loading ? 'Creating...' : '🛡️ Create Admin'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

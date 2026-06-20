import { useEffect, useState } from 'react'
import API from '../api'

export default function Profile() {
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({ email: '', phoneNumber: '' })
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    API.get('/users/profile').then(({ data }) => {
      setProfile(data)
      setForm({ email: data.email || '', phoneNumber: data.phoneNumber || '' })
    })
  }, [])

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async e => {
    e.preventDefault()
    setMsg('')
    setError('')
    if (!form.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      setError('Enter a valid email address')
      return
    }
    setLoading(true)
    try {
      const { data } = await API.put('/users/profile', form)
      setProfile(data)
      setMsg('Profile updated successfully')
    } catch (err) {
      setError(err.response?.data?.message || 'Update failed')
    } finally {
      setLoading(false)
    }
  }

  if (!profile) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  return (
    <div className="row justify-content-center">
      <div className="col-md-5">
        <div className="card shadow">
          <div className="card-body p-4">
            <h3 className="mb-4">👤 My Profile</h3>
            <p className="mb-1"><strong>Username:</strong> {profile.username}</p>
            <hr />
            {msg && <div className="alert alert-success">{msg}</div>}
            {error && <div className="alert alert-danger">{error}</div>}
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Email</label>
                <input className="form-control" type="email" name="email"
                  value={form.email} onChange={handleChange} required />
              </div>
              <div className="mb-3">
                <label className="form-label">Phone Number</label>
                <input className="form-control" name="phoneNumber"
                  value={form.phoneNumber} onChange={handleChange} />
              </div>
              <button className="btn btn-primary w-100" type="submit" disabled={loading}>
                {loading ? 'Saving...' : 'Save Changes'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

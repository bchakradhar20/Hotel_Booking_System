import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import API from '../api'

export default function BookRoom() {
  const { roomId } = useParams()
  const navigate = useNavigate()
  const [room, setRoom] = useState(null)
  const [form, setForm] = useState({ checkInDate: '', checkOutDate: '' })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const today = new Date().toISOString().split('T')[0]

  useEffect(() => {
    API.get(`/rooms/${roomId}`)
      .then(({ data }) => setRoom(data))
      .catch(() => setError('Room not found'))
  }, [roomId])

  const handleChange = e => {
    const updated = { ...form, [e.target.name]: e.target.value }
    // reset checkout if it's before new checkin
    if (e.target.name === 'checkInDate' && updated.checkOutDate && updated.checkOutDate <= e.target.value) {
      updated.checkOutDate = ''
    }
    setForm(updated)
  }

  const calcNights = () => {
    if (!form.checkInDate || !form.checkOutDate) return 0
    return Math.max(0, Math.floor((new Date(form.checkOutDate) - new Date(form.checkInDate)) / 86400000))
  }

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setSuccess('')
    if (form.checkInDate >= form.checkOutDate) {
      setError('Check-out date must be after check-in date')
      return
    }
    setLoading(true)
    try {
      await API.post('/reservations', { ...form, roomId: Number(roomId) })
      setSuccess('Reservation created successfully!')
      setTimeout(() => navigate('/my-reservations'), 1500)
    } catch (err) {
      setError(err.response?.data?.message || 'Booking failed')
    } finally {
      setLoading(false)
    }
  }

  if (!room) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  const nights = calcNights()
  const estimate = nights > 0 ? (nights * parseFloat(room.pricePerNight)).toFixed(2) : '—'

  return (
    <div className="row justify-content-center">
      <div className="col-md-6">
        <div className="card shadow">
          <div className="card-body p-4">
            <div className="d-flex align-items-center mb-3 gap-3">
              <button className="btn btn-sm btn-outline-secondary" onClick={() => navigate('/rooms')}>← Back</button>
              <h3 className="mb-0">Book Room <span style={{ color: 'var(--red)' }}>{room.roomNumber}</span></h3>
            </div>
            <p className="text-muted mb-3" style={{ fontSize: '0.9rem' }}>
              <span className="badge bg-primary">{room.roomType}</span> &nbsp;
              👥 {room.capacity} guests &nbsp;
              💰 <strong style={{ color: 'var(--red)' }}>${room.pricePerNight}</strong>/night
            </p>
            {error && <div className="alert alert-danger">{error}</div>}
            {success && <div className="alert alert-success">{success}</div>}
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Check-In Date</label>
                <input type="date" className="form-control" name="checkInDate"
                  value={form.checkInDate} onChange={handleChange}
                  min={today} required />
              </div>
              <div className="mb-3">
                <label className="form-label">Check-Out Date</label>
                <input type="date" className="form-control" name="checkOutDate"
                  value={form.checkOutDate} onChange={handleChange}
                  min={form.checkInDate ? (() => {
                    const d = new Date(form.checkInDate); d.setDate(d.getDate() + 1)
                    return d.toISOString().split('T')[0]
                  })() : today}
                  required />
              </div>
              {nights > 0 && (
                <div className="alert alert-info">
                  📆 {nights} night(s) × ${room.pricePerNight} = <strong>${estimate}</strong>
                  <br /><small className="text-muted">Final amount confirmed on booking</small>
                </div>
              )}
              <div className="d-flex gap-2">
                <button className="btn btn-primary flex-grow-1" type="submit" disabled={loading || !form.checkInDate || !form.checkOutDate}>
                  {loading ? 'Booking...' : '✅ Confirm Booking'}
                </button>
                <button className="btn btn-outline-secondary" type="button" onClick={() => navigate('/rooms')}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}

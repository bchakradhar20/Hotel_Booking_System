import { useEffect, useState } from 'react'
import API from '../api'
import ReservationCard from '../components/ReservationCard'

export default function MyReservations() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [msg, setMsg] = useState('')

  const fetchReservations = () => {
    setLoading(true)
    API.get('/reservations/my')
      .then(({ data }) => setReservations(data))
      .catch(() => setError('Failed to load reservations'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchReservations() }, [])

  const handleCancel = async id => {
    if (!window.confirm('Cancel this reservation?')) return
    try {
      await API.delete(`/reservations/my/${id}`)
      setMsg('Reservation cancelled successfully')
      fetchReservations()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel')
    }
  }

  if (loading) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  return (
    <div>
      <h2 className="mb-4">📋 My Reservations</h2>
      {error && <div className="alert alert-danger">{error}</div>}
      {msg && <div className="alert alert-success">{msg}</div>}
      {reservations.length === 0
        ? <p className="text-muted">You have no reservations yet.</p>
        : reservations.map(r => (
            <ReservationCard key={r.reservationId} reservation={r} onCancel={handleCancel} />
          ))
      }
    </div>
  )
}

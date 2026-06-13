import { useEffect, useState } from 'react'
import API from '../api'

export default function ManageReservations() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  const fetchReservations = () => {
    setLoading(true)
    API.get('/reservations')
      .then(({ data }) => setReservations(data))
      .catch(() => setError('Failed to load reservations'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchReservations() }, [])

  const handleDelete = async id => {
    if (!window.confirm('Delete this reservation?')) return
    try {
      await API.delete(`/reservations/${id}`)
      setMsg('Reservation deleted')
      fetchReservations()
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed')
    }
  }

  if (loading) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  return (
    <div>
      <h2 className="mb-4">📋 Manage Reservations</h2>
      {msg && <div className="alert alert-success">{msg}</div>}
      {error && <div className="alert alert-danger">{error}</div>}
      <div className="table-responsive">
        <table className="table table-hover table-bordered bg-white shadow-sm">
          <thead className="table-dark">
            <tr><th>ID</th><th>User</th><th>Room</th><th>Check-In</th><th>Check-Out</th><th>Total</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {reservations.map(r => (
              <tr key={r.reservationId}>
                <td>{r.reservationId}</td>
                <td>{r.username}</td>
                <td>{r.roomNumber}</td>
                <td>{r.checkInDate}</td>
                <td>{r.checkOutDate}</td>
                <td>${r.totalAmount}</td>
                <td>
                  <button className="btn btn-sm btn-danger" onClick={() => handleDelete(r.reservationId)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

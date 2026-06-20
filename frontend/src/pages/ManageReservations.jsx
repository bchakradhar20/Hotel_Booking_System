import { useEffect, useState } from 'react'
import API from '../api'

const EMPTY_EDIT = { checkInDate: '', checkOutDate: '' }

function validate(form) {
  if (!form.checkInDate) return 'Check-in date is required'
  if (!form.checkOutDate) return 'Check-out date is required'
  if (form.checkOutDate <= form.checkInDate) return 'Check-out must be after check-in'
  return ''
}

export default function ManageReservations() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')
  const [editTarget, setEditTarget] = useState(null) // reservation being edited
  const [editForm, setEditForm] = useState(EMPTY_EDIT)
  const [editError, setEditError] = useState('')
  const [saving, setSaving] = useState(false)

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
    setMsg(''); setError('')
    try {
      await API.delete(`/reservations/${id}`)
      setMsg('Reservation deleted')
      fetchReservations()
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed')
    }
  }

  const openEdit = r => {
    setEditTarget(r)
    setEditForm({ checkInDate: r.checkInDate, checkOutDate: r.checkOutDate })
    setEditError('')
  }

  const handleEditChange = e => {
    const updated = { ...editForm, [e.target.name]: e.target.value }
    if (e.target.name === 'checkInDate' && updated.checkOutDate && updated.checkOutDate <= e.target.value) {
      updated.checkOutDate = ''
    }
    setEditForm(updated)
    setEditError('')
  }

  const handleEditSubmit = async e => {
    e.preventDefault()
    const validationErr = validate(editForm)
    if (validationErr) { setEditError(validationErr); return }
    setSaving(true); setEditError('')
    try {
      await API.put(`/reservations/${editTarget.reservationId}`, {
        roomId: editTarget.roomId,
        checkInDate: editForm.checkInDate,
        checkOutDate: editForm.checkOutDate,
      })
      setMsg(`Reservation #${editTarget.reservationId} updated`)
      setEditTarget(null)
      fetchReservations()
    } catch (err) {
      setEditError(err.response?.data?.message || 'Update failed')
    } finally {
      setSaving(false)
    }
  }

  const minCheckout = editForm.checkInDate
    ? (() => { const d = new Date(editForm.checkInDate); d.setDate(d.getDate() + 1); return d.toISOString().split('T')[0] })()
    : ''

  if (loading) return <div className="text-center mt-5"><div className="spinner-border" /></div>

  return (
    <div>
      <h2 className="mb-4">📋 Manage Reservations</h2>
      {msg && <div className="alert alert-success alert-dismissible" onClick={() => setMsg('')}>{msg}</div>}
      {error && <div className="alert alert-danger alert-dismissible" onClick={() => setError('')}>{error}</div>}

      <div className="table-responsive">
        <table className="table table-hover table-bordered bg-white shadow-sm">
          <thead className="table-dark">
            <tr><th>ID</th><th>User</th><th>Room</th><th>Check-In</th><th>Check-Out</th><th>Total</th><th>Actions</th></tr>
          </thead>
          <tbody>
            {reservations.length === 0
              ? <tr><td colSpan={7} className="text-center text-muted">No reservations found</td></tr>
              : reservations.map(r => (
                <tr key={r.reservationId}>
                  <td>{r.reservationId}</td>
                  <td>{r.username}</td>
                  <td>{r.roomNumber}</td>
                  <td>{r.checkInDate}</td>
                  <td>{r.checkOutDate}</td>
                  <td>${r.totalAmount}</td>
                  <td className="d-flex gap-2">
                    <button className="btn btn-sm btn-warning" onClick={() => openEdit(r)}>Edit</button>
                    <button className="btn btn-sm btn-danger" onClick={() => handleDelete(r.reservationId)}>Delete</button>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      {/* Edit Modal */}
      {editTarget && (
        <div className="modal d-block" style={{ background: 'rgba(0,0,0,0.5)' }}>
          <div className="modal-dialog">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">Edit Reservation #{editTarget.reservationId}</h5>
                <button className="btn-close" onClick={() => setEditTarget(null)} />
              </div>
              <form onSubmit={handleEditSubmit}>
                <div className="modal-body">
                  <p className="text-muted mb-3">
                    <strong>User:</strong> {editTarget.username} &nbsp;|&nbsp;
                    <strong>Room:</strong> {editTarget.roomNumber}
                  </p>
                  {editError && <div className="alert alert-danger">{editError}</div>}
                  <div className="mb-3">
                    <label className="form-label">Check-In Date <span className="text-danger">*</span></label>
                    <input
                      type="date"
                      className="form-control"
                      name="checkInDate"
                      value={editForm.checkInDate}
                      onChange={handleEditChange}
                      required
                    />
                  </div>
                  <div className="mb-3">
                    <label className="form-label">Check-Out Date <span className="text-danger">*</span></label>
                    <input
                      type="date"
                      className="form-control"
                      name="checkOutDate"
                      value={editForm.checkOutDate}
                      onChange={handleEditChange}
                      min={minCheckout}
                      required
                    />
                  </div>
                </div>
                <div className="modal-footer">
                  <button type="button" className="btn btn-secondary" onClick={() => setEditTarget(null)}>Cancel</button>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Saving...' : 'Save Changes'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

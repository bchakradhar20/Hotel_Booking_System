import { useEffect, useState } from 'react'
import API from '../api'

const EMPTY_FORM = { roomNumber: '', roomType: 'STANDARD', pricePerNight: '', capacity: '', description: '' }

export default function ManageRooms() {
  const [rooms, setRooms] = useState([])
  const [form, setForm] = useState(EMPTY_FORM)
  const [editId, setEditId] = useState(null)
  const [msg, setMsg] = useState('')
  const [error, setError] = useState('')

  const fetchRooms = () => API.get('/rooms').then(({ data }) => setRooms(data))

  useEffect(() => { fetchRooms() }, [])

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async e => {
    e.preventDefault()
    setMsg('')
    setError('')
    if (!form.roomNumber.trim()) { setError('Room number is required'); return }
    if (!form.pricePerNight || parseFloat(form.pricePerNight) <= 0) { setError('Price must be greater than 0'); return }
    if (!form.capacity || parseInt(form.capacity) < 1) { setError('Capacity must be at least 1'); return }
    try {
      const payload = { ...form, pricePerNight: parseFloat(form.pricePerNight), capacity: parseInt(form.capacity) }
      if (editId) {
        await API.put(`/rooms/${editId}`, payload)
        setMsg('Room updated')
      } else {
        await API.post('/rooms', payload)
        setMsg('Room created')
      }
      setForm(EMPTY_FORM)
      setEditId(null)
      fetchRooms()
    } catch (err) {
      setError(err.response?.data?.message || 'Operation failed')
    }
  }

  const startEdit = room => {
    setEditId(room.roomId)
    setForm({ roomNumber: room.roomNumber, roomType: room.roomType, pricePerNight: room.pricePerNight, capacity: room.capacity, description: room.description || '' })
    window.scrollTo(0, 0)
  }

  const handleDelete = async id => {
    if (!window.confirm('Delete this room?')) return
    try {
      await API.delete(`/rooms/${id}`)
      setMsg('Room deleted')
      fetchRooms()
    } catch (err) {
      setError(err.response?.data?.message || 'Delete failed')
    }
  }

  return (
    <div>
      <h2 className="mb-4">🛏️ Manage Rooms</h2>
      {msg && <div className="alert alert-success">{msg}</div>}
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="card mb-4 shadow-sm">
        <div className="card-body">
          <h5>{editId ? 'Edit Room' : 'Add New Room'}</h5>
          <form onSubmit={handleSubmit} className="row g-2">
            <div className="col-md-2">
              <input className="form-control" placeholder="Room No." name="roomNumber" value={form.roomNumber} onChange={handleChange} required />
            </div>
            <div className="col-md-2">
              <select className="form-select" name="roomType" value={form.roomType} onChange={handleChange}>
                {['STANDARD', 'DELUXE', 'SUITE', 'FAMILY'].map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div className="col-md-2">
              <input className="form-control" type="number" placeholder="Price/night" name="pricePerNight" value={form.pricePerNight} onChange={handleChange} required min="0.01" step="0.01" />
            </div>
            <div className="col-md-1">
              <input className="form-control" type="number" placeholder="Cap." name="capacity" value={form.capacity} onChange={handleChange} required min="1" />
            </div>
            <div className="col-md-3">
              <input className="form-control" placeholder="Description" name="description" value={form.description} onChange={handleChange} />
            </div>
            <div className="col-md-2 d-flex gap-2">
              <button className="btn btn-primary flex-grow-1" type="submit">{editId ? 'Update' : 'Add'}</button>
              {editId && <button className="btn btn-secondary" type="button" onClick={() => { setEditId(null); setForm(EMPTY_FORM) }}>Cancel</button>}
            </div>
          </form>
        </div>
      </div>

      <table className="table table-hover table-bordered bg-white shadow-sm">
        <thead className="table-dark">
          <tr><th>No.</th><th>Type</th><th>Price/Night</th><th>Capacity</th><th>Description</th><th>Actions</th></tr>
        </thead>
        <tbody>
          {rooms.map(r => (
            <tr key={r.roomId}>
              <td>{r.roomNumber}</td>
              <td><span className="badge bg-secondary">{r.roomType}</span></td>
              <td>${r.pricePerNight}</td>
              <td>{r.capacity}</td>
              <td>{r.description}</td>
              <td>
                <button className="btn btn-sm btn-warning me-2" onClick={() => startEdit(r)}>Edit</button>
                <button className="btn btn-sm btn-danger" onClick={() => handleDelete(r.roomId)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

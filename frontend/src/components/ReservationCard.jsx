export default function ReservationCard({ reservation, onCancel }) {
  return (
    <div className="card mb-3" style={{ borderLeft: '4px solid var(--red)' }}>
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-center">
          <div>
            <h6 className="mb-1">Room <strong style={{ color: 'var(--red)' }}>{reservation.roomNumber}</strong></h6>
            <p className="mb-1 text-muted" style={{ fontSize: '0.9rem' }}>
              📅 {reservation.checkInDate} → {reservation.checkOutDate}
            </p>
            <p className="mb-0" style={{ fontSize: '0.9rem' }}>
              💵 Total: <strong className="text-success">${reservation.totalAmount}</strong>
            </p>
          </div>
          {onCancel && (
            <button
              className="btn btn-outline-danger btn-sm"
              onClick={() => onCancel(reservation.reservationId)}
            >
              Cancel
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

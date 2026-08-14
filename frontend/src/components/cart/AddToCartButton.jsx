import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { addCartItem } from '../../services/cartService'

export default function AddToCartButton({ product }) {
  const { isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [busy, setBusy] = useState(false)
  const [feedback, setFeedback] = useState(null)

  const handleAdd = async () => {
    setFeedback(null)
    if (!isAuthenticated) {
      navigate('/login', { state: { from: location } })
      return
    }
    setBusy(true)
    try {
      await addCartItem(product.id, 1)
      setFeedback({ type: 'success', text: 'Added to cart' })
    } catch (err) {
      setFeedback({
        type: 'error',
        text: err.response?.data?.detail || 'Unable to add to cart. Please try again.',
      })
    } finally {
      setBusy(false)
    }
  }

  return (
    <div>
      <button
        type="button"
        onClick={handleAdd}
        disabled={busy}
        className="rounded-md bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 disabled:opacity-60"
      >
        {busy ? 'Adding...' : 'Add to cart'}
      </button>
      {feedback ? (
        <p className={`mt-2 text-sm ${feedback.type === 'success' ? 'text-green-600' : 'text-red-600'}`}>
          {feedback.text}
        </p>
      ) : null}
    </div>
  )
}

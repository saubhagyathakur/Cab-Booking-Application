import React, { useState } from 'react';
import { ratingApi } from '../services/api';
import { Star, X } from 'lucide-react';

const RatingModal = ({ ride, onClose, onSuccess }) => {
  const [score, setScore] = useState(5);
  const [hoverScore, setHoverScore] = useState(0);
  const [comments, setComments] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setError('');
      const res = await ratingApi.submitRating({
        rideId: ride.id,
        score,
        comments,
      });
      if (res.data.success) {
        onSuccess();
        onClose();
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to submit rating');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div className="bg-white rounded-2xl max-w-sm w-full p-6 shadow-xl border border-slate-200 relative">
        <button
          onClick={onClose}
          className="absolute right-4 top-4 p-1 text-slate-400 hover:text-slate-600 rounded-lg"
        >
          <X className="w-4 h-4" />
        </button>

        <div className="text-center space-y-1 mb-5">
          <h3 className="text-lg font-bold text-slate-900">Rate your trip</h3>
          <p className="text-xs text-slate-500">How was your experience with {ride.driverName || 'your driver'}?</p>
        </div>

        {error && (
          <div className="p-2 mb-3 bg-red-50 text-red-700 text-xs rounded-lg border border-red-200">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Star Rating Selector */}
          <div className="flex justify-center gap-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                type="button"
                key={star}
                onMouseEnter={() => setHoverScore(star)}
                onMouseLeave={() => setHoverScore(0)}
                onClick={() => setScore(star)}
                className="p-1 focus:outline-none transition-transform hover:scale-110"
              >
                <Star
                  className={`w-8 h-8 ${
                    (hoverScore || score) >= star
                      ? 'fill-amber-400 text-amber-400'
                      : 'text-slate-200'
                  }`}
                />
              </button>
            ))}
          </div>

          <div className="text-center text-xs font-semibold text-slate-600">
            {score === 5 && 'Outstanding trip!'}
            {score === 4 && 'Very good experience'}
            {score === 3 && 'Average experience'}
            {score === 2 && 'Below expectations'}
            {score === 1 && 'Poor experience'}
          </div>

          {/* Feedback comments */}
          <div>
            <textarea
              rows={3}
              placeholder="Leave compliments or feedback..."
              value={comments}
              onChange={(e) => setComments(e.target.value)}
              className="w-full text-xs p-3 bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:border-emerald-500"
            />
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full py-3 px-4 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-md transition-all"
          >
            {submitting ? 'Submitting...' : 'Submit Rating'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default RatingModal;

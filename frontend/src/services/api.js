import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to inject JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('cab_jwt_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export const authApi = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (data) => api.post('/auth/register', data),
  getMe: () => api.get('/auth/me'),
};

export const rideApi = {
  getEstimate: (data) => api.post('/rides/estimate', data),
  requestRide: (data) => api.post('/rides/request', data),
  getRide: (rideId) => api.get(`/rides/${rideId}`),
  getMyRides: () => api.get('/rides/my-rides'),
  getActiveRide: () => api.get('/rides/active'),
  cancelRide: (rideId, reason) => api.post(`/rides/${rideId}/cancel`, null, { params: { reason } }),
};

export const driverApi = {
  getNearby: (lat, lng, radiusKm, vehicleType) => 
    api.get('/drivers/nearby', { params: { lat, lng, radiusKm, vehicleType } }),
  getProfile: () => api.get('/drivers/me'),
  updateStatus: (status) => api.patch('/drivers/status', { status }),
  updateLocation: (lat, lng) => api.patch('/drivers/location', { latitude: lat, longitude: lng }),
  getAvailableRequests: () => api.get('/drivers/available-requests'),
  getActiveRide: () => api.get('/drivers/active-ride'),
  getMyRides: () => api.get('/drivers/my-rides'),
  acceptRide: (rideId) => api.post(`/drivers/rides/${rideId}/accept`),
  arrived: (rideId) => api.post(`/drivers/rides/${rideId}/arrived`),
  startRide: (rideId, otp) => api.post(`/drivers/rides/${rideId}/start`, null, { params: { otp } }),
  completeRide: (rideId) => api.post(`/drivers/rides/${rideId}/complete`),
};

export const paymentApi = {
  createIntent: (rideId) => api.post('/payments/create-intent', { rideId }),
  confirmPayment: (rideId, paymentIntentId) => 
    api.post('/payments/confirm', null, { params: { rideId, paymentIntentId } }),
  getPayment: (rideId) => api.get(`/payments/ride/${rideId}`),
};

export const ratingApi = {
  submitRating: (data) => api.post('/ratings', data),
  getDriverRatings: (driverId) => api.get(`/ratings/driver/${driverId}`),
};

export const promoApi = {
  validate: (code, fare) => api.get('/promos/validate', { params: { code, fare } }),
  getActive: () => api.get('/promos/active'),
};

export const receiptApi = {
  downloadUrl: (rideId) => `${API_BASE_URL}/receipts/${rideId}/download`,
  emailReceipt: (rideId, email) => api.post(`/receipts/${rideId}/email`, null, { params: { email } }),
};

export const adminApi = {
  getDashboard: () => api.get('/admin/dashboard'),
  getUsers: () => api.get('/admin/users'),
  getDrivers: () => api.get('/admin/drivers'),
  getRides: () => api.get('/admin/rides'),
  getTickets: () => api.get('/admin/tickets'),
  resolveTicket: (ticketId) => api.post(`/admin/tickets/${ticketId}/resolve`),
};

export default api;

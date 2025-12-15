import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {

    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  return config;
});

// Auth endpoints
export const authAPI = {
  register: (data: any) => api.post('/auth/register', data),
  login: (data: any) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
};

// User endpoints
export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data: any) => api.put('/user/profile', data),
  getAccount: () => api.get('/user/account'),
  createTransaction: (data: any) => api.post('/user/transaction', data),
  getTransactions: () => api.get('/user/transactions'),
  getSentTransactions: () => api.get('/user/transactions/sent'),
  getReceivedTransactions: () => api.get('/user/transactions/received'),
  getTransaction: (id: number) => api.get(`/user/transaction/${id}`),
};

// Admin endpoints
export const adminAPI = {
  getUsers: () => api.get('/admin/users'),
  getUserById: (id: number) => api.get(`/admin/users/${id}`),
  searchUsers: (query: string) => api.get('/admin/users/search', { params: { query } }),
  activateUser: (id: number) => api.put(`/admin/users/${id}/activate`),
  deactivateUser: (id: number) => api.put(`/admin/users/${id}/deactivate`),
  deleteUser: (id: number) => api.delete(`/admin/users/${id}`),
  getTransactions: () => api.get('/admin/transactions'),
  getTransactionById: (id: number) => api.get(`/admin/transactions/${id}`),
  getTransactionsByStatus: (status: string) => api.get(`/admin/transactions/status/${status}`),
  getTransactionsByUserId: (userId: number) => api.get(`/admin/transactions/user/${userId}`),
  updateTransactionStatus: (id: number, status: string) =>
    api.put(`/admin/transactions/${id}/status`, null, { params: { status } }),
  reverseTransaction: (id: number) => api.post(`/admin/transactions/${id}/reverse`),
};

export default api;

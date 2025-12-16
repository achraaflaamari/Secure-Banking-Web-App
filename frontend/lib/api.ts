import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

const getCookieValue = (name: string): string | null => {
  if (typeof document === 'undefined') return null;
  const match = document.cookie.match(new RegExp(`(^|; )${name}=([^;]*)`));
  return match ? decodeURIComponent(match[2]) : null;
};

export const getApiErrorMessage = (err: any): string | null => {
  const data = err?.response?.data;
  if (data) {
    if (typeof data.error === 'string' && data.error.trim()) return data.error;
    if (typeof data.message === 'string' && data.message.trim()) return data.message;
  }
  if (typeof err?.message === 'string' && err.message.trim()) return err.message;
  return null;
};

const api = axios.create({
  baseURL: API_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    const method = (config.method || 'get').toLowerCase();
    const isMutating = ['post', 'put', 'patch', 'delete'].includes(method);
    if (isMutating) {
      const csrf = getCookieValue('XSRF-TOKEN');
      if (csrf) {
        config.headers['X-XSRF-TOKEN'] = csrf;
      }
    }
  }
  return config;
});

// Auth endpoints
export const authAPI = {
  csrf: () => api.get('/csrf'),
  register: (data: any) => api.post('/auth/register', data),
  verifyRegister: (data: any) => api.post('/auth/register/verify', data),
  login: (data: any) => api.post('/auth/login', data),
  verifyLogin: (data: any) => api.post('/auth/login/verify', data),
  forgotPassword: (data: any) => api.post('/auth/forgot-password', data),
  resetPassword: (data: any) => api.post('/auth/reset-password', data),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
};

// User endpoints
export const userAPI = {
  getProfile: () => api.get('/user/profile'),
  updateProfile: (data: any) => api.put('/user/profile', data),
  requestPasswordChangeOtp: (data: any) => api.post('/user/password/change/request', data),
  verifyPasswordChangeOtp: (data: any) => api.post('/user/password/change/verify', data),
  getAccount: () => api.get('/user/account'),
  initiateTransaction: (data: any) => api.post('/user/transaction/initiate', data),
  verifyTransaction: (data: any) => api.post('/user/transaction/verify', data),
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
  reverseTransaction: (id: number) => api.post(`/admin/transactions/${id}/reverse`),
};

export default api;

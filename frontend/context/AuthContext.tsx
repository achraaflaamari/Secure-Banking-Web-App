'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '@/lib/api';

const extractErrorMessage = (err: any): string | null => {
  const data = err?.response?.data;
  if (data) {
    if (typeof data.error === 'string' && data.error.trim()) return data.error;
    if (typeof data.message === 'string' && data.message.trim()) return data.message;
  }
  if (typeof err?.message === 'string' && err.message.trim()) return err.message;
  return null;
};

interface User {
  username: string;
  role: string;
  token: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (password: string, fullname: string, email: string, phone: string, birthday: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const bootstrap = async () => {
      try {
        await authAPI.csrf();

        const token = localStorage.getItem('token');
        if (!token) {
          setUser(null);
          return;
        }

        const me = await authAPI.me();
        const { username, role } = me.data;
        localStorage.setItem('username', username);
        localStorage.setItem('role', role);
        setUser({ token, username, role });
      } catch (e) {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('role');
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    bootstrap();
  }, []);

  const login = async (email: string, password: string) => {
    try {
      const response = await authAPI.login({ email, password });
      const { token, username: responseUsername, role, message } = response.data;

      if (!token && message === 'OTP_REQUIRED') {
        localStorage.setItem('pending_otp_email', String(email || '').trim().toLowerCase());
        throw new Error('OTP_REQUIRED');
      }

      localStorage.setItem('token', token);
      localStorage.setItem('username', responseUsername);
      localStorage.setItem('role', role);

      setUser({ token, username: responseUsername, role });
    } catch (error) {
      if (error instanceof Error && (error.message === 'OTP_REQUIRED' || error.message === 'OTP_SENT')) {
        throw error;
      }
      throw new Error(extractErrorMessage(error) || 'Login failed');
    }
  };

  const register = async (
    password: string,
    fullname: string,
    email: string,
    phone: string,
    birthday: string
  ) => {
    try {
      const response = await authAPI.register({
        password,
        fullname,
        email,
        phone,
        birthday,
      });

      const { token, username: responseUsername, role, message } = response.data;

      if (!token && message === 'OTP_SENT') {
        localStorage.setItem('pending_otp_email', String(email || '').trim().toLowerCase());
        throw new Error('OTP_SENT');
      }

      localStorage.setItem('token', token);
      localStorage.setItem('username', responseUsername);
      localStorage.setItem('role', role);

      setUser({ token, username: responseUsername, role });
    } catch (error) {
      if (error instanceof Error && (error.message === 'OTP_SENT' || error.message === 'OTP_REQUIRED')) {
        throw error;
      }
      throw new Error(extractErrorMessage(error) || 'Registration failed');
    }
  };

  const logout = () => {
    try {
      authAPI.logout();
    } finally {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('role');
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '@/lib/api';

interface User {
  username: string;
  role: string;
  token: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, password: string, fullname: string, email: string, phone: string) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user is already logged in
    const token = localStorage.getItem('token');
    const username = localStorage.getItem('username');
    const role = localStorage.getItem('role');

    if (token && username && role) {
      setUser({ token, username, role });
    }
    setLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      const response = await authAPI.login({ username, password });
      const { token, username: responseUsername, role } = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('username', responseUsername);
      localStorage.setItem('role', role);

      setUser({ token, username: responseUsername, role });
    } catch (error) {
      throw new Error('Login failed');
    }
  };

  const register = async (
    username: string,
    password: string,
    fullname: string,
    email: string,
    phone: string
  ) => {
    try {
      const response = await authAPI.register({
        username,
        password,
        fullname,
        email,
        phone,
      });

      const { token, username: responseUsername, role } = response.data;

      localStorage.setItem('token', token);
      localStorage.setItem('username', responseUsername);
      localStorage.setItem('role', role);

      setUser({ token, username: responseUsername, role });
    } catch (error) {
      throw new Error('Registration failed');
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    setUser(null);
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

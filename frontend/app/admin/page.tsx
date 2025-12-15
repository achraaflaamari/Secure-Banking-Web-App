'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { adminAPI } from '@/lib/api';

interface UserDTO {
  id: number;
  username: string;
  role: string;
  fullname?: string;
  email?: string;
  phone?: string;
  isActive?: boolean;
}

interface TransactionDTO {
  id: number;
  senderUsername: string;
  receiverUsername: string;
  amount: number;
  createdAt: string;
  status: string;
}

export default function AdminDashboardPage() {
  const router = useRouter();
  const { user, isAuthenticated, loading, logout } = useAuth();

  const [statsLoading, setStatsLoading] = useState(true);
  const [usersCount, setUsersCount] = useState<number>(0);
  const [txCount, setTxCount] = useState<number>(0);
  const [error, setError] = useState('');

  const isAdmin = useMemo(() => user?.role === 'ADMIN', [user?.role]);

  useEffect(() => {
    if (loading) return;

    if (!isAuthenticated) {
      router.push('/admin/login');
      return;
    }

    if (!isAdmin) {
      router.push('/dashboard');
      return;
    }

    const loadStats = async () => {
      setStatsLoading(true);
      setError('');
      try {
        const [usersRes, txRes] = await Promise.all([
          adminAPI.getUsers(),
          adminAPI.getTransactions(),
        ]);

        const users: UserDTO[] = usersRes.data || [];
        const txs: TransactionDTO[] = txRes.data || [];

        setUsersCount(users.length);
        setTxCount(txs.length);
      } catch (e) {
        setError('Failed to load admin dashboard stats');
      } finally {
        setStatsLoading(false);
      }
    };

    loadStats();
  }, [isAuthenticated, isAdmin, loading, router]);

  const handleLogout = () => {
    logout();
    router.push('/admin/login');
  };

  if (loading || statsLoading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-gray-600">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div>
            <h1 className="text-xl font-bold text-gray-800">Admin Panel</h1>
            <p className="text-sm text-gray-600">Logged in as: {user?.username}</p>
          </div>
          <div className="flex items-center gap-4">
            <Link href="/admin/users" className="text-indigo-700 hover:text-indigo-800 font-semibold">
              Users
            </Link>
            <Link href="/admin/transactions" className="text-indigo-700 hover:text-indigo-800 font-semibold">
              Transactions
            </Link>
            <Link href="/admin/profile" className="text-indigo-700 hover:text-indigo-800 font-semibold">
              Profile
            </Link>
            <button
              onClick={handleLogout}
              className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition"
            >
              Logout
            </button>
          </div>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Total Users</p>
            <p className="text-3xl font-bold text-gray-800">{usersCount}</p>
          </div>
          <div className="bg-white rounded-lg shadow p-6">
            <p className="text-sm text-gray-600">Total Transactions</p>
            <p className="text-3xl font-bold text-gray-800">{txCount}</p>
          </div>
        </div>

        <div className="mt-8 bg-white rounded-lg shadow p-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-2">Quick actions</h2>
          <div className="flex flex-col sm:flex-row gap-3">
            <Link
              href="/admin/users"
              className="bg-indigo-700 text-white px-4 py-2 rounded-lg hover:bg-indigo-800 transition text-center"
            >
              Manage users
            </Link>
            <Link
              href="/admin/transactions"
              className="bg-indigo-700 text-white px-4 py-2 rounded-lg hover:bg-indigo-800 transition text-center"
            >
              Manage transactions
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

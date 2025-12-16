'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { adminAPI, getApiErrorMessage } from '@/lib/api';

interface UserDTO {
  id: number;
  username: string;
  role: string;
  fullname?: string;
  email?: string;
  phone?: string;
  isActive?: boolean;
}

export default function AdminUsersPage() {
  const router = useRouter();
  const { user, isAuthenticated, loading } = useAuth();
  const isAdmin = useMemo(() => user?.role === 'ADMIN', [user?.role]);

  const [query, setQuery] = useState('');
  const [users, setUsers] = useState<UserDTO[]>([]);
  const [fetching, setFetching] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [error, setError] = useState('');

  const loadUsers = async (search?: string) => {
    setFetching(true);
    setError('');
    try {
      const res = search && search.trim().length > 0
        ? await adminAPI.searchUsers(search.trim())
        : await adminAPI.getUsers();
      setUsers(res.data || []);
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to load users');
    } finally {
      setFetching(false);
    }
  };

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
    loadUsers();
  }, [isAuthenticated, isAdmin, loading, router]);

  const handleActivate = async (id: number) => {
    setActionLoading(id);
    setError('');
    try {
      await adminAPI.activateUser(id);
      await loadUsers(query);
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to activate user');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDeactivate = async (id: number) => {
    setActionLoading(id);
    setError('');
    try {
      await adminAPI.deactivateUser(id);
      await loadUsers(query);
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to deactivate user');
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async (id: number) => {
    const target = users.find((u) => u.id === id);
    if (target?.username && user?.username && target.username === user.username) {
      setError('You cannot delete your own account while logged in');
      return;
    }

    const ok = window.confirm('Are you sure you want to delete (deactivate) this user?');
    if (!ok) return;

    setActionLoading(id);
    setError('');
    try {
      await adminAPI.deleteUser(id);
      setUsers((prev) => prev.filter((u) => u.id !== id));
      await loadUsers(query);
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to delete user');
    } finally {
      setActionLoading(null);
    }
  };

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    await loadUsers(query);
  };

  if (loading || fetching) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-gray-600">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-800">User Management</h1>
            <p className="text-sm text-gray-600">Search, activate/deactivate, and soft-delete users</p>
          </div>
          <Link href="/admin" className="text-indigo-700 hover:text-indigo-800 font-semibold">
            ← Back to Admin Dashboard
          </Link>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        <form onSubmit={handleSearch} className="bg-white rounded-lg shadow p-4 mb-4 flex gap-2">
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
            placeholder="Search by username"
          />
          <button
            type="submit"
            className="bg-indigo-700 text-white px-4 py-2 rounded-lg hover:bg-indigo-800 transition"
          >
            Search
          </button>
          <button
            type="button"
            onClick={() => {
              setQuery('');
              loadUsers('');
            }}
            className="bg-gray-100 text-gray-800 px-4 py-2 rounded-lg hover:bg-gray-200 transition"
          >
            Reset
          </button>
        </form>

        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">ID</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Username</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Role</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Name</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Email</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Active</th>
                  <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id} className="border-t">
                    <td className="px-4 py-3 text-sm text-gray-800">{u.id}</td>
                    <td className="px-4 py-3 text-sm text-gray-800">{u.username}</td>
                    <td className="px-4 py-3 text-sm text-gray-800">{u.role}</td>
                    <td className="px-4 py-3 text-sm text-gray-800">{u.fullname || '-'}</td>
                    <td className="px-4 py-3 text-sm text-gray-800">{u.email || '-'}</td>
                    <td className="px-4 py-3 text-sm text-gray-800">{u.isActive ? 'Yes' : 'No'}</td>
                    <td className="px-4 py-3 text-sm">
                      <div className="flex flex-wrap gap-2">
                        <button
                          onClick={() => handleActivate(u.id)}
                          disabled={actionLoading === u.id}
                          className="bg-green-600 text-white px-3 py-1 rounded hover:bg-green-700 disabled:opacity-50"
                        >
                          Activate
                        </button>
                        <button
                          onClick={() => handleDeactivate(u.id)}
                          disabled={actionLoading === u.id}
                          className="bg-yellow-600 text-white px-3 py-1 rounded hover:bg-yellow-700 disabled:opacity-50"
                        >
                          Deactivate
                        </button>
                        <button
                          onClick={() => handleDelete(u.id)}
                          disabled={actionLoading === u.id}
                          className="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 disabled:opacity-50"
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {users.length === 0 && (
            <div className="p-6 text-gray-600">No users found</div>
          )}
        </div>
      </div>
    </div>
  );
}

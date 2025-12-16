'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { adminAPI, getApiErrorMessage } from '@/lib/api';

interface TransactionDTO {
  id: number;
  senderUsername: string;
  receiverUsername: string;
  amount: number;
  createdAt: string;
  status: string;
}

export default function AdminTransactionsPage() {
  const router = useRouter();
  const { user, isAuthenticated, loading } = useAuth();
  const isAdmin = useMemo(() => user?.role === 'ADMIN', [user?.role]);

  const [transactions, setTransactions] = useState<TransactionDTO[]>([]);
  const [fetching, setFetching] = useState(true);
  const [error, setError] = useState('');

  const [statusFilter, setStatusFilter] = useState('');
  const [userIdFilter, setUserIdFilter] = useState('');

  const [selectedTx, setSelectedTx] = useState<TransactionDTO | null>(null);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  const loadTransactions = async () => {
    setFetching(true);
    setError('');

    try {
      if (statusFilter.trim()) {
        const res = await adminAPI.getTransactionsByStatus(statusFilter.trim());
        setTransactions(res.data || []);
      } else if (userIdFilter.trim()) {
        const parsed = Number(userIdFilter.trim());
        if (!Number.isFinite(parsed)) {
          setError('User ID must be a number');
          setTransactions([]);
          return;
        }
        const res = await adminAPI.getTransactionsByUserId(parsed);
        setTransactions(res.data || []);
      } else {
        const res = await adminAPI.getTransactions();
        setTransactions(res.data || []);
      }
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to load transactions');
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
    loadTransactions();
  }, [isAuthenticated, isAdmin, loading, router]);

  const openDetails = async (id: number) => {
    setError('');
    try {
      const res = await adminAPI.getTransactionById(id);
      setSelectedTx(res.data);
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to load transaction details');
    }
  };

  const handleReverse = async (id: number) => {
    setActionLoading(id);
    setError('');
    try {
      await adminAPI.reverseTransaction(id);
      await openDetails(id);
      await loadTransactions();
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to reverse transaction');
    } finally {
      setActionLoading(null);
    }
  };

  const handleApplyFilter = async (e: React.FormEvent) => {
    e.preventDefault();
    await loadTransactions();
  };

  const handleReset = async () => {
    setStatusFilter('');
    setUserIdFilter('');
    await loadTransactions();
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
            <h1 className="text-2xl font-bold text-gray-800">Transaction Management</h1>
            <p className="text-sm text-gray-600">Filter, inspect, and reverse transactions</p>
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

        <form onSubmit={handleApplyFilter} className="bg-white rounded-lg shadow p-4 mb-4 grid grid-cols-1 md:grid-cols-3 gap-3">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <input
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="COMPLETED"
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">User ID</label>
            <input
              value={userIdFilter}
              onChange={(e) => setUserIdFilter(e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="1"
            />
          </div>
          <div className="flex items-end gap-2">
            <button
              type="submit"
              className="bg-indigo-700 text-white px-4 py-2 rounded-lg hover:bg-indigo-800 transition"
            >
              Apply
            </button>
            <button
              type="button"
              onClick={handleReset}
              className="bg-gray-100 text-gray-800 px-4 py-2 rounded-lg hover:bg-gray-200 transition"
            >
              Reset
            </button>
          </div>
        </form>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2 bg-white rounded-lg shadow overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">ID</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">From</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">To</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Amount</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Date</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Status</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx) => (
                    <tr key={tx.id} className="border-t">
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.id}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.senderUsername}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.receiverUsername}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">${Number(tx.amount).toFixed(2)}</td>
                      <td className="px-4 py-3 text-sm text-gray-600">{new Date(tx.createdAt).toLocaleString()}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.status}</td>
                      <td className="px-4 py-3 text-sm">
                        <button
                          onClick={() => openDetails(tx.id)}
                          className="bg-indigo-700 text-white px-3 py-1 rounded hover:bg-indigo-800"
                        >
                          Details
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {transactions.length === 0 && (
              <div className="p-6 text-gray-600">No transactions found</div>
            )}
          </div>

          <div className="bg-white rounded-lg shadow p-4">
            <h2 className="text-lg font-semibold text-gray-800 mb-2">Transaction details</h2>
            {selectedTx ? (
              <div className="space-y-2 text-sm text-gray-700">
                <div><span className="font-semibold">ID:</span> {selectedTx.id}</div>
                <div><span className="font-semibold">From:</span> {selectedTx.senderUsername}</div>
                <div><span className="font-semibold">To:</span> {selectedTx.receiverUsername}</div>
                <div><span className="font-semibold">Amount:</span> ${Number(selectedTx.amount).toFixed(2)}</div>
                <div><span className="font-semibold">Date:</span> {new Date(selectedTx.createdAt).toLocaleString()}</div>
                <div><span className="font-semibold">Status:</span> {selectedTx.status}</div>

                <div className="pt-3 border-t">
                  <button
                    onClick={() => handleReverse(selectedTx.id)}
                    disabled={actionLoading === selectedTx.id}
                    className="mt-2 w-full bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition disabled:opacity-50"
                  >
                    Reverse
                  </button>

                  <button
                    onClick={() => setSelectedTx(null)}
                    className="mt-2 w-full bg-gray-100 text-gray-800 px-4 py-2 rounded-lg hover:bg-gray-200 transition"
                  >
                    Close
                  </button>
                </div>
              </div>
            ) : (
              <div className="text-sm text-gray-600">Select a transaction to view details.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { userAPI } from '@/lib/api';

interface Transaction {
  id: number;
  senderUsername: string;
  receiverUsername: string;
  amount: number;
  createdAt: string;
  status: string;
}

export default function TransactionsPage() {
  const router = useRouter();
  const { isAuthenticated, loading } = useAuth();

  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [fetching, setFetching] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (loading) return;
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    const fetchTx = async () => {
      try {
        const res = await userAPI.getTransactions();
        setTransactions(res.data || []);
      } catch (e) {
        setError('Failed to load transactions');
      } finally {
        setFetching(false);
      }
    };

    fetchTx();
  }, [isAuthenticated, loading, router]);

  if (loading || fetching) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-gray-600">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Transaction History</h1>
          <div className="flex items-center gap-4">
            <Link href="/transaction" className="text-indigo-600 hover:text-indigo-700 font-semibold">
              Send money
            </Link>
            <Link href="/dashboard" className="text-indigo-600 hover:text-indigo-700 font-semibold">
              ← Back to Dashboard
            </Link>
          </div>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        <div className="bg-white rounded-lg shadow overflow-hidden">
          {transactions.length === 0 ? (
            <div className="p-6 text-gray-600">No transactions yet</div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">From</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">To</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Amount</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Date</th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-gray-700">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx) => (
                    <tr key={tx.id} className="border-t hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.senderUsername}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.receiverUsername}</td>
                      <td className="px-4 py-3 text-sm font-semibold text-gray-800">${Number(tx.amount).toFixed(2)}</td>
                      <td className="px-4 py-3 text-sm text-gray-600">{new Date(tx.createdAt).toLocaleString()}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

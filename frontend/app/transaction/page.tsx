'use client';

import { useEffect, useState, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { userAPI } from '@/lib/api';

export default function TransactionPage() {
  const router = useRouter();
  const { isAuthenticated, loading } = useAuth();

  const [receiverRip, setReceiverRip] = useState('');
  const [amount, setAmount] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    if (loading) return;
    if (!isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, loading, router]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!receiverRip || !amount) {
      setError('Please fill in all fields');
      return;
    }

    const parsed = Number(amount);
    if (!Number.isFinite(parsed) || parsed <= 0) {
      setError('Amount must be a positive number');
      return;
    }

    setSubmitting(true);
    try {
      const res = await userAPI.initiateTransaction({ receiverRip, amount: parsed });
      const { transactionId, message } = res.data;
      if (message === 'OTP_REQUIRED' && transactionId) {
        router.push(`/transaction/verify?id=${transactionId}`);
        return;
      }
      setError('Failed to initiate transaction');
    } catch (e) {
      setError('Failed to initiate transaction');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-gray-600">Loading...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Send Money</h1>
          <Link href="/dashboard" className="text-indigo-600 hover:text-indigo-700 font-semibold">
            ← Back to Dashboard
          </Link>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
            {error}
          </div>
        )}

        {success && (
          <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4">
            {success}
          </div>
        )}

        <div className="bg-white rounded-lg shadow p-6">
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Receiver RIP</label>
              <input
                value={receiverRip}
                onChange={(e) => setReceiverRip(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="RIP"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Amount</label>
              <input
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="100"
                required
              />
            </div>

            <button
              type="submit"
              disabled={submitting}
              className="w-full bg-indigo-600 text-white py-2 rounded-lg font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
            >
              {submitting ? 'Sending...' : 'Send'}
            </button>
          </form>

          <div className="mt-4 text-sm">
            <Link href="/transactions" className="text-indigo-600 hover:text-indigo-700 font-semibold">
              View transaction history →
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

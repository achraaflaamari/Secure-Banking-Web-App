'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { userAPI } from '@/lib/api';
import Link from 'next/link';
import { LogOut, Send, History, User, Wallet } from 'lucide-react';

interface Profile {
  fullname: string;
  email: string;
  phone: string;
}

interface Account {
  solde: number;
}

interface Transaction {
  id: number;
  senderUsername: string;
  receiverUsername: string;
  amount: number;
  createdAt: string;
  status: string;
}

export default function DashboardPage() {
  const router = useRouter();
  const { user, logout, isAuthenticated, loading: authLoading } = useAuth();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [account, setAccount] = useState<Account | null>(null);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (authLoading) return;
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    const fetchData = async () => {
      try {
        const [profileRes, accountRes, transactionsRes] = await Promise.all([
          userAPI.getProfile(),
          userAPI.getAccount(),
          userAPI.getTransactions(),
        ]);

        setProfile(profileRes.data);
        setAccount(accountRes.data);
        setTransactions(transactionsRes.data.slice(0, 5)); // Show last 5 transactions
      } catch (err: any) {
        setError('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated, authLoading, router]);

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navigation */}
      <nav className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div className="flex items-center">
            <Wallet className="w-8 h-8 text-indigo-600 mr-2" />
            <h1 className="text-2xl font-bold text-gray-800">KMA Bank</h1>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-gray-600">Welcome, {user?.username}</span>
            <button
              onClick={handleLogout}
              className="flex items-center bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 transition"
            >
              <LogOut className="w-4 h-4 mr-2" />
              Logout
            </button>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
            {error}
          </div>
        )}

        {/* Account Balance Card */}
        <div className="bg-gradient-to-r from-indigo-600 to-indigo-800 text-white rounded-lg shadow-lg p-8 mb-8">
          <p className="text-indigo-100 mb-2">Account Balance</p>
          <h2 className="text-4xl font-bold mb-4">${account?.solde?.toFixed(2) || '0.00'}</h2>
          <p className="text-indigo-100">Account Status: Active</p>
        </div>

        {/* Action Buttons */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
          <Link
            href="/transaction"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-center"
          >
            <Send className="w-8 h-8 text-indigo-600 mx-auto mb-2" />
            <h3 className="font-semibold text-gray-800">Send Money</h3>
            <p className="text-sm text-gray-600">Transfer funds to another account</p>
          </Link>

          <Link
            href="/transactions"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-center"
          >
            <History className="w-8 h-8 text-indigo-600 mx-auto mb-2" />
            <h3 className="font-semibold text-gray-800">Transaction History</h3>
            <p className="text-sm text-gray-600">View all your transactions</p>
          </Link>

          <Link
            href="/profile"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition text-center"
          >
            <User className="w-8 h-8 text-indigo-600 mx-auto mb-2" />
            <h3 className="font-semibold text-gray-800">My Profile</h3>
            <p className="text-sm text-gray-600">View and edit your profile</p>
          </Link>
        </div>

        {/* Profile Info */}
        <div className="bg-white rounded-lg shadow p-6 mb-8">
          <h3 className="text-xl font-semibold text-gray-800 mb-4">Profile Information</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p className="text-sm text-gray-600">Full Name</p>
              <p className="font-semibold text-gray-800">{profile?.fullname}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Email</p>
              <p className="font-semibold text-gray-800">{profile?.email}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Phone</p>
              <p className="font-semibold text-gray-800">{profile?.phone || 'Not provided'}</p>
            </div>
            <div>
              <p className="text-sm text-gray-600">Username</p>
              <p className="font-semibold text-gray-800">{user?.username}</p>
            </div>
          </div>
        </div>

        {/* Recent Transactions */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-xl font-semibold text-gray-800 mb-4">Recent Transactions</h3>
          {transactions.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">From</th>
                    <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">To</th>
                    <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">Amount</th>
                    <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">Date</th>
                    <th className="px-4 py-2 text-left text-sm font-semibold text-gray-700">Status</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx) => (
                    <tr key={tx.id} className="border-t hover:bg-gray-50">
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.senderUsername}</td>
                      <td className="px-4 py-3 text-sm text-gray-800">{tx.receiverUsername}</td>
                      <td className="px-4 py-3 text-sm font-semibold text-gray-800">${tx.amount.toFixed(2)}</td>
                      <td className="px-4 py-3 text-sm text-gray-600">
                        {new Date(tx.createdAt).toLocaleDateString()}
                      </td>
                      <td className="px-4 py-3 text-sm">
                        <span className="px-2 py-1 bg-green-100 text-green-800 rounded-full text-xs font-semibold">
                          {tx.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-600 text-center py-8">No transactions yet</p>
          )}
          <Link
            href="/transactions"
            className="text-indigo-600 hover:text-indigo-700 font-semibold mt-4 inline-block"
          >
            View all transactions →
          </Link>
        </div>
      </div>
    </div>
  );
}

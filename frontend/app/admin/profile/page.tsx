'use client';

import { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import api from '@/lib/api';

interface ProfileDTO {
  id: number;
  userId: number;
  fullname: string;
  email: string;
  phone?: string;
  birthday?: string;
  isActive?: boolean;
}

export default function AdminProfilePage() {
  const router = useRouter();
  const { user, isAuthenticated, loading } = useAuth();
  const isAdmin = useMemo(() => user?.role === 'ADMIN', [user?.role]);

  const [profile, setProfile] = useState<ProfileDTO | null>(null);
  const [formData, setFormData] = useState({ fullname: '', phone: '', birthday: '' });
  const [fetching, setFetching] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const loadProfile = async () => {
    setFetching(true);
    setError('');
    try {
      const res = await api.get('/admin/profile');
      setProfile(res.data);
      setFormData({
        fullname: res.data?.fullname || '',
        phone: res.data?.phone || '',
        birthday: res.data?.birthday ? String(res.data.birthday) : '',
      });
    } catch (e) {
      setError('Failed to load admin profile');
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
    loadProfile();
  }, [isAuthenticated, isAdmin, loading, router]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');

    try {
      const payload: any = {
        fullname: formData.fullname,
        phone: formData.phone,
      };
      if (formData.birthday && formData.birthday.trim().length > 0) {
        payload.birthday = formData.birthday;
      }

      const res = await api.put('/admin/profile', payload);
      setProfile(res.data);
      setSuccess('Profile updated successfully');
    } catch (e) {
      setError('Failed to update admin profile');
    } finally {
      setSaving(false);
    }
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
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-800">Admin Profile</h1>
          <Link href="/admin" className="text-indigo-700 hover:text-indigo-800 font-semibold">
            ← Back to Admin Dashboard
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
          <div className="mb-4 text-sm text-gray-600">
            <div><span className="font-semibold">Username:</span> {user?.username}</div>
            <div><span className="font-semibold">Email:</span> {profile?.email}</div>
          </div>

          <form onSubmit={handleSave} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Full name</label>
              <input
                name="fullname"
                value={formData.fullname}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Birthday (YYYY-MM-DD)</label>
              <input
                name="birthday"
                value={formData.birthday}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="1990-01-01"
              />
            </div>

            <button
              type="submit"
              disabled={saving}
              className="w-full bg-indigo-700 text-white py-2 rounded-lg font-semibold hover:bg-indigo-800 transition disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

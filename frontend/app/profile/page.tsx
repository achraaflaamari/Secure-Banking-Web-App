'use client';

import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { userAPI, getApiErrorMessage } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';

interface Profile {
  fullname: string;
  email: string;
  rip?: string;
  phone: string;
  birthday?: string;
}

export default function ProfilePage() {
  const router = useRouter();
  const { isAuthenticated, loading } = useAuth();

  const [profile, setProfile] = useState<Profile | null>(null);
  const [formData, setFormData] = useState({ fullname: '', email: '', phone: '', birthday: '' });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [pwCurrentPassword, setPwCurrentPassword] = useState('');
  const [pwCode, setPwCode] = useState('');
  const [pwNewPassword, setPwNewPassword] = useState('');
  const [pwSending, setPwSending] = useState(false);
  const [pwVerifying, setPwVerifying] = useState(false);
  const [pwError, setPwError] = useState('');
  const [pwSuccess, setPwSuccess] = useState('');

  useEffect(() => {
    if (loading) return;
    if (!isAuthenticated) {
      router.push('/login');
      return;
    }

    const fetchProfile = async () => {
      try {
        const res = await userAPI.getProfile();
        setProfile(res.data);
        setFormData({
          fullname: res.data?.fullname || '',
          email: res.data?.email || '',
          phone: res.data?.phone || '',
          birthday: res.data?.birthday ? String(res.data.birthday) : '',
        });
      } catch (e) {
        setError(getApiErrorMessage(e) || 'Failed to load profile');
      }
    };

    fetchProfile();
  }, [isAuthenticated, loading, router]);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev: typeof formData) => ({ ...prev, [name]: value }));
  };

  const handleSave = async (e: FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    setSuccess('');

    try {
      const res = await userAPI.updateProfile(formData);
      setProfile(res.data);
      setSuccess('Profile updated successfully');
    } catch (e) {
      setError(getApiErrorMessage(e) || 'Failed to update profile');
    } finally {
      setSaving(false);
    }
  };

  const handleRequestPasswordOtp = async (e: FormEvent) => {
    e.preventDefault();
    setPwSending(true);
    setPwError('');
    setPwSuccess('');
    try {
      await userAPI.requestPasswordChangeOtp({ currentPassword: pwCurrentPassword });
      setPwSuccess('OTP sent to your email');
    } catch (e) {
      setPwError(getApiErrorMessage(e) || 'Failed to send OTP');
    } finally {
      setPwSending(false);
    }
  };

  const handleVerifyPasswordOtp = async (e: FormEvent) => {
    e.preventDefault();
    setPwVerifying(true);
    setPwError('');
    setPwSuccess('');
    try {
      await userAPI.verifyPasswordChangeOtp({ code: pwCode, newPassword: pwNewPassword });
      setPwSuccess('Password updated successfully');
      setPwCurrentPassword('');
      setPwCode('');
      setPwNewPassword('');
    } catch (e) {
      setPwError(getApiErrorMessage(e) || 'Failed to update password');
    } finally {
      setPwVerifying(false);
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
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-800">My Profile</h1>
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
          <form onSubmit={handleSave} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
              <input
                name="fullname"
                value={formData.fullname}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Full name"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
              <input
                name="email"
                type="email"
                value={formData.email}
                onChange={handleChange}
                disabled
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Email"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Birthday</label>
              <input
                name="birthday"
                type="date"
                value={formData.birthday}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">RIP</label>
              <input
                value={profile?.rip || ''}
                disabled
                className="w-full px-4 py-2 border border-gray-300 rounded-lg bg-gray-50"
                placeholder="RIP"
              />
              <div className="mt-1 text-xs text-gray-500">Share this RIP with others so they can send you money.</div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
              <input
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Phone"
              />
            </div>

            <button
              type="submit"
              disabled={saving}
              className="w-full bg-indigo-600 text-white py-2 rounded-lg font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
            >
              {saving ? 'Saving...' : 'Save'}
            </button>
          </form>

          {profile && (
            <div className="mt-6 text-sm text-gray-600">
              <div>Current: {profile.fullname} ({profile.email})</div>
            </div>
          )}
        </div>

        <div className="bg-white rounded-lg shadow p-6 mt-6">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">Change Password (OTP)</h2>

          {pwError && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4">
              {pwError}
            </div>
          )}

          {pwSuccess && (
            <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-4">
              {pwSuccess}
            </div>
          )}

          <form onSubmit={handleRequestPasswordOtp} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Current password</label>
              <input
                type="password"
                value={pwCurrentPassword}
                onChange={(e) => setPwCurrentPassword(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="Current password"
                required
              />
            </div>

            <button
              type="submit"
              disabled={pwSending}
              className="w-full bg-gray-800 text-white py-2 rounded-lg font-semibold hover:bg-gray-900 transition disabled:opacity-50"
            >
              {pwSending ? 'Sending OTP...' : 'Send OTP'}
            </button>
          </form>

          <form onSubmit={handleVerifyPasswordOtp} className="space-y-4 mt-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">OTP code</label>
              <input
                value={pwCode}
                onChange={(e) => setPwCode(e.target.value)}
                inputMode="numeric"
                pattern="[0-9]{6}"
                maxLength={6}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="6-digit code"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">New password</label>
              <input
                type="password"
                value={pwNewPassword}
                onChange={(e) => setPwNewPassword(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="New password"
                required
              />
            </div>

            <button
              type="submit"
              disabled={pwVerifying}
              className="w-full bg-indigo-600 text-white py-2 rounded-lg font-semibold hover:bg-indigo-700 transition disabled:opacity-50"
            >
              {pwVerifying ? 'Updating...' : 'Verify OTP & Update Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

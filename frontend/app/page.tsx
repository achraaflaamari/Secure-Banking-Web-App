'use client';

import { useEffect, useState } from 'react';

interface TestItem {
  id: number;
  name: string;
}

export default function Home() {
  const [data, setData] = useState<TestItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch(`${process.env.NEXT_PUBLIC_API_URL}/test-data`)
      .then(response => response.json())
      .then(data => {
        setData(data);
        setLoading(false);
      })
      .catch(error => {
        setError(error.message);
        setLoading(false);
      });
  }, []);

  if (loading) return <div className="flex min-h-screen items-center justify-center">Loading...</div>;
  if (error) return <div className="flex min-h-screen items-center justify-center text-red-500">Error: {error}</div>;

  return (
    <div className="flex min-h-screen items-center justify-center bg-zinc-50 font-sans dark:bg-black">
      <main className="flex min-h-screen w-full max-w-3xl flex-col items-center justify-between py-32 px-16 bg-white dark:bg-black sm:items-start">
        <h1 className="text-3xl font-semibold mb-8">Full-Stack Test Data</h1>
        <div className="w-full">
          <h2 className="text-xl font-medium mb-4">Test Items from Database:</h2>
          <ul className="space-y-2">
            {data.map(item => (
              <li key={item.id} className="p-4 border rounded-lg">
                <span className="font-medium">ID: {item.id}</span> - <span>{item.name}</span>
              </li>
            ))}
          </ul>
        </div>
      </main>
    </div>
  );
}

'use client';

import { useQuery } from '@tanstack/react-query';
import { api, type AvailabilityItem } from '@/lib/api';
import { useCurrentUser } from '@/lib/user-context';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

function toLocalIso(date: Date): string {
  return date.toISOString().slice(0, 19);
}

function groupByDate(items: AvailabilityItem[]): Record<string, AvailabilityItem[]> {
  return items.reduce<Record<string, AvailabilityItem[]>>((acc, item) => {
    const date = item.startTime.slice(0, 10);
    (acc[date] ??= []).push(item);
    return acc;
  }, {});
}

export default function AvailabilityPage() {
  const { userId } = useCurrentUser();
  const router = useRouter();

  const [from, setFrom] = useState(() => {
    const d = new Date();
    d.setHours(0, 0, 0, 0);
    return toLocalIso(d);
  });
  const [to, setTo] = useState(() => {
    const d = new Date();
    d.setDate(d.getDate() + 6);
    d.setHours(23, 59, 0, 0);
    return toLocalIso(d);
  });

  const { data: items = [], isLoading } = useQuery({
    queryKey: ['availability', userId, from, to],
    queryFn: () => api.slots.availability(userId!, from, to),
    enabled: !!userId,
  });

  if (!userId) {
    router.push('/');
    return null;
  }

  const grouped = groupByDate(items);
  const dates = Object.keys(grouped).sort();
  const freeCount = items.filter((i) => i.status === 'FREE').length;
  const busyCount = items.filter((i) => i.status === 'BUSY').length;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-bold">Availability</h1>
        <p className="text-sm text-gray-500">Your free and busy slots in the selected time range</p>
      </div>

      <div className="flex items-end gap-4 p-4 bg-gray-50 rounded-xl">
        <div>
          <label className="block text-xs text-gray-500 mb-1">From</label>
          <input
            type="datetime-local"
            value={from}
            onChange={(e) => setFrom(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#0A5C48]"
          />
        </div>
        <div>
          <label className="block text-xs text-gray-500 mb-1">To</label>
          <input
            type="datetime-local"
            value={to}
            onChange={(e) => setTo(e.target.value)}
            className="border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#0A5C48]"
          />
        </div>
        <div className="flex gap-3 text-sm text-gray-600 pb-2">
          <span className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-[#d7efdc] inline-block" />
            {freeCount} free
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded bg-[#0A5C48] inline-block" />
            {busyCount} busy
          </span>
        </div>
      </div>

      {isLoading ? (
        <div className="space-y-2">
          {[...Array(5)].map((_, i) => <div key={i} className="h-16 bg-gray-50 rounded-xl animate-pulse" />)}
        </div>
      ) : dates.length === 0 ? (
        <div className="border-2 border-dashed border-gray-200 rounded-xl p-12 text-center text-gray-400">
          <p className="text-sm">No slots in this period</p>
        </div>
      ) : (
        <div className="border border-gray-200 rounded-xl overflow-hidden">
          <div className="grid text-xs font-medium text-gray-500 bg-gray-50 border-b border-gray-200"
            style={{ gridTemplateColumns: '140px 1fr' }}>
            <div className="p-3 border-r border-gray-200">Date</div>
            <div className="p-3">Time slots</div>
          </div>
          {dates.map((date) => (
            <div key={date} className="grid border-b border-gray-100 last:border-0"
              style={{ gridTemplateColumns: '140px 1fr' }}>
              <div className="p-3 border-r border-gray-100 text-sm font-medium text-gray-900">
                {new Date(date + 'T00:00:00').toLocaleDateString('en', { weekday: 'short', month: 'short', day: 'numeric' })}
              </div>
              <div className="p-3 flex flex-wrap gap-2">
                {grouped[date].map((item) => (
                  <span
                    key={item.slotId}
                    className={[
                      'px-3 py-1.5 rounded-lg text-xs font-medium',
                      item.status === 'FREE'
                        ? 'bg-[#d7efdc] text-[#0A5C48] border border-[#ACC8C1]'
                        : 'bg-[#0A5C48] text-white',
                    ].join(' ')}
                  >
                    {new Date(item.startTime).toLocaleTimeString('en', { hour: '2-digit', minute: '2-digit' })}
                    {' – '}
                    {new Date(item.endTime).toLocaleTimeString('en', { hour: '2-digit', minute: '2-digit' })}
                    {item.status === 'BUSY' && ' 🔒'}
                  </span>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

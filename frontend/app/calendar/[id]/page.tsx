'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api, type TimeSlot } from '@/lib/api';
import { useState, use } from 'react';

const HOURS = Array.from({ length: 10 }, (_, i) => i + 8);
const DAYS = 7;

function getWeekDays(base: Date): Date[] {
  const start = new Date(base);
  start.setDate(base.getDate() - base.getDay() + 1);
  return Array.from({ length: DAYS }, (_, i) => {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    return d;
  });
}

function toLocalIso(date: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`;
}

function slotCoversHour(slot: TimeSlot, day: Date, hour: number): boolean {
  const slotStart = new Date(slot.startTime);
  const slotEnd = new Date(slot.endTime);
  const cellStart = new Date(day);
  cellStart.setHours(hour, 0, 0, 0);
  const cellEnd = new Date(day);
  cellEnd.setHours(hour + 1, 0, 0, 0);
  return slotStart < cellEnd && slotEnd > cellStart;
}

export default function CalendarPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const [week, setWeek] = useState(new Date());
  const queryClient = useQueryClient();

  const weekDays = getWeekDays(week);
  const from = toLocalIso(weekDays[0]);
  const to = toLocalIso(weekDays[DAYS - 1]);

  const { data: slots = [] } = useQuery({
    queryKey: ['slots', id, from, to],
    queryFn: () => api.slots.listByCalendar(id, { from, to }),
  });

  const { data: calendar } = useQuery({
    queryKey: ['calendar', id],
    queryFn: () => api.calendars.get(id),
  });

  const createSlot = useMutation({
    mutationFn: ({ day, hour }: { day: Date; hour: number }) => {
      const startTime = new Date(day);
      startTime.setHours(hour, 0, 0, 0);
      const endTime = new Date(day);
      endTime.setHours(hour + 1, 0, 0, 0);
      return api.slots.create(id, { startTime: toLocalIso(startTime), endTime: toLocalIso(endTime) });
    },
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['slots', id] }),
  });

  const deleteSlot = useMutation({
    mutationFn: (slotId: string) => api.slots.delete(slotId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['slots', id] }),
  });

  const handleCellClick = (day: Date, hour: number) => {
    const existing = slots.find((s) => slotCoversHour(s, day, hour));
    if (existing) {
      if (existing.status === 'FREE') deleteSlot.mutate(existing.id);
      return;
    }
    createSlot.mutate({ day, hour });
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-xl font-bold">{calendar?.name ?? 'Calendar'}</h1>
          <p className="text-sm text-gray-500">Click a cell to add or remove a free slot</p>
        </div>
        <div className="flex gap-2">
          <button
            onClick={() => setWeek((w) => { const d = new Date(w); d.setDate(d.getDate() - 7); return d; })}
            className="px-3 py-1.5 rounded-lg border border-gray-200 text-sm hover:border-[#0A5C48] hover:text-[#0A5C48]"
          >
            ← Prev
          </button>
          <button
            onClick={() => setWeek(new Date())}
            className="px-3 py-1.5 rounded-lg border border-gray-200 text-sm hover:border-[#0A5C48] hover:text-[#0A5C48]"
          >
            Today
          </button>
          <button
            onClick={() => setWeek((w) => { const d = new Date(w); d.setDate(d.getDate() + 7); return d; })}
            className="px-3 py-1.5 rounded-lg border border-gray-200 text-sm hover:border-[#0A5C48] hover:text-[#0A5C48]"
          >
            Next →
          </button>
        </div>
      </div>

      {/* Grid */}
      <div className="overflow-x-auto rounded-xl border border-gray-200">
        <table className="w-full text-sm border-collapse">
          <thead>
            <tr className="bg-gray-50">
              <th className="w-16 p-3 text-gray-400 font-normal text-left border-r border-gray-200" />
              {weekDays.map((day) => (
                <th key={day.toISOString()} className="p-3 font-medium text-gray-700 text-center border-r border-gray-100 last:border-0">
                  <div>{day.toLocaleDateString('en', { weekday: 'short' })}</div>
                  <div className="text-xs font-normal text-gray-400">{day.getDate()}</div>
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {HOURS.map((hour) => (
              <tr key={hour} className="border-t border-gray-100">
                <td className="p-2 text-xs text-gray-400 border-r border-gray-200 text-right pr-3">
                  {hour}:00
                </td>
                {weekDays.map((day) => {
                  const slot = slots.find((s) => slotCoversHour(s, day, hour));
                  const isFree = slot?.status === 'FREE';
                  const isBusy = slot?.status === 'BUSY';
                  return (
                    <td
                      key={day.toISOString()}
                      onClick={() => handleCellClick(day, hour)}
                      className={[
                        'border-r border-gray-100 last:border-0 h-10 cursor-pointer transition-colors text-center text-xs',
                        isFree ? 'bg-[#d7efdc] hover:bg-[#ACC8C1]' : '',
                        isBusy ? 'bg-[#0A5C48] text-white cursor-not-allowed' : '',
                        !slot ? 'hover:bg-gray-50' : '',
                      ].join(' ')}
                      title={isBusy ? 'Busy — linked to meeting' : isFree ? 'Free — click to remove' : 'Click to add slot'}
                    >
                      {isFree && '✓'}
                      {isBusy && '●'}
                    </td>
                  );
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="flex gap-4 text-xs text-gray-500">
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-[#d7efdc] inline-block" /> Free slot</span>
        <span className="flex items-center gap-1.5"><span className="w-3 h-3 rounded bg-[#0A5C48] inline-block" /> Busy (meeting scheduled)</span>
      </div>
    </div>
  );
}

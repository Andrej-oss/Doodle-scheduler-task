'use client';

import { useQuery, useMutation } from '@tanstack/react-query';
import { api, type TimeSlot, type Calendar } from '@/lib/api';
import { useCurrentUser } from '@/lib/user-context';
import { useState } from 'react';
import { useRouter } from 'next/navigation';

type Step = 1 | 2 | 3;

const STEPS = ['Select slot', 'Details', 'Done'];

export default function NewMeetingPage() {
  const { userId } = useCurrentUser();
  const router = useRouter();

  const [step, setStep] = useState<Step>(1);
  const [selectedCalendar, setSelectedCalendar] = useState('');
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [participantInput, setParticipantInput] = useState('');
  const [participantIds, setParticipantIds] = useState<string[]>([]);

  const { data: calendars = [] } = useQuery({
    queryKey: ['calendars', userId],
    queryFn: () => api.calendars.listByUser(userId!),
    enabled: !!userId,
  });

  const { data: slots = [] } = useQuery({
    queryKey: ['slots-free', selectedCalendar],
    queryFn: () => api.slots.listByCalendar(selectedCalendar, { status: 'FREE' }),
    enabled: !!selectedCalendar,
  });

  const scheduleMeeting = useMutation({
    mutationFn: () =>
      api.meetings.schedule({
        slotId: selectedSlot!.id,
        organizerId: userId!,
        title,
        description: description || undefined,
        participantIds,
      }),
    onSuccess: () => {
      setStep(3);
      setTimeout(() => router.push('/'), 2000);
    },
  });

  if (!userId) {
    router.push('/');
    return null;
  }

  const addParticipant = () => {
    const trimmed = participantInput.trim();
    if (trimmed && !participantIds.includes(trimmed)) {
      setParticipantIds((prev) => [...prev, trimmed]);
      setParticipantInput('');
    }
  };

  return (
    <div className="max-w-xl mx-auto space-y-6">
      <h1 className="text-xl font-bold">Schedule a Meeting</h1>

      <div className="flex items-center gap-2">
        {STEPS.map((label, i) => {
          const n = (i + 1) as Step;
          const active = step === n;
          const done = step > n;
          return (
            <div key={label} className="flex items-center gap-2">
              <div className={[
                'w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-colors',
                active ? 'bg-[#0A5C48] text-white' : done ? 'bg-[#d7efdc] text-[#0A5C48]' : 'bg-gray-100 text-gray-400',
              ].join(' ')}>
                {done ? '✓' : n}
              </div>
              <span className={`text-sm ${active ? 'text-[#0A5C48] font-medium' : 'text-gray-400'}`}>{label}</span>
              {i < STEPS.length - 1 && <div className="w-8 h-px bg-gray-200 mx-1" />}
            </div>
          );
        })}
      </div>

      {step === 1 && (
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-2">Calendar</label>
            <select
              value={selectedCalendar}
              onChange={(e) => { setSelectedCalendar(e.target.value); setSelectedSlot(null); }}
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#0A5C48]"
            >
              <option value="">Select a calendar…</option>
              {calendars.map((c: Calendar) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>

          {selectedCalendar && (
            <div>
              <label className="block text-sm font-medium mb-2">Available slot</label>
              {slots.length === 0 ? (
                <p className="text-sm text-gray-400 py-4 text-center border border-dashed border-gray-200 rounded-xl">
                  No free slots. Add them in the calendar view first.
                </p>
              ) : (
                <div className="space-y-2 max-h-64 overflow-y-auto">
                  {slots.map((slot) => (
                    <button
                      key={slot.id}
                      onClick={() => setSelectedSlot(slot)}
                      className={[
                        'w-full text-left px-4 py-3 rounded-xl border transition-all text-sm',
                        selectedSlot?.id === slot.id
                          ? 'border-[#0A5C48] bg-[#d7efdc] text-[#0A5C48]'
                          : 'border-gray-200 hover:border-[#ACC8C1]',
                      ].join(' ')}
                    >
                      {new Date(slot.startTime).toLocaleDateString('en', { weekday: 'short', month: 'short', day: 'numeric' })}
                      {' · '}
                      {new Date(slot.startTime).toLocaleTimeString('en', { hour: '2-digit', minute: '2-digit' })}
                      {' – '}
                      {new Date(slot.endTime).toLocaleTimeString('en', { hour: '2-digit', minute: '2-digit' })}
                    </button>
                  ))}
                </div>
              )}
            </div>
          )}

          <button
            disabled={!selectedSlot}
            onClick={() => setStep(2)}
            className="w-full py-2.5 rounded-xl text-sm font-medium text-white bg-[#0A5C48] hover:bg-[#0E3830] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
          >
            Continue →
          </button>
        </div>
      )}

      {step === 2 && (
        <div className="space-y-4">
          <div className="bg-[#d7efdc] rounded-xl p-4 text-sm text-[#0A5C48]">
            <strong>Slot: </strong>
            {new Date(selectedSlot!.startTime).toLocaleString('en', { dateStyle: 'medium', timeStyle: 'short' })}
            {' – '}
            {new Date(selectedSlot!.endTime).toLocaleTimeString('en', { hour: '2-digit', minute: '2-digit' })}
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Title</label>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g. Team Sync"
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#0A5C48]"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Optional…"
              className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#0A5C48] resize-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium mb-1">Participants</label>
            <div className="flex gap-2">
              <input
                value={participantInput}
                onChange={(e) => setParticipantInput(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && addParticipant()}
                placeholder="Paste user UUID…"
                className="flex-1 border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#0A5C48]"
              />
              <button
                onClick={addParticipant}
                className="px-4 py-2 rounded-lg text-sm font-medium text-white bg-[#0A5C48] hover:bg-[#0E3830] transition-colors"
              >
                Add
              </button>
            </div>
            {participantIds.length > 0 && (
              <div className="mt-2 flex flex-wrap gap-1">
                {participantIds.map((id) => (
                  <span key={id} className="bg-[#d7efdc] text-[#0A5C48] text-xs px-2 py-1 rounded-lg flex items-center gap-1">
                    {id.slice(0, 8)}…
                    <button onClick={() => setParticipantIds((prev) => prev.filter((p) => p !== id))}>×</button>
                  </span>
                ))}
              </div>
            )}
          </div>

          {scheduleMeeting.isError && (
            <p className="text-sm text-red-500">{(scheduleMeeting.error as Error).message}</p>
          )}

          <div className="flex gap-3">
            <button onClick={() => setStep(1)} className="flex-1 py-2.5 rounded-xl text-sm border border-gray-200 hover:border-gray-300">
              ← Back
            </button>
            <button
              disabled={!title.trim() || scheduleMeeting.isPending}
              onClick={() => scheduleMeeting.mutate()}
              className="flex-1 py-2.5 rounded-xl text-sm font-medium text-white bg-[#0A5C48] hover:bg-[#0E3830] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
            >
              {scheduleMeeting.isPending ? 'Scheduling…' : 'Schedule Meeting'}
            </button>
          </div>
        </div>
      )}

      {step === 3 && (
        <div className="text-center py-12 space-y-3">
          <div className="w-16 h-16 rounded-full bg-[#d7efdc] flex items-center justify-center mx-auto text-3xl">✓</div>
          <h2 className="text-lg font-semibold text-[#0A5C48]">Meeting scheduled</h2>
          <p className="text-sm text-gray-400">Redirecting…</p>
        </div>
      )}
    </div>
  );
}

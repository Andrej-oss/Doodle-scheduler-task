'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { api, type Calendar, type MeetingResponse } from '@/lib/api';
import { useCurrentUser } from '@/lib/user-context';
import Link from 'next/link';
import { useState } from 'react';

function CalendarCard({ calendar }: { calendar: Calendar }) {
  return (
    <Link href={`/calendar/${calendar.id}`}>
      <div className="border border-gray-200 rounded-xl p-5 hover:border-[#0A5C48] hover:shadow-sm transition-all cursor-pointer group">
        <div className="w-10 h-10 rounded-full mb-3 flex items-center justify-center bg-[#d7efdc]">
          <span className="text-lg">📅</span>
        </div>
        <h3 className="font-semibold text-gray-900 group-hover:text-[#0A5C48]">{calendar.name}</h3>
        <p className="text-xs text-gray-400 mt-1">
          {new Date(calendar.createdAt).toLocaleDateString()}
        </p>
      </div>
    </Link>
  );
}

function MeetingCard({ meeting }: { meeting: MeetingResponse }) {
  const start = new Date(meeting.startTime);
  return (
    <div className="flex items-start gap-4 py-3 border-b border-gray-100 last:border-0">
      <div className="text-center min-w-[48px]">
        <div className="text-xs text-gray-400 uppercase">{start.toLocaleDateString('en', { month: 'short' })}</div>
        <div className="text-xl font-bold text-[#0A5C48]">{start.getDate()}</div>
      </div>
      <div>
        <p className="font-medium text-gray-900">{meeting.title}</p>
        <p className="text-sm text-gray-500">
          {start.toLocaleTimeString('en', { hour: '2-digit', minute: '2-digit' })} —{' '}
          {new Date(meeting.endTime).toLocaleTimeString('en', { hour: '2-digit', minute: '2-digit' })}
        </p>
        {meeting.participantIds.length > 0 && (
          <p className="text-xs text-gray-400 mt-0.5">{meeting.participantIds.length} participant(s)</p>
        )}
      </div>
    </div>
  );
}

function UserSetup({ onSet }: { onSet: (id: string) => void }) {
  const queryClient = useQueryClient();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');

  const createUser = useMutation({
    mutationFn: () => api.users.create({ username, email }),
    onSuccess: (user) => {
      queryClient.invalidateQueries();
      onSet(user.id);
    },
  });

  return (
    <div className="max-w-sm mx-auto mt-20 space-y-4">
      <div className="text-center mb-6">
        <div className="w-12 h-12 rounded-full bg-[#0A5C48] flex items-center justify-center mx-auto mb-3">
          <span className="text-white font-bold text-lg">D</span>
        </div>
        <h1 className="text-xl font-bold">Doodle Scheduler</h1>
        <p className="text-sm text-gray-500 mt-1">Create your account to get started</p>
      </div>
      <input
        value={username}
        onChange={(e) => setUsername(e.target.value)}
        placeholder="Username"
        className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-[#0A5C48]"
      />
      <input
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        type="email"
        placeholder="Email"
        className="w-full border border-gray-200 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:border-[#0A5C48]"
      />
      {createUser.isError && (
        <p className="text-xs text-red-500">{(createUser.error as Error).message}</p>
      )}
      <button
        disabled={!username.trim() || !email.trim() || createUser.isPending}
        onClick={() => createUser.mutate()}
        className="w-full py-2.5 rounded-xl text-sm font-medium text-white bg-[#0A5C48] hover:bg-[#0E3830] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
      >
        {createUser.isPending ? 'Creating…' : 'Get Started'}
      </button>
    </div>
  );
}

function CreateCalendarForm({ userId }: { userId: string }) {
  const queryClient = useQueryClient();
  const [name, setName] = useState('');
  const [open, setOpen] = useState(false);

  const create = useMutation({
    mutationFn: () => api.calendars.create({ userId, name }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['calendars', userId] });
      setName('');
      setOpen(false);
    },
  });

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="border-2 border-dashed border-gray-200 rounded-xl p-10 text-center text-gray-400 w-full hover:border-[#0A5C48] hover:text-[#0A5C48] transition-colors text-sm"
      >
        + New Calendar
      </button>
    );
  }

  return (
    <div className="border border-[#0A5C48] rounded-xl p-5 space-y-3">
      <p className="text-sm font-medium">New Calendar</p>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="e.g. Work, Personal…"
        className="w-full border border-gray-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:border-[#0A5C48]"
        autoFocus
      />
      {create.isError && (
        <p className="text-xs text-red-500">{(create.error as Error).message}</p>
      )}
      <div className="flex gap-2">
        <button
          onClick={() => setOpen(false)}
          className="flex-1 py-2 rounded-lg text-sm border border-gray-200 hover:border-gray-300"
        >
          Cancel
        </button>
        <button
          disabled={!name.trim() || create.isPending}
          onClick={() => create.mutate()}
          className="flex-1 py-2 rounded-lg text-sm font-medium text-white bg-[#0A5C48] hover:bg-[#0E3830] transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {create.isPending ? 'Creating…' : 'Create'}
        </button>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { userId, setUserId } = useCurrentUser();

  const { data: calendars, isLoading: loadingCals } = useQuery({
    queryKey: ['calendars', userId],
    queryFn: () => api.calendars.listByUser(userId!),
    enabled: !!userId,
  });

  const { data: meetings, isLoading: loadingMeetings } = useQuery({
    queryKey: ['meetings', userId],
    queryFn: () => api.meetings.listByUser(userId!),
    enabled: !!userId,
  });

  if (!userId) {
    return <UserSetup onSet={setUserId} />;
  }

  return (
    <div className="space-y-8">
      <div className="rounded-2xl p-8 text-white bg-[#0A5C48]">
        <h1 className="text-2xl font-bold mb-1">Dashboard</h1>
        <p className="text-sm opacity-80">Manage your calendars and schedule meetings</p>
        <div className="mt-4 flex gap-3">
          <Link href="/availability" className="bg-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-gray-100 transition-colors text-[#0A5C48]">
            View Availability
          </Link>
          <Link href="/meetings/new" className="border border-white text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-white/10 transition-colors">
            Schedule Meeting
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2">
          <h2 className="text-lg font-semibold mb-4">My Calendars</h2>
          {loadingCals ? (
            <div className="grid grid-cols-2 gap-3">
              {[...Array(2)].map((_, i) => (
                <div key={i} className="border border-gray-100 rounded-xl p-5 animate-pulse h-28 bg-gray-50" />
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-3">
              {(calendars ?? []).map((cal) => <CalendarCard key={cal.id} calendar={cal} />)}
              <CreateCalendarForm userId={userId} />
            </div>
          )}
        </div>

        <div>
          <h2 className="text-lg font-semibold mb-4">Upcoming Meetings</h2>
          <div className="border border-gray-200 rounded-xl p-4">
            {loadingMeetings ? (
              <div className="space-y-3">
                {[...Array(3)].map((_, i) => (
                  <div key={i} className="h-14 bg-gray-50 rounded animate-pulse" />
                ))}
              </div>
            ) : meetings && meetings.length > 0 ? (
              meetings.slice(0, 5).map((m) => <MeetingCard key={m.id} meeting={m} />)
            ) : (
              <p className="text-sm text-gray-400 text-center py-6">No upcoming meetings</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

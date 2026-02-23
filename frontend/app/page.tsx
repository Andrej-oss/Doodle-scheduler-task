'use client';

import { useQuery } from '@tanstack/react-query';
import { api, type Calendar, type MeetingResponse } from '@/lib/api';
import Link from 'next/link';

const DEMO_USER_ID = '00000000-0000-0000-0000-000000000001';

function CalendarCard({ calendar }: { calendar: Calendar }) {
  return (
    <Link href={`/calendar/${calendar.id}`}>
      <div className="border border-gray-200 rounded-xl p-5 hover:border-[#0A5C48] hover:shadow-sm transition-all cursor-pointer group">
        <div className="w-10 h-10 rounded-full mb-3 flex items-center justify-center bg-[#d7efdc]">
          <span className="text-lg">📅</span>
        </div>
        <h3 className="font-semibold text-gray-900 group-hover:text-[#0A5C48]">{calendar.name}</h3>
        <p className="text-xs text-gray-400 mt-1">
          Created {new Date(calendar.createdAt).toLocaleDateString()}
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

export default function DashboardPage() {
  const { data: calendars, isLoading: loadingCals } = useQuery({
    queryKey: ['calendars', DEMO_USER_ID],
    queryFn: () => api.calendars.listByUser(DEMO_USER_ID),
  });

  const { data: meetings, isLoading: loadingMeetings } = useQuery({
    queryKey: ['meetings', DEMO_USER_ID],
    queryFn: () => api.meetings.listByUser(DEMO_USER_ID),
  });

  return (
    <div className="space-y-8">
      <div className="rounded-2xl p-8 text-white bg-[#0A5C48]">
        <h1 className="text-2xl font-bold mb-1">Welcome back</h1>
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
          ) : calendars && calendars.length > 0 ? (
            <div className="grid grid-cols-2 gap-3">
              {calendars.map((cal) => <CalendarCard key={cal.id} calendar={cal} />)}
            </div>
          ) : (
            <div className="border-2 border-dashed border-gray-200 rounded-xl p-10 text-center text-gray-400">
              <p className="text-sm">No calendars yet</p>
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

const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080';

export async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json', ...options?.headers },
    ...options,
  });
  if (!res.ok) {
    const error = await res.text();
    throw new Error(error || `HTTP ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export interface User {
  id: string;
  username: string;
  email: string;
  createdAt: string;
}

export interface Calendar {
  id: string;
  userId: string;
  name: string;
  createdAt: string;
}

export interface TimeSlot {
  id: string;
  calendarId: string;
  startTime: string;
  endTime: string;
  status: 'FREE' | 'BUSY';
  meetingId: string | null;
}

export interface AvailabilityItem {
  slotId: string;
  startTime: string;
  endTime: string;
  status: 'FREE' | 'BUSY';
}

export interface MeetingResponse {
  id: string;
  title: string;
  description: string | null;
  organizerId: string;
  slotId: string;
  startTime: string;
  endTime: string;
  participantIds: string[];
  createdAt: string;
}

export const api = {
  users: {
    create: (body: { username: string; email: string }) =>
      apiFetch<User>('/api/v1/users', { method: 'POST', body: JSON.stringify(body) }),
    get: (userId: string) => apiFetch<User>(`/api/v1/users/${userId}`),
    search: (q: string) => apiFetch<User[]>(`/api/v1/users/search?q=${encodeURIComponent(q)}`),
  },
  calendars: {
    create: (body: { userId: string; name: string }) =>
      apiFetch<Calendar>('/api/v1/calendars', { method: 'POST', body: JSON.stringify(body) }),
    get: (calendarId: string) => apiFetch<Calendar>(`/api/v1/calendars/${calendarId}`),
    listByUser: (userId: string) => apiFetch<Calendar[]>(`/api/v1/users/${userId}/calendars`),
  },
  slots: {
    create: (calendarId: string, body: { startTime: string; endTime: string }) =>
      apiFetch<TimeSlot>(`/api/v1/calendars/${calendarId}/slots`, { method: 'POST', body: JSON.stringify(body) }),
    update: (slotId: string, body: Partial<{ startTime: string; endTime: string; status: string }>) =>
      apiFetch<TimeSlot>(`/api/v1/slots/${slotId}`, { method: 'PUT', body: JSON.stringify(body) }),
    delete: (slotId: string) =>
      apiFetch<void>(`/api/v1/slots/${slotId}`, { method: 'DELETE' }),
    listByCalendar: (calendarId: string, params?: { status?: string; from?: string; to?: string }) => {
      const query = new URLSearchParams(params as Record<string, string>).toString();
      return apiFetch<TimeSlot[]>(`/api/v1/calendars/${calendarId}/slots${query ? `?${query}` : ''}`);
    },
    availability: (userId: string, from: string, to: string) =>
      apiFetch<AvailabilityItem[]>(`/api/v1/users/${userId}/availability?from=${from}&to=${to}`),
  },
  meetings: {
    schedule: (body: {
      slotId: string;
      organizerId: string;
      title: string;
      description?: string;
      participantIds: string[];
    }) => apiFetch<MeetingResponse>('/api/v1/meetings', { method: 'POST', body: JSON.stringify(body) }),
    get: (meetingId: string) => apiFetch<MeetingResponse>(`/api/v1/meetings/${meetingId}`),
    listByUser: (userId: string) => apiFetch<MeetingResponse[]>(`/api/v1/users/${userId}/meetings`),
  },
};

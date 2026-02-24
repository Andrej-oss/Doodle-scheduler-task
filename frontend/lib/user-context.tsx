'use client';

import { createContext, useContext, useState, type ReactNode } from 'react';

interface UserContextValue {
  userId: string | null;
  setUserId: (id: string) => void;
}

const UserContext = createContext<UserContextValue | null>(null);

export function UserProvider({ children }: { children: ReactNode }) {
  const [userId, setUserIdState] = useState<string | null>(() => {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('userId');
  });

  const setUserId = (id: string) => {
    localStorage.setItem('userId', id);
    setUserIdState(id);
  };

  return (
    <UserContext.Provider value={{ userId, setUserId }}>
      {children}
    </UserContext.Provider>
  );
}

export function useCurrentUser(): UserContextValue {
  const ctx = useContext(UserContext);
  if (!ctx) throw new Error('useCurrentUser must be used within UserProvider');
  return ctx;
}

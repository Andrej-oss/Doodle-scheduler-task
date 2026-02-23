'use client';

import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';

interface UserContextValue {
  userId: string | null;
  setUserId: (id: string) => void;
}

const UserContext = createContext<UserContextValue | null>(null);

export function UserProvider({ children }: { children: ReactNode }) {
  const [userId, setUserIdState] = useState<string | null>(null);

  useEffect(() => {
    const stored = localStorage.getItem('userId');
    if (stored) setUserIdState(stored);
  }, []);

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

import type { Metadata } from 'next';
import { Geist } from 'next/font/google';
import Link from 'next/link';
import './globals.css';
import { Providers } from '@/lib/providers';

const geist = Geist({ variable: '--font-geist-sans', subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Doodle Scheduler',
  description: 'Mini meeting scheduling platform',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={`${geist.variable} antialiased bg-white text-gray-900`}>
        <Providers>
          <header className="border-b border-gray-200">
            <div className="max-w-6xl mx-auto px-4 h-14 flex items-center gap-3">
              <div className="w-7 h-7 rounded-full flex items-center justify-center" style={{ backgroundColor: '#0A5C48' }}>
                <span className="text-white text-xs font-bold">D</span>
              </div>
              <span className="font-semibold text-lg" style={{ color: '#0A5C48' }}>Doodle Scheduler</span>
              <nav className="ml-6 flex gap-4 text-sm text-gray-600">
                <Link href="/" className="hover:text-[#0A5C48]">Dashboard</Link>
                <Link href="/availability" className="hover:text-[#0A5C48]">Availability</Link>
              </nav>
            </div>
          </header>
          <main className="max-w-6xl mx-auto px-4 py-6">
            {children}
          </main>
        </Providers>
      </body>
    </html>
  );
}

'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';

/**
 * Navigation items for admin sidebar
 * Each item links to a specific management page
 */
const navItems = [
  {
    title: 'Overview',
    href: '/',
    icon: '📊',
    description: 'Service health and statistics'
  },
  {
    title: 'Simulation',
    href: '/simulation',
    icon: '🌧️',
    description: 'Weather event simulation'
  },
  {
    title: 'Claims',
    href: '/claims',
    icon: '📋',
    description: 'Claims management'
  },
  {
    title: 'Policies',
    href: '/policies',
    icon: '📝',
    description: 'Policy analytics'
  },
  {
    title: 'Workers',
    href: '/workers',
    icon: '👷',
    description: 'Worker management'
  },
  {
    title: 'Events',
    href: '/events',
    icon: '📡',
    description: 'Real-time event stream'
  },
  {
    title: 'Logs',
    href: '/logs',
    icon: '📜',
    description: 'System logs'
  },
];

/**
 * Sidebar navigation component
 * Displays navigation links with active state highlighting
 */
export function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-64 bg-zinc-900 text-white flex flex-col min-h-screen">
      {/* Logo/Brand */}
      <div className="p-4 border-b border-zinc-800">
        <div className="flex items-center gap-2">
          <span className="text-2xl">🛡️</span>
          <div>
            <h1 className="font-bold text-lg">Parametrix</h1>
            <p className="text-xs text-zinc-400">Admin Dashboard</p>
          </div>
        </div>
      </div>

      {/* Navigation Links */}
      <nav className="flex-1 p-4">
        <ul className="space-y-2">
          {navItems.map((item) => {
            const isActive = pathname === item.href;
            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={cn(
                    'flex items-center gap-3 px-3 py-2 rounded-lg transition-colors',
                    isActive
                      ? 'bg-blue-600 text-white'
                      : 'text-zinc-400 hover:bg-zinc-800 hover:text-white'
                  )}
                >
                  <span className="text-lg">{item.icon}</span>
                  <div>
                    <span className="block text-sm font-medium">{item.title}</span>
                    {!isActive && (
                      <span className="block text-xs text-zinc-500">
                        {item.description}
                      </span>
                    )}
                  </div>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-zinc-800">
        <div className="flex items-center gap-2 text-xs text-zinc-500">
          <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
          <span>All services healthy</span>
        </div>
      </div>
    </aside>
  );
}

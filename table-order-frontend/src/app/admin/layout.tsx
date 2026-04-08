'use client';

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import Link from 'next/link';
import { authService } from '@/services/auth';

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const [storeName, setStoreName] = useState('');

  useEffect(() => {
    if (pathname === '/admin/login') return;
    const session = authService.getSession();
    if (!session || session.role !== 'ADMIN') { router.replace('/admin/login'); return; }
    setStoreName(session.storeName as string || '');
  }, [pathname, router]);

  if (pathname === '/admin/login') return <>{children}</>;

  const navItems = [
    { href: '/admin/dashboard', label: '주문현황', icon: '📋' },
    { href: '/admin/tables', label: '테이블', icon: '🪑' },
    { href: '/admin/menus', label: '메뉴관리', icon: '🍽️' },
  ];

  return (
    <div className="min-h-screen bg-coolGray-50">
      <header className="bg-white border-b border-coolGray-200 px-6 py-3 flex items-center justify-between">
        <h1 className="text-t5 font-bold text-coolGray-900">{storeName} 관리</h1>
        <button onClick={() => { authService.logout(); router.push('/admin/login'); }}
                className="text-t7 text-coolGray-500" data-testid="admin-logout-button">로그아웃</button>
      </header>
      <nav className="bg-white border-b border-coolGray-200 flex">
        {navItems.map((item) => (
          <Link key={item.href} href={item.href}
                className={`flex-1 text-center py-3 text-t7 ${pathname === item.href
                  ? 'text-blue-300 font-bold border-b-2 border-blue-300' : 'text-coolGray-500'}`}
                data-testid={`admin-nav-${item.label}`}>
            {item.icon} {item.label}
          </Link>
        ))}
      </nav>
      <main className="p-4">{children}</main>
    </div>
  );
}

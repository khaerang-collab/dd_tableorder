import '@/styles/globals.css';
import type { Metadata } from 'next';

export const metadata: Metadata = {
  title: '테이블오더',
  description: '디지털 테이블오더 서비스',
  viewport: 'width=device-width, initial-scale=1, viewport-fit=cover',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}

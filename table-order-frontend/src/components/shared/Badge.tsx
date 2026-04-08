import { BADGE_STYLES } from '@/lib/constants';

export default function Badge({ type }: { type: string }) {
  const label: Record<string, string> = { POPULAR: '인기', NEW: '신규', EVENT: '이벤트', SOLD_OUT: '품절' };
  return (
    <span className={`inline-block px-2 py-0.5 rounded-full text-[11px] font-bold ${BADGE_STYLES[type] || ''}`}>
      {label[type] || type}
    </span>
  );
}

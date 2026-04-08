'use client';

import { useEffect, useState } from 'react';

interface ToastProps { message: string; onClose: () => void; }

export default function Toast({ message, onClose }: ToastProps) {
  const [visible, setVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => { setVisible(false); onClose(); }, 3000);
    return () => clearTimeout(timer);
  }, [onClose]);

  if (!visible) return null;
  return (
    <div className="fixed top-16 left-1/2 -translate-x-1/2 z-50 max-w-[90%]"
         data-testid="toast">
      <div className="bg-black/60 text-white px-4 py-2 rounded-lg text-t7 text-center">
        {message}
      </div>
    </div>
  );
}

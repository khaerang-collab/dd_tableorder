interface ConfirmDialogProps {
  title: string;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmDialog({ title, message, onConfirm, onCancel }: ConfirmDialogProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" data-testid="confirm-dialog">
      <div className="bg-white rounded-2xl p-6 mx-4 max-w-sm w-full shadow-depth1">
        <h3 className="text-t5 text-coolGray-900 font-bold">{title}</h3>
        <p className="text-t7 text-coolGray-500 mt-2">{message}</p>
        <div className="flex gap-3 mt-5">
          <button onClick={onCancel} className="flex-1 py-3 rounded-xl bg-coolGray-100 text-coolGray-700 text-t6"
                  data-testid="confirm-cancel">취소</button>
          <button onClick={onConfirm} className="flex-1 py-3 rounded-xl bg-red-300 text-white text-t6"
                  data-testid="confirm-ok">확인</button>
        </div>
      </div>
    </div>
  );
}

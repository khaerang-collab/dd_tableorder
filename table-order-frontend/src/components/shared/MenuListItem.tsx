import Badge from './Badge';
import { formatPrice } from '@/lib/utils';
import type { MenuItem } from '@/types';

interface Props { menu: MenuItem; onClick?: () => void; }

export default function MenuListItem({ menu, onClick }: Props) {
  return (
    <div className="flex items-center justify-between px-6 py-3 cursor-pointer active:bg-coolGray-50"
         onClick={onClick} data-testid={`menu-item-${menu.id}`}>
      <div className="flex-1 pr-4">
        {menu.badgeType && <Badge type={menu.badgeType} />}
        <p className="text-t5 text-coolGray-900 mt-1">{menu.name}</p>
        <p className="text-t6 text-coolGray-900 mt-0.5">{formatPrice(menu.price)}</p>
        {menu.description && (
          <p className="text-t7 text-coolGray-500 mt-0.5 line-clamp-2">{menu.description}</p>
        )}
      </div>
      {menu.imageUrl && (
        <img src={menu.imageUrl} alt={menu.name}
             className="w-[100px] h-[100px] rounded-[18px] object-cover flex-shrink-0" />
      )}
    </div>
  );
}

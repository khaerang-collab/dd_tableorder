'use client';

interface Props {
  categories: { id: number; name: string }[];
  activeId: number | null;
  onSelect: (id: number) => void;
}

export default function CategoryTabs({ categories, activeId, onSelect }: Props) {
  return (
    <div className="sticky top-[52px] z-10 bg-white border-b border-coolGray-200">
      <div className="flex overflow-x-auto scrollbar-hide px-4 gap-1">
        {categories.map((cat) => (
          <button key={cat.id} onClick={() => onSelect(cat.id)}
                  className={`flex-shrink-0 px-3 py-3 text-t5 whitespace-nowrap border-b-2 transition-colors ${
                    activeId === cat.id
                      ? 'text-coolGray-900 font-bold border-yellow-400'
                      : 'text-coolGray-500 border-transparent'
                  }`}
                  data-testid={`category-tab-${cat.id}`}>
            {cat.name}
          </button>
        ))}
      </div>
    </div>
  );
}

package com.tableorder.common.exception;

public class CategoryHasMenusException extends BusinessException {
    public CategoryHasMenusException() {
        super("CATEGORY_HAS_MENUS", "소속 메뉴가 있는 카테고리는 삭제할 수 없습니다");
    }
}

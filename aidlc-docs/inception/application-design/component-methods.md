# Component Methods - 테이블오더 서비스

> 메서드 시그니처 정의. 상세 비즈니스 규칙은 Functional Design에서 정의.

---

## 1. Auth 도메인

### AuthController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `loginAdmin(LoginRequest)` | POST | `/api/admin/auth/login` | 관리자 로그인 |
| `loginTable(TableLoginRequest)` | POST | `/api/customer/auth/table-login` | 태블릿 테이블 로그인 |
| `accessTable(storeId, tableNumber)` | POST | `/api/customer/auth/qr-access` | QR 코드 접속 |
| `refreshToken(RefreshRequest)` | POST | `/api/auth/refresh` | 토큰 갱신 |

### AuthService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| `authenticateAdmin(storeId, username, password)` | String, String, String | TokenResponse | 관리자 인증 + JWT 발급 |
| `authenticateTable(storeId, tableNumber)` | String, Integer | TokenResponse | 태블릿 자동 로그인 |
| `accessByQr(storeId, tableNumber)` | String, Integer | CustomerSessionResponse | QR 접속 (개인 세션 생성) |
| `checkLoginAttempt(storeId, username)` | String, String | void | 로그인 시도 제한 확인 |

---

## 2. Store 도메인

### StoreController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `getStoreInfo(storeId)` | GET | `/api/stores/{storeId}` | 매장 정보 조회 |

---

## 3. Table 도메인

### TableController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `createTable(storeId, CreateTableRequest)` | POST | `/api/admin/stores/{storeId}/tables` | 테이블 등록 + QR 생성 |
| `getTables(storeId)` | GET | `/api/admin/stores/{storeId}/tables` | 테이블 목록 조회 |
| `getTableQr(storeId, tableId)` | GET | `/api/admin/stores/{storeId}/tables/{tableId}/qr` | QR 코드 조회 |
| `completeTable(storeId, tableId)` | POST | `/api/admin/stores/{storeId}/tables/{tableId}/complete` | 이용 완료 |
| `getActiveUsers(storeId, tableId)` | GET | `/api/customer/tables/{tableId}/active-users` | 동시 접속자 수 |

### TableService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| `createTable(storeId, tableNumber)` | Long, Integer | TableResponse | 테이블 등록 + QR URL 생성 |
| `getTablesByStore(storeId)` | Long | List\<TableResponse\> | 매장 테이블 목록 |
| `generateQrCode(storeId, tableId)` | Long, Long | byte[] | QR 코드 이미지 생성 |

### TableSessionService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| `getOrCreateSession(tableId)` | Long | TableSession | 활성 세션 조회 또는 생성 |
| `completeSession(tableId)` | Long | void | 이용 완료 (주문→이력 이동, 장바구니 초기화) |
| `getActiveUserCount(tableId)` | Long | Integer | 동시 접속자 수 |
| `registerUser(tableId, deviceId)` | Long, String | void | 접속자 등록 |
| `unregisterUser(tableId, deviceId)` | Long, String | void | 접속자 해제 |

---

## 4. Menu 도메인

### MenuController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `getMenus(storeId)` | GET | `/api/stores/{storeId}/menus` | 메뉴 목록 (카테고리별) |
| `getMenu(menuId)` | GET | `/api/menus/{menuId}` | 메뉴 상세 |
| `createMenu(storeId, CreateMenuRequest)` | POST | `/api/admin/stores/{storeId}/menus` | 메뉴 등록 |
| `updateMenu(menuId, UpdateMenuRequest)` | PUT | `/api/admin/menus/{menuId}` | 메뉴 수정 |
| `deleteMenu(menuId)` | DELETE | `/api/admin/menus/{menuId}` | 메뉴 삭제 |
| `updateMenuOrder(storeId, UpdateOrderRequest)` | PUT | `/api/admin/stores/{storeId}/menus/order` | 메뉴 순서 변경 |

### CategoryController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `getCategories(storeId)` | GET | `/api/stores/{storeId}/categories` | 카테고리 목록 |
| `createCategory(storeId, CreateCategoryRequest)` | POST | `/api/admin/stores/{storeId}/categories` | 카테고리 등록 |
| `updateCategory(categoryId, UpdateCategoryRequest)` | PUT | `/api/admin/categories/{categoryId}` | 카테고리 수정 |
| `deleteCategory(categoryId)` | DELETE | `/api/admin/categories/{categoryId}` | 카테고리 삭제 |

### ImageService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| `uploadImage(MultipartFile)` | MultipartFile | String (URL) | 이미지 업로드 → 저장 경로 반환 |
| `deleteImage(imageUrl)` | String | void | 이미지 삭제 |

---

## 5. Cart 도메인

### CartController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `getCart(tableSessionId)` | GET | `/api/customer/sessions/{sessionId}/cart` | 공유 장바구니 조회 |
| `addToCart(sessionId, AddCartRequest)` | POST | `/api/customer/sessions/{sessionId}/cart/items` | 장바구니 추가 |
| `updateCartItem(cartItemId, UpdateCartRequest)` | PUT | `/api/customer/cart/items/{cartItemId}` | 수량 변경 |
| `removeCartItem(cartItemId)` | DELETE | `/api/customer/cart/items/{cartItemId}` | 장바구니 항목 삭제 |
| `clearMyCart(sessionId, deviceId)` | DELETE | `/api/customer/sessions/{sessionId}/cart/mine` | 내 메뉴 전체 삭제 |

### CartWebSocketHandler
| 메서드 | 설명 |
|--------|------|
| `handleConnect(session)` | WebSocket 연결 시 테이블 세션 구독 |
| `handleMessage(session, message)` | 장바구니 변경 메시지 수신 및 브로드캐스트 |
| `handleDisconnect(session)` | 연결 해제 시 접속자 수 업데이트 |
| `broadcastCartUpdate(tableSessionId, event)` | 테이블 전체 접속자에게 장바구니 변경 알림 |

### CartService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| `getCart(sessionId)` | Long | CartResponse | 공유 장바구니 조회 (내 메뉴/멤버 메뉴 구분) |
| `addItem(sessionId, menuId, quantity, deviceId)` | Long, Long, Integer, String | CartItemResponse | 메뉴 추가 |
| `updateItemQuantity(cartItemId, quantity, deviceId)` | Long, Integer, String | CartItemResponse | 수량 변경 (본인 메뉴만) |
| `removeItem(cartItemId, deviceId)` | Long, String | void | 항목 삭제 (본인 메뉴만) |
| `clearCart(sessionId)` | Long | void | 장바구니 전체 비우기 (주문 확정 시) |

---

## 6. Order 도메인

### OrderController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `createOrder(sessionId, CreateOrderRequest)` | POST | `/api/customer/sessions/{sessionId}/orders` | 주문 생성 (장바구니→주문) |
| `getOrders(sessionId)` | GET | `/api/customer/sessions/{sessionId}/orders` | 현재 세션 주문 목록 |
| `updateOrderStatus(orderId, UpdateStatusRequest)` | PUT | `/api/admin/orders/{orderId}/status` | 주문 상태 변경 |
| `deleteOrder(orderId)` | DELETE | `/api/admin/orders/{orderId}` | 주문 삭제 |
| `getOrderHistory(storeId, tableId, params)` | GET | `/api/admin/stores/{storeId}/tables/{tableId}/order-history` | 과거 주문 내역 |

### OrderSseController
| 메서드 | HTTP | 경로 | 설명 |
|--------|------|------|------|
| `streamOrders(storeId)` | GET | `/api/admin/stores/{storeId}/orders/stream` | SSE 주문 실시간 스트림 |

### OrderService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| `createOrder(sessionId)` | Long | OrderResponse | 장바구니 → 주문 변환 + 장바구니 비우기 |
| `getOrdersBySession(sessionId)` | Long | List\<OrderResponse\> | 세션 주문 목록 |
| `getOrdersByStore(storeId)` | Long | List\<OrderResponse\> | 매장 전체 활성 주문 |
| `updateStatus(orderId, status)` | Long, OrderStatus | OrderResponse | 상태 변경 + SSE 이벤트 발행 |
| `deleteOrder(orderId)` | Long | void | 주문 삭제 + 총액 재계산 |

### OrderHistoryService
| 메서드 | 입력 | 출력 | 설명 |
|--------|------|------|------|
| `archiveOrders(sessionId)` | Long | void | 세션 주문 → 이력 이동 |
| `getHistory(storeId, tableId, dateRange)` | Long, Long, DateRange | List\<OrderHistoryResponse\> | 과거 이력 조회 |

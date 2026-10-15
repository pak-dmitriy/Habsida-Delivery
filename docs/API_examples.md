## Документация API — Примеры запросов
### 1. Авторизация логин
* **URL:** `POST /auth/login`
* **Описание:** Аутентификация пользователя и получение JWT-токена.

### Тело запроса (Request Body):
```json
{
"email": "user@user.com",
"password": "987456321" 
}
```
### Ответ (Response 200 ОК)
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "tokenType": "Bearer"
}
```

### 2. Создание магазина 
* **URL:** `/api/stores`
* **Описание:** Заполнение полей магазина 

### Тело запроса (Request body)
```json
{
"name": "Tasty King",
"storeSlug": "tasty-king",
"description": "Eat more",
"typeStoreServices": "FAST_FOOD",
"phone": "01023654578",
"logo": "logo.png",
"pickupAddress": "Asan 23/5",
"status": "ACTIVE",
"deliveryFee": 0
}
```

### Тело ответа (Response body 201 CREATED)
```json
{
"id": 1,
"name": "Tasty King",
"storeSlug": "tasty-king",
"description": "Eat more",
"typeStoreServices": "FAST_FOOD",
"phone": "01023654578",
"logo": "logo.png",
"status": "ACTIVE",
"pickupAddress": "Asan 23/5",
"deliveryFee": 0
}
```
### 3. Создание категории
* **URL:** `/stores/{storeId}/categories`
* **Описание:** Заполнение полей категории и указание id магазина

***storeId:*** 1

### Тело запроса (Request body)
```json
{
  "name": "Hot-dog",
  "description": "Delicious",
  "categoryPhoto": "hotdog.png"
}
```

### Тело ответа (Response body 201 CREATED)
```json
{
  "id": 1,
  "name": "Hot-dog",
  "description": "Delicious",
  "categoryPhoto": "hotdog.png",
  "storeId": 1
}
```
### 4. Создание заказа
* **URL:** `/stores/{storeId}/orders`
* **Описание:** Создание заказа и указание id магазина,
для заказа с DELIVERY у магазина должны быть настройки доставки и зона с названием города клиента (здесь Asan), иначе придёт ошибка.


* ***storeId:*** 1

### Тело запроса (Request body)
```json
{
  "type": "DELIVERY",
  "method": "ONLINE",
  "customerNote": "Call before arrival",
  "customerId": 1,
  "customerAddressId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 1,
      "modifierOptionIds": [
        1
      ]
    }
  ]
}
```

### Тело ответа (Response body 201 CREATED)
```json
{
  "id": 1,
  "storeId": 1,
  "customerId": 1,
  "customerAddressId": 1,
  "orderType": "DELIVERY",
  "orderStatus": "CREATED",
  "rejectReason": null,
  "customerNote": "Call before arrival",
  "subtotal": 10,
  "deliveryFee": 0,
  "discountTotal": 0,
  "total": 10,
  "orderItems": [
    {
      "id": 1,
      "orderId": 1,
      "productName": "Burger",
      "productPrice": 10,
      "quantity": 1,
      "subtotal": 10,
      "discountPercent": 0,
      "discountAmount": 0,
      "orderItemModifiers": [
        {
          "id": 1,
          "optionName": "small",
          "priceDelta": 0
        }
      ]
    }
  ],
  "orderNumber": "1",
  "currency": "KRW",
  "deliveryCity": "Asan",
  "deliveryStreet": "Street 2",
  "deliveryHouse": "23",
  "deliveryApartment": "1"
}
```
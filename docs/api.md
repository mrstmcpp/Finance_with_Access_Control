# ZorvynFinance API Documentation

## 1) Overview

- **Auth type**: JWT Bearer token
- **Base path**: relative to your server host (for example: `http://localhost:8080`)
- **Content type**: `application/json`
- **Rate limiting**:
  - Configured by:
    - `app.rate-limit.max-requests` (default in project: `120`)
    - `app.rate-limit.window-seconds` (default in project: `60`)
  - Exceeded requests return `429 Too Many Requests`

---

## 2) Authentication

### Header

Use this header for secured endpoints:

```http
Authorization: Bearer <jwt_token>
```

### Auth endpoints

#### POST `/auth/register`
Create a new user (default role assigned by backend: `VIEWER`).

Request body:

```json
{
  "username": "john_doe",
  "password": "secret123"
}
```

Validation:
- `username`: required, non-blank
- `password`: required, minimum 6 chars

Responses:
- `201 Created` -> `{ "token": "..." }`
- `400 Bad Request` -> validation error
- `409 Conflict` -> username already exists

#### POST `/auth/login`
Login user and receive token.

Request body:

```json
{
  "username": "john_doe",
  "password": "secret123"
}
```

Responses:
- `200 OK` -> `{ "token": "..." }`
- `400 Bad Request` -> validation error
- `401 Unauthorized` -> invalid credentials / inactive user

---

## 3) Role Access Matrix

| Endpoint Group | VIEWER | ANALYST | ADMIN |
|---|---:|---:|---:|
| `GET /transactions/**` | No | Yes | Yes |
| `POST/PUT/PATCH/DELETE /transactions/**` | No | No | Yes |
| `GET /dashboard/**` | Yes | Yes | Yes |
| `/users/**` | No | No | Yes |

Authorization failures:
- `401` when unauthenticated
- `403` when authenticated but role is not allowed

---

## 4) Transactions API

### Transaction model returned by API

```json
{
  "id": "...",
  "createdAt": "2026-04-03T12:00:00Z",
  "updatedAt": "2026-04-03T12:05:00Z",
  "type": "EXPENSE",
  "category": "FOOD",
  "date": "2026-04-03",
  "description": "Lunch",
  "amount": 450,
  "userId": "...",
  "deleted": false,
  "deletedAt": null
}
```

### Enums

- `Type`: `INCOME`, `EXPENSE`
- `Category`: `FOOD`, `ENTERTAINMENT`, `BILLS`, `TRANSPORT`, `SHOPPING`, `HEALTH`, `EDUCATION`, `INVESTMENT`, `SALARY`, `TRANSFER`, `OTHER`

### POST `/transactions/`
Create transaction.

Request body:

```json
{
  "type": "EXPENSE",
  "category": "FOOD",
  "date": "2026-04-03",
  "amount": 450,
  "description": "Lunch"
}
```

Validation:
- `type`: required
- `category`: required
- `date`: required, must be today/past
- `amount`: required, > 0
- `description`: optional, max 255 chars

Responses:
- `201 Created`
- `400 Bad Request`

### GET `/transactions/{transactionId}`
Get single transaction.

Responses:
- `200 OK`
- `400 Bad Request` (invalid/missing path value)
- `404 Not Found` (not found, not owned, or soft-deleted)

### PATCH `/transactions/{transactionId}`
Update transaction fields partially.

Request body (all fields optional, but at least one required):

```json
{
  "category": "TRANSPORT",
  "amount": 120
}
```

Validation:
- `date`: if provided, must be today/past
- `amount`: if provided, > 0
- `description`: if provided, max 255 chars
- empty payload -> invalid operation

Responses:
- `200 OK`
- `400 Bad Request`
- `404 Not Found`

### DELETE `/transactions/{transactionId}`
Soft delete transaction.

Behavior:
- Record is not physically removed
- Backend sets `deleted=true` and `deletedAt`

Responses:
- `204 No Content`
- `404 Not Found`

### GET `/transactions`
List transactions with filters, pagination, and text search.

Query params:

| Param | Type | Required | Notes |
|---|---|---|---|
| `type` | enum `Type` | No | `INCOME` or `EXPENSE` |
| `category` | enum `Category` | No | Category filter |
| `startDate` | `yyyy-MM-dd` | No | Must be sent with `endDate` |
| `endDate` | `yyyy-MM-dd` | No | Must be sent with `startDate` |
| `search` | string | No | Max 100 chars; matches description/category/type |
| `page` | int | No | Default `0`, min `0` |
| `size` | int | No | Default `10`, min `1`, max `100` |

Response shape:

```json
{
  "data": [ { "id": "..." } ],
  "currentPage": 0,
  "pageSize": 10,
  "totalRecords": 42,
  "totalPages": 5
}
```

Notes:
- Soft-deleted transactions are excluded.
- Invalid date range (missing one side / start > end) returns `400`.

---

## 5) Dashboard API

All dashboard routes are under `/dashboard` and require role `VIEWER`, `ANALYST`, or `ADMIN`.

### Trend enum
- `TrendType`: `WEEKLY`, `MONTHLY`

### GET `/dashboard/summary`
Returns combined summary payload.

Query params:
- `startDate`, `endDate` (optional, but must be together)
- `recentLimit` (default 5, min 1, max 50)
- `trendType` (default `MONTHLY`)
- `periods` (default 6, min 1, max 24)

Response:

```json
{
  "totalIncome": 100000,
  "totalExpense": 45000,
  "netBalance": 55000,
  "categoryTotals": [
    { "category": "FOOD", "income": 0, "expense": 5000, "net": -5000 }
  ],
  "recentActivity": [
    {
      "transactionId": "...",
      "type": "EXPENSE",
      "category": "FOOD",
      "amount": 450,
      "date": "2026-04-03",
      "description": "Lunch"
    }
  ],
  "trends": [
    { "label": "2026-04", "income": 20000, "expense": 7000, "net": 13000 }
  ]
}
```

### GET `/dashboard/totals`
Returns total income/expense for date range.

### GET `/dashboard/categories`
Returns category-wise totals.

### GET `/dashboard/recent`
Query:
- `limit` default 5, min 1, max 50

### GET `/dashboard/trends`
Query:
- `startDate`, `endDate` optional pair
- `trendType` default `MONTHLY`
- `periods` default 6, min 1, max 24

Notes:
- All dashboard calculations exclude soft-deleted transactions.

---

## 6) Error Response Format

Most validation/business errors return this JSON format:

```json
{
  "message": "Validation failed",
  "status": 400,
  "timestamp": "2026-04-03T14:00:00"
}
```

Common status codes:
- `400` validation/invalid input/invalid operation
- `401` invalid token or unauthenticated
- `403` forbidden by role
- `404` not found
- `409` conflict (e.g., existing user)
- `429` rate limit exceeded
- `500` fallback internal error

Security-layer error examples (`401`, `403`, `429`) are also returned as JSON with the same core fields.

---

## 7) Soft Delete Notes

- Deleting a transaction performs **soft delete**.
- Soft-deleted transactions are hidden from:
  - `GET /transactions`
  - `GET /transactions/{id}`
  - dashboard endpoints
- Current API does not expose a restore endpoint.

---

## 8) User Administration API

All `/users/**` endpoints require role `ADMIN`.

### User fields related to promotion

The user record now keeps promotion audit metadata:

- `promotedByUserId`: ID of the admin who performed the latest promotion
- `promotionDate`: timestamp for when the latest promotion happened

### PATCH `/users/{userId}/status`

Mark user as active/inactive.

Request body:

```json
{
  "status": "INACTIVE"
}
```

Allowed status values:
- `ACTIVE`
- `INACTIVE`

Response (`200 OK`):

```json
{
  "id": "...",
  "username": "john_doe",
  "role": "VIEWER",
  "status": "INACTIVE",
  "promotedByUserId": null,
  "promotionDate": null
}
```

### PATCH `/users/{userId}/promote`

Promote a user role (this endpoint does not support demotion).

Request body:

```json
{
  "role": "ANALYST"
}
```

Allowed promotion role values:
- `ANALYST`
- `ADMIN`

Response (`200 OK`):

```json
{
  "id": "...",
  "username": "john_doe",
  "role": "ANALYST",
  "status": "ACTIVE",
  "promotedByUserId": "admin_user_id",
  "promotionDate": "2026-04-05T10:30:00Z"
}
```

Errors:
- `400` invalid role/status payload, demotion attempt, or no-op promotion
- `404` user not found


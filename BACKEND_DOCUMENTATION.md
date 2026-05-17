# Subsment Mobile - Backend Dokümantasyonu

## Genel Bilgiler
- **Proje Adı:** Abonelik Takip Uygulaması (Subsment)
- **Frontend:** React Native + Expo
- **Database:** PostgreSQL veya MongoDB önerilir
- **Authentication:** JWT (JSON Web Token)
- **API Format:** RESTful JSON API
- **Dil:** Node.js + Express.js veya Python + FastAPI

---

## 1. Veritabanı Modelleri

### 1.1 User (Kullanıcı)
```
id: UUID (Primary Key)
email: String (UNIQUE, NOT NULL)
name: String (NOT NULL)
password: String (HASH, NOT NULL)
created_at: DateTime
updated_at: DateTime
deleted_at: DateTime (Soft delete)
```

### 1.2 Subscription (Abonelik)
```
id: UUID (Primary Key)
user_id: UUID (Foreign Key -> User, NOT NULL)
name: String (NOT NULL)
category: String (NOT NULL) 
  - Enum: 'Eğlence', 'Müzik', 'Bulut Depolama', 'Yazılım', 'Spor', 'Oyun', 'Haberler', 'Eğitim', 'Diğer'
amount: Decimal (NOT NULL)
currency: String (NOT NULL)
  - Enum: 'TRY', 'USD', 'EUR', 'GBP'
period: String (NOT NULL)
  - Enum: 'monthly', 'yearly', 'weekly', 'custom'
custom_period_label: String (Optional)
first_payment_date: Date (NOT NULL)
next_payment_date: Date (NOT NULL)
payment_method: String (NOT NULL)
card_last_four: String (Optional)
status: String (NOT NULL)
  - Enum: 'active', 'inactive', 'cancelled'
free_trial_end_date: Date (Optional)
cancel_reminder_date: Date (Optional)
color: String (Hex code, NOT NULL)
emoji: String (NOT NULL)
notes: String (Optional)
created_at: DateTime
updated_at: DateTime
deleted_at: DateTime (Soft delete)
```

### 1.3 Profile (Profil Ayarları)
```
id: UUID (Primary Key)
user_id: UUID (Foreign Key -> User, UNIQUE, NOT NULL)
primary_currency: String (NOT NULL)
  - Enum: 'TRY', 'USD', 'EUR', 'GBP'
  - Default: 'TRY'
convert_foreign_currency: Boolean (Default: true)
dark_mode: Boolean (Default: false)
notifications_enabled: Boolean (Default: true)
cancel_reminder_days: Integer (Default: 3)
created_at: DateTime
updated_at: DateTime
```

### 1.4 PaymentHistory (Ödeme Geçmişi)
```
id: UUID (Primary Key)
subscription_id: UUID (Foreign Key -> Subscription, NOT NULL)
payment_date: Date (NOT NULL)
amount: Decimal (NOT NULL)
currency: String (NOT NULL)
status: String (NOT NULL)
  - Enum: 'paid', 'failed', 'pending'
created_at: DateTime
```

### 1.5 Category (Kategori) - Optional (eğer dinamik kategoriler istenirse)
```
id: UUID (Primary Key)
name: String (UNIQUE, NOT NULL)
emoji: String (NOT NULL)
color: String (Hex code, NOT NULL)
created_at: DateTime
```

---

## 2. Authentication (Kimlik Doğrulama) Servisi

### 2.1 Register Endpoint
**Method:** POST  
**Path:** `/api/auth/register`  
**Auth Required:** No

**Request Body:**
```json
{
  "name": "string",
  "email": "string (email format)",
  "password": "string (min 8 chars)"
}
```

**Response (200 Created):**
```json
{
  "success": true,
  "user": {
    "id": "uuid",
    "name": "string",
    "email": "string"
  },
  "token": "jwt_token",
  "message": "Kayıt başarılı"
}
```

**Error Responses:**
- 400: Validation error (eksik/hatalı veri)
- 409: Email already exists (email zaten kayıtlı)

### 2.2 Login Endpoint
**Method:** POST  
**Path:** `/api/auth/login`  
**Auth Required:** No

**Request Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "user": {
    "id": "uuid",
    "name": "string",
    "email": "string"
  },
  "token": "jwt_token",
  "message": "Giriş başarılı"
}
```

**Error Responses:**
- 401: Invalid credentials (geçersiz kullanıcı adı/şifre)
- 404: User not found (kullanıcı bulunamadı)

### 2.3 Logout Endpoint
**Method:** POST  
**Path:** `/api/auth/logout`  
**Auth Required:** Yes (Bearer Token)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Çıkış başarılı"
}
```

### 2.4 Refresh Token Endpoint
**Method:** POST  
**Path:** `/api/auth/refresh`  
**Auth Required:** Yes (Bearer Token)

**Response (200 OK):**
```json
{
  "success": true,
  "token": "new_jwt_token"
}
```

**Error Responses:**
- 401: Invalid token (geçersiz token)

### 2.5 Verify Token Endpoint
**Method:** GET  
**Path:** `/api/auth/verify`  
**Auth Required:** Yes (Bearer Token)

**Response (200 OK):**
```json
{
  "success": true,
  "valid": true
}
```

---

## 3. User (Kullanıcı) Servisi

### 3.1 Get User Profile
**Method:** GET  
**Path:** `/api/users/me`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "user": {
    "id": "uuid",
    "name": "string",
    "email": "string",
    "created_at": "datetime"
  }
}
```

### 3.2 Update User Profile
**Method:** PUT  
**Path:** `/api/users/me`  
**Auth Required:** Yes

**Request Body:**
```json
{
  "name": "string (optional)",
  "email": "string (optional)",
  "password": "string (optional, old password required)"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "user": {
    "id": "uuid",
    "name": "string",
    "email": "string"
  },
  "message": "Profil güncellendi"
}
```

### 3.3 Delete User Account
**Method:** DELETE  
**Path:** `/api/users/me`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Hesap silindi"
}
```

---

## 4. Subscription (Abonelik) Servisi

### 4.1 Get All Subscriptions
**Method:** GET  
**Path:** `/api/subscriptions`  
**Auth Required:** Yes

**Query Parameters:**
- `status` (optional): 'active', 'inactive', 'cancelled', 'all'
- `category` (optional): kategori adı
- `search` (optional): arama metni
- `page` (optional): sayfa numarası (default: 1)
- `limit` (optional): sonuç limiti (default: 50)

**Response (200 OK):**
```json
{
  "success": true,
  "subscriptions": [
    {
      "id": "uuid",
      "name": "string",
      "category": "string",
      "amount": "number",
      "currency": "string",
      "period": "string",
      "custom_period_label": "string (optional)",
      "first_payment_date": "date",
      "next_payment_date": "date",
      "payment_method": "string",
      "card_last_four": "string (optional)",
      "status": "string",
      "free_trial_end_date": "date (optional)",
      "cancel_reminder_date": "date (optional)",
      "color": "string",
      "emoji": "string",
      "notes": "string (optional)",
      "created_at": "datetime",
      "updated_at": "datetime"
    }
  ],
  "pagination": {
    "page": "number",
    "limit": "number",
    "total": "number",
    "pages": "number"
  }
}
```

### 4.2 Get Subscription by ID
**Method:** GET  
**Path:** `/api/subscriptions/{id}`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "subscription": {
    "id": "uuid",
    "name": "string",
    "category": "string",
    "amount": "number",
    "currency": "string",
    "period": "string",
    "custom_period_label": "string (optional)",
    "first_payment_date": "date",
    "next_payment_date": "date",
    "payment_method": "string",
    "card_last_four": "string (optional)",
    "status": "string",
    "free_trial_end_date": "date (optional)",
    "cancel_reminder_date": "date (optional)",
    "color": "string",
    "emoji": "string",
    "notes": "string (optional)",
    "created_at": "datetime",
    "updated_at": "datetime"
  }
}
```

**Error Responses:**
- 404: Subscription not found

### 4.3 Create Subscription
**Method:** POST  
**Path:** `/api/subscriptions`  
**Auth Required:** Yes

**Request Body:**
```json
{
  "name": "string (required)",
  "category": "string (required)",
  "amount": "number (required, > 0)",
  "currency": "string (required, enum)",
  "period": "string (required, enum)",
  "custom_period_label": "string (optional)",
  "first_payment_date": "date (required, YYYY-MM-DD)",
  "next_payment_date": "date (required, YYYY-MM-DD)",
  "payment_method": "string (required)",
  "card_last_four": "string (optional)",
  "status": "string (required, enum)",
  "free_trial_end_date": "date (optional, YYYY-MM-DD)",
  "cancel_reminder_date": "date (optional, YYYY-MM-DD)",
  "color": "string (required, hex code)",
  "emoji": "string (required)",
  "notes": "string (optional)"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "subscription": {
    "id": "uuid",
    "name": "string",
    "category": "string",
    "amount": "number",
    "currency": "string",
    "period": "string",
    "custom_period_label": "string (optional)",
    "first_payment_date": "date",
    "next_payment_date": "date",
    "payment_method": "string",
    "card_last_four": "string (optional)",
    "status": "string",
    "free_trial_end_date": "date (optional)",
    "cancel_reminder_date": "date (optional)",
    "color": "string",
    "emoji": "string",
    "notes": "string (optional)",
    "created_at": "datetime",
    "updated_at": "datetime"
  },
  "message": "Abonelik oluşturuldu"
}
```

**Error Responses:**
- 400: Validation error
- 422: Invalid enum value

### 4.4 Update Subscription
**Method:** PUT  
**Path:** `/api/subscriptions/{id}`  
**Auth Required:** Yes

**Request Body:** (Tüm alanlar optional)
```json
{
  "name": "string (optional)",
  "category": "string (optional)",
  "amount": "number (optional)",
  "currency": "string (optional)",
  "period": "string (optional)",
  "custom_period_label": "string (optional)",
  "first_payment_date": "date (optional)",
  "next_payment_date": "date (optional)",
  "payment_method": "string (optional)",
  "card_last_four": "string (optional)",
  "status": "string (optional)",
  "free_trial_end_date": "date (optional)",
  "cancel_reminder_date": "date (optional)",
  "color": "string (optional)",
  "emoji": "string (optional)",
  "notes": "string (optional)"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "subscription": {
    "id": "uuid",
    "name": "string",
    "category": "string",
    "amount": "number",
    "currency": "string",
    "period": "string",
    "custom_period_label": "string (optional)",
    "first_payment_date": "date",
    "next_payment_date": "date",
    "payment_method": "string",
    "card_last_four": "string (optional)",
    "status": "string",
    "free_trial_end_date": "date (optional)",
    "cancel_reminder_date": "date (optional)",
    "color": "string",
    "emoji": "string",
    "notes": "string (optional)",
    "created_at": "datetime",
    "updated_at": "datetime"
  },
  "message": "Abonelik güncellendi"
}
```

**Error Responses:**
- 404: Subscription not found
- 400: Validation error

### 4.5 Delete Subscription
**Method:** DELETE  
**Path:** `/api/subscriptions/{id}`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Abonelik silindi"
}
```

**Error Responses:**
- 404: Subscription not found

### 4.6 Update Subscription Status
**Method:** PATCH  
**Path:** `/api/subscriptions/{id}/status`  
**Auth Required:** Yes

**Request Body:**
```json
{
  "status": "string (enum: 'active', 'inactive', 'cancelled')"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "subscription": {
    "id": "uuid",
    "status": "string",
    "updated_at": "datetime"
  },
  "message": "Abonelik durumu güncellendi"
}
```

---

## 5. Payment History (Ödeme Geçmişi) Servisi

### 5.1 Get Payment History
**Method:** GET  
**Path:** `/api/subscriptions/{id}/payment-history`  
**Auth Required:** Yes

**Query Parameters:**
- `limit` (optional): sonuç limiti (default: 12)

**Response (200 OK):**
```json
{
  "success": true,
  "payments": [
    {
      "id": "uuid",
      "subscription_id": "uuid",
      "payment_date": "date",
      "amount": "number",
      "currency": "string",
      "status": "string (paid, failed, pending)",
      "created_at": "datetime"
    }
  ]
}
```

**Error Responses:**
- 404: Subscription not found

### 5.2 Create Payment Record (Backend'de otomatik oluşturulacak)
**Method:** POST  
**Path:** `/api/subscriptions/{id}/payment-history`  
**Auth Required:** Yes

**Request Body:**
```json
{
  "payment_date": "date",
  "status": "string (enum: 'paid', 'failed', 'pending')"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "payment": {
    "id": "uuid",
    "subscription_id": "uuid",
    "payment_date": "date",
    "amount": "number",
    "currency": "string",
    "status": "string",
    "created_at": "datetime"
  }
}
```

---

## 6. Profile (Profil Ayarları) Servisi

### 6.1 Get Profile Settings
**Method:** GET  
**Path:** `/api/profile`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "profile": {
    "id": "uuid",
    "user_id": "uuid",
    "primary_currency": "string",
    "convert_foreign_currency": "boolean",
    "dark_mode": "boolean",
    "notifications_enabled": "boolean",
    "cancel_reminder_days": "number",
    "created_at": "datetime",
    "updated_at": "datetime"
  }
}
```

**Error Responses:**
- 404: Profile not found (İlk kez login olan kullanıcı için default values dönüleceği veya create edilecek)

### 6.2 Update Profile Settings
**Method:** PUT  
**Path:** `/api/profile`  
**Auth Required:** Yes

**Request Body:**
```json
{
  "primary_currency": "string (optional, enum)",
  "convert_foreign_currency": "boolean (optional)",
  "dark_mode": "boolean (optional)",
  "notifications_enabled": "boolean (optional)",
  "cancel_reminder_days": "number (optional, 1-30)"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "profile": {
    "id": "uuid",
    "user_id": "uuid",
    "primary_currency": "string",
    "convert_foreign_currency": "boolean",
    "dark_mode": "boolean",
    "notifications_enabled": "boolean",
    "cancel_reminder_days": "number",
    "created_at": "datetime",
    "updated_at": "datetime"
  },
  "message": "Profil ayarları güncellendi"
}
```

**Error Responses:**
- 400: Validation error

---

## 7. Statistics (İstatistikler) Servisi

### 7.1 Get Dashboard Statistics
**Method:** GET  
**Path:** `/api/statistics/dashboard`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "statistics": {
    "total_monthly_amount": "number",
    "total_yearly_estimate": "number",
    "active_subscription_count": "number",
    "inactive_subscription_count": "number",
    "cancelled_subscription_count": "number",
    "upcoming_payments": [
      {
        "id": "uuid",
        "name": "string",
        "amount": "number",
        "currency": "string",
        "next_payment_date": "date",
        "days_until_payment": "number",
        "status": "string"
      }
    ]
  }
}
```

### 7.2 Get Category Statistics
**Method:** GET  
**Path:** `/api/statistics/categories`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "categories": [
    {
      "name": "string",
      "emoji": "string",
      "color": "string",
      "count": "number",
      "total_amount": "number",
      "currency": "string"
    }
  ]
}
```

### 7.3 Get Currency Statistics
**Method:** GET  
**Path:** `/api/statistics/currencies`  
**Auth Required:** Yes

**Response (200 OK):**
```json
{
  "success": true,
  "currencies": [
    {
      "currency": "string",
      "symbol": "string",
      "total_amount": "number",
      "count": "number",
      "percentage": "number"
    }
  ]
}
```

### 7.4 Get Monthly Breakdown
**Method:** GET  
**Path:** `/api/statistics/monthly`  
**Auth Required:** Yes

**Query Parameters:**
- `months` (optional): ay sayısı (default: 12)

**Response (200 OK):**
```json
{
  "success": true,
  "months": [
    {
      "month": "string (YYYY-MM)",
      "total_amount": "number",
      "currency": "string",
      "subscription_count": "number"
    }
  ]
}
```

### 7.5 Get Top Subscriptions
**Method:** GET  
**Path:** `/api/statistics/top-subscriptions`  
**Auth Required:** Yes

**Query Parameters:**
- `limit` (optional): limit (default: 10)

**Response (200 OK):**
```json
{
  "success": true,
  "subscriptions": [
    {
      "id": "uuid",
      "name": "string",
      "amount": "number",
      "currency": "string",
      "category": "string",
      "emoji": "string"
    }
  ]
}
```

---

## 8. Calendar (Takvim) Servisi

### 8.1 Get Payments for Month
**Method:** GET  
**Path:** `/api/calendar/payments`  
**Auth Required:** Yes

**Query Parameters:**
- `year` (required): yıl
- `month` (required): ay (1-12)

**Response (200 OK):**
```json
{
  "success": true,
  "payments": [
    {
      "day": "number",
      "subscriptions": [
        {
          "id": "uuid",
          "name": "string",
          "amount": "number",
          "currency": "string",
          "emoji": "string",
          "color": "string",
          "status": "string"
        }
      ]
    }
  ]
}
```

### 8.2 Get Day Details
**Method:** GET  
**Path:** `/api/calendar/day`  
**Auth Required:** Yes

**Query Parameters:**
- `date` (required): tarih (YYYY-MM-DD)

**Response (200 OK):**
```json
{
  "success": true,
  "date": "date",
  "subscriptions": [
    {
      "id": "uuid",
      "name": "string",
      "amount": "number",
      "currency": "string",
      "emoji": "string",
      "color": "string",
      "status": "string",
      "category": "string"
    }
  ],
  "total_amount": "number"
}
```

---

## 9. Category (Kategori) Servisi - Optional

### 9.1 Get All Categories
**Method:** GET  
**Path:** `/api/categories`  
**Auth Required:** No

**Response (200 OK):**
```json
{
  "success": true,
  "categories": [
    {
      "id": "uuid",
      "name": "string",
      "emoji": "string",
      "color": "string"
    }
  ]
}
```

---

## 10. Business Logic & Gereksinimler

### 10.1 Authentication & Security
- JWT token yapısı: `header.payload.signature`
- Token expiration: 24 saat (refresh token: 7 gün)
- Password hashing: bcrypt (salt rounds: 10)
- CORS enabled (frontend domain)
- HTTPS only (production)
- Rate limiting: 100 requests/minute per IP

### 10.2 Abonelik İş Mantığı
- **Aylık Hesapla:** 
  - Monthly: tutar olarak
  - Yearly: tutar / 12
  - Weekly: tutar * 4.33
  - Custom: not calculated

- **Para Birimi Dönüşümü:**
  - TRY: 1
  - USD: 38.5
  - EUR: 42.0
  - GBP: 49.0
  - NOT: Gerçek API'den güncel kurlar çekilmelidir

- **Ödeme Gelecek Tarihi:**
  - Period'a göre otomatik hesaplanmalı
  - User'ın profil settings'i göz önüne alınmalı

- **İptal Hatırlatıcısı:**
  - cancel_reminder_days ayarına göre otomatik gönderilmeli
  - Schedule job gerekir

### 10.3 Bildirimler
- Yaklaşan ödeme bildirimleri
- İptal hatırlatıcı bildirimleri
- Ödeme başarısız bildirimleri
- Push notification servisi entegrasyonu (OneSignal, Firebase Cloud Messaging vb.)

### 10.4 Veri Validasyonu
- Email format validation
- Password strength validation (min 8 karakter)
- Amount > 0
- Tarihler valid ve logical (first_payment <= next_payment)
- Currency enum values
- Period enum values
- Status enum values
- Color hex code validation
- Emoji validation

### 10.5 Soft Delete
- User, Subscription, Payment History için soft delete kullanılmalı
- deleted_at field set edilmeli (NULL değilse silinmiş sayılır)
- Query'lerde deleted_at IS NULL filtrelemesi yapılmalı

### 10.6 Pagination
- Default limit: 50
- Max limit: 500
- Offset-based pagination
- Response'da total count ve page info dönüleceği

### 10.7 Error Handling
Tüm endpoints standard error response döndürmelidir:

```json
{
  "success": false,
  "error": {
    "code": "string (ERROR_CODE)",
    "message": "string (User-friendly message)",
    "details": "object (optional, validation errors)",
    "timestamp": "datetime"
  }
}
```

### 10.8 Logging & Monitoring
- Tüm API requests log edilmeli
- Tüm errors log edilmeli (ERROR, WARNING, INFO levels)
- Performance monitoring (response times)
- Database query monitoring
- Authentication attempts logging

### 10.9 Caching
- User profile settings caching (1 saat)
- Category list caching (24 saat)
- Statistics caching (30 dakika)
- Cache invalidation on data update

### 10.10 Backup & Recovery
- Daily database backups
- Point-in-time recovery capability
- User data export (GDPR compliance)

---

## 11. Database Indexes

```sql
-- User
CREATE UNIQUE INDEX idx_user_email ON users(email);

-- Subscription
CREATE INDEX idx_subscription_user_id ON subscriptions(user_id);
CREATE INDEX idx_subscription_status ON subscriptions(status);
CREATE INDEX idx_subscription_next_payment ON subscriptions(next_payment_date);
CREATE INDEX idx_subscription_category ON subscriptions(category);

-- Profile
CREATE UNIQUE INDEX idx_profile_user_id ON profiles(user_id);

-- PaymentHistory
CREATE INDEX idx_payment_subscription_id ON payment_history(subscription_id);
CREATE INDEX idx_payment_date ON payment_history(payment_date);
```

---

## 12. Örnek İş Akışları

### 12.1 Kullanıcı Kaydolma
1. POST /api/auth/register (name, email, password)
2. Email ve password validation
3. User oluştur (password hash)
4. Default Profile oluştur (DEFAULT_PROFILE values)
5. JWT token generate et
6. Response döndür

### 12.2 Abonelik Ekleme
1. POST /api/subscriptions (subscription data)
2. Validation (amount > 0, tarihler valid, etc.)
3. next_payment_date calculate et (eğer custom value yoksa)
4. Subscription create et (user_id ile link)
5. İlk payment history entry oluştur
6. Response döndür

### 12.3 Dashboard Yükleme
1. GET /api/statistics/dashboard
2. User'ın tüm aktif subscriptions'ı getir
3. Aylık toplam hesapla (period'a göre monthly amount)
4. Yaklaşan ödemeler listele (next 10 days)
5. Count ve estimates hesapla
6. Response döndür (cached)

### 12.4 Profil Ayarları Güncelleme
1. PUT /api/profile (ayarlar)
2. Validation
3. Profile update et
4. Cache invalidate et
5. Response döndür

---

## 13. Frontend-Backend Entegrasyonu

### 13.1 Header'lar
```
Content-Type: application/json
Authorization: Bearer {jwt_token}
X-API-Version: 1.0
```

### 13.2 Token Refresh Flow
1. Request yapılırken 401 hatası alınırsa
2. Refresh token'ı kullan (POST /api/auth/refresh)
3. Yeni token al
4. Original request'i retry et

### 13.3 Error Handling
- Network errors: Retry logic (exponential backoff)
- 401 Unauthorized: Login screen'e yönlendir
- 403 Forbidden: Access denied message göster
- 4xx Client errors: User-friendly message göster
- 5xx Server errors: Retry et veya offline mode

---

## 14. Deployment Checklist

- [ ] Database production setup (PostgreSQL/MongoDB)
- [ ] Environment variables (.env)
- [ ] JWT secret key
- [ ] CORS configuration
- [ ] Rate limiting
- [ ] HTTPS SSL certificate
- [ ] Email service (notifications)
- [ ] File logging setup
- [ ] Database backups automated
- [ ] Monitoring & alerting setup
- [ ] CI/CD pipeline
- [ ] Load balancing (eğer multi-server)
- [ ] Security headers (Content-Security-Policy, etc.)
- [ ] Database migration scripts

---

## 15. Teknoloji Stack Önerileri

### Backend Framework'ler
- **Node.js**: Express.js, Fastify, NestJS
- **Python**: FastAPI, Flask, Django
- **Go**: Gin, Echo (yüksek performance)

### Database
- **PostgreSQL**: Relational, JSONB support
- **MongoDB**: Flexible schema, scalability

### Authentication
- **JWT**: jwt library
- **bcrypt**: Password hashing
- **Passport.js** (eğer Node.js)

### Caching
- **Redis**: Session, cache management

### Queue & Jobs
- **Bull** (Node.js) / **Celery** (Python): Background jobs
- Ödeme reminders, notifications

### Email Service
- **SendGrid**, **Mailgun**, **Nodemailer**

### Push Notifications
- **Firebase Cloud Messaging**
- **OneSignal**

### Monitoring
- **Sentry**: Error tracking
- **New Relic** / **DataDog**: APM
- **ELK Stack**: Logging

### Testing
- **Jest** (Node.js) / **pytest** (Python)
- **Supertest**: API testing
- Unit, Integration, E2E tests

---

## 16. API Response Standardizasyonu

Tüm responses aşağıdaki formatı takip etmelidir:

**Success Response:**
```json
{
  "success": true,
  "data": { /* endpoint'e özel data */ },
  "message": "string (optional)",
  "timestamp": "datetime"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "User-friendly message",
    "details": { /* optional validation errors */ }
  },
  "timestamp": "datetime"
}
```

---

## Özet - Gerekli Endpointler Listesi

### Authentication (5)
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/logout
- POST /api/auth/refresh
- GET /api/auth/verify

### User (3)
- GET /api/users/me
- PUT /api/users/me
- DELETE /api/users/me

### Subscriptions (6)
- GET /api/subscriptions
- GET /api/subscriptions/{id}
- POST /api/subscriptions
- PUT /api/subscriptions/{id}
- DELETE /api/subscriptions/{id}
- PATCH /api/subscriptions/{id}/status

### Payment History (2)
- GET /api/subscriptions/{id}/payment-history
- POST /api/subscriptions/{id}/payment-history

### Profile (2)
- GET /api/profile
- PUT /api/profile

### Statistics (5)
- GET /api/statistics/dashboard
- GET /api/statistics/categories
- GET /api/statistics/currencies
- GET /api/statistics/monthly
- GET /api/statistics/top-subscriptions

### Calendar (2)
- GET /api/calendar/payments
- GET /api/calendar/day

### Categories (1)
- GET /api/categories

**Toplam: 26 API Endpoint**

---

Bu dokümantasyon, Subsment Mobile uygulaması için tam backend gereksinimlerini kapsamaktadır.

# Customer Query/Inbox Feature

## ✅ Feature Complete

A search/inbox type feature has been added to MyApplication that allows users to submit customer queries to the deep-search Spring Boot service.

## 📱 Android App Components

### 1. **QueryScreen.kt**
- New screen with a form to submit customer queries
- Fields: Email, Subject, Message
- Auto-fills email from logged-in user
- Shows loading state and success/error messages
- Accessible from HomeScreen via "Submit Query" card

### 2. **HomeScreen.kt** (Updated)
- Added "Submit Query" quick action card
- Navigates to QueryScreen when clicked
- Improved UI with cards and better layout

### 3. **QueryApi.kt** (New)
- API interface for customer query endpoints
- `CustomerQueryRequest` data class
- `CustomerQueryResponse` data class
- `submitQuery()` method

### 4. **RetrofitClient.kt** (Updated)
- Added `queryApi` instance for query endpoints

### 5. **MainActivity.kt** (Updated)
- Added navigation state for QueryScreen
- Tracks user email for auto-filling query form

## 🔧 Spring Boot Backend Components

### 1. **CustomerQuery.java** (Entity)
- Stores customer queries in database
- Fields: id, email, subject, message, createdAt, status
- Table: `customer_queries`

### 2. **CustomerQueryRequest.java** (DTO)
- Request DTO with validation
- Fields: email, subject, message

### 3. **CustomerQueryResponse.java** (DTO)
- Response DTO
- Includes: success, message, queryId, email, subject, createdAt, status

### 4. **CustomerQueryRepository.java**
- JPA repository for database operations
- Methods to find queries by email and status

### 5. **CustomerQueryService.java**
- Business logic for submitting queries
- Saves queries to database
- Returns success/error responses

### 6. **CustomerQueryController.java**
- REST controller
- Endpoint: `POST /api/queries/submit`

### 7. **Database Schema** (Updated)
- Added `customer_queries` table
- Indexes for performance

## 🔗 API Endpoint

**Submit Query:**
```
POST https://deep-search-z3bh.onrender.com/api/queries/submit
Content-Type: application/json

{
  "email": "user@example.com",
  "subject": "Question about service",
  "message": "I have a question about..."
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Query submitted successfully",
  "queryId": 1,
  "email": "user@example.com",
  "subject": "Question about service",
  "createdAt": "2026-02-16T10:30:00",
  "status": "PENDING"
}
```

## 🎯 How to Use

1. **Login** to the app
2. On the **Home Screen**, tap **"Submit Query"** card
3. Fill in the form:
   - Email (auto-filled from login)
   - Subject (brief description)
   - Message (detailed query)
4. Tap **"Submit Query"** button
5. See success message with Query ID

## 📊 Database

The `customer_queries` table stores:
- Query ID (auto-generated)
- Customer email
- Subject
- Message (text)
- Created timestamp
- Status (PENDING, IN_PROGRESS, RESOLVED, CLOSED)

## 🚀 Next Steps (Optional Enhancements)

1. **Query History**: Show user's previous queries
2. **Status Updates**: Allow admins to update query status
3. **Notifications**: Send email notifications when query is submitted
4. **Admin Dashboard**: View and manage all queries
5. **Search/Filter**: Search queries by email, subject, or status

## 🧪 Testing

### Test with curl:
```bash
curl -X POST https://deep-search-z3bh.onrender.com/api/queries/submit \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "subject": "Test Query",
    "message": "This is a test query from curl"
  }'
```

### Test in App:
1. Login with: `tushar@example.com` / `password123`
2. Navigate to "Submit Query"
3. Fill form and submit
4. Check success message


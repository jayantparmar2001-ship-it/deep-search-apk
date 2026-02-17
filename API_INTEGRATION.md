# Deep Search API Integration

## ✅ Integration Complete

Your MyApplication Android app is now integrated with the deployed deep-search API on Render.

## 🔗 API Endpoint

**Production URL:** `https://deep-search-z3bh.onrender.com/`

## 📝 What Was Updated

### 1. RetrofitClient.kt
- ✅ Updated `BASE_URL` to use the deployed Render URL
- ✅ Added comments for easy switching between production and local development
- ✅ Configured for HTTPS connection

### 2. Existing Integration
- ✅ `AuthScreen.kt` already has login/register functionality
- ✅ `AuthApi.kt` defines the API endpoints
- ✅ AndroidManifest has INTERNET permission

## 🧪 Testing the Integration

### Test Login
The app will call:
```
POST https://deep-search-z3bh.onrender.com/api/auth/login
Content-Type: application/json

{
  "email": "tushar@example.com",
  "password": "password123"
}
```

### Test Register
The app will call:
```
POST https://deep-search-z3bh.onrender.com/api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

## 🔄 Switching Between Environments

To switch back to local development, edit `RetrofitClient.kt`:

```kotlin
// For local development (Android Emulator):
private const val BASE_URL = "http://10.0.2.2:8080/"

// For local development (Physical Device):
private const val BASE_URL = "http://192.168.1.100:8080/"  // Replace with your IP

// For production (current):
private const val BASE_URL = "https://deep-search-z3bh.onrender.com/"
```

## 📱 How to Use

1. **Run the app** on your Android device or emulator
2. **Login** with:
   - Email: `tushar@example.com`
   - Password: `password123`
3. **Or Register** a new account

## 🔍 Debugging

The app includes HTTP logging. Check Logcat for:
- Request/Response details
- Network errors
- API responses

Look for logs tagged with `OkHttp` to see the full API communication.

## ⚠️ Important Notes

1. **HTTPS**: The production URL uses HTTPS, so no additional network security configuration is needed
2. **Internet Permission**: Already configured in AndroidManifest.xml
3. **Timeout**: API calls have 30-second timeout
4. **Error Handling**: Network errors are displayed as Toast messages

## 🐛 Troubleshooting

### If login fails:
1. Check Logcat for error messages
2. Verify the Render service is running: `https://deep-search-z3bh.onrender.com/api/auth/health`
3. Check internet connection on device
4. Verify email/password are correct

### If you see SSL errors:
- Render uses valid SSL certificates, so this shouldn't happen
- If it does, check device date/time settings

### If you see connection timeout:
- Render free tier may spin down after inactivity
- First request might take longer (cold start)
- Wait a few seconds and try again

## 📊 API Response Format

**Success Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "...",
  "name": "Tushar",
  "email": "tushar@example.com"
}
```

**Error Response:**
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```


# Google Places API Setup Instructions

## How to Get Your Google Places API Key

1. **Go to Google Cloud Console**
   - Visit: https://console.cloud.google.com/

2. **Create a New Project (if you don't have one)**
   - Click on the project dropdown at the top
   - Click "New Project"
   - Enter a project name (e.g., "Mobile Final Project")
   - Click "Create"

3. **Enable Places API**
   - In the Google Cloud Console, go to "APIs & Services" > "Library"
   - Search for "Places API"
   - Click on "Places API"
   - Click "Enable"

4. **Create API Credentials**
   - Go to "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "API Key"
   - Your API key will be created and displayed
   - Copy this key

5. **Restrict the API Key (Recommended for Production)**
   - Click on the API key you just created
   - Under "Application restrictions", select "Android apps"
   - Click "Add an item"
   - Enter your package name: `com.example.mobilefinalproject`
   - Get your SHA-1 certificate fingerprint by running:
     ```
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - Add the SHA-1 fingerprint
   - Under "API restrictions", select "Restrict key"
   - Select "Places API"
   - Click "Save"

6. **Add the API Key to Your Project**
   - Replace `YOUR_API_KEY_HERE` in two locations:
     
     **File 1: AndroidManifest.xml**
     ```xml
     <meta-data
         android:name="com.google.android.geo.API_KEY"
         android:value="YOUR_ACTUAL_API_KEY_HERE" />
     ```
     
     **File 2: CustomerNewOrderFragment.kt (line 95)**
     ```kotlin
     Places.initialize(requireContext(), "YOUR_ACTUAL_API_KEY_HERE")
     ```

## Important Notes

- **For Development**: You can use an unrestricted API key, but make sure to restrict it before deploying to production
- **Billing**: Google Places API requires a billing account, but provides $200 free credit per month
- **Alternative for Testing**: If you don't want to set up billing, you can use a mock implementation or test data

## How It Works

Once configured, the location-based address inputs work as follows:

1. User clicks on "Pickup address" or "Delivery address" field
2. Google Places Autocomplete overlay opens
3. User types an address and selects from suggestions
4. The selected location's:
   - Full address is displayed in the text field
   - Latitude and longitude are stored in the `Location` object
   - Data is accessible via `getPickupLocation()` and `getDeliveryLocation()` methods

## Location Data Structure

```kotlin
data class Location(
    val address: String,      // Full formatted address
    val latitude: Double,     // Latitude coordinate
    val longitude: Double     // Longitude coordinate
)
```

The stored lat/lng values can be used for:
- Displaying locations on a map
- Calculating distances
- Routing and navigation
- Storing in database for future reference

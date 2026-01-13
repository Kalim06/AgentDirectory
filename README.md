# Agent Directory

A modern Android application for browsing and managing agent/user directories with offline support, real-time search, and background data synchronization.

## Features

- 📱 **Agent Directory**: Browse a paginated list of agents/users with smooth scrolling
- 🔍 **Real-time Search**: Search agents with debounced input for optimal performance
- 👤 **Agent Profiles**: View detailed agent information and their posts
- 💾 **Offline Support**: Full offline mode with local caching using Room database
- 🔄 **Background Refresh**: Automatic data synchronization every 15 minutes via WorkManager
- ⚙️ **Settings**: Configure auto-refresh and offline-only mode
- 🎨 **Modern UI**: Material Design with shimmer loading effects
- 🖼️ **Image Loading**: Efficient image loading and caching with Glide

## Tech Stack

### Core Technologies
- **Kotlin** - Primary programming language
- **Android SDK** - Min SDK 26, Target SDK 36
- **Gradle** - Build system with version catalogs

### Architecture Components
- **MVVM Architecture** - Model-View-ViewModel pattern
- **Repository Pattern** - Single source of truth for data
- **ViewBinding** - Type-safe view references

### Libraries & Frameworks
- **Room** - Local database for offline caching
- **Retrofit** - REST API client
- **OkHttp** - HTTP client with logging and retry support
- **Coroutines & Flow** - Asynchronous programming
- **WorkManager** - Background task scheduling
- **DataStore** - Modern preferences storage
- **Navigation Component** - Fragment navigation
- **Glide** - Image loading and caching
- **Shimmer** - Loading placeholders
- **Paging3** - Pagination support

## Architecture

This app follows Android's recommended architecture patterns:

- **Data Layer**: Repository pattern with Room database and Retrofit API service
- **UI Layer**: Fragments with ViewModels and ViewBinding
- **Background Tasks**: WorkManager for periodic data refresh
- **Caching Strategy**: Write-through cache with cache-first reads

For detailed architecture documentation, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Project Structure

```
app/src/main/java/com/kalim/agentdirectory/
├── data/
│   ├── api/              # Retrofit API interfaces
│   ├── local/            # Room database and DAOs
│   ├── model/            # Data models (User, Post)
│   └── repository/       # Repository implementation
├── ui/
│   ├── adapter/          # RecyclerView adapters
│   ├── fragment/         # UI fragments
│   ├── viewmodel/        # ViewModels
│   └── work/             # WorkManager workers
├── util/                 # Utility classes
└── AgentDirectoryApplication.kt
```

## Setup & Installation

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK with API level 26+

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd AgentDirectory
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle dependencies
   - Wait for the sync to complete

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press `Shift+F10`

## Usage

### Agent Directory Screen
- Browse through the list of agents
- Pull down to refresh the list
- Use the search bar to find specific agents
- Tap on an agent to view their profile

### Agent Profile Screen
- View detailed agent information
- Browse agent's posts
- Pull down to refresh data

### Settings Screen
- Toggle **Auto-Refresh** to enable/disable background data synchronization
- Enable **Offline-Only Mode** to use only cached data (no network requests)

## API Endpoints

The app uses the [DummyJSON API](https://dummyjson.com/):

- `GET /users?limit=20&skip=<offset>` - Get paginated users
- `GET /users/search?q=<query>` - Search users
- `GET /posts/user/<userId>` - Get user posts

## Key Features Explained

### Offline Mode
- When enabled, the app operates entirely offline using cached data
- All network requests are blocked
- Search queries work on local database only

### Background Refresh
- Automatically syncs data every 15 minutes
- Respects battery optimization settings
- Only runs when network is available and battery is not low
- Can be disabled in Settings

### Caching Strategy
- **Cache-First**: UI always displays cached data immediately
- **Background Update**: Network requests update cache silently
- **Write-Through**: All network responses are saved to Room database
- Data persists for offline use even after app restart

### Search Optimization
- 500ms debounce to prevent excessive API calls
- Distinct until changed to avoid duplicate searches
- Shows cached results immediately, updates from network

## Performance Optimizations

- **RecyclerView with DiffUtil**: Efficient list updates
- **Image Caching**: Glide handles memory and disk caching
- **Coroutine Scoping**: Proper lifecycle-aware coroutine management
- **Flow-based Data**: Reactive data streams with automatic updates

## Testing

The project includes test directories for:
- Unit tests (`src/test`)
- Instrumented tests (`src/androidTest`)

Run tests using:
```bash
./gradlew test          # Unit tests
./gradlew connectedAndroidTest  # Instrumented tests
```

## Future Improvements

- [ ] Migrate to Hilt or Koin for dependency injection
- [ ] Implement proper pagination with Paging3
- [ ] Add exponential backoff for network retries
- [ ] Implement cache expiration (TTL)
- [ ] Add comprehensive unit test coverage
- [ ] Improve error handling and recovery strategies

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available for educational purposes.

---

**Note**: This app uses the DummyJSON API for demonstration purposes. Replace with your own API endpoints for production use.

# Architecture Documentation

## Project Structure

```
app/src/main/java/com/kalim/agentdirectory/
├── data/
│   ├── api/
│   │   ├── ApiService.kt          # Retrofit API interface
│   │   └── NetworkModule.kt       # Network configuration
│   ├── local/
│   │   ├── AppDatabase.kt        # Room database
│   │   ├── UserDao.kt            # User data access
│   │   └── PostDao.kt             # Post data access
│   ├── model/
│   │   ├── User.kt                # User entity
│   │   ├── Post.kt                # Post entity
│   │   └── ApiResponse.kt         # API response models
│   └── repository/
│       └── AgentRepository.kt     # Repository with caching logic
├── ui/
│   ├── adapter/
│   │   ├── AgentAdapter.kt        # RecyclerView adapter for agents
│   │   └── PostAdapter.kt         # RecyclerView adapter for posts
│   ├── fragment/
│   │   ├── AgentDirectoryFragment.kt
│   │   ├── AgentProfileFragment.kt
│   │   └── SettingsFragment.kt
│   ├── viewmodel/
│   │   ├── AgentDirectoryViewModel.kt
│   │   ├── AgentProfileViewModel.kt
│   │   ├── SettingsViewModel.kt
│   │   └── ViewModelFactory.kt
│   └── work/
│       └── RefreshWorker.kt       # WorkManager worker
├── util/
│   ├── NetworkMonitor.kt         # Network connectivity monitoring
│   ├── SettingsManager.kt         # DataStore settings management
│   └── WorkManagerHelper.kt       # WorkManager utilities
└── AgentDirectoryApplication.kt   # Application class with DI
```

## Data Layer + API Handling

### API Service
- **Base URL**: `https://dummyjson.com/`
- **Endpoints**:
  - `GET /users?limit=20&skip=<offset>` - Get paginated users
  - `GET /users/search?q=<query>` - Search users
  - `GET /posts/user/<userId>` - Get user posts

### Network Configuration
- **Retrofit** with Gson converter
- **OkHttp** client with:
  - 30-second timeouts (connect, read, write)
  - Logging interceptor for debugging
  - Retry on connection failure

### Error Handling
- Network errors are caught and handled gracefully
- Falls back to cached data when network fails
- User-friendly error messages displayed in UI

## Caching Approach (Room)

### Database Schema
- **Users Table**: Stores user information with `cachedAt` timestamp
- **Posts Table**: Stores posts with `userId` foreign key and `cachedAt` timestamp

### Caching Strategy
1. **Write-Through Cache**: All network responses are immediately written to Room
2. **Read-From-Cache-First**: UI always displays cached data first for instant loading
3. **Background Refresh**: Network requests happen in background, updating cache silently
4. **Cache Invalidation**: Data is marked with timestamps but not automatically expired (kept for offline use)

### Room Configuration
- Database version: 1
- Migration strategy: Destructive migration (for development)
- DAOs use Flow for reactive data updates

## Offline Mode Behavior

### Offline-Only Mode
When enabled:
- All network requests are blocked
- App uses only cached data from Room
- Search queries search local database only
- No background refresh occurs

### Network Unavailable
When network is unavailable (but offline-only mode is disabled):
- App attempts network requests
- On failure, falls back to cached data
- User sees cached data immediately
- Error messages shown only if no cached data exists

### Cache-First Strategy
1. UI subscribes to Room Flow (instant display)
2. Repository attempts network refresh in background
3. Network response updates Room database
4. Flow automatically emits updated data to UI

## Background Refresh Strategy

### WorkManager Implementation
- **Periodic Work**: Runs every 15 minutes
- **Constraints**:
  - Requires network connection
  - Requires battery not low
- **Unique Work**: Prevents duplicate workers
- **Cancellation**: Cancelled when auto-refresh disabled or app uninstalled

### Lifecycle-Aware Behavior
- Auto-refresh pauses when:
  - App goes to background (handled by WorkManager)
  - Screen is locked (handled by WorkManager)
  - Offline-Only mode is enabled (checked in worker)
- WorkManager respects Doze mode and App Standby

### Battery Optimization
- Uses WorkManager's built-in battery optimization
- Respects system battery saver mode
- Only runs when device is charging or battery not low (configurable via constraints)

## Performance Improvements

### List Rendering
- **RecyclerView** with `DiffUtil` for efficient updates
- **View Recycling** to minimize memory usage
- **LinearLayoutManager** for simple vertical lists
- **ListAdapter** for automatic diff calculation

### Search Optimization
- **Debouncing**: 500ms delay before executing search
- **Distinct Until Changed**: Prevents duplicate searches
- **Flow-based**: Reactive search with automatic cancellation
- **Cache-First**: Shows cached results immediately, updates from network

### Image Loading
- **Glide** with automatic caching
- **Circle Crop** transformation for avatars
- **Placeholder** and **Error** handling
- **Memory and Disk Caching** handled by Glide

### Coroutine Management
- **Structured Concurrency**: All coroutines scoped to ViewModel
- **Automatic Cancellation**: Coroutines cancelled when ViewModel cleared
- **Background Dispatchers**: Network calls on IO dispatcher
- **Main Dispatcher**: UI updates on main dispatcher

## Network Retry and Debounce Logic

### Retry Strategy
- **Exponential Backoff**: Not implemented (can be added with Retrofit interceptor)
- **Manual Retry**: User can pull-to-refresh to retry
- **WorkManager Retry**: WorkManager automatically retries failed work

### Debounce Logic
- **Search Debounce**: 500ms delay implemented in ViewModel
- **Flow Debounce**: Uses Kotlin Flow's `debounce()` operator
- **Cancellation**: Previous search cancelled when new query arrives

### Request Deduplication
- **Flow Distinct Until Changed**: Prevents duplicate Flow emissions
- **Unique Work**: WorkManager prevents duplicate periodic work
- **Repository Caching**: Same requests return cached results

## Data Flow

### Agent Directory Screen
1. Fragment observes ViewModel's `users` Flow
2. ViewModel subscribes to Repository's `getAllUsersFlow()` (Room Flow)
3. ViewModel triggers background network refresh
4. Repository updates Room database
5. Room Flow emits updated data
6. ViewModel Flow emits to Fragment
7. Adapter updates RecyclerView with DiffUtil

### Agent Profile Screen
1. Fragment passes `userId` to ViewModel
2. ViewModel loads user from cache (instant display)
3. ViewModel triggers background network refresh
4. ViewModel observes posts Flow from Repository
5. Repository updates Room database
6. Flow emits updated data to UI

### Settings Screen
1. Fragment observes ViewModel's settings Flows
2. ViewModel observes DataStore preferences
3. User toggles settings
4. ViewModel updates DataStore
5. WorkManager scheduled/cancelled based on auto-refresh setting

## Dependency Injection

Simple manual DI using Application class:
- `AgentDirectoryApplication` provides singleton instances
- ViewModels receive dependencies via Factory
- All dependencies created once and reused

## Testing Considerations

- Repository can be tested with mock API and database
- ViewModels can be tested with mock repositories
- UI tests can use Espresso with test data
- WorkManager can be tested with TestWorker

## Future Improvements

1. **Dependency Injection**: Migrate to Hilt or Koin
2. **Paging3**: Implement proper pagination for large datasets
3. **Exponential Backoff**: Add retry interceptor to Retrofit
4. **Cache Expiration**: Implement TTL for cached data
5. **Unit Tests**: Add comprehensive test coverage
6. **Error Recovery**: Better error handling and recovery strategies


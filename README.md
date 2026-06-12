# EIM Call Manager — Android Source Code

**Educators Institute of Management | Call Recording App**  
Built for internal institutional use. Sideloaded APK (not Play Store).

---

## 📱 Features

| Feature | Description |
|---|---|
| **Auto Call Recording** | Records ALL incoming & outgoing calls automatically |
| **Call Log** | Full history with incoming/outgoing/missed categorization |
| **Recordings Library** | Browse, play, share, and delete recorded calls |
| **Contacts** | Browse device contacts and dial directly |
| **Dialpad** | Built-in numeric dialer |
| **Offline DB** | All data stored locally using Room (SQLite) |
| **Notes** | Add notes to any call record |

---

## 🏗️ Project Structure

```
app/src/main/java/com/eim/callapp/
├── model/
│   ├── CallRecording.kt        # Room Entity
│   ├── CallRecordingDao.kt     # Database queries
│   └── AppDatabase.kt          # Room Database
├── service/
│   └── CallRecordingService.kt # Foreground service for recording
├── receiver/
│   └── CallReceiver.kt         # Detects call state changes
├── ui/
│   ├── MainActivity.kt         # Main screen with call log
│   ├── DialerActivity.kt       # Numeric dialpad
│   ├── RecordingsActivity.kt   # Recordings browser + player
│   ├── ContactsActivity.kt     # Contacts browser
│   ├── CallViewModel.kt        # ViewModel (LiveData)
│   ├── CallLogAdapter.kt       # RecyclerView adapter
│   ├── RecordingAdapter.kt     # RecyclerView adapter
│   └── ContactAdapter.kt       # RecyclerView adapter
└── utils/
    ├── ContactUtils.kt         # Phone → Contact name lookup
    └── FileUtils.kt            # File paths, size/duration formatting
```

---

## 🔧 Build Instructions

### Prerequisites
- **Android Studio Hedgehog** (2023.1.1) or newer
- **JDK 17**
- **Android SDK** with API 26–34

### Steps

1. **Clone / Open the project**
   ```
   Open Android Studio → File → Open → Select EIMCallApp folder
   ```

2. **Sync Gradle**
   - Android Studio will auto-prompt. Click **"Sync Now"**

3. **Build APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   Output: `app/build/outputs/apk/debug/app-debug.apk`

4. **Install on device**
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```
   Or transfer APK to phone and enable "Install unknown apps"

---

## ⚠️ Android Recording Limitations

| Android Version | Recording Behavior |
|---|---|
| Android 8–9 (API 26–28) | Full recording works (both sides) |
| Android 10–11 (API 29–30) | May only capture microphone (your voice) |
| Android 12+ (API 31+) | Recording may be silent — depends on device OEM |

**Note:** Samsung, Xiaomi, OPPO, and Vivo often allow call recording on their custom ROM. Stock Android 12+ is most restrictive.

### Best tested devices for EIM use:
- Samsung Galaxy (One UI) — recording works well
- Xiaomi/Redmi (MIUI) — recording works well
- Stock Android (Pixel) — limited on Android 10+

---

## 📋 Permissions Required

Grant ALL of these when prompted on first launch:

- `CALL_PHONE` — make calls
- `READ_PHONE_STATE` — detect call state
- `RECORD_AUDIO` — record calls
- `READ_CONTACTS` — show contact names
- `READ/WRITE_CALL_LOG` — log call history
- `PROCESS_OUTGOING_CALLS` — detect outgoing calls
- `WRITE_EXTERNAL_STORAGE` — save recordings (Android 9 and below)
- `POST_NOTIFICATIONS` — show recording notification

---

## 📁 Recordings Storage Location

Recordings are saved to:
- **Android 10+:** `/Android/data/com.eim.callapp/files/Music/EIM_Recordings/`
- **Android 9 and below:** `/Music/EIM_Recordings/`

File format: `EIM_{phoneNumber}_{timestamp}.m4a`

---

## 🔒 Legal Note

For institutional use, inform all parties that calls may be recorded, as required under applicable Indian telecommunications law and IT Act provisions. This app is intended for internal educator-student communication tracking only.

---

## 🚀 Future Enhancements

- [ ] Cloud backup of recordings (Google Drive / EIM server)
- [ ] Search recordings by keyword (Whisper transcription)
- [ ] Export call report as PDF/Excel
- [ ] Student-tagging (link recordings to enrolled students)
- [ ] WhatsApp call detection

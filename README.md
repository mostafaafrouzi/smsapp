# AutoConnect SMS

AutoConnect SMS is a native Android application that automatically sends messages after phone calls. It supports multiple messaging channels including SMS, WhatsApp, and Telegram, with intelligent deduplication and comprehensive logging.

## Features

### Core Functionality
- **Automatic Call Detection**: Detects incoming, outgoing, and missed calls
- **Multi-Channel Messaging**: Send messages via SMS, WhatsApp API, or Telegram Bot
- **Smart Deduplication**: Prevents duplicate messages within configurable time windows
- **Comprehensive Logging**: Track all message attempts with detailed status information

### Messaging Channels
- **SMS**: Send messages directly from device SIM card
- **WhatsApp**: Integration with WhatsApp Business API (360Messenger)
- **Telegram**: Send notifications via Telegram Bot API

### User Experience
- **Bilingual Support**: English and Persian (Farsi) with RTL support
- **Modern UI**: Clean Material Design 3 interface
- **Real-time Statistics**: View message counts and recent activity
- **Flexible Settings**: Customize message templates and channel preferences

## Technical Specifications

- **Platform**: Native Android (Kotlin)
- **Architecture**: MVVM with Android Jetpack
- **Database**: Room with SQLite
- **Build System**: Gradle
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)

## Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 26+
- Java 17 or later
- Device with SMS capabilities for testing

## Installation

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/autoconnect-sms.git
   cd autoconnect-sms
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and select it

3. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

### GitHub Actions (Recommended)

The project includes automated CI/CD via GitHub Actions. Every push to the main branch will:

1. Build both debug and release APKs
2. Upload artifacts for download
3. Optionally sign release APKs if signing secrets are configured

**To get APKs from GitHub Actions:**
1. Go to the Actions tab in your repository
2. Click on the latest successful workflow run
3. Download the desired APK from the Artifacts section

## Configuration

### Required Permissions

The app requires the following permissions:
- `READ_PHONE_STATE`: Detect phone call states
- `READ_CALL_LOG`: Determine call types and history
- `SEND_SMS`: Send SMS messages
- `INTERNET`: Access WhatsApp and Telegram APIs
- `POST_NOTIFICATIONS`: Display service notifications

### Initial Setup

1. **Launch the app** and grant required permissions
2. **Disable battery optimization** for reliable call detection
3. **Configure message templates** in Settings
4. **Set up API keys** for WhatsApp and Telegram (optional)
5. **Enable the main switch** to start automatic messaging

### WhatsApp API Setup

1. Obtain an API key from [360Messenger](https://360messenger.com)
2. Go to Settings → API Configuration
3. Enter your API key
4. Enable WhatsApp messaging

### Telegram Bot Setup

1. Create a bot via [@BotFather](https://t.me/botfather)
2. Get your bot token
3. Find your chat ID (you can use [@userinfobot](https://t.me/userinfobot))
4. Go to Settings → API Configuration
5. Enter bot token and chat ID
6. Enable Telegram messaging

## Usage

### Main Screen
- **Enable Switch**: Toggle automatic messaging on/off
- **Statistics**: View weekly message count and recent activity
- **Settings**: Configure app preferences and API keys
- **Logs**: View detailed message history and status

### Settings
- **General**: Enable/disable channels and set deduplication time
- **Message Templates**: Customize messages for different call types
- **API Configuration**: Set up WhatsApp and Telegram credentials
- **Advanced**: Language selection and auto-cleanup options

### Message Flow
1. **Call Detection**: App monitors phone state changes
2. **Call Analysis**: Determines call type (incoming/outgoing/missed)
3. **Deduplication Check**: Verifies no recent messages to the number
4. **Message Selection**: Chooses appropriate template based on call type
5. **Channel Priority**: Attempts WhatsApp → Telegram → SMS (if enabled)
6. **Logging**: Records all attempts and results in database

## Architecture

```
app/
├── ui/                    # Activities and UI components
├── viewmodel/            # ViewModels for UI logic
├── data/                 # Data layer
│   ├── db/              # Room database
│   ├── prefs/           # SharedPreferences
│   └── model/           # Data models
├── core/                 # Business logic
│   ├── telephony/       # Call detection
│   ├── sms/             # SMS sending
│   ├── whatsapp/        # WhatsApp API
│   └── telegram/        # Telegram API
└── res/                  # Resources and layouts
```

## Security & Privacy

- **Local Storage**: All data stored locally on device
- **API Keys**: Securely stored in SharedPreferences
- **No Data Collection**: App doesn't send personal data externally
- **Permission Transparency**: Clear explanation of required permissions

## Troubleshooting

### Common Issues

1. **Calls not detected**
   - Check battery optimization settings
   - Verify all permissions are granted
   - Ensure app is enabled in main switch

2. **Messages not sending**
   - Check network connection for WhatsApp/Telegram
   - Verify API keys and credentials
   - Check SMS permissions and SIM card status

3. **App crashes**
   - Clear app data and cache
   - Reinstall the application
   - Check device compatibility (Android 8.0+)

### Debug Mode

Enable debug logging by running:
```bash
adb logcat -s "AutoConnect SMS"
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For issues and questions:
- Create an issue on GitHub
- Check the troubleshooting section above
- Review the code comments for implementation details

## Roadmap

- [ ] Enhanced call type detection
- [ ] Message scheduling
- [ ] Contact group support
- [ ] Backup and restore functionality
- [ ] Advanced analytics dashboard
- [ ] Multiple language support expansion

---

**Note**: This application requires appropriate permissions and should be used in compliance with local telecommunications regulations and privacy laws.

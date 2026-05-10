# Hasta-Kala Shop 🎨

**Hasta-Kala Shop** is a lightweight artisan billing and micro-sales analytics application designed specifically for small craft businesses and individual artisans. It bridges the gap between traditional craftsmanship and modern digital management.

---

## 🚀 Key Features

- **Quick Billing:** Streamlined checkout process for handmade products.
- **Sales Analytics:** Visual insights into sales trends and revenue.
- **Inventory Management:** Track stock levels with automated **Low Stock Alerts**.
- **Artisan Assistant:** AI-powered business consultant integrated with **Google Gemini 2.0** for smart growth suggestions.
- **Security:** Built-in **App Lock** (Biometric/PIN) to protect sensitive business data.
- **Multi-language:** Localized for English, Hindi, Malayalam, Tamil, Kannada, and Telugu.

---

## 🛠️ Implementation Guide (Step-by-Step)

Follow these steps to set up and run the project locally.

### 1. Prerequisites
- **Android Studio Ladybug** (or newer)
- **JDK 17** or higher
- A **Firebase Account**
- A **Google AI Studio** account (for Gemini API)

### 2. Clone the Repository
```bash
git clone https://github.com/tatakae-B/Hasta-Kala.git
cd Hasta-Kala
```

### 3. Firebase Configuration
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Create a new project: `Hasta-Kala Shop`.
3. Add an Android App with package name: `com.hastakala.shop`.
4. Download `google-services.json` and place it in the `app/` directory.
5. **Authentication:** Enable **Email/Password** provider.
6. **Firestore:** 
   - Enable Cloud Firestore.
   - Copy the rules from the `firestore.rules` file in the project root to your Firebase Console.

### 4. Setup Gemini AI Assistant
1. Visit [Google AI Studio](https://aistudio.google.com/) and create an **API Key**.
2. Open your `local.properties` file in the project root.
3. Add the following line:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
   *Note: This key is automatically picked up by the build system and is excluded from Git.*

### 5. Build and Deploy
1. Open the project in **Android Studio**.
2. Click **File > Sync Project with Gradle Files**.
3. Select your device/emulator and click **Run**.

---

## 🔄 Development Workflow

### Automated Sync
The project includes a `sync.bat` script to simplify the Git workflow:
- Double-click `sync.bat` to automatically add all changes, commit with a timestamped message, and push to the repository.

### Commit Checks
Before pushing, it is recommended to run local validations:
```bash
./gradlew lintDebug testDebugUnitTest
```

---

## 🤝 Contributing
Contributions are welcome! Please follow these steps:
1. Fork the Project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your Changes (`git commit -m 'Add AmazingFeature'`).
4. Push to the Branch.
5. Open a Pull Request.

## 📄 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
**Developed by [Bdriii](https://github.com/tatakae-B)**  
*Empowering artisans, one craft at a time.*

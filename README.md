# NirapodFamily — Native Android Parental Monitoring Solution

একটি সম্পূর্ণ নেটিভ Android Studio মাল্টি-মডিউল প্রজেক্ট (Kotlin + Jetpack Compose)। এই প্রজেক্টে অভিভাবক এবং সন্তান দুজনের জন্য দুটি আলাদা স্বতন্ত্র স্বয়ংসম্পূর্ণ অ্যাপ রয়েছে:
1. **Parent App** (`com.nirapod.family.parent`) — "NirapodFamily Parent"
2. **Child App** (`com.nirapod.family.child`) — "NirapodFamily Child"

---

### মডিউল আর্কিটেকচার (Multi-Module Architecture)
```
NirapodFamily/
├── build.gradle.kts (Root Gradle Build)
├── settings.gradle.kts
├── gradle.properties
├── gradle/libs.versions.toml (Version Catalog)
├── shared/ (Common models, WebRTC, Firebase Auth & Firestore, Licensing)
├── child-app/ (Device Admin, MediaProjection, CameraX, Mic, FCM, App Hiding)
└── parent-app/ (Dashboard, Live WebRTC Viewers, Location Maps, Licensing UI)
```

---

### কী কী ফিচার ইমপ্লিমেন্ট করা হয়েছে:
1. **Child App (`child-app`)**:
   - **সেটআপ উইজার্ড (SetupWizardScreen)**: প্রতিটি স্পর্শকাতর পারমিশন ক্রমান্বয়ে অভিভাবকের মাধ্যমে সক্রিয় করা (Device Admin, Camera, Microphone, Background Location, Battery Optimization Whitelist)।
   - **অ্যান্টি-আনইনস্টল সুরক্ষা (DeviceAdminReceiver)**: অ্যান্ড্রয়েড ডিভাইস অ্যাডমিন পলিসির মাধ্যমে অনুমোদনহীন আনইনস্টলেশন রোধ।
   - **অ্যাপ হাইডিং ও সিক্রেট ডায়ালার (`LauncherAlias` & `SecretDialReceiver`)**: সেটআপ শেষে লঞ্চার থেকে অ্যাপ আইকন লুকিয়ে ফেলা এবং ফোন ডায়ালারে `*#9753#` ডায়াল করে পুনরায় অ্যাপের ইন্টারফেসে প্রবেশ করা।
   - **পারসিস্টেন্ট ব্যাকগ্রাউন্ড সার্ভিস (`ChildMonitorService`)**: `START_STICKY` ও `RECEIVE_BOOT_COMPLETED` দিয়ে রিবুটের পরও স্বয়ংক্রিয় ব্যাকগ্রাউন্ড হার্টবিট ও লোকেশন সার্ভিস চালু থাকা।
   - **WebRTC স্ট্রিমিং সার্ভিস**: 
     - `ScreenCaptureService`: MediaProjection API ব্যবহার করে লাইভ স্ক্রিন মিররিং।
     - `CameraStreamService`: CameraX / Camera2 ব্যবহার করে ব্যাকগ্রাউন্ডে সামনে/পেছনের লাইভ ক্যামেরা ফিড।
     - `AudioStreamService`: ডিভাইসের পরিবেষ্টনকারী শব্দ (Ambient Audio) লাইভ শোনা।
   - **Firebase কমান্ড রিসিভার (`FCMCommandService`)**: অভিভাবক অ্যাপ থেকে পুশ কমান্ডের মাধ্যমে তৎক্ষণাৎ যেকোনো স্ট্রিমিং চালু বা বন্ধ করা।
   - **লাইসেন্স অ্যাক্টিভেশন**: ডিভাইস ফিঙ্গারপ্রিন্ট (`Settings.Secure.ANDROID_ID`) ও লাইসেন্স কী ভ্যালিডেশন।

2. **Parent App (`parent-app`)**:
   - **অভিভাবক লগইন ও রেজিস্ট্রেশন**: Firebase Authentication দিয়ে নিরাপদ সাইন ইন।
   - **ড্যাশবোর্ড**: সংযুক্ত সকল চাইল্ড ডিভাইসের রিয়েলটাইম অনলাইন স্ট্যাটাস, ব্যাটারি লেভেল, চার্জিং স্ট্যাটাস।
   - **লাইভ স্ক্রিন ভিউয়ার (`ScreenMirrorScreen`)**: WebRTC `SurfaceViewRenderer` দিয়ে লো-লেটেন্সি লাইভ স্ক্রিন ফিড।
   - **লাইভ ক্যামেরা ভিউয়ার (`CameraViewScreen`)**: ফ্রন্ট ও ব্যাক ক্যামেরা স্যুইচ অপশনসহ লাইভ ভিডিও ফিড।
   - **লাইভ অডিও লিসেনার (`AudioMonitorScreen`)**: রিয়েলটাইম পরিবেষ্টনকারী শব্দ শ্রবণ।
   - **লাইভ লোকেশন ম্যাপ (`LocationMapScreen`)**: Google Maps Compose দিয়ে সর্বশেষ রিয়েলটাইম পিনপয়েন্ট ট্র্যাকিং।
   - **লাইসেন্স ও সেটিংস ব্যবস্থাপনা**: সক্রিয় সাবস্ক্রিপশন, ব্যবহারের সীমা ও অ্যাকাউন্ট কনফিগারেশন।

---

### বিল্ড করার নিয়ম (Build Instructions)

#### ১. পূর্বশর্ত:
- Android Studio Ladybug / Koala বা এর পরবর্তী সংস্করণ।
- JDK 17 (বা JDK 21)।
- Android SDK Platform 34 (Android 14) এবং Android SDK Build-Tools 34.0.0।

#### ২. Firebase সেটআপ:
1. [Firebase Console](https://console.firebase.google.com)-এ একটি নতুন প্রজেক্ট তৈরি করুন।
2. দুটি অ্যান্ড্রয়েড অ্যাপ যুক্ত করুন:
   - চাইল্ড অ্যাপ: `com.nirapod.family.child`
   - প্যারেন্ট অ্যাপ: `com.nirapod.family.parent`
3. Firebase কনসোল থেকে `google-services.json` ডাউনলোড করে যথাক্রমে `child-app/google-services.json` এবং `parent-app/google-services.json`-এ রিপ্লেস করুন।
4. Firebase Firestore, Firebase Realtime Database (ফর WebRTC সিগনালিং) এবং Firebase Auth চালু করুন।

#### ৩. রিলিজ APK বিল্ড কমান্ড:
টার্মিনালে প্রজেক্ট রুটে গিয়ে কমান্ড রান করুন:
```bash
# Parent App Release APK
./gradlew :parent-app:assembleRelease

# Child App Release APK
./gradlew :child-app:assembleRelease
```
বিল্ড হওয়া APK লোকেশন:
- Parent APK: `parent-app/build/outputs/apk/release/parent-app-release-unsigned.apk`
- Child APK: `child-app/build/outputs/apk/release/child-app-release-unsigned.apk`

---

### APK সাইনিং ও সাইডলোডিং (Sideloading Guide)
যেহেতু চাইল্ড অ্যাপটি গুগল প্লে স্টোরে নয় বরং সরাসরি সাইডলোড (ওয়েবসাইট থেকে ডাউনলোড) করে ইনস্টল করার জন্য তৈরি:
1. `keytool` দিয়ে একটি সাইনিং কি-স্টোর তৈরি করুন:
   ```bash
   keytool -genkey -v -keystore nirapod-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias nirapod
   ```
2. `apksigner` দিয়ে APK সাইন করুন:
   ```bash
   apksigner sign --ks nirapod-release-key.jks --out NirapodChild.apk child-app-release-unsigned.apk
   apksigner sign --ks nirapod-release-key.jks --out NirapodParent.apk parent-app-release-unsigned.apk
   ```
3. সন্তানের ফোনে `NirapodChild.apk` সাইডলোড করে "Unknown Sources" চালু করে ইনস্টল করুন।
4. অ্যাপ খুলে সেটআপ উইজার্ডের প্রতিটি পারমিশন সফলভাবে প্রদান করে সুরক্ষিত রাখুন।

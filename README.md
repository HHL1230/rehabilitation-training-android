# 復健訓練 Android App (Rehabilitation Training)

一個專為 70 歲以上、髖關節或膝關節置換術後約 3 個月的長者設計的 Android 復健訓練記錄與提醒 App。協助使用者規律執行、記錄與分享每日訓練狀況。

> [!IMPORTANT]
> **免責聲明：** 本 App 的目的不是提供醫療診斷或復健處方，僅供規律執行紀錄。請使用者務必遵循醫師或物理治療師之指示進行訓練。

---

## 📱 產品定位與高齡友善設計
* **簡單分頁：** 僅保留「紀錄」、「統計」、「提醒」、「分享」四個核心功能。
* **高齡友善介面：** 設計較大的字體、大按鈕與易點擊區域，並在每次開啟/從背景回到前景時輪換柔和主題色，保持新鮮感與可讀性。
* **無痛分享：** 格式化純文字摘要，可一鍵分享至 LINE 聊天室，照護者能直觀閱讀，不需 Excel、Google Sheet 或 LINE SDK。

---

## 🛠️ 主要功能
1. **四種核心訓練項目紀錄：**
   * **彈力帶彎腿：** 記錄訓練時間（分鐘）。
   * **阻力伸腿：** 記錄組數、每組次數與重量負荷（公斤）。
   * **阻力騎腳踏車：** 記錄騎乘時間（分鐘）、騎乘距離（公里）與阻力等級（1~20）。
   * **跑步機走路：** 記錄走路時間（分鐘）與走路距離（公里）。
2. **本機資料儲存：** 使用 Room database 儲存在手機本機，完全不需要登入帳號，無隱私外洩風險。
3. **儀表板與統計：**
   * 今日訓練狀態提示。
   * 近 7 天 / 近 30 天的訓練次數與總時間。
   * 最近訓練紀錄列表（含日期與 `HH:mm` 精確時間）。
4. **每日提醒功能：** 使用 WorkManager 進行各項目獨立定時提醒。支援開機自動恢復提醒排程，Android 13+ 主動要求通知權限。
5. **資料分享功能：** 限定分享近 7 天或近 30 天紀錄，以簡明的文字表格形式呈現。

---

## 💻 技術棧
* **語言：** Kotlin
* **UI 框架：** Jetpack Compose + Material 3
* **架構：** MVVM (ViewModel + StateFlow)
* **資料庫：** Room
* **背景任務與排程：** WorkManager
* **測試：** JUnit

---

## 🚀 開發與建置指引

### 1. 環境需求
* Android Studio (Ladybug / Koala 或更新版本)
* JDK 17 (建議直接使用 Android Studio 內建的 JBR 17+)
* Android SDK 35 (Target SDK) / 26 (Min SDK)

### 2. 設定環境變數
在執行 Gradle 指令前，請確保已設定 `JAVA_HOME` 和 `ANDROID_HOME`。

**PowerShell 範例：**
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:ANDROID_HOME = "C:\Users\Vivobook S16\AppData\Local\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
```

### 3. 測試與建置
在根目錄下執行以下指令以執行單元測試並產生 Debug APK：
```powershell
.\gradlew.bat testDebugUnitTest assembleDebug --no-daemon --console=plain
```

---

## 📲 安裝與執行 (模擬器/實機)

### 1. 安裝 APK
確保已啟用開發者模式與 USB 偵錯，並透過 ADB 安裝編譯完成的 APK：
```powershell
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

### 2. 啟動應用程式
```powershell
adb shell monkey -p com.example.rehabilitationtraining 1
```

> [!TIP]
> 如果在模擬器上無法使用電腦鍵盤打字，請至 AVD 設定中確認硬體鍵盤支援已啟用：
> ```text
> hw.keyboard=yes
> ```

---

## 📂 專案結構
```text
rehabilitation-training/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   └── java/com/example/rehabilitationtraining/
│   │   │       ├── data/      # Room DB, Entities, Daos, Repositories
│   │   │       ├── domain/    # 輸入驗證、統計邏輯與資料轉換
│   │   │       ├── ui/        # Compose 畫面、元件與 ViewModels
│   │   │       ├── reminder/  # WorkManager 提醒排程
│   │   │       └── sharing/   # Sharesheet 與文字摘要生成
│   └── build.gradle.kts
├── docs/                      # 相關開發手冊與規範
├── build.gradle.kts
└── settings.gradle.kts
```

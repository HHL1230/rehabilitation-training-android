# 復健訓練 Android App (Rehabilitation Training)

一個專為 70 歲以上、髖關節或膝關節置換術後約 3 個月的長者設計的 Android 復健訓練記錄與提醒 App。協助使用者規律執行、記錄與分享每日訓練狀況。

> [!IMPORTANT]
> **免責聲明：** 本 App 的目的不是提供醫療診斷或復健處方，僅供規律執行紀錄。請使用者務必遵循醫師或物理治療師之指示進行訓練。

## 📥 正式版下載

- **產品頁：** https://hhl1230.github.io/my-products/rehabilitation-training/
- **v0.2.0 正式 APK：** https://github.com/HHL1230/my-products/releases/download/rehabilitation-training-v0.2.0/LegRehabilitationTraining-v0.2.0.apk

請以 Samsung Internet 或 Chrome 開啟產品頁下載 APK。LINE 內建瀏覽器可能封鎖 APK
下載；請使用「在瀏覽器中開啟」或複製連結到外部瀏覽器。首次安裝時，依 Android
提示允許該瀏覽器或「我的檔案」安裝未知來源 App 即可。

---

## 📱 產品定位與高齡友善設計
* **簡單分頁：** 僅保留「紀錄」、「統計」、「提醒」、「分享」四個核心功能。
* **高齡友善介面：** 設計較大的字體、大按鈕與易點擊區域，並在每次開啟/從背景回到前景時輪換柔和主題色，保持新鮮感與可讀性。
* **無痛分享：** 格式化純文字摘要，可一鍵分享至 LINE 聊天室，照護者能直觀閱讀，不需 Excel、Google Sheet 或 LINE SDK。

---

## 🛠️ 主要功能
1. **四種核心訓練項目紀錄：**
   * **彈力帶彎腿：** 依序記錄次數與組數。
   * **彈力帶伸腿：** 依序記錄次數、組數與阻力（Kg，預設 2）。
   * **騎器械腳踏車：** 記錄騎乘時間（分鐘）、騎乘距離（km）與 LEVEL（1~20，預設 1）。
   * **跑步機走路：** 記錄走路時間（分鐘）、走路距離（km）與 Incline（0 以上整數，預設 0）。
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
在根目錄下執行以下指令以執行單元測試，並產生 Debug 與已簽署的正式 APK：
```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease --no-daemon --console=plain
```

---

## 📲 安裝與執行 (模擬器/實機)

### 1. 安裝 APK
確保已啟用開發者模式與 USB 偵錯，並透過 ADB 安裝編譯完成的 APK：
```powershell
adb install -r "app\build\outputs\apk\debug\app-debug.apk"
```

Debug APK 會以「腿部復健訓練（開發版）」安裝，套件 ID 為
`com.example.rehabilitationtraining.debug`；它可與正式發布版並存，且兩者資料互不共用。

### 2. 啟動應用程式
```powershell
adb shell monkey -p com.example.rehabilitationtraining.debug 1
```

正式 APK 使用 `com.example.rehabilitationtraining`，與開發版可並存；兩者資料互不共用。

### 3. 發布正式版

正式 APK 由 release 簽章金鑰簽署後發佈到 GitHub Release。**建議使用 GitHub Actions 發布**，這樣在任何一台電腦上都能出版，不需要隨身攜帶金鑰檔。

#### 方式 A：GitHub Actions（推薦，適用於任何電腦）

1. 調整 `app/build.gradle.kts` 的 `versionCode` 與 `versionName`，並提交推送。
2. 推送版本標籤，CI 會自動測試、建置已簽署 APK，並建立 GitHub Release：
   ```powershell
   git tag v0.2.1
   git push origin v0.2.1
   ```
3. 也可以到 GitHub 網頁的 **Actions → Build signed release APK → Run workflow** 手動觸發。
   `tag` 欄位填入版本標籤即會發布 Release；留空則只建置並上傳 artifact 供下載檢查。
4. 同步更新產品頁的版本與下載連結。

CI 內建兩道保護，任一不符即中止並且不會發布：

* **版號一致性：** 標籤必須是 `v<versionName>`。若推了 `v0.2.1` 卻忘記更新
  `app/build.gradle.kts`，會在建置前就失敗。加後綴的標籤（例如 `v0.2.0-citest`）
  視為測試版，會標記成 pre-release 而不會蓋掉正式版的 Latest。
* **簽章指紋：** 建置後比對 APK 憑證的 SHA-256 是否等於既有正式版
  （`414eb130…f5dc`，定義在 workflow 的 `EXPECTED_SIGNING_CERT_SHA256`）。
  這可防止誤用其他金鑰而發出「使用者裝不上去、必須移除舊版才能更新」的 APK。

> [!WARNING]
> 使用者的訓練紀錄只存在手機本機的 Room 資料庫，沒有雲端備份。一旦簽章金鑰換掉，
> 使用者必須移除舊版才能安裝新版，所有紀錄會一併消失，務必沿用同一把
> `release-key.jks`。

CI 使用的簽章資訊儲存在 repository secrets（**Settings → Secrets and variables → Actions**）：

| Secret | 內容 |
| --- | --- |
| `KEYSTORE_BASE64` | `release-key.jks` 的 Base64 編碼字串 |
| `KEYSTORE_PASSWORD` | keystore 密碼 |
| `KEY_ALIAS` | 金鑰別名 |
| `KEY_PASSWORD` | 金鑰密碼 |

若日後需要重新建立這些 secrets（例如換 repo），在持有金鑰的電腦上執行：

```powershell
$b64 = [Convert]::ToBase64String([System.IO.File]::ReadAllBytes("release-key.jks"))
$b64 | gh secret set KEYSTORE_BASE64
```

其餘三個 secret 依 `keystore.properties` 內的值以 `gh secret set <NAME>` 設定。

#### 方式 B：本機建置

在持有 `release-key.jks` 與 `keystore.properties` 的電腦上：

1. 調整 `versionCode` 與 `versionName`。
2. 建置 `assembleRelease`。
3. 將 `app\build\outputs\apk\release\app-release.apk` 更名為
   `LegRehabilitationTraining-v<versionName>.apk`（與 CI 及產品頁連結的命名一致），
   再上傳為新版 Release 資產。
4. 同步更新產品頁的版本與下載連結。

若電腦上沒有 `keystore.properties`，也可改用環境變數提供簽章資訊：

```powershell
$env:KEYSTORE_FILE = "release-key.jks"
$env:KEYSTORE_PASSWORD = "<store password>"
$env:KEY_ALIAS = "<alias>"
$env:KEY_PASSWORD = "<key password>"
```

兩者皆未設定時，debug 建置仍可正常運作，但 release APK 不會被簽署（無法安裝）。

> [!IMPORTANT]
> 請安全備份 `release-key.jks` 與 `keystore.properties`（兩者已被 `.gitignore` 排除，不會進入版本控制）。
> 後續版本必須使用相同簽章金鑰，才能覆蓋安裝既有正式版；金鑰遺失將無法再更新已安裝的 App。

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

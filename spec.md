# 復健訓練 Android App 規格

## 產品定位

復健訓練是一個 Android 手機 App，目標使用者是 70 歲以上、髖關節或膝關節置換術後約 3 個月、需要持續訓練腿部肌力與關節活動度的使用者。

App 的目的不是提供醫療診斷或復健處方，而是協助使用者依照醫師或物理治療師建議，規律執行、記錄與分享訓練狀況。

## 目前版本

- Version name: `0.2.0`
- Version code: `4`
- Android application ID: `com.example.rehabilitationtraining`
- Minimum SDK: Android 8.0 / API 26
- Target SDK: Android 15 / API 35
- 技術棧：Kotlin、Jetpack Compose、Material 3、Room、WorkManager
- 正式發布：已使用本機 release 簽章金鑰簽署 APK，並透過 GitHub Release 與 GitHub Pages 產品頁提供下載。

## 已實作功能

### 1. 訓練紀錄

使用者可以選擇日期、紀錄時間與訓練項目，記錄每日訓練內容。紀錄時間使用 `HH:mm` 格式，新增紀錄時預設帶入當下時間，使用者可在儲存前調整。

目前支援四種訓練：

1. 彈力帶彎腿
   - 依序記錄次數與組數。
2. 彈力帶伸腿
   - 依序記錄次數、組數與阻力（Kg，預設 2）。
3. 騎器械腳踏車
   - 記錄騎乘時間（分鐘）、騎乘距離（km）與 LEVEL（1 到 20，預設 1）。
4. 跑步機走路
   - 記錄走路時間（分鐘）、走路距離（km）與 Incline（0 以上整數，預設 0）。

每筆紀錄可加上備註。輸入時會檢查必要欄位，例如訓練時間必須大於 0，騎器械腳踏車的 LEVEL 需在 1 到 20 之間，跑步機 Incline 不可小於 0。

### 2. 本機資料儲存

訓練紀錄使用 Room database 儲存在手機本機，不需要登入帳號，也不需要後端伺服器。

### 3. 儀表板與統計

App 提供統計頁面，顯示：

- 今天是否已有訓練紀錄。
- 近 7 天紀錄筆數與總訓練時間。
- 近 30 天紀錄筆數與總訓練時間。
- 四種訓練項目的個別紀錄筆數與總時間。
- 最近訓練紀錄列表，包含日期與 `HH:mm` 紀錄時間。

### 4. 每項訓練每日提醒

App 可為每個訓練項目分別設定每日固定提醒時間，包含：

1. 彈力帶彎腿提醒。
2. 彈力帶伸腿提醒。
3. 騎器械腳踏車提醒。
4. 跑步機走路提醒。

每個提醒都可以獨立開啟或關閉，並分別設定小時與分鐘。提醒訊息會包含訓練名稱，例如：

> 要做訓練囉，要有耐心，一定會進步，恢復行動自如，加油！

實際通知會顯示類似「要做『彈力帶彎腿』訓練囉，要有耐心，一定會進步，恢復行動自如，加油！」。

提醒功能使用 WorkManager 排程，並支援 Android 開機後恢復各訓練項目的排程。Android 13 以上會要求通知權限。

### 5. 分享紀錄

App 可透過 Android 系統分享面板分享紀錄。分享內容包含：

- 照護者可直接閱讀的純文字摘要。
- 純文字摘要表，包含日期時間、訓練項目與訓練內容。
- 分享範圍限定為近 7 天或近 30 天紀錄，不提供全部紀錄分享。

若手機已安裝 LINE，使用者可以在分享面板中選 LINE 傳送紀錄。內容會直接顯示在聊天室，不需要 Excel、Google Sheet 或 LINE SDK。

### 6. 高齡友善介面

目前 UI 採用 Jetpack Compose 實作，設計重點包含：

- 較大的字體。
- 較大的按鈕與點擊區。
- 簡化分頁：紀錄、統計、提醒、分享。
- 每次開啟 App 或從背景回到前景時會輪換一組柔和主題色，保持畫面新鮮感，同時維持高對比與可讀性。
- 明確提示使用者依照醫師或物理治療師建議調整訓練量。

### 7. 安裝與發布

- 開發版使用套件 ID `com.example.rehabilitationtraining.debug`，可透過 USB／ADB 安裝，並可與正式版並存。
- 正式版使用套件 ID `com.example.rehabilitationtraining`，由同一組 release 金鑰簽署，確保後續 APK 可直接覆蓋更新。
- 正式下載入口：
  - 產品頁：https://hhl1230.github.io/my-products/rehabilitation-training/
  - GitHub Release：https://github.com/HHL1230/my-products/releases
- 若從 LINE 開啟連結，應使用「在瀏覽器中開啟」改由 Samsung Internet 或 Chrome 下載 APK；LINE 內建瀏覽器可能封鎖 APK 下載。

## 已加入測試

目前包含單元測試，涵蓋：

- 訓練紀錄輸入驗證。
- 訓練統計邏輯。
- 純文字摘要表、紀錄時間與分享格式。
- 提醒排程延遲時間計算。

可使用以下指令驗證：

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleRelease --no-daemon --console=plain
```

## 目前限制

1. 尚未提供雲端同步或多裝置同步。
2. 尚未提供照護者帳號或遠端查看功能。
3. 尚未上架 Google Play，正式 APK 目前透過 GitHub Release 下載與安裝。
4. 提醒目前以每項訓練每日固定時間為主，尚未支援同一訓練項目一天多次提醒。
5. App 目前僅作訓練紀錄與提醒，不提供醫療建議。

## 後續可擴充方向

1. 加入更完整的趨勢圖表。
2. 加入月報表或 PDF 匯出。
3. 加入復健目標設定與達成率。
4. 加入照護者分享模板。
5. 建立 Google Play 發布流程。

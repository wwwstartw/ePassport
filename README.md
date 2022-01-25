# ePassport
- 具備讀取及寫入電子護照之功能，此專案皆以 `IntelliJ` 開發
## ePassportApplet (安裝Applet工具)
寫卡前需利用 Gradle Script 安裝 ePassport Applet，點擊右方 `installJavaCard`:
![](./images/Gradle.png)
## ePassportWriter App (寫卡)
### 寫入白卡
- 執行 WriterCard.java ，輸入資料後上傳大頭照，最後點擊寫入按鈕，成功寫入 ePassport: 
  - ![](./images/normal_write.png)
### 寫入 SQL injection 指令
- 執行 WriterCard.java ，於姓氏欄中輸入SQLi指令，最後點擊寫入按鈕，成功寫入 ePassport: 
  - ![](./images/SQLi_write.png)
## ePassportReader App (讀卡)
### 讀取白卡
- 執行 Reader.java ，輸入BAC key後，點擊讀取按鈕，成功讀取 ePassport:
  - ![](./images/normal_read.png)
### 讀取真實護照
- 執行 Reader.java ，輸入BAC key後，點擊讀取按鈕，成功讀取 ePassport: 
  - ![](./images/passport_reader.png)
### 驗卡
- 建立資料庫，預先存放合格的使用者資料
  - ![](./images/SQLi_database.png)
- 執行 Reader.java ，輸入BAC key後，點擊讀取按鈕，成功讀取 ePassport:
  - ![](./images/fake_read.png)
- 讀取成功後，點擊出現的驗卡按鈕，由於該姓名與資料庫中資料不吻合，結果為驗卡失敗:
  - ![](./images/fake_verify.png)
#### 繞過驗卡 (利用 SQL injection)
- 執行 Reader.java ，輸入BAC key後，點擊讀取按鈕，讀取完成後可於姓氏欄中發現 SQL 指令:
  - ![](./images/SQLi_read.png)
- 點擊出驗卡按鈕，雖然姓名與資料庫中不吻合，但由於 SQL injection 攻擊成功，結果為驗卡成功:
  - ![](./images/SQLi_verify.png)
## JMRTD App 寫卡
### 步驟
0. 必須先使用 ePassportApplet 執行 installJavaCard ，才可使用 ePassport Tool 寫護照
1. 安裝 `jmrtd_installer-0.4.9.exe`，安裝過程會有當機現象，直接退出即可
![](./images/JMRTD_install.png)
2. 預設安裝路徑為 `C:\Program Files\JMRTD`，打開 `jmrtd.bat`
3. File -> New，建立新護照，並選擇 Tool -> Import Portrait，注意必須上傳 `JPG` 格式的大頭照，否則上傳過程會出現 `6F00` 錯誤並 fail
![](./images/JMRTD_create_epassport.png)
4. Tools -> Upload passport，輸入 BAC Key，點擊 Upload 開始將護照寫入卡片
![](./images/JMRTD_upload.png)
    - 寫入成功 APDU trace 底端會是 `9000`
      - ![](./images/upload_ok_APDU.png)
5. 回到 JMRTD 主介面，選擇 Tools -> Reload Cards，輸入剛才的 BAC Key
    - ![](./images/JMRTD_reload.png)
6. 讀取護照成功
    - ![](./images/JMRTD_read_passport.png)
7. 上傳護照後，put_mrz 的功能會被鎖上，所以若想上傳不同護照，需要先刪掉 applet 再利用 ePassportApplet 重新安裝 applet，最後在從第三步開始重做，否則會出現 `6985` error
    - 刪除 passport applet (若出現無法刪除的情況，可加上 `-f` 嘗試):
        ```
        gp -delete A00000024710
        ```
    
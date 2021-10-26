# ePassport
- 此專案中 ePassportApplet、ePassportReader 皆以 `IntelliJ` 開發
## ePassportApplet (寫卡)
利用寫好的 Gradle Script 寫入 ePassport 。
![](./images/Gradle.png)
### 步驟
1. installJavaCard
2. createfile
![](./images/createFile.png)
3. put_data_mrz
![](./images/put_data_mrz.png)
4. update_binary_DG1
![](./images/update_binary_DG1.png)
## ePassportReader (讀卡)
直接執行 Main.java ，利用預設 BAC key 通過 BA C驗證，成功讀取 ePassport 。
![](./images/passport_reader.png)
## GUI 讀/寫卡

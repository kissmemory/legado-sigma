# 构建和验证记录（2026-11-19）

基线：Gitee lyc486/legado main，8b87c5aba4df91c39a3a0939a68a1180b9f2ee1c。

结果：完整 Android 构建已成功，testAppDebugUnitTest 与 assembleAppDebug 均通过。14项相关测试，0失败、0错误、0跳过。

APK：Sigma-改名识别测试-20261119.apk；版本3.26.111901-jjid-test；包名io.legado.app.jjidentitytest；名称Sigma 改名识别测试；minSdk21/targetSdk36，包含arm64-v8a、armeabi-v7a、x86、x86_64。v1/v2签名验证通过。独立包名及全部provider authority、应用自定义权限均已检查，不与原Sigma共用。

功能：发现/搜索绿点按固定晋江novelId识别；点击不同链接形式的同作品时复用已有书架记录，保留原书源、分组与进度。详情页前置曾用名，按观察顺序去重并排除当前官网名；刷新时同步官网文案，保留手写简介，失败时保留旧内容。历史保存在现有Book.variable字段，正常备份可携带，不增加数据库表或列。过去未记录到的名字无法凭空恢复。

验证范围：完整Kotlin/Java源码编译、Android资源与打包、14项JVM测试、签名与APK安装元信息。另已在前次检查中验证3795条真实备份URL身份等价。独立代码审查通过。git diff --check通过。

未验证：手机实际安装运行、书源实际响应、备份恢复及界面表现。未自动安装到手机，未操作手机书架，未改两张电脑追更表，未创建远程fork或发布。

构建：JDK17、Gradle8.14.4、Android SDK36/BuildTools35；首次完整构建10分15秒。项目推荐Maven镜像仅通过本地init脚本使用，不修改正式settings.gradle。必要工具均在F盘项目内，已删除主要重复下载归档；保留构建缓存和独立测试签名供后续更新。

后续重现：docs/local-fixes/build-test.ps1可传JdkHome、SdkHome和可选RepositoryInit。独立测试参数-PjjIdentityTest=true，默认构建包名不变。

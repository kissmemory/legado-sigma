# 项目维护约定

- 每次修改代码、增加功能或修复问题，必须在同一次提交中同步更新 `app/src/main/assets/updateLog.md`。
- 沿用现有日志格式：最新日期放在前面，使用 `**YYYY/MM/DD**`，逐条说明具体改动及对使用者的影响；同一天的后续改动补在该日期下，必要时注明版本。
- 清楚区分已完成的验证和仍待实测的内容，不用“优化代码、修复问题”代替具体说明。
- 修改日志与功能代码保持在同一分支；尚未合并到 `main` 的功能，不在 `main` 中记成已发布。

- 每次发布修改版必须同步递增 `shukuS-version.properties` 的 versionName 和 versionCode，并在修改日志注明本版版本号。使用 `docs/local-fixes/build-test.ps1` 默认自动递增；仅开发重复验证可使用 `-ReuseVersion`，不得将不同代码以同一版本号发布。

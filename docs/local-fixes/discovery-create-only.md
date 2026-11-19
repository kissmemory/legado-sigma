# 阅见安全新增接口（3.26.1119.6）

首次在手机长按“Web服务”选择“复制阅见配对码”，填入电脑发现页。配对码只保存在手机 noBackupFilesDir，和实例UUID一起不随普通书架备份恢复；不会从能力接口下发或写入电脑回执。

专用接口：GET /getDiscoveryBridgeInfo、POST /resolveDiscoveryBook、POST /createDiscoveryBook。都要求 X-Discovery-Pairing，拒绝带 Origin 的直接网页调用。电脑发现页仅同源请求自己服务，服务端对手机请求；旧App不支持时禁止回退 /saveBook。

单源查询直接 WebBook.searchBookAwait，不使用跨书源 SearchModel，不写搜索缓存；固定ID匹配后返回原生书目，目录URL与书源变量保留。新增时在Room事务内核对实时分组名/位码、原站固定ID与已有URL，再用INSERT IGNORE只插入新书。不调用Book.save或WebDAV，不更改已有书的进度。

创建操作ID保存在新书既有variable命名空间 __reading_discovery_operation_v1，与新书同事务落盘。电脑回读用该回执证明创建，而不是仅凭同名字段推断；响应丢失可只读复核，不重复POST。电脑关联原生URL摘要键，使之后正常导入不会把本次新增再拆成一行。

本版不自动安装、不对真实手机发送验证写请求。编译、JVM回归与手机接口模拟通过；真机显示和新增待用户安装、配对、预览确认后实测。

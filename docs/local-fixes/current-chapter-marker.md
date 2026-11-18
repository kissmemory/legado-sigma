# 当前阅读章购买标识补齐

上一版仅生成 latestChapterTitle 标识，durChapterTitle 的多条保存路径仍使用无标识的标题。

新增 BookChapter.getProgressTitle，仅用于进度摘要，保留 getDisplayTitle 的原阅读排版语义。目录刷新、正文/漫画/音频进度保存、换源迁移、进度同步与本地目录生成复用此方法；视频保留原始标题语义并附加购买标识。书架和详情页继续显示同一个 durChapterTitle。

购买状态沿用书源 isVip/isPay，免费章和卷标题不新增标识。原始章节标题、索引、位置、分组均不修改。安装后刷新目录一次；手机界面仍待实际验证。

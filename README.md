# RayMusicPlayer
音乐数据：ContentProvider读取系统媒体库
多进程：Service单独开一个进程，进程音乐管理
IPC ： Messenger

Bug：

1. 要先点击播放，开始播放后，再点击音乐列表按钮，否则出错  ---- 逻辑问题，有空修补

2. 播放过程中不定时出错，有时没问题，有时出错 --- MediaPlayer的使用应该有没注意到的地方

# 聊天室
服务器 Java nio
客户端 Java bio

# 客户端
select后，根据可accept、可读分发
可读根据消息结构体的type分发

## 线程池
使用线程池，放入处理消息的任务

## 关注事件取消注册、恢复注册

`key.interestOps(key.interestOps() & (~ SelectionKey.OP_READ));
`

`key.interestOps(key.interestOps() | ( SelectionKey.OP_READ));`

当一个socketchannel可读的时候，创建一个runnable放进线程池
如果不取消注册，selector.selectedKeys会一直返回，当已提交的runnable还没处理完的时候，for遍历key会不停的创建
重复的runnable。严重的时候还会多个重复runnable同时运行，对同一个socketchannel同时读，这样读到的不是完整的信息

`key.selector().wakeup();
`
这个是增加感兴趣的事件后，原来的主线程并没有监听这个事件，因为在之前取消监听了，线程会一直阻塞等待，所以需要唤醒

# TCP粘包解决
打包一个数据包，在前面加上一个数据包长度
解码一个数据包，先获取长度，再读取指定长度的数据
代码在NetUtil

# lombok、fashjson
序列化class的时候，需要用到getter和setter来获取和设置属性

# 运行线程是哪个
一个线程调用另一个线程对象的方法，还是原来的线程在执行这个方法
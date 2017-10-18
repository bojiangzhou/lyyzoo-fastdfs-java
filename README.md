## FastDFS Client Java API

这是基于[fastdfs-client-java](https://github.com/happyfish100/fastdfs-client-java)
开发、使用commons-pool2连接池封装的工具类，提供简易的上传下载功能.

* FastDFSClient 主要的工具类
* ErrorCode 异常编码
* FastDFSException 异常类
* FileResponseData 文件返回信息
* TrackerServerFactory TrackerServer Factory
* TrackerServerPool TrackerServer Pool

搭好FastDFS环境后，修改config.properties的相关配置，可直接启动运行项目，通过提供的接口访问，可使用Postman进行测试

搭建环境的步骤可参考个人博客：[用FastDFS一步步搭建文件管理系统](http://www.cnblogs.com/chiangchou/p/fastdfs.html)



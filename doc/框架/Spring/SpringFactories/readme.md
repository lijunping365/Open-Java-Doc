# Spring Factories 讲解

## Java SPI

Spring Boot中有一种非常解耦的扩展机制：Spring Factories。这种扩展机制实际上是仿照Java中的SPI扩展机制来实现的。
java SPI机制：为某个接口寻找服务实现的机制。有点类似IOC的思想，就是将装配的控制权移到程序之外，在模块化设计中这个机制尤其重要。

## Spring Boot中的 SPI 机制

在Spring中也有一种类似与Java SPI的加载机制。它在META-INF/spring.factories文件中配置接口的实现类名称，然后在程序中读取这些配置文件并实例化。

作者：王勇1024
链接：https://www.jianshu.com/p/00e49c607fa1
来源：简书
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

常见的使用方式

图见：[常见使用方式.png]






























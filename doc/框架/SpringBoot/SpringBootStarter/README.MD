# 可能存在误解，如有误解请指出

@Import 注解是一个 SPI 注解

registrar：

默认情况下，registrar 只允许 @SpringBootApplication 所在包路径下扫描并导入到容器中

当我们指定了 registrar，它允许我们扫描其他包路径并导入到容器中

selector： 

默认情况下，selector 会去 spring.factories 文件中找全限定类名并导入到容器中

当我们指定了 selector，它允许我们导入指定的全限定类名到容器中


2. 我们平时用的 @Enablexxx 都是导入 xxx 到容器中，意思是直接把 xxx 导入到容器中

但是对于某些组件我们需要根据条件才能导入到容器，所以这些组件就可以在 xxxAutoConfiguration（自动配置类）中进行配置，只有符合条件才会导入该组件到容器

所以，在 starter 中我们可以结合 @Import 和 xxxAutoConfiguration 共同来完成组件的导入


# 自动配置文件：spring.factories

Spring Boot中有一种非常解耦的扩展机制：Spring Factories。这种扩展机制实际上是仿照Java中的SPI扩展机制来实现的。
java SPI机制：为某个接口寻找服务实现的机制。有点类似IOC的思想，就是将装配的控制权移到程序之外，在模块化设计中这个机制尤其重要。

## Spring Boot中的 SPI 机制

在Spring中也有一种类似与Java SPI的加载机制。它在META-INF/spring.factories文件中配置接口的实现类名称，然后在程序中读取这些配置文件并实例化。



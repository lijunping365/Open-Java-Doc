# SpringBoot自定义专属业务的Starter

## 前言

在我们学习 SpringBoot时都已经了解到 starter是 SpringBoot的核心组成部分， SpringBoot为我们提供了尽可能完善的封装，提供了一系列的自动化配置的 starter插件，
我们在使用 spring-boot-starter-web时只需要在 pom.xml配置文件内添加依赖就可以了，我们之前传统方式则是需要添加很多相关 SpringMVC配置文件。
而 spring-boot-starter-web为我们提供了几乎所有的默认配置，很好的降低了使用框架时的复杂度。 因此在使用 xx.starter时你就不用考虑该怎么配置，
即便是有一些必要的配置在 application.properties配置文件内对应配置就可以了，那好，为什么我在 application.properties配置对应属性后 xx.starter就可以获取到并作出处理呢？
下面我们带着这个疑问来编写我们自定义的 starter让我们深入了解 SpringBoot


## 目标

自定义 starter并且通过 spring-boot-autoconfigure完成自动化配置。

## 构建项目

### 配置映射参数实体

starter是如何读取 application.properties或者 application.yml配置文件内需要的配置参数的呢？那么接下来我们就看看如何可以获取自定义的配置信息。 SpringBoot在处理这种事情上早就已经考虑到了，所以提供了一个注解 @ConfigurationProperties，
该注解可以完成将 application.properties配置文件内的有规则的配置参数映射到实体内的 field内，不过需要提供setter方法，自定义配置参数实体代码如下所示：

```java
@Data
@ConfigurationProperties("hello")
public class HelloProperties {

  private String message;

}
```
在上面代码中， @ConfigurationProperties注解内我们使用到了属性 preffix，该属性配置了读取参数的前缀，根据上面的实体属性对应配置文件内的配置则是 hello.msg、 hello.show，当然我们提供了默认值，配置文件内不进行配置时则是使用默认值。


### 编写自定义业务

我们为自定义 starter提供一个 Service，并且提供一个名为 sayHello的方法用于返回我们配置的 msg内容。代码如下所示：
```java
public class HelloService {

  public String sayHello(){
    return "hello";
  }
}
```

### 实现自动化配置

接下来我们开始编写自动配置，这一块是 starter的核心部分，配置该部分后在启动项目时才会自动加载配置，当然其中有很多细节性质的配置
自动化配置其实只是提供实体bean的验证以及初始化，我们先来看看代码：

```java
@Configuration//开启配置
@EnableConfigurationProperties(HelloProperties.class) //开启使用映射实体对象
@ConditionalOnClass(HelloService.class)//存在HelloService时初始化该配置类
@ConditionalOnProperty( //存在对应配置信息时初始化该配置类
    prefix = "hello",//存在配置前缀hello
    value = "enabled",//开启
    matchIfMissing = true//缺失检查
)
public class HelloAutoConfig {

  //application.properties配置文件映射前缀实体对象
  @Autowired
  private HelloProperties helloProperties;

  /**
   * 根据条件判断不存在HelloService时初始化新bean到SpringIoc
   * @param helloProperties
   * @return
   */
  @Bean
  @ConditionalOnMissingBean
  public HelloService helloService(HelloProperties helloProperties){
    HelloService helloService = new HelloService();
    helloService.setMessage(helloProperties.getMessage());
    return helloService;
  }

}
```
自动化配置代码中有很多我们之前没有用到的注解配置，我们从上开始讲解

> @EnableConfigurationProperties

这是一个开启使用配置参数的注解， value值就是我们配置实体参数映射的 ClassType，将配置实体作为配置来源。

> SpringBoot内置条件注解

有关 @ConditionalOnXxx相关的注解这里要系统的说下，因为这个是我们配置的关键，根据名称我们可以理解为 具有Xxx条件，当然它实际的意义也是如此，条件注解是一个系列，下面我们详细做出解释

@ConditionalOnBean：当 SpringIoc容器内存在指定 Bean的条件 @ConditionalOnClass：当 SpringIoc容器内存在指定 Class的条件 @ConditionalOnExpression：基于SpEL表达式作为判断条件 @ConditionalOnJava：基于 JVM版本作为判断条件 @ConditionalOnJndi：在JNDI存在时查找指定的位置 @ConditionalOnMissingBean：当 SpringIoc容器内不存在指定 Bean的条件 @ConditionalOnMissingClass：当 SpringIoc容器内不存在指定 Class的条件 @ConditionalOnNotWebApplication：当前项目不是Web项目的条件 @ConditionalOnProperty：指定的属性是否有指定的值 @ConditionalOnResource：类路径是否有指定的值 @ConditionalOnSingleCandidate：当指定 Bean在 SpringIoc容器内只有一个，或者虽然有多个但是指定首选的 Bean @ConditionalOnWebApplication：当前项目是Web项目的条件

以上注解都是元注解 @Conditional演变而来的，根据不用的条件对应创建以上的具体条件注解。

到目前为止我们还没有完成自动化配置 starter，我们需要了解 SpringBoot运作原理后才可以完成后续编码。


### Starter自动化运作原理

在注解 @SpringBootApplication上存在一个开启自动化配置的注解 @EnableAutoConfiguration来完成自动化配置，注解源码如下所示：




















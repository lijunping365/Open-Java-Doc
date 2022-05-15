# 七种方式，教你在SpringBoot初始化时搞点事情！

## 容器刷新完成扩展点
1、监听容器刷新完成扩展点ApplicationListener<ContextRefreshedEvent>
2、SpringBoot的CommandLineRunner接口
3、SpringBoot的ApplicationRunner接口

## Bean初始化完成扩展点
1、@PostConstruct注解
2、 InitializingBean接口
3、@Bean注解的初始化方法
4、通过构造函数注入


### 1、监听容器刷新完成扩展点ApplicationListener<ContextRefreshedEvent>

**基本用法**

熟悉Spring的同学一定知道，容器刷新成功意味着所有的Bean初始化已经完成，
当容器刷新之后Spring将会调用容器内所有实现了ApplicationListener<ContextRefreshedEvent>的Bean的onApplicationEvent方法，
应用程序可以以此达到监听容器初始化完成事件的目的。

```java
@Component
public class StartupApplicationListenerExample implements
  ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = Logger.getLogger(StartupApplicationListenerExample.class);

    public static int counter;

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Increment counter");
        counter++;
    }
}
```

**易错的点**

这个扩展点用在web容器中的时候需要额外注意，在web 项目中（例如spring mvc），系统会存在两个容器，
一个是root application context,另一个就是我们自己的context（作为root application context的子容器）。
如果按照上面这种写法，就会造成onApplicationEvent方法被执行两次。解决此问题的方法如下：

```java
@Component
public class StartupApplicationListenerExample implements
  ApplicationListener<ContextRefreshedEvent> {

    private static final Logger LOG = Logger.getLogger(StartupApplicationListenerExample.class);

    public static int counter;

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            // root application context 没有parent
            LOG.info("Increment counter");
            counter++;
        }
    }
}
```

**高阶玩法**

当然这个扩展还可以有更高阶的玩法：自定义事件 ，可以借助Spring以最小成本实现一个观察者模式：
先自定义一个事件：

```java
public class NotifyEvent extends ApplicationEvent {
    private String email;
    private String content;
    public NotifyEvent(Object source) {
        super(source);
    }
    public NotifyEvent(Object source, String email, String content) {
        super(source);
        this.email = email;
        this.content = content;
    }
    // 省略getter/setter方法
}

```
注册一个事件监听器

```java
@Component
public class NotifyListener implements ApplicationListener<NotifyEvent> {

    @Override
    public void onApplicationEvent(NotifyEvent event) {
        System.out.println("邮件地址：" + event.getEmail());
        System.out.println("邮件内容：" + event.getContent());
    }
}
```

发布事件

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class ListenerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Test
    public void testListener() {
        NotifyEvent event = new NotifyEvent("object", "abc@qq.com", "This is the content");
        webApplicationContext.publishEvent(event);
    }
}
```
执行单元测试可以看到邮件的地址和内容都被打印出来了

### 2、SpringBoot的CommandLineRunner接口

当容器上下文初始化完成之后，SpringBoot也会调用所有实现了CommandLineRunner接口的run方法，下面这段代码可起到和上文同样的作用：

```java
@Component
public class CommandLineAppStartupRunner implements CommandLineRunner {
    private static final Logger LOG = LoggerFactory.getLogger(CommandLineAppStartupRunner.class);

    public static int counter;

    @Override
    public void run(String...args) throws Exception {
        LOG.info("Increment counter");
        counter++;
    }
}
```
对于这个扩展点的使用有额外两点需要注意：
(1) 多个实现了CommandLineRunner的Bean的执行顺序可以根据Bean上的@Order注解调整
(2) 其run方法可以接受从控制台输入的参数，跟ApplicationListener<ContextRefreshedEvent>这种扩展相比，更加灵活

```
// 从控制台输入参数示例
java -jar CommandLineAppStartupRunner.jar abc abcd
```

### 3、SpringBoot的ApplicationRunner接口

这个扩展和SpringBoot的CommandLineRunner接口的扩展类似，只不过接受的参数是一个ApplicationArguments类，
对控制台输入的参数提供了更好的封装，以--开头的被视为带选项的参数，否则是普通的参数

```java
@Component
public class AppStartupRunner implements ApplicationRunner {
    private static final Logger LOG =
      LoggerFactory.getLogger(AppStartupRunner.class);

    public static int counter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        LOG.info("Application started with option names : {}",
          args.getOptionNames());
        LOG.info("Increment counter");
        counter++;
    }
}
```

```
例如：
java -jar CommandLineAppStartupRunner.jar abc abcd --autho=mark verbose
```

Bean初始化完成扩展点

前面的内容总结了针对容器初始化的扩展点，在有些场景，比如监听消息的时候，我们希望Bean初始化完成之后立刻注册监听器，
而不是等到整个容器刷新完成，Spring针对这种场景同样留足了扩展点：

### 1、@PostConstruct注解

@PostConstruct注解一般放在Bean的方法上，被@PostConstruct修饰的方法会在Bean初始化后马上调用：

```java
@Component
public class PostConstructExampleBean {

    private static final Logger LOG = Logger.getLogger(PostConstructExampleBean.class);

    @Autowired
    private Environment environment;

    @PostConstruct
    public void init() {
        LOG.info(Arrays.asList(environment.getDefaultProfiles()));
    }
}
```

### 2、 InitializingBean接口

InitializingBean的用法基本上与@PostConstruct一致，只不过相应的Bean需要实现afterPropertiesSet方法

```java
@Component
public class InitializingBeanExampleBean implements InitializingBean {

    private static final Logger LOG
      = Logger.getLogger(InitializingBeanExampleBean.class);

    @Autowired
    private Environment environment;

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info(Arrays.asList(environment.getDefaultProfiles()));
    }
}
```

### 3、@Bean注解的初始化方法

通过@Bean注入Bean的时候可以指定初始化方法：

Bean的定义
```java
public class InitMethodExampleBean {

    private static final Logger LOG = Logger.getLogger(InitMethodExampleBean.class);

    @Autowired
    private Environment environment;

    public void init() {
        LOG.info(Arrays.asList(environment.getDefaultProfiles()));
    }
}
```

Bean注入

```
@Bean(initMethod="init")
public InitMethodExampleBean initMethodExampleBean() {
    return new InitMethodExampleBean();
}
```

### 4、通过构造函数注入

Spring也支持通过构造函数注入，我们可以把搞事情的代码写在构造函数中，同样能达到目的

```java
@Component
public class LogicInConstructorExampleBean {

    private static final Logger LOG
      = Logger.getLogger(LogicInConstructorExampleBean.class);

    private final Environment environment;

    @Autowired
    public LogicInConstructorExampleBean(Environment environment) {
        this.environment = environment;
        LOG.info(Arrays.asList(environment.getDefaultProfiles()));
    }
}
```

## Bean初始化完成扩展点执行顺序？
可以用一个简单的测试：

```java
@Component
@Scope(value = "prototype")
public class AllStrategiesExampleBean implements InitializingBean {

    private static final Logger LOG
      = Logger.getLogger(AllStrategiesExampleBean.class);

    public AllStrategiesExampleBean() {
        LOG.info("Constructor");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOG.info("InitializingBean");
    }

    @PostConstruct
    public void postConstruct() {
        LOG.info("PostConstruct");
    }

    public void init() {
        LOG.info("init-method");
    }
}
```

实例化这个Bean后输出：

```
[main] INFO o.b.startup.AllStrategiesExampleBean - Constructor
[main] INFO o.b.startup.AllStrategiesExampleBean - PostConstruct
[main] INFO o.b.startup.AllStrategiesExampleBean - InitializingBean
[main] INFO o.b.startup.AllStrategiesExampleBean - init-method
```













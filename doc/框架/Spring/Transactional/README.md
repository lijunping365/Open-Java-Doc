# 使用@Transactional竟然出现了长事务，导致生产事故！

在Spring中进行事务管理非常简单，只需要在方法上加上注解@Transactional，Spring就可以自动帮我们进行事务的开启、提交、回滚操作。
甚至很多人心里已经将Spring事务与@Transactional划上了等号，只要有数据库相关操作就直接给方法加上@Transactional注解。

## 事故还原

```
/**
 * 保存报销单并创建工作流
 * 代码非常简单也很 “优雅”，先通过http接口调用工作流引擎创建审批流，然后保存报销单
 */
@Transactional(rollbackFor = Exception.class)
public void save(RequestBillDTO requestBillDTO){
     //调用流程HTTP接口创建工作流
    workflowUtil.createFlow("BILL",requestBillDTO);
    
    //转换DTO对象
    RequestBill requestBill = JkMappingUtils.convert(requestBillDTO, RequestBill.class);
    requestBillDao.save(requestBill);
    //保存明细表
    requestDetailDao.save(requestBill.getDetail())
}
```

报销项目属于公司内部项目，本身是没什么高并发的，系统也一直稳定运行着。
在年末的一天下午,公司发通知邮件说年度报销窗口即将关闭，需要尽快将未报销的费用报销掉，而刚好那天工作流引擎在进行安全加固。
收到邮件后报销的人开始逐渐增多，在接近下班的时候到达顶峰，此时报销系统开始出现了故障：数据库监控平台一直收到告警短信，数据库连接不足，出现大量死锁；
日志显示调用流程引擎接口出现大量超时；同时一直提示CannotGetJdbcConnectionException，数据库连接池连接占满。
在发生故障后，我们尝试过杀掉死锁进程，也进行过暴力重启，只是不到10分钟故障再次出现，收到大量电话投诉。 最后没办法只能向全员发送停机维护邮件并发送故障报告

## 事故原因分析

我们知道@Transactional 注解，是使用 AOP 实现的，本质就是在目标方法执行前后进行拦截。
在目标方法执行前加入或创建一个事务，在执行方法执行后，根据实际情况选择提交或是回滚事务。

当 Spring 遇到该注解时，会自动从数据库连接池中获取 connection，并开启事务然后绑定到 ThreadLocal 上，
对于@Transactional注解包裹的整个方法都是使用同一个connection连接。如果我们出现了耗时的操作，比如第三方接口调用，业务逻辑复杂，
大批量数据处理等就会导致我们我们占用这个connection的时间会很长，数据库连接一直被占用不释放。一旦类似操作过多，就会导致数据库连接池耗尽。

在一个事务中执行RPC操作导致数据库连接池撑爆属于是典型的长事务问题，类似的操作还有在事务中进行大量数据查询，业务规则处理等...

### 何为长事务？

顾名思义就是运行时间比较长，长时间未提交的事务，也可以称之为大事务。

### 长事务会引发哪些问题？ 

长事务引发的常见危害有：

```
数据库连接池被占满，应用无法获取连接资源；
容易引发数据库死锁；
数据库回滚时间长；
在主从架构中会导致主从延时变大。
```

### 如何避免长事务？

既然知道了长事务的危害，那如何在开发中避免出现长事务问题呢？

很明显，解决长事务的宗旨就是 对事务方法进行拆分，尽量让事务变小，变快，减小事务的颗粒度。

既然提到了事务的颗粒度，我们就先回顾一下Spring进行事务管理的方式。

## 声明式事务

首先我们要知道，通过在方法上使用@Transactional注解进行事务管理的操作叫声明式事务 。

使用声明式事务的优点 很明显，就是使用很简单，可以自动帮我们进行事务的开启、提交以及回滚等操作。使用这种方式，程序员只需要关注业务逻辑就可以了。

声明式事务有一个最大的缺点，就是事务的颗粒度是整个方法，无法进行精细化控制。

与声明式事务对应的就是编程式事务。

## 编程式事务

基于底层的API，开发者在代码中手动的管理事务的开启、提交、回滚等操作。在spring项目中可以使用TransactionTemplate类的对象，手动控制事务。

```
@Autowired 
private TransactionTemplate transactionTemplate; 
 
... 

public void save(RequestBill requestBill) { 
    transactionTemplate.execute(transactionStatus -> {
        requestBillDao.save(requestBill);
        //保存明细表
        requestDetailDao.save(requestBill.getDetail());
        return Boolean.TRUE; 
    });
} 
```

使用编程式事务最大的好处就是可以精细化控制事务范围。

所以避免长事务最简单的方法就是不要使用声明式事务@Transactional，而是使用编程式事务手动控制事务范围。

有的同学会说，@Transactional使用这么简单，有没有办法既可以使用@Transactional，又能避免产生长事务？

那就需要对方法进行拆分，将不需要事务管理的逻辑与事务操作分开：
query()与validate()不需要事务，我们将其与事务方法saveData()拆开。
```
@Service
public class OrderService{

    public void createOrder(OrderCreateDTO createDTO){
        query();
        validate();
        saveData(createDTO);
    }
  
  //事务操作
    @Transactional(rollbackFor = Throwable.class)
    public void saveData(OrderCreateDTO createDTO){
        orderDao.insert(createDTO);
    }
}
```

当然，这种拆分会命中使用@Transactional注解时事务不生效的经典场景，

## @Transactional注解时事务不生效的经典场景

@Transactional注解的声明式事务是通过spring aop起作用的，而spring aop需要生成代理对象，直接在同一个类中方法调用使用的还是原始对象，事务不生效。其他几个常见的事务不生效的场景为：

```
1. @Transactional 应用在非 public 修饰的方法上
2. @Transactional 注解属性 propagation 设置错误
3. @Transactional 注解属性 rollbackFor 设置错误
4. 同一个类中方法调用，导致@Transactional失效
5. 异常被catch捕获导致@Transactional失效
```

### 正确的拆分方法应该使用下面两种：

1. 可以将方法放入另一个类，如新增 manager层，通过spring注入，这样符合了在对象之间调用的条件。

```
@Service
public class OrderService{
  
    @Autowired
   private OrderManager orderManager;

    public void createOrder(OrderCreateDTO createDTO){
        query();
        validate();
        orderManager.saveData(createDTO);
    }
}

@Service
public class OrderManager{
  
    @Autowired
   private OrderDao orderDao;
  
  @Transactional(rollbackFor = Throwable.class)
    public void saveData(OrderCreateDTO createDTO){
        orderDao.saveData(createDTO);
    }
}
```

2. 启动类添加@EnableAspectJAutoProxy(exposeProxy = true)，方法内使用AopContext.currentProxy()获得代理类，使用事务。

```
SpringBootApplication.java

@EnableAspectJAutoProxy(exposeProxy = true)
@SpringBootApplication
public class SpringBootApplication {}

```

```
OrderService.java
  
public void createOrder(OrderCreateDTO createDTO){
    OrderService orderService = (OrderService)AopContext.currentProxy();
    orderService.saveData(createDTO);
}
```

## 小结

使用@Transactional注解在开发时确实很方便，但是稍微不注意就可能出现长事务问题。
所以对于复杂业务逻辑，我这里更建议你使用编程式事务来管理事务，当然，如果你非要使用@Transactional，可以根据上文提到的两种方案进行方法拆分。













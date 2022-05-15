# ioc 概念

IOC(Inversion of Control) 控制反转的核心思想在于，资源的使用不由使用各自管理，而是交给不使用资源的第三方进行管理。
这样的好处是资源是集中管理的，可配置，易维护，同时也降低了双方的依赖做到了低耦合。

# ioc 主要实现策略
ioc 可能被不同的方式去实现，通常用以下两种实现策略
（1）依赖查找
（2）依赖注入 

## 依赖查找（主动）
### 依赖查找的实现方式：
### 实际使用案例：
（1）比如使用 BeanFactory 查找

## 依赖注入（被动 & 主动）
### 依赖注入的实现方式：
（1） 构造器注入
（2） 参数注入
（3） Setter 注入
（4） 接口注入
### 实际使用案例：
（1）@Autowired 依赖注入


# 面试题
（1）什么是 IOC ？
简单的说， IOC 是反转控制，类似于好莱坞原则，主要有依赖查找和依赖注入实现。
（2）依赖查找和依赖注入的区别
依赖查找是主动或手动的依赖查找方式，通常需要依赖容器或标准的 API 实现。
而依赖注入则是手动或自动依赖绑定的方式，无需依赖特定的容器和 API.
（3）Spring 作为 IOC 容器有什么优势
典型的 IOC 管理，依赖查找和依赖注入

# IOC 容器

## Spring IOC 依赖查找
一。根据 Bean 名称查找
（1）实时查找
（2）延迟查找
二。根据 Bean 类型查找
（1）单个 Bean 对象
（2）集合 Bean 对象
三。根据 Bean 名称 + 类型查找
四。根据 Java 注解查找
（1）单个 Bean 对象
（2）集合 Bean 对象

一个 Spring 应用可以有多个上下文。

## Spring IOC 依赖注入
一。根据 Bean 名称注入
二。根据 Bean 类型注入
（1）单个 Bean 对象
（2）集合 Bean 对象
三。注入容器内建 Bean 对象
四。注入非 Bean 对象
五。注入类型
（1）实时注入
（2）延迟注入

（1） 什么是 IOC 容器
DI（dependence inject）依赖注入

（2）BeanFactory 与 FactoryBean 的区别？
BeanFactory 是 IOC 底层容器
FactoryBean 是 创建 Bean 的一种方式，帮助实现复杂的初始化逻辑

（3）Spring IOC 容器启动时做了哪些准备?
IOC 配置元信息读取和解析（可以读 XML， 可以读 Bean 的注解的方式），IOC 容器生命周期，Spring 事件发布，国际化等

（4）BeanFactory 与 ApplicationContext 的区别
BeanFactory 是一个底层的 IOC 容器， ApplicationContext 是在这基础上增加了一些特性
ApplicationContext 是对 BeanFactory 的一个超集，就是说 BeanFactory 有的功能，ApplicationContext 都有，
并且 ApplicationContext 提供了更多的特性，比如说 AOP 更好的整合，国际化，事务的发布，
但是需要注意 BeanFactory 和 ApplicationContext 不是同一个对象。

（5） Aware 提供的回调函数

# 了解 spring bean 的生命周期的作用：
其实就是为了在应用启动过程中，在 spring bean 的生命周期中，通过 spring 为我们提供的 api，把自己的东西放到 IOC 容器中。


## Spring IOC 特性 --- 接口类
(1) FactoryBean
(2) BeanPostProcessor
(3) BeanFactoryAware
(4) ApplicationContextAware







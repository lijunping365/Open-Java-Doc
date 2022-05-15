#为什么需要 ThreadLocal

最近不是开放三胎政策嘛，假设你有三个孩子。
现在你带着三个孩子出去逛街，路过了玩具店，三个孩子都看中了一款变形金刚。
所以你买了一个变形金刚，打算让三个孩子轮着玩。
回到家你发现，孩子因为这个玩具吵架了，三个都争着要玩，谁也不让着谁。
这时候怎么办呢？你可以去拉架，去讲道理，说服孩子轮流玩，但这很累。
所以一个简单的办法就是出去再买两个变形金刚，这样三个孩子都有各自的变形金刚，世界就暂时得到了安宁。
映射到我们今天的主题，变形金刚就是共享变量，孩子就是程序运行的线程。
有多个线程(孩子)，争抢同一个共享变量(玩具)，就会产生冲突，而程序的解决办法是加锁(父母说服，讲道理，轮流玩)，但加锁就意味着性能的消耗(父母比较累)。
所以有一种解决办法就是避免共享(让每个孩子都各自拥有一个变形金刚)，这样线程之间就不需要竞争共享变量(孩子之间就不会争抢)。
所以为什么需要 ThreadLocal？
就是为了通过本地化资源来避免共享，避免了多线程竞争导致的锁等消耗。
这里需要强调一下，不是说任何东西都能直接通过避免共享来解决，因为有些时候就必须共享。
举个例子：当利用多线程同时累加一个变量的时候，此时就必须共享，因为一个线程的对变量的修改需要影响要另个线程，不然累加的结果就不对了。
再举个不需要共享的例子：比如现在每个线程需要判断当前请求的用户来进行权限判断，那这个用户信息其实就不需要共享，因为每个线程只需要管自己当前执行操作的用户信息，跟别的用户不需要有交集。
好了，道理很简单，这下子想必你已经清晰了 ThreadLocal 出现的缘由了。
再来看一下 ThreadLocal 使用的小 demo。

```java
public class YesThreadLocal {

    private static final ThreadLocal<String> threadLocalName = ThreadLocal.withInitial(() -> Thread.currentThread().getName());

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                System.out.println("threadName: " + threadLocalName.get());
            }, "yes-thread-" + i).start();
        }
    }
}

```
可以看到，我在 new 线程的时候，设置了每个线程名，每个线程都操作同一个 ThreadLocal 对象的 get 却返回的各自的线程名，是不是很神奇？


#应该如何设计 ThreadLocal

那应该怎么设计 ThreadLocal 来实现以上的操作，即本地化资源呢？
我们的目标已经明确了，就是用 ThreadLocal 变量来实现线程隔离。
从代码上看，可能最直接的实现方法就是将 ThreadLocal 看做一个 map ，然后每个线程是  key，这样每个线程去调用 ThreadLocal.get 的时候，将自身作为 key 去 map 找，这样就能获取各自的值了。
听起来很完美？错了！
这样 ThreadLocal 就变成共享变量了，多个线程竞争 ThreadLocal ，那就得保证 ThreadLocal 的并发安全，那就得加锁了，这样绕了一圈就又回去了。

所以这个方案不行，那应该怎么做？
答案其实上面已经讲了，是需要在每个线程的本地都存一份值，说白了就是每个线程需要有个变量，来存储这些需要本地化资源的值，并且值有可能有多个，所以怎么弄呢？
在线程对象内部搞个 map，把 ThreadLocal 对象自身作为 key，把它的值作为 map 的值。
这样每个线程可以利用同一个对象作为 key ，去各自的 map 中找到对应的值。
这不就完美了嘛！比如我现在有 3 个 ThreadLocal  对象，2 个线程。

这样一来就满足了本地化资源的需求，每个线程维护自己的变量，互不干扰，实现了变量的线程隔离，同时也满足存储多个本地变量的需求，完美！
JDK就是这样实现的！我们来看看源码。

#从源码看ThreadLocal 的原理

前面我们说到 Thread 对象里面会有个 map，用来保存本地变量。我们来看下 jdk 的 Thread 实现

```
public class Thread implements Runnable {
     // 这就是我们说的那个 map 。
    ThreadLocal.ThreadLocalMap threadLocals = null;
}
```

可以看到，确实有个 map ，不过这个 map 是 ThreadLocal 的静态内部类，记住这个变量的名字 threadLocals，下面会有用的哈。
看到这里，想必有很多小伙伴会产生一个疑问。
竟然这个 map 是放在 Thread 里面使用，那为什么要定义成 ThreadLocal 的静态内部类呢？

首先内部类这个东西是编译层面的概念，就像语法糖一样，经过编译器之后其实内部类会提升为外部顶级类，和平日里外部定义的类没有区别，也就是说在 JVM 中是没有内部类这个概念的。
一般情况下非静态内部类用在内部类，跟其他类无任何关联，专属于这个外部类使用，并且也便于调用外部类的成员变量和方法，比较方便。
而静态外部类其实就等于一个顶级类，可以独立于外部类使用，所以更多的只是表明类结构和命名空间。
所以说这样定义的用意就是说明 ThreadLocalMap 是和 ThreadLocal 强相关的，专用于保存线程本地变量。

现在我们来看一下 ThreadLocalMap 的定义：
代码可查看 ThreadLocal 中的 ThreadLocalMap 类

```java
static class ThreadLocalMap {

        /**
         * The entries in this hash map extend WeakReference, using
         * its main ref field as the key (which is always a
         * ThreadLocal object).  Note that null keys (i.e. entry.get()
         * == null) mean that the key is no longer referenced, so the
         * entry can be expunged from table.  Such entries are referred to
         * as "stale entries" in the code that follows.
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * The initial capacity -- MUST be a power of two.
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * The table, resized as necessary.
         * table.length MUST always be a power of two.
         */
        private Entry[] table;

        /**
         * The number of entries in the table.
         */
        private int size = 0;
}
```

重点我已经标出来了，首先可以看到这个 ThreadLocalMap 里面有个 Entry 数组，熟悉 HashMap 的小伙伴可能有点感觉了。
这个 Entry 继承了 WeakReference 即弱引用。这里需要注意，不是说 Entry 自己是弱引用，看到我标注的 Entry 构造函数的 super(k) 没，这个 key 才是弱引用。
所以 ThreadLocalMap 里有个 Entry 的数组，这个 Entry 的 key 就是 ThreadLocal 对象，value 就是我们需要保存的值。
那是如何通过 key 在数组中找到 Entry 然后得到 value 的呢 ？


#ThreadLocal 内存泄露之为什么要用弱引用



#ThreadLocal 的最佳实践



#InheritableThreadLocal
















# threadLocal 的作用 
ThreadLocal的作用主要
（1）是做数据隔离，填充的数据只属于当前线程，变量的数据对别的线程而言是相对隔离的，在多线程环境下，如何防止自己的变量被其它线程篡改。
（2）ThreadLocal 要解决的是线程内资源共享
# threadLocal 的使用场景
（1）一般会用在全链路监控中，或者是像日志框架 MDC 这样的组件里。
（2）Spring实现事务隔离级别
Spring采用Threadlocal的方式，来保证单个线程中的数据库操作使用的是同一个数据库连接，同时，采用这种方式可以使业务层使用事务时不需要感知并管理connection对象，通过传播级别，巧妙地管理多个事务配置之间的切换，挂起和恢复。

Spring框架里面就是用的ThreadLocal来实现这种隔离，主要是在TransactionSynchronizationManager这个类里面，代码如下所示:
···
private static final Log logger = LogFactory.getLog(TransactionSynchronizationManager.class);

 private static final ThreadLocal<Map<Object, Object>> resources =
   new NamedThreadLocal<>("Transactional resources");

 private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations =
   new NamedThreadLocal<>("Transaction synchronizations");

 private static final ThreadLocal<String> currentTransactionName =
   new NamedThreadLocal<>("Current transaction name");

  ……
···

# threadLocal 的底层实现原理
其实使用真的很简单，线程进来之后初始化一个可以泛型的ThreadLocal对象，之后这个线程只要在remove之前去get，都能拿到之前set的值，注意这里我说的是remove之前。
他是能做到线程间数据隔离的，所以别的线程使用get（）方法是没办法拿到其他线程的值的，但是有办法可以做到，后面会说。

# 
























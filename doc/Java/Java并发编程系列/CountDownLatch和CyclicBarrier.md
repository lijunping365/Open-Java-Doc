# 场景
现在我有 50 个任务，这 50 个任务在完成之后，才能执行下一个[函数]，要是你，你怎么设计(可能不止一个线程执行哦)

# 解决
可以用 JDK 提供的线程工具类， CountDownLatch 或 CyclicBarrier 都可以完成这个需求
这两个类都是线程同步的工具类，都是基于 AQS 实现的，都可以等到线程完成之后，才去执行某些操作。

# CountDownLatch 和 CyclicBarrier 的区别
区别一
CountDownLatch 允许一个或多个线程一直等待，直到这些线程完成他们的操作
而 CyclicBarrier 不一样，它往往是当当线程达到某种状态后，暂停下来等待其他线程，等到所有线程都达到后，才继续执行。
可以发现这两者的等待主体是不一样的。

区别二
CountDownLatch 调用 await() 通常是主线程/调用线程，而 CyclicBarrier 调用 await() 是在任务线程调用的
所以 CyclicBarrier 中阻塞的是任务的线程，而主线程是不受影响的，


区别三
CountDownLatch 用完了，就结束了，没法复用，而 CyclicBarrier 不一样，它可以复用。
就是 CyclicBarrier 可在完成时重置进而重复使用。

# 源码解析

## CountDownLatch 
上面说了，CountDownLatch 是基于 AQS 实现的，它的机制很简单
当我们构建 CountDownLatch 对象时，传入的值其实就会赋值给 AQS 的关键变量 state，
执行 countDown 方法时，其实就是利用 CAS 将 state - 1；
执行 await 方法时，其实就是判断 state 是否为 0，不为 0 则加入到队列中，将该线程阻塞掉（除了头节点）
因为头节点会一直自旋等待 state 为 0，当 state 为 0 时，头节点把剩余的在队列中阻塞的节点也一并唤醒。

## CyclicBarrier
CyclicBarrier 重点就是 await 方法，从源码不难发现的是，他没有像 CountDownLatch 和 ReentrantLock 
使用 AQS 的 state 变量，而 CyclicBarrier 是直接借助 ReentrantLock 加上 Condition 等待唤醒的功能进而实现的。
在构建 CyclicBarrier 时，传入的值会赋值给 CyclicBarrier 内部维护的 count 变量，也会赋值给 parties 变量（这是可以复用的关键）
每次调用 await 时，会将 count - 1，操作 count 值是直接使用 ReentrantLock 来保证线程安全性。
如果 count 不为 0，则添加到 Condition 队列中。
如果 count 等于 0，则把节点从 Condition 队列添加至 AQS 的队列中进行全部唤醒，并且将 parties 的值重新赋值为 count 的值（实现复用）

# 总结
1 CountDownLatch 基于 AQS 实现，会将构造 CountDownLatch 的入参传递至 state，countDown() 就是在利用 CAS 将 state - 1，
await() 实际就是让头节点一直在等待 state 为 0 时，释放所有等待的线程。
2 而 CyclicBarrier 则利用 ReentrantLock 和 Condition， 自身维护了 count 和 parties 变量。
每次调用 await 方法将 count - 1，并将线程加入到 condition 队列上，等到 count 为 0 时，则将 
condition 队列的节点移交至 AQS 队列，并全部释放。















# 何为 AQS

AbstractQueuedSynchronizer抽象同步队列简称A Q S, 它是实现同步器的基础组件, 如常用的ReentrantLock、Semaphore、CountDownLatch等。

A Q S定义了一套多线程访问共享资源的同步模板，解决了实现同步器时涉及的大量细节问题，能够极大地减少实现工作，

虽然大多数开发者可能永远不会使用A Q S实现自己的同步器（J U C包下提供的同步器基本足够应对日常开发），但是知道A Q S的原理对于架构设计还是很有帮助的，面试还可以吹吹牛，下面是A Q S的组成结构。

![][/img/]





















































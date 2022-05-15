# 1 HashMap 在 JAVA 中的怎么工作的？

基于 Hash 的原理。

# 2 什么是哈希？

最简单形式的 hash，是一种在对任何变量 / 对象的属性应用任何公式 / 算法后， 为其分配唯一代码的方法。

一个真正的 hash 方法必须遵循下面的原则：
```
哈希函数每次在相同或相等的对象上应用哈希函数时, 应每次返回相同的哈希码。换句话说, 两个相等的对象必须一致地生成相同的哈希码。
```
Java 中所有的对象都有 Hash 方法，Java 中的所有对象都继承 Object 类中定义的 hashCode() 函数的默认实现。此函数通常通过将对象的内部地址转换为整数来生成哈希码，从而为所有不同的对象生成不同的哈希码。


# 3 HashMap 中的 Node 类

Map 的定义是：将键映射到值的对象。
因此，HashMap 中必须有一些机制来存储这个键值对。答案是肯定的。HashMap 有一个内部类 Node，如下所示：
```java
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash; // 记录hash值， 以便重hash时不需要再重新计算
    final K key; 
    V value;
    Node<K,V> next;
    
    ...// 其余的代码
}
```

当然，Node 类具有存储为属性的键和值的映射。
key 已被标记为 final，另外还有两个字段：next 和 hash。
在下面中， 我们将会理解这些属性的必须性。

# 4 键值对在 HashMap 中是如何存储的

键值对在 HashMap 中是以 Node 内部类的数组存放的， 如下所示：

```
transient Node<K,V>[] table;
```

哈希码计算出来之后， 会转换成该数组的下标， 在该下标中存储对应哈希码的键值对， 在此先不详细讲解 hash 碰撞的情况。
该数组的长度始终是 2 的次幂， 通过以下的函数实现该过程

```
static final int tableSizeFor(int cap) {
    int n = cap - 1;// 如果不做该操作， 则如传入的 cap 是 2 的整数幂， 则返回值是预想的 2 倍
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```
其原理是将传入参数 (cap) 的低二进制全部变为 1， 最后加 1 即可获得对应的大于 cap 的 2 的次幂作为数组长度。

## 为什么要使用 2 的次幂作为数组的容量呢？

在此有涉及到 HashMap 的 hash 函数及数组下标的计算， 键 (key) 所计算出来的哈希码有可能是大于数组的容量的， 那怎么办？

可以通过简单的求余运算来获得， 但此方法效率太低。HashMap 中通过以下的方法保证 hash 的值计算后都小于数组的容量。

```
(n - 1) & hash
```

这也正好解释了为什么需要 2 的次幂作为数组的容量。由于 n 是 2 的次幂， 因此， n - 1 类似于一个低位掩码。

通过与操作， 高位的 hash 值全部归零，保证低位才有效， 从而保证获得的值都小于 n。同时， 在下一次 resize() 操作时， 重新计算每个 Node 的数组下标将会因此变得很简单， 具体的后文讲解。

以默认的初始值 16 为例：

```
01010011 00100101 01010100 00100101
&   00000000 00000000 00000000 00001111
----------------------------------
    00000000 00000000 00000000 00000101    //高位全部归零，只保留末四位
    // 保证了计算出的值小于数组的长度 n

```

但是， 使用了该功能之后， 由于只取了低位， 因此 hash 碰撞会也会相应的变得很严重。这时候就需要使用 「扰动函数」

```
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

```

该函数通过将哈希码的高 16 位的右移后与原哈希码进行异或而得到， 以以上的例子为例
图见：[扰动函数异或运算.png]

^ 异或

此方法保证了高 16 位不变， 低 16 位根据异或后的结果改变。计算后的数组下标将会从原先的 5 变为 0。
使用了 「扰动函数」 之后， hash 碰撞的概率将会下降。有人专门做过类似的测试， 虽然使用该 「扰动函数」 并没有获得最大概率的避免 hash 碰撞， 但考虑其计算性能和碰撞的概率， JDK 中使用了该方法， 且只 hash 一次。

# 5 哈希碰撞及其处理
在理想的情况下， 哈希函数将每一个 key 都映射到一个唯一的 bucket， 然而， 这是不可能的。哪怕是设计在良好的哈希函数， 也会产生哈希冲突。

前人研究了很多哈希冲突的解决方法， 在维基百科中， 总结出了四大类
图见：[哈希碰撞解决方案图.png]

哈希碰撞解决方法
在 Java 的 HashMap 中， 采用了第一种 Separate chaining 方法 (大多数翻译为拉链法)+ 链表和红黑树来解决冲突。
图见：[hashMap结构.png]

JDK8 中 HashMap 结构
在 HashMap 中， 哈希碰撞之后会通过 Node 类内部的成员变量 Node<K,V> next; 来形成一个链表 (节点小于 8) 或红黑树（节点大于 8， 在小于 6 时会从新转换为链表）， 从而达到解决冲突的目的。

```
static final int TREEIFY_THRESHOLD = 8;

static final int UNTREEIFY_THRESHOLD = 6;
```

# 6 HashMap 的初始化

```
public HashMap();
public HashMap(int initialCapacity);
public HashMap(Map<? extends K, ? extends V> m);
public HashMap(int initialCapacity, float loadFactor); 

```

HashMap 中有四个构造函数， 大多是初始化容量和负载因子的操作。以 
public HashMap(int initialCapacity, float loadFactor) 为例

```
public HashMap(int initialCapacity, float loadFactor) {
    // 初始化的容量不能小于0
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                           initialCapacity);
    // 初始化容量不大于最大容量
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    // 负载因子不能小于 0
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                           loadFactor);
    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
}

```

通过该函数进行了容量和负载因子的初始化，如果是调用的其他的构造函数， 则相应的负载因子和容量会使用默认值（默认负载因子 = 0.75， 默认容量 = 16）。

在此时， 还没有进行存储容器 table 的初始化， 该初始化要延迟到第一次使用时进行。HashMap 面试 21 问！面试的推荐你看下。

# 7 HashMap 中哈希表的初始化或动态扩容

所谓的哈希表， 指的就是下面这个类型为内部类Node的 table 变量。

```
transient Node<K,V>[] table;
```

作为数组， 其在初始化时就需要指定长度。在实际使用过程中， 我们存储的数量可能会大于该长度，因此 HashMap 中定义了一个阈值参数 (threshold)， 在存储的容量达到指定的阈值时， 需要进行扩容。
我个人认为初始化也是动态扩容的一种， 只不过其扩容是容量从 0 扩展到构造函数中的数值（默认 16）。而且不需要进行元素的重 hash.

# 7.1 扩容发生的条件

初始化的话只要数值为空或者数组长度为 0 就会进行。而扩容是在元素的数量大于阈值（threshold）时就会触发。

```
threshold = loadFactor * capacity
```

比如 HashMap 中默认的 loadFactor=0.75, capacity=16, 则

```
threshold = loadFactor * capacity = 0.75 * 16 = 12
```

那么在元素数量大于 12 时， 就会进行扩容。扩容后的 capacity 和 threshold 也会随之而改变。

负载因子影响触发的阈值， 因此， 它的值较小的时候， HashMap 中的 hash 碰撞就很少， 此时存取的性能都很高， 对应的缺点是需要较多的内存；而它的值较大时， HashMap 中的 hash 碰撞就很多， 此时存取的性能相对较低， 对应优点是需要较少的内存；不建议更改该默认值， 如果要更改， 建议进行相应的测试之后确定。

# 7.2 再谈容量为 2 的整数次幂和数组索引计算

前面说过了数组的容量为 2 的整次幂， 同时， 数组的下标通过下面的代码进行计算

```
index = (table.length - 1) & hash
```
该方法除了可以很快的计算出数组的索引之外， 在扩容之后， 进行重 hash 时也会很巧妙的就可以算出新的 hash 值。由于数组扩容之后， 容量是现在的 2 倍， 扩容之后 n-1 的有效位会比原来多一位， 而多的这一位与原容量二进制在同一个位置。示例

15
0000 1111









































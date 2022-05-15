# 彻底理解HashMap及LinkedHashMap

HashMap是Map族中最为常用的一种，也是Java Collection Framework的重要成员。HashMap和双向链表合二为一即是LinkedHashMap。所谓LinkedHashMap，其落脚点在HashMap，因此更准确地说，它是一个将所有Node节点链入一个双向链表的HashMap。

下面基于JDK 1.8的源码来学习HashMap及LinkedHashMap的数据结构、原理。不同JDK版本之间也许会有些许差异，但不影响原理学习，JDK8相比以前对HashMap的修改比较大。

## 1、HashMap概述

Map是 Key-Value键值对映射的抽象接口，该映射不包括重复的键，即一个键对应一个值。HashMap是Java Collection Framework的重要成员，也是Map族(如下图所示)中我们最为常用的一种。简单地说，HashMap是基于哈希表的Map接口的实现，以Key-Value的形式存在，即存储的对象是 Node (同时包含了Key和Value) 。在HashMap中，其会根据hash算法来计算key-value的存储位置并进行快速存取。特别地，HashMap最多只允许一条Node的key为Null，但允许多条Node的value为Null。此外，HashMap是Map 的一个非同步的实现。

以下是HashMap的类继承图

必须指出的是，虽然容器号称存储的是 Java 对象，但实际上并不会真正将 Java 对象放入容器中，只是在容器中保留这些对象的引用。也就是说，Java 容器实际上包含的是引用变量，而这些引用变量指向了我们要实际保存的 Java 对象。

1.1、HashMap定义及构造函数

```java
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable {
    //...
}
```

HashMap 一共提供了四个构造函数，其中 默认无参的构造函数 和 参数为Map的构造函数 为 Java Collection Framework 规范的推荐实现，其余两个构造函数则是 HashMap 专门提供的。


```java
public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}
//仅仅将负载因子初始化为默认值
public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; 
    // all other fields defaulted
}
```

HashMap(int initialCapacity, float loadFactor)构造函数意在构造一个指定初始容量和指定负载因子的空HashMap，其源码如下：

```java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " +
                                           initialCapacity);
    //容量最大为2的30次方
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +
                                           loadFactor);
    this.loadFactor = loadFactor;
    //这里调用函数计算触发扩容的阈值，threshold/loadFactor就是容量
    this.threshold = tableSizeFor(initialCapacity);
}
```

以上构造函数的最后一行就是jdk8的修改，实际上在jdk7之前的版本，这个构造方法最后一行就是：

```java
table = new Entry[capacity];
```

但是jdk8的最后一行并没有立刻new出一个数组，而是调用了tableSizeFor方法，将结果赋值给了threshold变量。tableSizeFor方法源码如下，从注释就可以看出来，其目的是要获得大于cap的最小的2的幂。比如cap=10，则返回16。

```java
/**
 * Returns a power of two size for the given target capacity.
 */
 static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
 }
```

## 1.2、HashMap的数据结构

我们知道，在Java中最常用的两种结构是数组和链表，几乎所有的数据结构都可以利用这两种来组合实现，HashMap就是这种应用的一个典型。实际上，经典的HashMap就是一个链表数组，只是jdk1.8再次对经典hashMap的数据结构作了小幅调整，如下是当前HaspMap的数据结构：


在JDK1.6和JDK1.7中，HashMap采用数组+链表实现，即使用链表处理冲突，同一hash值的key-value键值对都存储在一个链表里。但是当数组中一个位置上的元素较多，即hash值相等的元素较多时，通过key值依次查找的效率较低。而在JDK1.8中，HashMap采用数组+链表+红黑树实现，当链表长度超过阈值8时，并且数组总容量超过64时，将链表转换为红黑树，这样大大减少了查找时间。从链表转换为红黑树后新加入键值对的效率降低，但查询、删除的效率都变高了。而当发生扩容或remove键值对导致原有的红黑树内节点数量小于6时，则又将红黑树转换成链表。

每一个HashMap都有一个Node类型的table数组，其中Node类型的定义如下：

```java

static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;         // 声明 hash 值为 final 的
    final K key;            // 声明 key 为 final 的
    V value;                // 键值对的值
    Node<K,V> next;         // 指向下一个节点的引用

    Node(int hash, K key, V value, Node<K,V> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
}
```

Node为HashMap的内部类，实现了Map.Entry接口，其包含了键key、值value、下一个节点next，以及hash值四个属性。事实上，Node是构成哈希表的基石，是哈希表所存储的元素的具体形式。值得注意的是，int类型的hash值及引用变量key都被声明成final，即不可变。


## 1.3、HashMap的快速存取

在HashMap中，我们最常用的两个操作就是：put(Key,Value)和get(Key)。我们都知道，HashMap中的Key是唯一的，那它是如何保证唯一性的呢？我们首先想到的是用equals比较，没错，这样是可以实现的，但随着元素的增多，put和get的效率将越来越低，这里的时间复杂度是O(n)。也就是说，假如HashMap有1000个元素，那么put时就需要比较1000次，这是相当耗时的，远达不到HashMap快速存取的目的。

实际上，HashMap很少会用到equals方法，因为其内通过一个哈希表管理所有元素，利用哈希算法可以快速的存取元素。当我们调用put方法存值时，HashMap首先会调用Key的hashCode方法，然后基于此值获取Key的哈希码，通过哈希码快速找到某个位置，这个位置可以被称之为bucketIndex。根据equals方法与hashCode的协定可以知道，如果两个对象的hashCode不同，那么equals一定为 false；如果其hashCode相同，equals也不一定为true。所以，理论上，hashCode 可能存在碰撞的情况，当碰撞发生时，这时会取出bucketIndex桶内已存储的元素（如果该桶next引用不空，即有了链表也要遍历链表），并通过hashCode()和equals()来逐个比较以判断Key是否已存在。如果已存在，则使用新Value值替换旧Value值，并返回旧Value值；如果不存在，则存放新的键值对<Key, Value>到链表中。因此，在HashMap中，equals()方法只有在哈希码碰撞时才会被用到。

结合源码来看HashMap的put操作：

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
```

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    //第一次put元素时，table数组为空，先调用resize生成一个指定容量的数组
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    //hash值和n-1的与运算结果为桶的位置，如果该位置空就直接放置一个Node
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    //如果计算出的bucket不空，即发生哈希冲突，就要进一步判断
    else {
        Node<K,V> e; K k;
        //判断当前Node的key与要put的key是否相等
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        //判断当前Node是否是红黑树的节点
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        //以上都不是，说明要new一个Node，加入到链表中
        else {
            for (int binCount = 0; ; ++binCount) {
             //在链表尾部插入新节点，注意jdk1.8是在链表尾部插入新节点
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // 如果当前链表中的元素大于树化的阈值，进行链表转树的操作
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                //在链表中继续判断是否已经存在完全相同的key
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        //走到这里，说明本次put是更新一个已存在的键值对的value
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            //在hashMap中，afterNodeAccess方法体为空，交给子类去实现
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    //如果当前size超过临界值，就扩容。注意是先插入节点再扩容
    if (++size > threshold)
        resize();
    //在hashMap中，afterNodeInsertion方法体为空，交给子类去实现
    afterNodeInsertion(evict);
    return null;
}
```

通过上述源码我们可以清楚了解到HashMap保存数据的过程。先计算key的hash值，然后根据hash值搜索在table数组中的索引位置，如果table数组在该位置处有元素，则查找是否存在相同的key，若存在则覆盖原来key的value，否则将该元素保存在链表尾部，注意JDK1.7中采用的是头插法，即每次都将冲突的键值对放置在链表头，这样最初的那个键值对最终就会成为链尾，而JDK1.8中使用的是尾插法。此外，若table在该处没有元素，则直接保存。

对于hash函数，具体的来看下源码

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

以上可以看到key==null时，直接返回0，所以HashMap中键为NULL的键值对，一定在第一个桶中。

h >>> 16是用来取出h的高16位(>>>是无符号右移) 如下展示：

```java
0000 0100 1011 0011  1101 1111 1110 0001

>>> 16 

0000 0000 0000 0000  0000 0100 1011 0011
```

通过之前putVal的源码可以知道，HashMap是用(length - 1) & hash来计算数组下标的。绝大多数情况下length一般都小于2^16即小于65536。所以hash & (length-1)；结果始终是hash的低16位与（length-1）进行&运算。要是能让hash的高16位也参与运算，会让得到的下标更加散列。

如何让高16也参与运算呢。方法就是让hashCode()和自己的高16位进行^运算。由于与运单和或运单都会使得结果偏向0或者1，并不是均匀的概念，所以用异或。

结合源码来看HashMap的get操作：

```java
public V get(Object key) {
    Node<K,V> e;
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}
```

```java
final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (first = tab[(n - 1) & hash]) != null) {
        if (first.hash == hash && // always check first node
            ((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        if ((e = first.next) != null) {
         //如果是红黑树，就调用树的查找方法，否则遍历链表直到找到
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            do {
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}
```

在这里能够根据key快速的取到value，除了和HashMap的数据结构密不可分外，还和Node有莫大的关系。在前面就已经提到过，HashMap在存储过程中并没有将key/value分开来存储，而是当做一个整体key-value来处理的，这个整体就是Node对象。

## 1.4 为什么HashMap的底层数组长度总是2的n次方

当底层数组的length为2的n次方时， hash & (length - 1)就相当于对length取模，其效率要比直接取模高得多，这是HashMap在效率上的一个优化。

我们希望HashMap中的元素存放的越均匀越好。最理想的效果是，Node数组中每个位置都只有一个元素，这样，查询的时候效率最高，不需要遍历单链表，也不需要通过equals去比较Key，而且空间利用率最大。

那如何计算才会分布最均匀呢？正如上一节提到的，HashMap采用了一个分两步走的哈希策略：

使用hash()方法对Key的hashCode进行重新计算，以防止质量低下的hashCode()函数实现。由于HashMap的支撑数组长度总是2的倍数，通过右移可以使低位的数据尽量的不同，从而使Key的hash值的分布尽量均匀；使用hash & (length - 1)方法进行取余运算，以使每个键值对的插入位置尽量分布均匀；由于length是2的整数幂，length-1的低位就全是1，高位全部是0。在与hash值进行低位&运算时，低位的值总是与原来hash值相同，高位&运算时值为0。这就保证了不同的hash值发生碰撞的概率比较小，这样就会使得数据在table数组中分布较均匀，查询速度也较快。

## 1.5 HashMap的扩容

随着HashMap中元素的数量越来越多，发生碰撞的概率将越来越大，所产生的子链长度就会越来越长，这样势必会影响HashMap的存取速度。为了保证HashMap的效率，系统必须要在某个临界点进行扩容处理，该临界点就是HashMap中元素的数量在数值上等于threshold（table数组长度*加载因子）。但是，不得不说，扩容是一个非常耗时的过程，因为它需要重新计算这些元素在新table数组中的位置并进行复制处理。

首先回答一个问题，在插入一个临界节点时，HashMap是先扩容后插入还是先插入后扩容？这样选取的优势是什么？

答案是：先插入后扩容。通过查看putVal方法的源码可以发现是先执行完新节点的插入后，然后再做size是否大于threshold的判断的。

```java
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
  ...
    //如果插入新结点后的size超过了临界值，就扩容，注意是先插入节点再扩容
    if (++size > threshold)
        resize();
    //在hashMap中，afterNodeInsertion方法体为空，交给子类去实现
    afterNodeInsertion(evict);
    return null;
}
```

为什么是先插入后扩容？源码已经很清楚的表达了扩容原因，调用put不一定是新增数据，还可能是覆盖掉原来的数据，这里就存在一个key的比较问题。以先扩容为例，先比较是否是新增的数据，再判断增加数据后是否要扩容，这样比较会浪费时间，而先插入后扩容，就有可能在中途直接通过return返回了（本次put是覆盖操作，size不变不需要扩容），这样可以提高效率的。

JDK1.8相对于JDK1.7对HashMap的实现有较大改进，做了很多优化，链表转红黑树是其中的一项，其实扩容方法JDK1.8也有优化，与JDK1.7有较大差别。

JDK1.8中resize方法源码如下：

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
     // 原来的容量就已经超过最大值就不再扩容了，就只好随你碰撞去吧
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 没超过最大值，就扩容为原来的2倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    // 计算新的resize上限，即新的threshold值
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
     // 把旧的bucket都移动到新的buckets中
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        //原来的桶索引值
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    // 扩容后，键值对在新table数组中的位置与旧数组中一样
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    // 扩容后，键值对在新table数组中的位置与旧数组中不一样
                    // 新的位置=原来的位置 + oldCap
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

必要的位置已经加了注释。最让人疑惑的有两个点：

为什么（e.hash & oldCap）== 0时就放入lo链表，否则就是hi链表; 为什么 j + oldCap就是键值对在新的table数组中的位置；其实这里包含着一些数学技巧。类似于上一小节为什么HashMap中数组的长度总是取2的整数次幂。

查看源码，我们发现扩容时，使用的是2次幂的扩展即长度扩为原来2倍，所以，元素的位置要么是在原位置，要么是在原位置再移动2次幂的位置。看下图可以明白这句话的意思，n为table的长度，图中上半部分表示扩容前的key1和key2两个Node的索引位置，图中下半部分表示扩容后key1和key2两个Node新的索引位置。

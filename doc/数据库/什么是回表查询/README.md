# 什么是回表查询

## InnoDB有两大类索引

这先要从InnoDB的索引实现说起，InnoDB有两大类索引：

(1). 聚集索引(clustered index)

(2). 普通索引(secondary index)

## InnoDB聚集索引和普通索引有什么差异？

(1). InnoDB 聚集索引的叶子节点存储行记录，因此， InnoDB必须要有，且只有一个聚集索引：

（1）如果表定义了PK，则PK就是聚集索引；

（2）如果表没有定义PK，则第一个not NULL unique列是聚集索引；

（3）否则，InnoDB会创建一个隐藏的row-id作为聚集索引；

```
画外音：所以PK查询非常快，直接定位行记录。
```

(2). InnoDB 普通索引的叶子节点存储主键值。

```
画外音：注意，不是存储行记录头指针，MyISAM的索引叶子节点存储记录指针。
```

举个栗子，不妨设有表：

t(id PK, name KEY, sex, flag);

```
画外音：id是聚集索引，name是普通索引。该表共四个字段
```

表中有四条记录：

```
id    name        sex     flag   
1     shenjian    m       A

3     zhangsan    m       A

5     lisi        m       A

9     wangwu      f       B
```

![](img/索引图.png)

两个B+树索引分别如上图：

（1）id为PK，聚集索引，叶子节点存储行记录；

（2）name为KEY，普通索引，叶子节点存储PK值，即id；

既然从普通索引无法直接定位行记录，那普通索引的查询过程是怎么样的呢？

通常情况下，需要扫描两遍索引树。

例如：

```sql
select * from t where name='lisi';　
```

是如何执行的呢？

![](img/普通索引执行流程.png)

如粉红色路径，需要扫码两遍索引树：

（1）先通过普通索引定位到主键值id=5；

（2）在通过聚集索引定位到行记录；

**这就是所谓的回表查询，先定位主键值，再定位行记录，它的性能较扫一遍索引树更低。**































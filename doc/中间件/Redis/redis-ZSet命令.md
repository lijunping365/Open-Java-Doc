最近接了一个游戏需求，里面有个点就是排行榜。

如果自己用队列，然后针对每条插入数据都进行排序，显然很低效。之前看过redis支持的类型有5中，String，list，set，sortedSet，map。刚好这次可以用一下sortedSet这个有序集合

关于redis的配置，我有空会在另外一个章节里面梳理下，其实不大喜欢写配置，因为网上一大堆，这一章主要讲解一下opsForZset的几个方法。

1、先看它的add方法，Boolean add(K key, V value, double score);

这个有序队列的结构就是Key，Value，Score

key就是这个有序队列的key，

value表示一个你需要排序附带的值，比如你可以放一个用户的ID或者其他的。

Score表示一个分数，所有的排序都是基于这个score。可以正序排列，也可以倒叙排列。



2、Set< V > range(K key, long start, long end);  正序

Set reverseRange(K key, long start, long end);倒叙

获取队列的方法，start表示起始位置的index，从0开始。index表示end的位置，-1表示获取全部

opsForZSet.range("key",0,-1)，表示获取key队列的所有元素。

对应的方法还有几个

Set< TypedTuple< V >> rangeWithScores(K key, long start, long end);

Set< TypedTuple< V >> reverseRangeWithScores(K key, long start, long end);

这两个方法跟上面的方法差不多，只是返回的时候回带上score，有时候业务需要输出这个score，比如排行榜的分数，你就可以用2个这个。



3、

Set< V > rangeByScore(K key, double min, double max);

Set< V > reverseRangeByScore(K key, double min, double max);

这俩方法表示在某个分数区间内的集合。min表示最小的分数，max表示最大的分数，集合返回在min和max之间的集合有哪些。

业务场景：比如某个分数区间内的用户数量统计，或者对这些用户给予一些奖励。具体就不赘述了。

类似的方法如下

Set< TypedTuple< V >> rangeByScoreWithScores(K key, double min, double max);

Set< TypedTuple< V >> rangeByScoreWithScores(K key, double min, double max, long offset, long count);

Set< TypedTuple< V >> reverseRangeByScoreWithScores(K key, double min, double max);

Set< V > reverseRangeByScore(K key, double min, double max, long offset, long count);

Set< TypedTuple< V >> reverseRangeByScoreWithScores(K key, double min, double max, long offset, long count);

这几个方法比较类似，要么是倒叙，要么是对返回对象进行了限制输出，加上了offset，count来限制输出的对象，就是类似于数据库的limit，offset。



4、

Long size(K key);

Long zCard(K key);

这两方法表示的都是这个队列的长度，不知道为啥要写俩个，size()底层就是调用的zCard()



5、

Long unionAndStore(K key, K otherKey, K destKey);

Long unionAndStore(K key, Collection< K > otherKeys, K destKey);

这两个方法挺有意思的，意思不仅仅是去重，而且会把重复的数据score进行相加，返回值是这个集合的长度

先解释第一个，key，otherKey这两个key的有序队列进行相加，如果两个队列中存在相同的value，就将value的score相加，最终将最后的组合结果放到destKey中。

第二个方法则是多了一个集合参数，多个集合可以用同一个方法，节省代码，举个简单的例子：



Long unionAndStore2 = opsForZSet.unionAndStore("A", Arrays.asList("B","C"), "D");



6、

Long intersectAndStore(K key, K otherKey, K destKey);

Long intersectAndStore(K key, Collection< K > otherKeys, K destKey);

这俩方法刚好和上面2个方法相反，这个是交集。把交集的结果放到destKey中



7、

Cursor< TypedTuple< V >> scan(K key, ScanOptions options);

这方法自己用的时候，发现跟interator基本上一样，就是用来遍历这个key的集合里面的所有元素的。

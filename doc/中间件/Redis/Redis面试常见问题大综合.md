阅读参考： https://blog.csdn.net/belongtocode/article/details/104246075

1 redis 是单线程的吗？
redis 只使用了单线程去处理网络请求。在 6.0 后这个答案还要修改为多线程处理网络请求，但是真正操作数据部分程序是单线程的。

2 lua 脚本
（1）注意点：
在 Lua 函数内部可以使用 KEYS[N] 和 ARGV[N] 引用键和参数，需要注意 KEYS 和 ARGV 的参数序号都是从 1 开始的。
还需要注意在 Lua 脚本中，Redis 返回为空时，结果是 false，而不是 nil；

（2）使用场景：
可以使用 Lua 脚本实现原子性操作，避免不同客户端访问 Redis 服务器造成的数据冲突。
在前后多次请求的结果有依赖时，可以使用 Lua 脚本把多个请求整合为一个请求。
（3）另外注意点
使用 Lua 脚本，我们还需要注意：
要保证安全性，在 Lua 脚本中不要使用全局变量，以免污染 Lua 环境，虽然使用全局变量全报错，Lua 脚本停止执行，但还是在定义变量时添加 local 关键字。
要注意 Lua 脚本的时间复杂度，Redis 的单线程同样会阻塞在 Lua 脚本的执行中。
使用 Lua 脚本实现原子操作时，要注意如果 Lua 脚本报错，之前的命令同样无法回滚。
一次发出多个 Redis 请求，但请求前后无依赖时，使用 pipeline，比 Lua 脚本方便。

3 如何批量执行 redis 命令
（1）使用 redis 提供的 pipeline
但它需要多个命令的请求和响应之间没有依赖关系。
（2）使用 lua 脚本执行
可以实现命令的次序性和 redis 服务端计算。
例如：一次 lpop 出多个值，直到值为 n，或 list 为空（pipeline 也可轻易实现）；
```lua
local list = {};
local item = false;
local num = tonumber(KEYS[2]);
while (num > 0)
do
    item = redis.call('LPOP', KEYS[1]);
    if item == false then
        break;
    end;
    table.insert(list, item);
    num = num - 1;
end;
return list;
```
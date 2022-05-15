CentOS Linux 7.7 服务器集群。为了快速定位服务器性能问题，提供以下命令。

## 查看系统当前网络连接数

> netstat -n | awk '/^tcp/ {++S[$NF]} END {for(a in S) print a, S[a]}'

## 查看堆内对象的分布 Top 50（定位内存泄漏）

> jmap –histo:live $pid | sort-n -r -k2 | head-n 50

## 按照 CPU 的使用情况列出 Top 10 的进程

> ps -aeo pcpu,user,pid,cmd | sort -nr | head -10

## 按照 内存 的使用情况列出 Top 10 的进程

> ps axo %mem,pid,euser,cmd | sort -nr | head -10

## 显示系统整体的 CPU 利用率和闲置率

> grep "cpu " /proc/stat | awk -F ' ' '{total = $2 + $3 + $4 + $5} END {print "idle \t used\n" $5*100/total "% " $2*100/total "%"}'

## 按照 Swap 分区的使用情况列出 Top 10 的进程

> for file in /proc/*/status ; do awk '/VmSwap|Name|^Pid/{printf $2 " " $3}END{ print ""}' $file; done | sort -k 3 -n -r | head -10

## 按线程状态统计线程数

> jstack $pid | grep java.lang.Thread.State:|sort|uniq -c | awk '{sum+=$1; split($0,a,":");gsub(/^[ \t]+|[ \t]+$/, "", a[2]);printf "%s: %s\n", a[2], $1}; END {printf "TOTAL: %s",sum}'

## 显示垃圾收集信息（间隔1秒持续输出）

> jstat -gcutil $pid 1000

## 显示老年代容量

> jstat -gcoldcapacity $pid

## 显示新生代容量及使用情况

> jstat -gcnewcapacity $pid

## 显示各个代的容量及使用情况

> jstat -gccapacity $pid

## 显示最后一次或当前正在发生的垃圾收集的诱发原因

> jstat -gccause $pid

## 查找/目录下占用磁盘空间最大的 Top 10 文件

> find / -type f -print0 | xargs -0 du -h | sort -rh | head -n 10

## 快速杀死所有的 Java 进程

> ps aux | grep java | awk '{ print $2 }' | xargs kill -9
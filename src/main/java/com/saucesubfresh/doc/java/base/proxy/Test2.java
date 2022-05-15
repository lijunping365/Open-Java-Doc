package com.saucesubfresh.doc.java.base.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 我们可以看到WorkHandler代理调用处理程序打印proxy参数输出的结果是com.sun.proxy.$Proxy0，这也说明proxy参数是代理类的真实代理对象；
 * Proxy类生成的代理对象可以调用work方法并且返回真实的代理对象，也可以通过反射来对真实的代理对象进行操作。
 * @author: 李俊平
 * @Date: 2021-04-18 13:39
 */
public class Test2 {
  public static void main(String[] args) {
    People people = new Student();
    InvocationHandler handler = new WorkHandler2(people);

    People proxy = (People) Proxy.newProxyInstance(people.getClass().getClassLoader(), people.getClass().getInterfaces(), handler);
    People p = proxy.work("写代码").work("开会").work("上课");

    System.out.println("打印返回的对象");
    System.out.println(p.getClass());

    String time = proxy.time();
    System.out.println(time);
  }
}

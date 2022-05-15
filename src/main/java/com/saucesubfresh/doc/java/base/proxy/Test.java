package com.saucesubfresh.doc.java.base.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 通过上面的讲解和示例动态代理的原理及使用方法，在Spring中的两大核心IOC和AOP中的AOP(面向切面编程)的思想就是动态代理，在代理类的前面和后面加上不同的切面组成面向切面编程。
 * @author: 李俊平
 * @Date: 2021-04-18 13:26
 */
public class Test {

  public static void main(String[] args) {
    //要代理的真实对象
    People people = new Teacher();
    //代理对象的调用处理程序，我们将要代理的真实对象传入代理对象的调用处理的构造函数中，最终代理对象的调用处理程序会调用真实对象的方法
    InvocationHandler handler = new WorkHandler(people);
    /**
     * 通过Proxy类的newProxyInstance方法创建代理对象，我们来看下方法中的参数
     * 第一个参数：people.getClass().getClassLoader()，使用handler对象的classloader对象来加载我们的代理对象
     * 第二个参数：people.getClass().getInterfaces()，这里为代理类提供的接口是真实对象实现的接口，这样代理对象就能像真实对象一样调用接口中的所有方法
     * 第三个参数：handler，我们将代理对象关联到上面的InvocationHandler对象上
     */
    People proxy = (People) Proxy.newProxyInstance(handler.getClass().getClassLoader(), people.getClass().getInterfaces(), handler);
    //System.out.println(proxy.toString());
    System.out.println(proxy.work());
  }
}

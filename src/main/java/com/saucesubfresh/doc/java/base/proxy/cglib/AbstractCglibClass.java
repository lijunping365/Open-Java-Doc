package com.saucesubfresh.doc.java.base.proxy.cglib;


/**
 * @author lijunping on 2021/8/27
 */
public abstract class AbstractCglibClass implements CglibInterface {

  @Override
  public String hello(String name) {
    System.out.println(name);
    return null;
  }
}

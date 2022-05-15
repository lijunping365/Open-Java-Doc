package com.saucesubfresh.doc.java.base.proxy;

/**
 * @author: 李俊平
 * @Date: 2021-04-18 13:36
 */
public class Student implements People {

  @Override
  public String work() {
    return null;
  }

  @Override
  public People work(String workName) {
    System.out.println("工作内容是"+workName);
    return this;
  }
  @Override
  public String time() {
    return "2018-06-12";
  }
}

package com.saucesubfresh.doc.java.base.proxy;

/**
 * @author: 李俊平
 * @Date: 2021-04-18 13:24
 */
public class Teacher implements People {

  @Override
  public String work() {
    System.out.println("老师教书育人...");
    return "教书";
  }

  @Override
  public People work(String workName) {
    return null;
  }

  @Override
  public String time() {
    return null;
  }

}


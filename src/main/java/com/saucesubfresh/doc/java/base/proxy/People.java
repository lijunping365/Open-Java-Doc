package com.saucesubfresh.doc.java.base.proxy;

/**
 * @author: 李俊平
 * @Date: 2021-04-18 13:24
 */
public interface People {

  public String work();

  /**
   * 方法的返回对象是它本身
   * @param workName
   * @return
   */
  public People work(String workName);

  public String time();
}

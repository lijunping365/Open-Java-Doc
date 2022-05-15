package com.saucesubfresh.doc.java.base.reflect;

/**
 * @author: 李俊平
 * @Date: 2021-06-02 22:59
 */
public class Person {

  public Person() {
  }

  private String name;

  private Integer age;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  @Override
  public String toString() {
    return "Person{" +
        "name='" + name + '\'' +
        ", age=" + age +
        '}';
  }
}

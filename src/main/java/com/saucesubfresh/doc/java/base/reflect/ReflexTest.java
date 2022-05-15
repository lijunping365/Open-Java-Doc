package com.saucesubfresh.doc.java.base.reflect;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * 在阿里巴巴开发规约有一条
 * 【强制】避免用Apache Beanutils进行属性的copy。
 * 说明：Apache BeanUtils性能较差，可以使用其他方案比如Spring BeanUtils, Cglib BeanCopier，注意均是浅拷贝。
 * 反例：[性能提升300%：Apache的BeanUtils的坑]
 *
 * @author: 李俊平
 * @Date: 2021-06-02 22:48
 */
public class ReflexTest {

  /**
   *
   * @param tClass
   * @param obj
   * @param propertyName
   * @param <T>
   * @return
   */
  private static <T> Object getPropertyVal(Class<T> tClass, T obj, String propertyName){
    // 1. 校验
    if (Objects.isNull(tClass) || Objects.isNull(obj) || StringUtils.isBlank(propertyName)){
      return null;
    }
    // 2.是否是原始类型
    if (tClass.isPrimitive()){
      return null;
    }
    // 3.是否 Map 类型，直接用 get
    if (Map.class.isAssignableFrom(tClass)){
      return ((Map)obj).get(propertyName);
    }
    // 使用 BeanUtils 工具获取,在 BeanUtils 中实现了 Method 的缓存。
    PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(tClass, propertyName);
    assert propertyDescriptor != null;
    Method method = propertyDescriptor.getReadMethod();
    if (Objects.isNull(method)){
      return null;
    }
    try {
      return method.invoke(obj);
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   *
   * @param tClass
   * @param obj
   * @param propertyName
   * @param <T>
   * @return
   */
  private static <T> Object setPropertyVal(Class<T> tClass, T obj, String propertyName, String propertyValue){
    // 1. 校验
    if (Objects.isNull(tClass) || Objects.isNull(obj) || StringUtils.isBlank(propertyName)){
      return obj;
    }
    // 2.是否是原始类型
    if (tClass.isPrimitive()){
      return obj;
    }

    // 使用 BeanUtils 工具获取,在 BeanUtils 中实现了 Method 的缓存。
    PropertyDescriptor propertyDescriptor = BeanUtils.getPropertyDescriptor(tClass, propertyName);
    assert propertyDescriptor != null;
    Method method = propertyDescriptor.getWriteMethod();
    if (Objects.isNull(method)){
      return obj;
    }
    try {
      method.invoke(obj, propertyValue);
      return obj;
    } catch (IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
    return obj;
  }

  public static void main(String[] args) {
    //Person person = new Person();
    //person.setName("张三");
    //Object name = getPropertyVal(Person.class, person, "name");
    //System.out.println(name);

    Person person = new Person();
    Object name = setPropertyVal(Person.class, person, "name", "李四");
    System.out.println(name);
  }
}

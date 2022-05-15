package com.saucesubfresh.doc.java.base.proxy.cglib;

import lombok.Data;

/**
 * @author lijunping on 2021/8/27
 */
@Data
public class Test {

    public String hello(String name){
        System.out.println(name);
        return name;
    }

    public static void main(String[] args) {
        final Test proxyInstance = CglibProxy.getProxyInstance(Test.class);
        proxyInstance.hello("hhhhhhhhh");
    }
}

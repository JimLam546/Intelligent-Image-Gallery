package com.jim.yun_picture.test;

import lombok.Data;

/**
 * @author Jim_Lam
 * @description Test
 */

public class Test {
    public static void main(String[] args) {
        A a = new A();
        String name = a.getName();
        System.out.println(name);
        a.setName("Jim");
        System.out.println(name);

    }
}

@Data
class A {
    private String name;


}
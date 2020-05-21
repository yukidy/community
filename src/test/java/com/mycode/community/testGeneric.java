package com.mycode.community;

import com.mycode.community.entity.User;

import java.util.List;

public class testGeneric {

    public static void main(String[] args) {

        //定义的泛型类，就一定要传入泛型类型实参么？
        // 并不是这样，在使用泛型的时候如果传入泛型实参，
        // 则会根据传入的泛型实参做相应的限制，此时泛型才会起到本应起到的限制作用。
        // 如果不传入泛型类型实参的话，在泛型类中使用泛型的方法或成员变量定义的类型可以为任何的类型。

        // 做了原本应起到的限制作用
        Generic generic = new Generic<Integer> (123);
        System.out.println(generic.getKey());

        // 传入的实参是什么就是什么
        User user =new User();
        user.setId(1);

//        generic = new Generic(user);
        user.setUsername("年后");
        generic.setKey(user);
        System.out.println(generic.getKey());

        // 泛型的类型参数只能是类类型，不能是简单类型。
        // 不能对确切的泛型类型使用instanceof操作。如下面的操作是非法的，编译时会出错。
        //　　if(user instanceof Generic<User>){ }


        // instanceof 严格来说是Java中的一个双目运算符，用来测试一个对象是否为一个类的实例
        // instanceof 运算符只能用作对象的判断,必须为引用类型，不能是基本类型
        // 编译器会检查 obj 是否能转换成右边的class类型，如果不能转换则直接报错，如果不能确定类型，则通过编译，具体看运行时定

        User p1 = new User();
        int i;

        //System.out.println(p1 instanceof String);//编译报错
        System.out.println(p1 instanceof List);//false
        System.out.println(p1 instanceof List<?>);//false
        //System.out.println(p1 instanceof List<User>);//编译报错

        // 这里就存在问题了，Person 的对象 p1 很明显不能转换为 String 对象，
        // 那么自然 Person 的对象 p1 instanceof String 不能通过编译，
        // 但为什么 p1 instanceof List 却能通过编译呢？而 instanceof List<Person> 又不能通过编译了？

        // 表达式 obj instanceof T，instanceof 运算符的 obj 操作数的类型必须是引用类型或空类型; 否则，会发生编译时错误。

        // instanceof的实现策略

        // 1、obj如果为null，则返回false；否则设S为obj的类型对象，剩下的问题就是检查S是否为T的子类型；
        //
        //　2、如果S == T，则返回true；
        //
        //　3、接下来分为3种情况，之所以要分情况是因为instanceof要做的是“子类型检查”，
        // 而Java语言的类型系统里数组类型、接口类型与普通类类型三者的子类型规定都不一样，必须分开来讨论。
        //
        //　①、S是数组类型：如果 T 是一个类类型，那么T必须是Object；如果 T 是接口类型，那么 T 必须是由数组实现的接口之一；
        //
        //　②、接口类型：对接口类型的 instanceof 就直接遍历S里记录的它所实现的接口，看有没有跟T一致的；
        //
        //　③、类类型：对类类型的 instanceof 则是遍历S的super链（继承链）一直到Object，看有没有跟T一致的。遍历类的super链意味着这个算法的性能会受类的继承深度的影响。


    }

}

package com.mycode.community;

/**
 * 这是一个泛型类
 * T需要被外部指定具体类型
 * @param <T>
 */
public class Generic<T> {

    // 如果成员变量的类型为T
    private T key;

    // 此泛型类的构造方法
    public Generic (T key) { //泛型构造方法形参key的类型也为T，T的类型由外部指定
        this.key = key;
    }

    public T getKey() { //泛型方法getKey的返回值类型为T，T的类型由外部指定
        return key;
    }

    public void setKey (T key) {
        this.key = key;
    }
}

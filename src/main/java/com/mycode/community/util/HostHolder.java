package com.mycode.community.util;

import com.mycode.community.entity.User;
import org.springframework.stereotype.Component;

/**
 *  持有用户的信息，用来代替session对象
 */
@Component
public class HostHolder {

    private ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public void setUsers (User user) {
        userThreadLocal.set(user);
    }

    public User getUser () {
        return userThreadLocal.get();
    }

    // 清除数据，请求完成后清除所持有的数据
    public void clear () {
        userThreadLocal.remove();
    }

}

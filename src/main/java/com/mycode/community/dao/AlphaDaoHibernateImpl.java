package com.mycode.community.dao;

import org.springframework.stereotype.Repository;

//@Repository
//也可自定义Bean的名字
@Repository("alphaHibernate")
public class AlphaDaoHibernateImpl implements AlphaDao {
    @Override
    public String select() {
        return "Hibernate";
    }
}

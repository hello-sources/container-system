package com.image.test.dao;

import com.image.test.bean.UserBean;

import java.util.List;

/**
 * UserDao
 *
 * @Author litianwei
 * @Date 2023/12/2
 **/
public interface UserDao {
    List<UserBean> findall();
    UserBean findByname(String name);
    boolean addUser(UserBean user);
    boolean updateByName(String name,String age);
    boolean deleteByName(String name);
}
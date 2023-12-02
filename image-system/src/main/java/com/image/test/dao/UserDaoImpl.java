package com.image.test.dao;

import com.image.test.bean.UserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UserDaoImpl
 *
 * @Author litianwei
 * @Date 2023/12/2
 **/
@Service("userDaoImpl") //@Service为给类增加别名
public class UserDaoImpl implements UserDao{

    @Autowired //是用在JavaBean中的注解，通过byType形式，用来给指定的字段或方法注入所需的外部资源。
    private JdbcTemplate jdbcTemplate; //jdbc连接工具类



    //查询所有数据
    @Override
    public List<UserBean> findall() {
        String sql = "select * from test_user";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<UserBean>(UserBean.class));
    }


    //查询指定name
    @Override
    public UserBean findByname(String name) {
        String sql = "select * from test_user where name=?";
        Object[] params = new Object[]{name};
        return jdbcTemplate.queryForObject(
            sql,
            params,
            new BeanPropertyRowMapper<>(UserBean.class));
    }

    //增
    @Override
    public boolean addUser(UserBean user) {
        String sql = "insert into test_user(name,age)values(?,?)";
        Object[] params = {user.getName(),user.getAge()};
        return jdbcTemplate.update(sql, params)>0;
    }

    //改
    @Override
    public boolean updateByName(String name,String age) {
        String sql = "update test_user set age=? where name=?";
        Object[] params = {name,age};
        return jdbcTemplate.update(sql,params)>0;

    }

    //删
    @Override
    public boolean deleteByName(String name) {
        String sql = "delete from test_user where name=?";
        Object[] params = {name};
        return jdbcTemplate.update(sql,params)>0;
    }
}
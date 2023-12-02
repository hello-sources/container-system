package com.image.test.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserBean
 *
 * @Author litianwei
 * @Date 2023/12/2
 **/
@Data //包含了get，set和toString
@AllArgsConstructor //有参构造器 set
@NoArgsConstructor  //无参构造器 get
public class UserBean {
    private String name;
    private String age;
}

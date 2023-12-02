package com.image.test;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * testController
 *
 * @Author litianwei
 * @Date 2023/12/2
 **/
@RestController
public class TestController {
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    public String hello() {
        return "Hello Spring Boot!";
    }
}
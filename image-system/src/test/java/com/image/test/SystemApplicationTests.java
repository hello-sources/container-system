package com.image.test;

import com.image.util.SshUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class SystemApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testSSHUtils() {
        SshUtil ssh = new SshUtil();
        Map<String, Object> map = ssh.execCommand("docker images", "157.0.19.2", 10813, "root", "ictnj@123456");
        String code = map.get("code").toString();
        System.out.println(code);
        String out = map.get("out").toString();
        System.out.println(out);
    }
}

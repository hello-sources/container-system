package com.image.test;

import com.image.util.SshConnectionPool;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * SshTest
 *
 * @Author litianwei
 * @Date 2023/12/11
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class SshTest {

    // 测试一个session执行多条命令
    @Test
    public void testExecCmd() {
        // 创建SSH连接池
        SshConnectionPool connectionPool = new SshConnectionPool();

        try {
            // 获取SSH会话
            Session session = connectionPool.getSession();

            // 执行任意条命令
            connectionPool.executeCommand(session, "ls -l");
            connectionPool.executeCommand(session, "pwd");
            connectionPool.executeCommand(session, "docker exec kind_swartz hostname");
            connectionPool.executeCommand(session, "hostname");

            // 释放SSH会话回到连接池
            connectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    // 测试执行交互式命令，好像没成功
    @Test
    public void testInteractiveCmd() {
        // 创建SSH连接池
        SshConnectionPool connectionPool = new SshConnectionPool();

        try {
            // 获取SSH会话
            Session session = connectionPool.getSession();

            // 执行交互式命令
            connectionPool.executeInteractiveCommand(session, "ls -l");

            // 释放SSH会话回到连接池
            connectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}

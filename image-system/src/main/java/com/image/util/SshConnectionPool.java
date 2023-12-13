package com.image.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * SshConnectPool
 *
 * @Author litianwei
 * @Date 2023/12/11
 **/
@Slf4j
public class SshConnectionPool {

    private JSch jsch;
    private Session session;
    private String charset = Charset.defaultCharset().toString();

    public SshConnectionPool() {
        this.jsch = new JSch();
        // 设置你的SSH连接信息，如用户名、主机和端口
        String user = "root";
        String host = "157.0.19.2";
        int port = 10813;

        try {
            log.info("--------start create connect session--------");
            this.session = jsch.getSession(user, host, port);
            // 设置密码或私钥路径
            session.setPassword("ictnj@123456");
            session.setConfig("StrictHostKeyChecking", "no"); // 设置非严格的主机密钥检查
            session.connect();
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    // 获取SSH会话
    public synchronized Session getSession() {
        return session;
    }

    // 释放SSH会话
    public synchronized void releaseSession(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    // 执行SSH命令
    public Map<String, Object> executeCommand(Session session, String command) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand(command);

        Map<String, Object> result = new HashMap<>();
        String stdOut = "";
        String stdErr = "";
        int statusCode = 0;

        // 获取命令执行结果
        java.io.InputStream in = channel.getInputStream();
        java.io.InputStream err = channel.getErrStream();
        channel.connect();

        byte[] tmp = new byte[10240];
        while (true) {
            // 接收标准输出
            while (in.available() > 0) {
                int i = in.read(tmp, 0, 10240);
                if (i < 0) break;
                String str = new String(tmp, 0, i);
                stdOut += str + "\n";
                // System.out.print(str);
            }

            // 接受标准错误输出
            stdErr = getStrByInputStream(session, err);

            // 接受返回状态码
            if (channel.isClosed()) {
                if (in.available() > 0) continue;
                statusCode = channel.getExitStatus();
                System.out.println("exit-status: " + channel.getExitStatus());
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ignored) {}
        }
        result.put("out", stdOut);
        result.put("err", stdErr);
        result.put("code", statusCode);
        channel.disconnect();
        return result;
    }


    // 执行交互式SSH命令
    public void executeInteractiveCommand(Session session, String command) throws Exception {
        ChannelShell channel = (ChannelShell) session.openChannel("shell");

        channel.connect();

        try {
            // 获取输入输出流
            java.io.InputStream in = channel.getInputStream();
            java.io.OutputStream out = channel.getOutputStream();

            // 向通道写入命令
            out.write((command + "\n").getBytes());
            out.flush();

            // 读取命令执行结果
            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    System.out.println("exit-status: " + channel.getExitStatus());
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {}
            }
        } finally {
            channel.disconnect();
        }
    }

    // 处理工作流
    public String getStrByInputStream(Session session , InputStream inputStream){
        log.info("stdOut:{}", inputStream.toString());
        StringBuffer stringBuffer = new StringBuffer();
        byte[] bytes = new byte[1024];
        int result = -1;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        String str = null;
        try {
            while ((result = inputStream.read(bytes, 0, bytes.length)) != -1){
                byteArrayOutputStream.write(bytes,0,result);
            }
            str = new String(byteArrayOutputStream.toByteArray(), charset);
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();

        } catch (IOException e) {
            log.error("获取输出失败", e);
        }
        // log.info("获取linux的标准输出的结果:{}", str);
        log.info("获取Linux标准输出结果成功");
        return str;
    }
}

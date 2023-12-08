package com.image.util;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * SshUtil
 *
 * @Author litianwei
 * @Date 2023/12/6
 **/
@Slf4j
public class SshUtil {

    private String charset = Charset.defaultCharset().toString();
    private static final int TIME_OUT = 1000 * 5 * 60;

    /**
     * 连接主机，执行命令
     * @param command
     * @param ip
     * @param user
     * @param passwd
     * @return
     */
    public Map<String, Object> execCommand(String command, String ip, int port,String user, String passwd) {
        Map<String, Object> result = new HashMap<>();
        Connection connection = new Connection(ip, port);
        Session session = null;
        try {
            connection.connect();
            log.info("开始连接，ip:{},port:{},user:{},passwd:{},command:{}",ip, port, user, passwd, command);
            boolean connectResult = connection.authenticateWithPassword(user,passwd);

            if(!connectResult){
                log.info("连接失败，ip:{},port:{},user:{},passwd:{},command:{}",ip, port, user, passwd, command);
                return result;
            }

            session = connection.openSession();
            // 1.执行命令
            session.execCommand(command);
            //接收目标服务器上的控制台返回结果，读取br中的内容；
            InputStream stdOut = new StreamGobbler(session.getStdout());
            String stdOutStr = getStrByInputStream(session, stdOut);
            //接收目标服务器上的控制台返回结果，读取br中的内容；
            InputStream stdErrOut = new StreamGobbler(session.getStderr());
            String stdErrStr = getStrByInputStream(session, stdErrOut);

            //当超过 TIME_OUT 中的时候的时候session自动关闭
            session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);

            //得到脚本运行成功与否的标志 ：0－成功 非0－失败
            int ret = session.getExitStatus();
            log.info("连接成功，ip:{},port:{},user:{},passwd:{},command:{},ret:{}",ip, port, user, passwd,
                command, ret);
            result.put("code", ret);
            result.put("out", stdOutStr);
            result.put("error", stdErrStr);
        } catch (IOException e) {
            log.error("连接失败，ip:{},port:{},user:{},passwd:{},command:{}",ip, port, user, passwd, command, e);
        }finally {
            if (session != null) {
                session.close();
            }
            connection.close();
        }
        return result;
    }

    /**
     * 处理工作流
     * @param session
     * @param inputStream
     * @return
     */
    public String getStrByInputStream(Session session ,InputStream inputStream){
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
            str = new String(byteArrayOutputStream.toByteArray(),charset);
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
package com.image.test;

import com.image.util.GraphViz;
import com.image.util.SimplifyUtil;
import com.image.util.Ssh2Util;
import com.image.util.SshUtil;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
class SystemApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testSSHUtils() {
        SshUtil ssh = new SshUtil();
        Map<String, Object> map = ssh.execCommand("docker run -it --rm 30a9b9bec984 /bin/bash && rpm -qa | wc -l",
            "157.0.19.2", 10813, "root", "ictnj@123456");
        String code = map.get("code").toString();
        System.out.println(code);
        String out = map.get("out").toString();
        System.out.println(out);
    }

    @Test
    void testGraphviz() {
        String dotFormat="1->2;1->3;1->4;4->5;4->6;6->7;5->7;3->8;3->6;8->7;2->8;2->5;";
        GraphViz gv = new GraphViz();
        gv.addln(gv.start_graph());
        gv.add(dotFormat);
        gv.addln(gv.end_graph());
        String type = "png";
        // gv.increaseDpi();
        gv.decreaseDpi();
        gv.decreaseDpi();
        File out =
            new File("D:\\Workspace\\container-system\\image-system\\src\\dot-picture\\dotGraphTest" + "." + type);
        gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
        System.out.println("--------finish--------");
    }

    @Test
    void testGraphviz1() {
        String dotFormat="1->2;1->3;1->4;4->5;4->6;6->7;5->7;3->8;3->6;8->7;2->8;2->5;";
        createDotGraph(dotFormat, "DotGraph");
    }

    public static void createDotGraph(String dotFormat,String fileName)
    {
        GraphViz gv=new GraphViz();
        gv.addln(gv.start_graph());
        gv.add(dotFormat);
        gv.addln(gv.end_graph());
        // png为输出格式，还可改为pdf，gif，jpg等
        String type = "png";
        // gv.increaseDpi();
        gv.decreaseDpi();
        gv.decreaseDpi();
        File out = new File(fileName+"."+ type);
        gv.writeGraphToFile( gv.getGraph( gv.getDotSource(), type ), out );
    }

    @Test
    void testSsh2Util() throws Exception {
        Ssh2Util ssh2Util = new Ssh2Util();
        boolean conn = ssh2Util.connect("root", "ictnj@123456", "157.0.19.2", 10813);
        if (conn) {
            System.out.println("连接服务器成功");
        } else {
            System.out.println("连接服务器失败");
        }
    }

    @Test
    void testSsh2UtilExecCmd() throws Exception {
        Ssh2Util ssh2Util = new Ssh2Util();
        String command = "rpm -qa | wc -l";
        String ans = ssh2Util.execCmd(command, "root", "ictnj@123456", "157.0.19.2", 10813);
        System.out.println(ans);
        return ;
    }

    @Test
    void testSsh2UtilMultiCmd() throws Exception {
        Ssh2Util ssh2Util = new Ssh2Util();
        List<String> cmds = new ArrayList<>();
        cmds.add("docker run -it --rm 30a9b9bec984 /bin/bash");
        cmds.add("rpm -qa | wc -l");
        ssh2Util.execCmdOnPTY(cmds, "root", "ictnj@123456", "157.0.19.2", 10813);
        return ;
    }

    @Test
    void testJschCmd() {
        String host = "157.0.19.2";
        String username = "root";
        String password = "ictnj@123456";
        int port = 10813; // 默认SSH端口号

        try {
            // 创建JSch对象
            JSch jsch = new JSch();

            // 创建SSH会话
            Session session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); // 关闭密钥检查

            // 连接SSH会话
            session.connect();

            // 第一个命令：docker run 启动一个容器
            executeCommand(session, "docker run -it --rm 30a9b9bec984 /bin/bash");

            // 第二个命令：rpm -qa 查询容器内的rpm包
            String rpmQueryResult = executeCommand(session, "rpm -qa | wc -l");

            // 打印命令执行结果
            System.out.println("RPM Packages in the Container:");
            System.out.println(rpmQueryResult);

            // 关闭会话
            session.disconnect();

        } catch (JSchException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String executeCommand(Session session, String command) throws JSchException, IOException {
        // 创建执行命令的通道
        ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command);

        // 获取命令执行结果
        InputStream inputStream = channelExec.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // 连接通道
        channelExec.connect();

        // 读取命令执行结果
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }

        // 关闭通道
        channelExec.disconnect();

        return result.toString();
    }

    @Test
    void testProcessStringToFormat() {
        String str = "mysql-common (>= 5.6.51-1debian9), mysql-client (= 5.6.51-1debian9), perl, psmisc, libaio1 (>= 0.3.93)"
            + ", libc6 (>= 2.17), libgcc1 (>= 1:3.0), libnuma1 (>= 2.0.11), libstdc++6 (>= 5.2), debconf (>= 0.5) | debconf-2.0, init-system-helpers (>= 1.18~), adduser, gpgv | gpgv2 | gpgv1";
        SimplifyUtil simplifyUtil = new SimplifyUtil();
        List<String> strings = simplifyUtil.processStringToFormat(str);
        for (String s : strings) {
            System.out.print(s + " ");
        }
    }
}

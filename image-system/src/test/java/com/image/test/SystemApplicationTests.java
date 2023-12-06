package com.image.test;

import com.image.util.GraphViz;
import com.image.util.SshUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
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
}

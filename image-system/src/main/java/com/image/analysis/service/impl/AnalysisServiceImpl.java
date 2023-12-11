package com.image.analysis.service.impl;

import com.image.analysis.service.AnalysisService;
import com.image.util.GraphViz;
import com.image.util.SshConnectionPool;
import com.image.util.SshUtil;
import com.jcraft.jsch.Session;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * AnalysisServiceImpl
 *
 * @Author litianwei
 * @Date 2023/12/6
 **/
@Slf4j
@Service
public class AnalysisServiceImpl implements AnalysisService {

    private SshUtil sshUtil;
    private SshConnectionPool sshConnectionPool;

    @Override
    public List<String> getAllRpmLists(String containerID) {
        log.info("----start query all rpm list----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        List<String> result = new ArrayList<>();
        try {
            Session session = sshConnectionPool.getSession();

            String command = "docker exec " + containerID + " rpm -qa";
            Map<String, Object> map = sshConnectionPool.executeCommand(session, command);
            Object obj = map.get("out");
            String str = obj.toString();
            String[] split = str.split("\n");
            for (int i = 0; i < split.length; i++) {
                result.add(split[i]);
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        log.info("----query rpm list down----");
        return result;
    }

    @Override
    public void writeListToFile(List<String> rpms, String containerID) {
        log.info("----start write list to File----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        try {
            Session session = sshConnectionPool.getSession();

            String getOSCmd = "docker exec " + containerID + " cat /etc/centos-release | awk '{print $1 substr($4, 1, 3)}'";
            Map<String, Object> map = sshConnectionPool.executeCommand(session, getOSCmd);
            String os = map.get("out").toString().replaceAll("\\r|\\n", "");;

            String getArch = "docker exec " + containerID + " arch";
            map = sshConnectionPool.executeCommand(session, getArch);
            String arch = map.get("out").toString().replaceAll("\\r|\\n", "");

            // 文件路径
            String filePath = "D:\\Workspace\\container-system\\image-system\\src\\conf\\";
            String fileName = containerID + "-" + os + "-" + arch + "-" + LocalDate.now().toString() + ".conf";
            Path path = Paths.get(filePath + fileName);

            sshConnectionPool.releaseSession(session);

            Files.write(path, rpms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("----Data written to file successfully----");
    }

    // TODO 简化版的rpm列表
    @Override
    public void writeSimpleListToFile(List<String> rpms, String containerID) {
        log.info("----start write simple rpm list----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        try {
            Session session = sshConnectionPool.getSession();

            String getOSCmd = "docker exec " + containerID + " cat /etc/centos-release | awk '{print $1 substr($4, 1, 3)}'";
            Map<String, Object> map = sshConnectionPool.executeCommand(session, getOSCmd);
            String os = map.get("out").toString().replaceAll("\\r|\\n", "");;

            String getArch = "docker exec " + containerID + " arch";
            map = sshConnectionPool.executeCommand(session, getArch);
            String arch = map.get("out").toString().replaceAll("\\r|\\n", "");

            // 文件路径
            String filePath = "D:\\Workspace\\container-system\\image-system\\src\\conf\\";
            String fileName = containerID + "-" + os + "-" + arch + "-" + LocalDate.now().toString() + "-simple" +
                ".conf";
            Path path = Paths.get(filePath + fileName);

            List<String> simpleList = new ArrayList<>();
            StringBuilder stb = new StringBuilder();
            for (int i = 0; i < rpms.size(); i++) {
                String[] words = rpms.get(i).split("-");
                stb.delete(0, stb.length());
                for (int j = 0; j < words.length; j++) {
                    if (words[j].charAt(0) <= 'z' && words[j].charAt(0) >= 'a') {
                        stb.append(words[j]);
                        if (j < words.length - 1 && words[j + 1].charAt(0) <= 'z' && words[j + 1].charAt(0) >= 'a') {
                            stb.append("-");
                        }
                    } else break;
                }
                simpleList.add(stb.toString());
            }

            sshConnectionPool.releaseSession(session);

            Files.write(path, simpleList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("----write rpm list finished----");
    }

    @Override
    public Map<String, String> buildOriginDependencies(List<String> rpms, String containerID) {
        Map<String, String> ans = new HashMap<>();
        for (String rpm : rpms) {
            ans.putAll(queryDependencies(rpm, containerID));
        }
        return ans;
    }

    @Override
    public Map<String, String> queryDependencies(String dep, String containerID) {
        log.info("----start analysis dependencies----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        Map<String, String> ans = new HashMap<>();
        System.out.println("获取依赖关系：" + dep);
        try {
            Session session = sshConnectionPool.getSession();

            String command = "docker exec " + containerID + " rpm -e --test " + dep;
            Map<String, Object> map = sshConnectionPool.executeCommand(session, command);
            String out = map.get("err").toString();
            String[] needDependencies = out.split("\n");
            Set<String> set = new HashSet<>();
            for (int i = 1; i < needDependencies.length; i++) {
                String[] tmp = needDependencies[i].split(" ");
                String[] packages = tmp[tmp.length - 1].split("-");
                String packageName = "";
                for (int j = 0; j < packages.length; j++) {
                    if (packages[j].charAt(0) <= 'z' && packages[j].charAt(0) >= 'a') {
                        packageName += packages[j];
                        if (j < packages.length - 1 && packages[j + 1].charAt(0) <= 'z' && packages[j + 1].charAt(0) >= 'a') {
                            packageName += "-";
                        }
                    } else break;
                }
                set.add(packageName);
            }
            for (String str : set) {
                ans.put(str, dep);
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // String command = "docker exec " + containerID + " rpm -e --test " + dep;
        // Map<String, Object> map = sshUtil.execCommand(command, "157.0.19.2", 10813, "root", "ictnj@123456");
        // String out = map.get("error").toString();
        // String[] needDependencies = out.split("\n");
        // Set<String> set = new HashSet<>();
        // for (int i = 1; i < needDependencies.length; i++) {
        //     String[] tmp = needDependencies[i].split(" ");
        //     String[] packages = tmp[tmp.length - 1].split("-");
        //     String packageName = "";
        //     for (int j = 0; j < packages.length; j++) {
        //         if (packages[j].charAt(0) <= 'z' && packages[j].charAt(0) >= 'a') {
        //             packageName += packages[j];
        //             if (j < packages.length - 1 && packages[j + 1].charAt(0) <= 'z' && packages[j + 1].charAt(0) >= 'a') {
        //                 packageName += "-";
        //             }
        //         } else break;
        //     }
        //     set.add(packageName);
        // }
        // Map<String, String> ans = new HashMap<>();
        // for (String str : set) {
        //     ans.put(str, dep);
        // }
        log.info("----dependencies analysis end----");
        return ans;
    }

    @Override
    public Boolean drawDependenciesTopology(Map<String, String> deps) {
        log.info("----start draw dependencies picture----");
        try {
            StringBuilder stb = new StringBuilder();
            for (Map.Entry<String, String> entry : deps.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                String[] words = value.split("-");
                String packageName = new String();
                for (int i = 0; i < words.length; i++) {
                    if (words[i].charAt(0) <= 'z' && words[i].charAt(0) >= 'a') {
                        packageName += words[i];
                        if (i < words.length - 1 && words[i + 1].charAt(0) <= 'z' && words[i + 1].charAt(0) >= 'a') {
                            packageName += "-";
                        }
                    } else break;
                }
                stb.append("\"" + key + "\"" + " -> " + "\"" + packageName + "\"" + "; ");
            }
            String dotFormat = stb.toString();

            GraphViz gv = new GraphViz();
            gv.addln(gv.start_graph());
            gv.add(dotFormat);
            gv.addln(gv.end_graph());
            String type = "png";
            String dotFileName = "dotGraph-" + LocalDate.now().toString();
            gv.decreaseDpi();
            gv.decreaseDpi();
            File out =
                new File("D:\\Workspace\\container-system\\image-system\\src\\dot-picture\\" + dotFileName + "." + type);
            gv.writeGraphToFile(gv.getGraph(gv.getDotSource(), type), out);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        log.info("----draw dependencies picture end----");
        return true;
    }


}

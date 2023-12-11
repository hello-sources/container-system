package com.image.analysis.service.impl;

import com.image.analysis.service.AnalysisService;
import com.image.util.SshConnectionPool;
import com.image.util.SshUtil;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private SshConnectionPool sshConnectionPool = new SshConnectionPool();

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
    public void drawDependenciesTopology(Map<String, String> deps) {

    }


}

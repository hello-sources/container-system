package com.image.analysis.service.impl;

import com.image.analysis.service.AnalysisService;
import com.image.util.GraphViz;
import com.image.util.SshConnectionPool;
import com.image.util.SshUtil;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public Map<String, List<String>> buildOriginDependencies(List<String> rpms, String containerID) {
        Map<String, List<String>> ans = new HashMap<>();
        for (String rpm : rpms) {
            Map<String, String> rpmDependencies = queryDependencies(rpm, containerID);
            for (Map.Entry<String, String> entry : rpmDependencies.entrySet()) {
                if (ans.containsKey(entry.getKey())) {
                    ans.get(entry.getKey()).add(entry.getValue());
                } else {
                    List<String> list = new ArrayList<>();
                    list.add(entry.getValue());
                    ans.put(entry.getKey(), list);
                }
            }
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

            // System.out.println("标准输出结果：" + map.get("out").toString());
            // System.out.println("标准错误输出：" + out);

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
    public Boolean drawDependenciesTopology(Map<String, List<String>> deps) {
        log.info("----start draw dependencies picture----");
        try {
            StringBuilder stb = new StringBuilder();
            for (Map.Entry<String, List<String>> entry : deps.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                for (int i = 0; i < values.size(); i++) {
                    String[] words = values.get(i).split("-");
                    String packageName = new String();
                    for (int j = 0; j < words.length; j++) {
                        if (words[j].charAt(0) <= 'z' && words[j].charAt(0) >= 'a') {
                            packageName += words[j];
                            if (j < words.length - 1 && words[j + 1].charAt(0) <= 'z' && words[j + 1].charAt(0) >= 'a') {
                                packageName += "-";
                            }
                        } else break;
                    }
                    stb.append("\"" + key + "\"" + " -> " + "\"" + packageName + "\"" + "; ");
                }
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

    @Override
    public List<String> querySingleFileDependency(String containerID, String filePath) {
        log.info("----start analysis single file dependency----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        List<String> res = new ArrayList<>();
        Set<String> set = new HashSet<>();
        try {
            Session session = sshConnectionPool.getSession();

            // 例如AuthenticAMD路径为/linpack-xtreme/linpack-xtreme-1.1.5-amd64/AuthenticAMD
            String lddCommand = "docker exec " + containerID + " ldd " + filePath;
            Map<String, Object> map = sshConnectionPool.executeCommand(session, lddCommand);
            String lddOut = map.get("out").toString();

            // 正则表达式查找匹配的数据
            String regex = "/[^\\s]+";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(lddOut);
            while (matcher.find()) {
                String dynamicFilePath = matcher.group().trim().replaceAll("\n", "");
                String dynamicCommand = "docker exec " + containerID + " rpm -qf " + dynamicFilePath;
                Map<String, Object> library = sshConnectionPool.executeCommand(session, dynamicCommand);
                String rpmLibrary = library.get("out").toString().replaceAll("\n", "");
                set.add(rpmLibrary);
                System.out.println("library : " + rpmLibrary);
            }

            for (String lib : set) {
                res.add(lib);
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("----analysis single file dependency finished----");
        return res;
    }

    @Override
    public List<String> queryMultipleFileDependencies(String containerID, List<String> filePaths) {
        log.info("----start analysis multiple file dependency----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        List<String> res = new ArrayList<>();
        Set<String> set = new HashSet<>();
        try {
            Session session = sshConnectionPool.getSession();

            for (int i = 0; i < filePaths.size(); i++) {
                String singleCommand = "docker exec " + containerID + " ldd " + filePaths.get(i);
                Map<String, Object> map = sshConnectionPool.executeCommand(session, singleCommand);
                String lddOut = map.get("out").toString();

                // 正则表达式查找匹配的数据
                String regex = "/[^\\s]+";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(lddOut);
                while (matcher.find()) {
                    String dynamicFilePath = matcher.group().trim().replaceAll("\n", "");
                    String dynamicCommand = "docker exec " + containerID + " rpm -qf " + dynamicFilePath;
                    Map<String, Object> library = sshConnectionPool.executeCommand(session, dynamicCommand);
                    String rpmLibrary = library.get("out").toString().replaceAll("\n", "");
                    set.add(rpmLibrary);
                    System.out.println("library : " + rpmLibrary);
                }
            }

            for (String lib : set) {
                res.add(lib);
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("----start analysis multiple file dependency----");
        return res;
    }

    @Override
    public Map<String, List<String>> querySingleRpmDependency(String containerID, String rpmName) {
        List<String> allRpms = getAllRpmLists(containerID);
        Map<String, List<String>> allDeps = buildOriginDependencies(allRpms, containerID);
        Map<String, List<String>> ans = new HashMap<>();
        // for (Map.Entry<String, List<String>> entry : allDeps.entrySet()) {
        //     String key = entry.getKey();
        //     String value = entry.getValue().toString();
        //     System.out.println("key : " + key + " value : " + value);
        // }
        Queue<String> que = new LinkedList<>();
        que.add(rpmName);
        while (!que.isEmpty()) {
            String rpm = que.poll();
            if (allDeps.get(rpm) == null) break;
            ans.put(rpm, allDeps.get(rpm));
            List<String> lists = allDeps.get(rpm);
            for (int i = 0; i < lists.size(); i++) {
                String[] words = lists.get(i).split("-");
                String packageName = new String();
                for (int j = 0; j < words.length; j++) {
                    if (words[j].charAt(0) <= 'z' && words[j].charAt(0) >= 'a') {
                        packageName += words[j];
                        if (j < words.length - 1 && words[j + 1].charAt(0) <= 'z' && words[j + 1].charAt(0) >= 'a') {
                            packageName += "-";
                        }
                    } else break;
                }
                que.add(packageName);
                if (allDeps.get(packageName) != null) ans.put(packageName, allDeps.get(packageName));
            }
        }
        return ans;
    }

    @Override
    public List<String> listNeedDeleteRpms(String containerID, List<String> filePaths) {
        // 获取该镜像的所有依赖
        List<String> allRpmLists = getAllRpmLists(containerID);

        // 获取应用直接依赖的库
        List<String> directDependencies = queryMultipleFileDependencies(containerID, filePaths);

        // 获取应用启动关联的所有依赖库
        Map<String, List<String>> rpmListMap = new HashMap<>();
        for (String direct : directDependencies) {
            Map<String, List<String>> stringListMap = querySingleRpmDependency(containerID, direct);
            for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
                if (rpmListMap.containsKey(entry.getKey())) {
                    rpmListMap.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    rpmListMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // 根据上述依赖关系结果，获取待删除的rpm包
        Set<String> rpmSet = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : rpmListMap.entrySet()) {
            rpmSet.add(entry.getKey());
            for (String str : entry.getValue()) {
                rpmSet.add(str);
            }
        }

        List<String> ans = new ArrayList<>();
        for (String str : allRpmLists) {
            if (rpmSet.contains(str)) continue;
            else ans.add(str);
        }
        return ans;
    }

    // TODO 删除依赖项，导出为新的镜像
    @Override
    public Boolean deleteAndCommitToImage(String containerID, String imageName, String tag,
        List<String> deleteRpmList) {



        return null;
    }

    // TODO 根据自定义的需求保留相应的依赖关系
    @Override
    public Boolean keepRpmDependencies(String containerID, List<String> rpmNames) {

        return null;
    }

    @Override
    public List<String> keepReservationDependencies(String containerID, String reservationFile) {
        List<String> handProcess = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(reservationFile))) {
            String line = new String();
            while ((line = br.readLine()) != null) {
                handProcess.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return handProcess;
    }


}

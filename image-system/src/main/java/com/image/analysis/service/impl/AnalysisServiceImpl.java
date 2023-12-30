package com.image.analysis.service.impl;

import com.image.analysis.service.AnalysisService;
import com.image.util.GraphViz;
import com.image.util.SimplifyUtil;
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
import java.util.Locale;
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
        // log.info("----start query all rpm list----");
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
        // log.info("----query rpm list down----");
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
                // for (int j = 0; j < words.length; j++) {
                //     if (words[j].charAt(0) <= 'z' && words[j].charAt(0) >= 'a') {
                //         stb.append(words[j]);
                //         if (j < words.length - 1 && words[j + 1].charAt(0) <= 'z' && words[j + 1].charAt(0) >= 'a') {
                //             stb.append("-");
                //         }
                //     } else break;
                // }
                SimplifyUtil simplifyUtil = new SimplifyUtil();
                stb.append(simplifyUtil.processMultipleToSimplify(words));
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
                // String[] packages = tmp[tmp.length - 1].split("-");
                // String packageName = "";
                // for (int j = 0; j < packages.length; j++) {
                //     if (packages[j].charAt(0) <= 'z' && packages[j].charAt(0) >= 'a') {
                //         packageName += packages[j];
                //         if (j < packages.length - 1 && packages[j + 1].charAt(0) <= 'z' && packages[j + 1].charAt(0) >= 'a') {
                //             packageName += "-";
                //         }
                //     } else break;
                // }
                // SimplifyUtil simplifyUtil = new SimplifyUtil();
                // packageName = simplifyUtil.processMultipleToSimplify(packages);
                // set.add(packageName);
                set.add(tmp[tmp.length - 1]);
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
                SimplifyUtil simplifyUtil = new SimplifyUtil();
                String[] keys = entry.getKey().split("-");
                String key = simplifyUtil.processMultipleToSimplify(keys);
                List<String> values = entry.getValue();
                for (int i = 0; i < values.size(); i++) {
                    String[] words = values.get(i).split("-");
                    String packageName = new String();
                    // for (int j = 0; j < words.length; j++) {
                    //     if (words[j].charAt(0) <= 'z' && words[j].charAt(0) >= 'a') {
                    //         packageName += words[j];
                    //         if (j < words.length - 1 && words[j + 1].charAt(0) <= 'z' && words[j + 1].charAt(0) >= 'a') {
                    //             packageName += "-";
                    //         }
                    //     } else break;
                    // }
                    packageName = simplifyUtil.processMultipleToSimplify(words);
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
                if (!rpmLibrary.contains("is not owned by any package")) {
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

        log.info("----analysis single file dependency finished----");
        return res;
    }

    @Override
    public List<String> queryMultipleFileDependencies(String containerID, List<String> filePaths) {
        // log.info("----start analysis multiple file dependency----");
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
                    if (!rpmLibrary.contains("is not owned by any package")) {
                        set.add(rpmLibrary);
                        // System.out.println("library : " + rpmLibrary);
                    }
                }
            }

            for (String lib : set) {
                res.add(lib);
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // log.info("----start analysis multiple file dependency----");
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
                // String packageName = new String();
                // for (int j = 0; j < words.length; j++) {
                //     if (words[j].charAt(0) <= 'z' && words[j].charAt(0) >= 'a') {
                //         packageName += words[j];
                //         if (j < words.length - 1 && words[j + 1].charAt(0) <= 'z' && words[j + 1].charAt(0) >= 'a') {
                //             packageName += "-";
                //         }
                //     } else break;
                // }
                que.add(lists.get(i));
                if (allDeps.get(lists.get(i)) != null) ans.put(lists.get(i), allDeps.get(lists.get(i)));
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
        Map<String, List<String>> stringListMap = new HashMap<>();
        Set<String> rpmSet = new HashSet<>();

        for (String direct : directDependencies) {
            rpmSet.add(direct);
            // System.out.println("当前查询的直接依赖项是：" + direct);
            stringListMap.clear();
            stringListMap = querySingleRpmDependency(containerID, direct);
            for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
                if (rpmListMap.containsKey(entry.getKey())) {
                    rpmListMap.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    rpmListMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // 根据手动裁剪的记录获取需要保留的包名
        String filePath = "D:\\Workspace\\container-system\\image-system\\src\\conf\\keepReservationDependencies.conf";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            // 逐行读取文件内容
            String line = new String();
            while ((line = br.readLine()) != null) {
                // 将每行添加到List中
                rpmSet.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 根据上述依赖关系结果，获取待删除的rpm包
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

    @Override
    public List<String> listKeepRpms(String containerID, List<String> filePaths) {
        // 获取该镜像的所有依赖
        List<String> allRpmLists = getAllRpmLists(containerID);

        // 获取应用直接依赖的库
        List<String> directDependencies = queryMultipleFileDependencies(containerID, filePaths);

        // 获取应用启动关联的所有依赖库
        Map<String, List<String>> rpmListMap = new HashMap<>();

        Set<String> rpmSet = new HashSet<>();

        for (String direct : directDependencies) {
            rpmSet.add(direct);
            Map<String, List<String>> stringListMap = querySingleRpmDependency(containerID, direct);
            for (Map.Entry<String, List<String>> entry : stringListMap.entrySet()) {
                if (rpmListMap.containsKey(entry.getKey())) {
                    rpmListMap.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    rpmListMap.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // 根据手动裁剪的记录获取需要保留的包名
        String filePath = "D:\\Workspace\\container-system\\image-system\\src\\conf\\keepReservationDependencies.conf";
        List<String> ans = keepReservationDependencies(containerID, filePath);

        for (Map.Entry<String, List<String>> entry : rpmListMap.entrySet()) {
            rpmSet.add(entry.getKey());
            for (String str : entry.getValue()) {
                rpmSet.add(str);
            }
        }

        for (String str : rpmSet) {
            ans.add(str);
        }
        return ans;
    }

    @Override
    public Boolean deleteRpmDependencies(String containerID, List<String> deleteRpmList) {
        log.info("----start optimize docker image----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();

        try {
            Session session = sshConnectionPool.getSession();

            log.info("----start delete rpm dependencies----");
            // 删除容器中相关依赖项
            StringBuilder stb = new StringBuilder();
            stb.append("docker exec " + containerID + " rpm -e --nodeps ");
            for (String str : deleteRpmList) {
                stb.append(str + " ");
            }
            Map<String, Object> map = sshConnectionPool.executeCommand(session, stb.toString());
            Object code = map.get("code");
            if (((Integer) code).intValue() != 0)  {
                Object err = map.get("err");
                System.out.println("err : " + err.toString());
                log.info("----删除镜像依赖失败----");
                return false;
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        log.info("----optimize docker image succeed----");
        return true;
    }

    @Override
    public List<String> keepRpmDependencies(String containerID, List<String> rpmNames, List<String> filePaths) {
        Set<String> rpmSet = new HashSet<>();
        List<String> keepRpms = listKeepRpms(containerID, filePaths);
        for (String str : keepRpms) {
            rpmSet.add(str);
        }

        for (String str : rpmNames) {
            rpmSet.add(str);
        }

        List<String> ans = new ArrayList<>(rpmSet);
        return ans;
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

    @Override
    public Boolean commitToImage(String containerID, String imageName, String tag) {
        // 导出容器为镜像
        log.info("----start commit to a new image----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();

        try {
            Session session = sshConnectionPool.getSession();
            // 以docker commit的方式导出，会保留镜像层信息
            imageName = imageName.toLowerCase();
            imageName += "-commit";
            String command = "docker commit " + containerID + " " + imageName + ":" + tag;
            Map<String, Object> commitImage = sshConnectionPool.executeCommand(session, command);
            Object code = commitImage.get("code");
            if (((Integer) code).intValue() != 0)  {
                Object err = commitImage.get("err");
                System.out.println("err : " + err.toString());
                log.info("----导出为新镜像失败----");
                return false;
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        log.info("----commit to a new image end----");
        return true;
    }

    @Override
    public Boolean exportToTarImage(String containerID, String imageName, String tag, String path) {
        log.info("----start export to a new image----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();

        try {
            Session session = sshConnectionPool.getSession();

            // 以docker export的方式导出镜像为tar文件，不保留镜像层信息
            imageName = imageName.toLowerCase();
            imageName += "-export";
            String command = "docker export -o " + path + "/" + imageName + "-" + tag + "-" + LocalDate.now() + ".tar"
                + " " + containerID;
            Map<String, Object> commitImage = sshConnectionPool.executeCommand(session, command);
            Object code = commitImage.get("code");
            if (((Integer) code).intValue() != 0)  {
                Object err = commitImage.get("err");
                System.out.println("err : " + err.toString());
                log.info("----导出为tar格式镜像失败----");
                return false;
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        log.info("----export to a new tar image end----");
        return true;
    }

    @Override
    public Boolean importTarToImage(String path, String sourTarImageName, String destImageName, String destTag) {
        log.info("----start import tar file to image----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();

        try {
            Session session = sshConnectionPool.getSession();

            // 以docker import导入tar格式的镜像文件
            destImageName = destImageName.toLowerCase(Locale.ROOT);
            String command = "docker import " + path + "/" + sourTarImageName + " " + destImageName + ":" + destTag;
            Map<String, Object> commitImage = sshConnectionPool.executeCommand(session, command);
            Object code = commitImage.get("code");
            if (((Integer) code).intValue() != 0)  {
                Object err = commitImage.get("err");
                System.out.println("err : " + err.toString());
                log.info("----导入tar文件为镜像失败----");
                return false;
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        log.info("---import tar file to image end----");
        return true;
    }


}

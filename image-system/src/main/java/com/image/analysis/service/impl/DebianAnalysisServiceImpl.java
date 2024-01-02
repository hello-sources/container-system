package com.image.analysis.service.impl;

import com.image.analysis.service.DebianAnalysisService;
import com.image.util.SimplifyUtil;
import com.image.util.SshConnectionPool;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DebianAnalysisServiceImpl
 *
 * @Author litianwei
 * @Date 2023/12/30
 **/
@Slf4j
@Service
public class DebianAnalysisServiceImpl implements DebianAnalysisService {


    @Override
    public List<String> getAllDpkgLists(String containerID) {
        log.info("----获取Debian系所有的dpkg包----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        List<String> result = new ArrayList<>();
        try {
            Session session = sshConnectionPool.getSession();

            String command = "docker exec " + containerID + " dpkg --get-selections | grep install | awk '{print $1}'";
            Map<String, Object> dpkgMap = sshConnectionPool.executeCommand(session, command);
            Object code = dpkgMap.get("code");
            if (((Integer) code).intValue() != 0)  {
                Object err = dpkgMap.get("err");
                System.out.println("err : " + err.toString());
                log.info("----获取dpkg包失败----");
                return null;
            }
            Object obj = dpkgMap.get("out");
            String str = obj.toString();
            String[] split = str.split("\n");
            for (int i = 0; i < split.length; i++) {
                result.add(split[i]);
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    @Override
    public Boolean writeListToFile(List<String> dpkgs, String containerID) {
        log.info("----start write dpkg list to File----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();

        try {
            Session session = sshConnectionPool.getSession();

            Map<String, Object> map = new HashMap<>();

            String getOSCmd = "docker exec " + containerID + " awk -F= '/^ID=/{print $2}' /etc/os-release";
            map = sshConnectionPool.executeCommand(session, getOSCmd);
            String os = map.get("out").toString().replaceAll("\\r|\\n", "");

            map.clear();
            String getArch = "docker exec " + containerID + " arch";
            map = sshConnectionPool.executeCommand(session, getArch);
            String arch = map.get("out").toString().replaceAll("\\r|\\n", "");

            // 文件路径
            String filePath = "D:\\Workspace\\container-system\\image-system\\src\\conf\\";
            String fileName = containerID + "-" + os + "-" + arch + "-" + LocalDate.now().toString() + ".conf";
            Path path = Paths.get(filePath + fileName);

            sshConnectionPool.releaseSession(session);

            Path write = Files.write(path, dpkgs);
            if (write == null || !Files.exists(write)) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("----Data written to file successfully----");
        return true;
    }

    @Override
    public Map<String, List<String>> queryDependencies(String dpkg, String containerID) {
        log.info("----start analysis dependencies----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        Map<String, List<String>> ans = new HashMap<>();

        System.out.println("----获取依赖关系：" + dpkg + "----");
        try {
            Session session = sshConnectionPool.getSession();

            // Depends字段是软件包在运行时需要依赖的其他软件包
            // docker exec 3023e8383575 dpkg --status libperl5.24 | grep Depends | sed 's/Depends: //'
            String command_depends =
                "docker exec " + containerID + " dpkg --status " + dpkg + " | grep -w \"^\\s*Depends\" | sed 's/Depends: //'";
            Map<String, Object> depends_map = sshConnectionPool.executeCommand(session, command_depends);
            String depends_out = depends_map.get("out").toString().replaceAll("\\r|\\n", "");

            // Pre_Depends字段是在安装软件包之前必须满足的依赖关系
            // docker exec 3023e8383575 dpkg --status libperl5.24 | grep -Po '^Pre-Depends: \K.*'
            String command_pre_depends = "docker exec " + containerID + " dpkg --status " + dpkg + " | grep -Po "
                + "'^Pre-Depends: \\K.*'";
            Map<String, Object> pre_depends_map = sshConnectionPool.executeCommand(session, command_pre_depends);
            String pre_depends_out = pre_depends_map.get("out").toString().replaceAll("\\r|\\n", "");

            // 正则表达式处理文本
            // String regex = "[\\w.-]+(?=\\s*\\(|,\\s*\\w|$)";
            // Pattern pattern = Pattern.compile(regex);
            // Matcher matcher = pattern.matcher(depends_out);
            // Matcher pre_matcher = pattern.matcher(pre_depends_out);

            // 不使用正则表达式，使用自定义工具类处理输出
            SimplifyUtil simplifyUtil = new SimplifyUtil();
            List<String> depends_list = simplifyUtil.processStringToFormat(depends_out);
            List<String> pre_depends_list = simplifyUtil.processStringToFormat(pre_depends_out);

            // 一个dpkg包可能需要多个依赖项，因此存成List类型，可能会存在Pre_Depends和Depends重复的现象，使用Set去重
            Set<String> set = new HashSet<>();
            List<String> dep = new ArrayList<>();
            depends_list.addAll(pre_depends_list);

            // while (matcher.find()) {
            //     set.add(matcher.group());
            // }
            // while (pre_matcher.find()) {
            //     set.add(pre_matcher.group());
            // }

            for (String s : depends_list) {
                set.add(s);
            }

            for (String str : set) {
                dep.add(str);
            }

            ans.put(dpkg, dep);

            sshConnectionPool.releaseSession(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("----dependencies analysis end----");
        return ans;
    }

    @Override
    public Map<String, List<String>> buildOriginDependencies_absent(String containerID) {
        log.info("----start build origin dependencies(absent)----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        Map<String, List<String>> ans = new HashMap<>();

        try {
            Session session = sshConnectionPool.getSession();

            // 只能使用bash -c的方式执行命令
            // dpkg --get-selections | awk '{print $1}' | xargs dpkg-query -Wf '${Package} -> ${Depends}\n'无法读取xargs变量
            // bash -c 'dpkg --get-selections | awk '\''{print $1}'\'' | xargs -I{} dpkg-query -Wf '\''${Package} -> ${Depends}\n'\'' {}'
            String command = "docker exec " + containerID + " bash -c 'dpkg --get-selections | awk '\\''{print $1}'\\'' | xargs -I{} dpkg-query -Wf '\\''${Package} -> ${Depends}\\n'\\'' {}'";
            Map<String, Object> map = sshConnectionPool.executeCommand(session, command);
            if (((Integer) map.get("code")).intValue() != 0) {
                System.out.println("获取dpkg依赖关系失败");
                return null;
            }

            // 获取输出
            String out = map.get("out").toString();
            String[] res = out.split("\n");
            List<String> relay = new ArrayList<>();
            for (String str : res) {
                String[] strs = str.split(" -> ");
                String dpkgName = strs[0];
                String dpkgDeps = null;
                if (strs.length > 1) {
                    dpkgDeps = strs[1];
                }
                relay = new ArrayList<>();

                // 使用简化的工具类处理输出
                SimplifyUtil simplifyUtil = new SimplifyUtil();

                if (dpkgDeps == null) {
                    ans.put(dpkgName, relay);
                    continue;
                }
                relay = simplifyUtil.processStringToFormat(dpkgDeps);
                ans.put(dpkgName, relay);
            }

            sshConnectionPool.releaseSession(session);
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("----build origin dependencies(absent) end----");
        return ans;
    }


    @Override
    public Map<String, List<String>> buildOriginDependencies(List<String> dpkgs, String containerID) {





        return null;
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

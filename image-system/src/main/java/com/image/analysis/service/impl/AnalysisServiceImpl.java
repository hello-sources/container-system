package com.image.analysis.service.impl;

import com.image.analysis.service.AnalysisService;
import com.image.util.SshUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Override
    public List<String> getAllRpmLists(String containerID) {
        sshUtil = new SshUtil();
        String command = "docker exec " + containerID + " rpm -qa";
        Map<String, Object> map = sshUtil.execCommand(command, "157.0.19.2", 10813, "root", "ictnj@123456");
        Object obj = map.get("out");
        String str = obj.toString();
        String[] split = str.split("\n");
        List<String> result = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            result.add(split[i]);
        }
        return result;
    }

    @Override
    public void writeListToFile(List<String> rpms, String containerID) {
        sshUtil = new SshUtil();
        String getOSCmd = "docker exec " + containerID + " cat /etc/centos-release | awk '{print $1 substr($4, 1, 3)}'";
        Map<String, Object> map = sshUtil.execCommand(getOSCmd, "157.0.19.2", 10813, "root", "ictnj@123456");
        String os = map.get("out").toString().replaceAll("\\r|\\n", "");;

        String getArch = "docker exec " + containerID + " arch";
        map = sshUtil.execCommand(getArch, "157.0.19.2", 10813, "root", "ictnj@123456");
        String arch = map.get("out").toString().replaceAll("\\r|\\n", "");;

        // 文件路径
        String filePath = "D:\\Workspace\\container-system\\image-system\\src\\conf\\";
        String fileName = containerID + "-" + os + "-" + arch + ".conf";

        Path path = Paths.get(filePath + fileName);
        try {
            Files.write(path, rpms);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("Data written to file successfully.");
    }

    @Override
    public void buildOriginDependencies(List<String> rpms, String containerID) {

    }

    @Override
    public Map<String, String> queryDependencies(String deps, String containerID) {
        log.info("----start analysis dependencies----");
        sshUtil = new SshUtil();
        String command = "docker exec " + containerID + " rpm -e --test " + deps;
        Map<String, Object> map = sshUtil.execCommand(command, "157.0.19.2", 10813, "root", "ictnj@123456");
        String out = map.get("error").toString();
        String[] needDependencies = out.split("\n");
        Set<String> set = new HashSet<>();
        for (int i = 1; i < needDependencies.length; i++) {
            String[] tmp = needDependencies[i].split(" ");
            set.add(tmp[tmp.length - 1]);
        }
        Map<String, String> ans = new HashMap<>();
        for (String str : set) {
            ans.put(str, deps);
        }
        log.info("----dependencies analysis end----");
        return ans;
    }


}

package com.image.analysis.service.impl;

import com.image.analysis.service.AnalysisService;
import com.image.util.SshUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AnalysisServiceImpl
 *
 * @Author litianwei
 * @Date 2023/12/6
 **/
@Slf4j
@Service
public class AnalysisServiceImpl implements AnalysisService {

    @Override
    public List<String> getAllRpmLists(String containerID) {
        SshUtil sshUtil = new SshUtil();
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
}

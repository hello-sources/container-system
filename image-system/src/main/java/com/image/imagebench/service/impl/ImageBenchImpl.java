package com.image.imagebench.service.impl;

import com.image.imagebench.service.ImageBench;
import com.image.util.SimplifyUtil;
import com.image.util.SshConnectionPool;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * ImageBenchImpl
 *
 * @Author litianwei
 * @Date 2024/1/4
 **/
@Slf4j
@Service
public class ImageBenchImpl implements ImageBench {

    @Override
    public Long getContainerCreateTime(String imageName, String tag) {
        log.info("----start create container----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        Long duration = Long.MAX_VALUE;

        try {
            Session session = sshConnectionPool.getSession();
            SimplifyUtil simplifyUtil = new SimplifyUtil();

            String beforeTime = simplifyUtil.getCurrentTime();
            String command = "docker run -itd  " + imageName + ":" + tag + " /bin/bash";
            Map<String, Object> createContainer = sshConnectionPool.executeCommand(session, command);
            String afterTime = simplifyUtil.getCurrentTime();

            Object code = createContainer.get("code");
            if (((Integer) code).intValue() != 0)  {
                Object err = createContainer.get("err");
                System.out.println("err : " + err.toString());
                log.info("----create container failed----");
                return duration;
            }

            duration = simplifyUtil.timeDuration(afterTime, beforeTime);

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        log.info("---create container succeed----");
        return duration;
    }

    @Override
    public Long getServiceStartTime(String containerID, String startPath) {
        log.info("----start start app service----");
        SshConnectionPool sshConnectionPool = new SshConnectionPool();
        Long duration = Long.MAX_VALUE;

        try {
            Session session = sshConnectionPool.getSession();
            SimplifyUtil simplifyUtil = new SimplifyUtil();

            String command = "docker exec  " + containerID + " " + startPath;
            String beforeTime = simplifyUtil.getCurrentTime();
            Map<String, Object> startService = sshConnectionPool.executeCommand(session, command);
            String afterTime = simplifyUtil.getCurrentTime();
            duration = simplifyUtil.timeDuration(afterTime, beforeTime);

            Object code = startService.get("code");
            if (((Integer) code).intValue() != 0)  {
                Object err = startService.get("err");
                System.out.println("err : " + err.toString());
                log.info("----start app service failed----");
                return Long.MAX_VALUE;
            }

            String out = startService.get("out").toString();
            System.out.println(out);

            sshConnectionPool.releaseSession(session);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        log.info("---start app service succeed----");
        return duration;
    }
}

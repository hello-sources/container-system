package com.image.analysis.service;

import java.util.List;

public interface AnalysisService {

    /**
     * 查询容器镜像所有的rpm包依赖
     **/
    List<String> getAllRpmLists(String containerID);

}

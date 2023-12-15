package com.image.analysis.controller;

import com.image.analysis.service.AnalysisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AnalysisController
 *
 * @Author litianwei
 * @Date 2023/12/15
 **/
@Slf4j
@RestController
@RequestMapping("/analysis")
public class AnalysisController {
    @Autowired
    public AnalysisService analysisService;

    /**
     * 获取容器内rpm包信息
     **/
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public List<String> getRpmList(String containerID) {
        List<String> allRpmLists = analysisService.getAllRpmLists(containerID);
        return allRpmLists;
    }

    /**
     * 将rpm包信息保存到文件中
     **/
    @RequestMapping(value = "/writeToFile", method = RequestMethod.GET)
    @ResponseBody
    public void saveRpmsToFile(String containerID) {
        List<String> allRpmLists = analysisService.getAllRpmLists(containerID);
        analysisService.writeListToFile(allRpmLists, containerID);
        return ;
    }

    /**
     * 获取特定的rpm包信息
     **/
    @RequestMapping(value = "/querySpecific", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> analysisDependencies(String rpmName, String containerID) {
        Map<String, String> stringStringMap = analysisService.queryDependencies(rpmName, containerID);
        return stringStringMap;
    }

    /**
     * 构建所有的rpm包依赖信息
     **/
    @RequestMapping(value = "/queryAll", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<String>> buildAllRpmDependencies(String containerID) {
        List<String> allRpmLists = analysisService.getAllRpmLists(containerID);
        Map<String, List<String>> stringListMap = analysisService.buildOriginDependencies(allRpmLists, containerID);
        return stringListMap;
    }

    /**
     * 构建所有rpm包信息dot图
     **/
    @RequestMapping(value = "/buildAll", method = RequestMethod.POST)
    @ResponseBody
    public Boolean drawDependenciesTopology(String containerID) {
        List<String> allRpms = analysisService.getAllRpmLists(containerID);
        Map<String, List<String>> allDeps = analysisService.buildOriginDependencies(allRpms, containerID);
        Boolean draw = analysisService.drawDependenciesTopology(allDeps);
        return draw;
    }

    /**
     * 构建简单格式的rpm到文件中
     **/
    @RequestMapping(value = "/buildSimple", method = RequestMethod.GET)
    @ResponseBody
    public void writeSimpleListToFile(String containerID) {
        List<String> allRpmLists = analysisService.getAllRpmLists(containerID);
        analysisService.writeSimpleListToFile(allRpmLists, containerID);
        return ;
    }

    /**
     * 单条可执行文件路径查询相关依赖
     **/
    @RequestMapping(value = "/singlePathDeps", method = RequestMethod.GET)
    @ResponseBody
    public List<String> querySingleFileDependency(String containerID, String path) {
        List<String> rpms = analysisService.querySingleFileDependency(containerID, path);
        return rpms;
    }

    /**
     * 多条可执行文件路径查询相关依赖
     **/
    @RequestMapping(value = "/multiplePathDeps", method = RequestMethod.GET)
    @ResponseBody
    public List<String> queryMultipleFileDependencies(String containerID, List<String> paths) {
        List<String> rpms = analysisService.queryMultipleFileDependencies(containerID, paths);
        return rpms;
    }

    /**
     * 从一个依赖项出发，获取以该依赖为中心的关系拓扑图
     **/
    @RequestMapping(value = "/querySingleRpmDeps", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, List<String>> querySingleRpmDependency(String containerID, String rpmName) {
        Map<String, List<String>> stringListMap = analysisService.querySingleRpmDependency(containerID, rpmName);
        return stringListMap;
    }

    /**
     * 以单个依赖为中心，画出依赖拓扑图
     **/
    @RequestMapping(value = "/drwaSingleRpmTopo", method = RequestMethod.POST)
    @ResponseBody
    public Boolean drawSingleVisionTopology(String containerID, String rpmName) {
        Map<String, List<String>> stringListMap = analysisService.querySingleRpmDependency(containerID, rpmName);
        Boolean drawSucceed = analysisService.drawDependenciesTopology(stringListMap);
        return drawSucceed;
    }

}

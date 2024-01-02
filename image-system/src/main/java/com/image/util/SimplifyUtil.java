package com.image.util;

import java.util.ArrayList;
import java.util.List;

/**
 * SimplifyUtil
 *
 * @Author litianwei
 * @Date 2023/12/26
 **/
public class SimplifyUtil {

    // 把复杂字符串处理成简单字符串
    public String processMultipleToSimplify(String[] words) {
        StringBuilder stb = new StringBuilder();
        for (int j = 0; j < words.length; j++) {
            if (words[j].charAt(0) <= 'z' && words[j].charAt(0) >= 'a') {
                stb.append(words[j]);
                if (j < words.length - 1 && words[j + 1].charAt(0) <= 'z' && words[j + 1].charAt(0) >= 'a') {
                    stb.append("-");
                }
            } else break;
        }
        return stb.toString();
    }

    // 处理复杂字符串到对应的格式，主要是处理dpkg输出的内容，简化为指定的包名
    public List<String> processStringToFormat(String str) {
        if (str == null || str == "") return null;
        List<String> ans = new ArrayList<>();
        String[] strs = str.split("[,|]");
        for (String s : strs) {
            ans.add(s.split("[(]")[0].trim());
        }
        return ans;
    }
}

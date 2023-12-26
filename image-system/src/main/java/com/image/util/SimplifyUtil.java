package com.image.util;

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
}

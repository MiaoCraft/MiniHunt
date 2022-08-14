package net.mcxk.hjyhunt.util;

import com.alibaba.fastjson2.JSONObject;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/13
 * @apiNote
 */
public class String2Json {
    private String2Json() {
    }

    public static JSONObject str2Json(String str) {
        return JSONObject.parseObject(str);
    }
}

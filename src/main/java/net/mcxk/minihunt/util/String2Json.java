package net.mcxk.minihunt.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/13
 * @apiNote
 */
public class String2Json {
    private String2Json() {
    }

    public static JsonElement str2Json(String str) {
        return new JsonParser().parse(str);
    }
}

package net.mcxk.hjyhunt.util;

import com.alibaba.fastjson2.JSONObject;
import net.mcxk.hjyhunt.HJYHunt;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/13
 * @apiNote
 */
public class SeedFilter {
    static final String FILTER_CODE = HJYHunt.getInstance().getConfig().getString("filterCode");
    static JSONObject token;

    private SeedFilter() {
    }

    public static String getSeed() {
        token = String2Json.str2Json(HTTPRequester.request("https://fsg.opalstacked.com/?filter=" + FILTER_CODE));
        String sclass = token.getString("class");
        String sstruct = token.getString("struct");
        String randBiome = token.getString("randbiome");
        String pref = token.getString("pref");
        String seed = " ";
        while (" ".equals(seed)) {
            seed = HTTPRequester.request(String.format("https://seedbankcustom.andynovo.repl.co/proxy2?class=%s&pref=%s&randbiome=%s&struct=%s", sclass, pref, randBiome, sstruct));
        }
        return seed;
    }
}

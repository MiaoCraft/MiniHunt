package net.mcxk.minihunt.util;

import com.google.gson.JsonObject;
import net.mcxk.minihunt.MiniHunt;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/13
 * @apiNote
 */
public class SeedFilter {
    static final String FILTER_CODE = MiniHunt.getInstance().getConfig().getString("filterCode");
    static JsonObject token;

    private SeedFilter() {
    }

    public static String getSeed() {
        token = String2Json.str2Json(HTTPRequester.request("https://fsg.opalstacked.com/?filter=" + FILTER_CODE)).getAsJsonObject();
        String sclass = token.get("class").getAsString();
        String sstruct = token.get("struct").getAsString();
        String randBiome = token.get("randbiome").getAsString();
        String pref = token.get("pref").getAsString();
        String seed = " ";
        int tries = 0;
        while (" ".equals(seed) && tries < 3) {
            seed = HTTPRequester.request(String.format("https://seedbankcustom.andynovo.repl.co/proxy2?class=%s&pref=%s&randbiome=%s&struct=%s", sclass, pref, randBiome, sstruct));
            tries++;
        }
        return seed;
    }
}

package net.mcxk.minihunt.util;

import net.mcxk.minihunt.MiniHunt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/12
 * @apiNote
 */
public class HTTPRequester {
    public static String request(String httpUrl) {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader br = null;
        StringBuilder res = new StringBuilder();
        try {
            URL url = new URL(httpUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000000);
            connection.connect();
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                if (is != null) {
                    br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                    String tmp;
                    while ((tmp = br.readLine()) != null) {
                        res.append(tmp);
                    }
                }
            }
        } catch (IOException e) {
            MiniHunt.getInstance().getLogger().info("HTTP请求失败！请到GitHub反馈此问题！");
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Objects.requireNonNull(connection).disconnect();
        }
        return res.toString();
    }
}

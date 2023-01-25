package net.mcxk.minihunt.util;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.Game;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/12
 * @apiNote
 */
public class GameEnd {
    static Game game = new Game();

    private GameEnd() {
    }

    public static void startEnd() {
        if (!game.isAutoKick()) {
            return;
        }
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.isEmpty() && player.isOnline()) {
                // 主动踢出玩家
                player.kickPlayer("游戏结束，后台正在重置地图，预计需要60秒! ");
            }
        });

        AtomicReference<String> seed = new AtomicReference<>("");
        final String serverPath = System.getProperty("user.dir");
        File propertiesFile = new File(serverPath + "/server.properties");
        Properties server = new Properties();
        switch (MiniHunt.seedFrom) {
            case 0:
                Bukkit.shutdown();
                break;
            case 1:
                MiniHunt.getInstance().getLogger().info("开始读取种子...");
                final File file = new File(serverPath + "/plugins/" + MiniHunt.pluginName + "/seeds.txt");
                try (final InputStreamReader inputStreamReader = new InputStreamReader(Files.newInputStream(propertiesFile.toPath()), StandardCharsets.UTF_8);
                     final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(propertiesFile.toPath()), StandardCharsets.UTF_8);
                     final BufferedReader seedReader = new BufferedReader(new FileReader(file))
                ) {
                    int seedNum = MiniHunt.config.getInt("LevelSeedNum");
                    if (!file.exists()) {
                        Bukkit.shutdown();
                        return;
                    }
                    seed.set(seedReader.readLine());
                    System.out.println(seedReader.readLine());
                    for (int i = 0; i <= seedNum; i++) {
                        if (i == seedNum) {
                            seed.set(seedReader.readLine());
                        } else {
                            seedReader.readLine();
                        }
                    }
                    if (StringUtils.isEmpty(seed.get())) {
                        MiniHunt.getInstance().getLogger().info("种子行数配置错误! 将使用随机种子! ");
                        seedNum = -1;
                        seed.set("0");
                    } else {
                        MiniHunt.getInstance().getLogger().log(Level.INFO, "读取到新的种子：{0}", seed);
                    }
                    MiniHunt.config.set("LevelSeedNum", seedNum + 1);
                    MiniHunt.getInstance().saveConfig();
                    server.load(inputStreamReader);
                    for(Map.Entry<Object,Object> entry : server.entrySet()){
                        server.setProperty(entry.getKey().toString(),entry.getValue().toString());
                    }
                    server.setProperty("level-seed", seed.get());
                    server.store(outputStreamWriter, "properties,write:level-seed");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Bukkit.shutdown();
                }
                break;
            case 2:
                Bukkit.getScheduler().runTaskAsynchronously(JavaPlugin.getPlugin(MiniHunt.class), task -> {
                    MiniHunt.getInstance().getLogger().info("开始筛种...");
                    try (final OutputStreamWriter outputStreamWriter = new OutputStreamWriter(Files.newOutputStream(propertiesFile.toPath()), StandardCharsets.UTF_8)) {
                        seed.set(SeedFilter.getSeed());
                        if (!" ".equals(seed.get())) {
                            server.setProperty("level-seed", seed.get());
                            server.store(outputStreamWriter, "propeties,write:level-seed");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Bukkit.shutdown();
                    }
                });
                break;
            default:
                MiniHunt.getInstance().getLogger().info("种子获取方式配置错误! 将使用随机种子! ");
                Bukkit.shutdown();
        }
    }
}

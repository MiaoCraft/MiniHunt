package net.mcxk.minehunt;

import com.google.common.base.Charsets;
import lombok.Getter;
import net.mcxk.minehunt.commands.MineHuntCommand;
import net.mcxk.minehunt.game.ConstantCommand;
import net.mcxk.minehunt.game.Game;
import net.mcxk.minehunt.listener.*;
import net.mcxk.minehunt.placeholder.Placeholder;
import net.mcxk.minehunt.watcher.CountDownWatcher;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

public final class MineHunt extends JavaPlugin {
    @Getter
    private static MineHunt instance;
    @Getter
    private Game game;

    @Getter
    private CountDownWatcher countDownWatcher;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        instance = this;
        game = new Game();
        countDownWatcher = new CountDownWatcher();
        final PluginCommand minehuntCommand = this.getCommand(ConstantCommand.MINE_HUNT);
        if (Objects.nonNull(minehuntCommand)) {
            minehuntCommand.setExecutor(new MineHuntCommand());
        }
        Plugin pluginPlaceholderApi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (pluginPlaceholderApi != null) {
            getLogger().info("检测到PlaceHolderAPI插件，变量功能已启用！");
            new Placeholder(this).register();
        }
        Plugin pluginAdvancedReplay = Bukkit.getPluginManager().getPlugin("AdvancedReplay");
        if (pluginAdvancedReplay != null) {
            getLogger().info("检测到AdvancedReplay插件，回放功能已启用！");
        }
        game.switchWorldRuleForReady(false);
        Bukkit.getPluginManager().registerEvents(new PlayerServerListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerCompassListener(), this);
        Bukkit.getPluginManager().registerEvents(new ProgressDetectingListener(), this);
        Bukkit.getPluginManager().registerEvents(new GameWinnerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        final FileConfiguration config = getConfig();
        if (!config.getBoolean("LevelSeed")) {
            return;
        }
        final String serverPath = System.getProperty("user.dir");
        try {
            int seedNum = config.getInt("LevelSeedNum");
            final File file = new File(serverPath + "/plugins/MineHunt/seeds.txt");
            if (!file.exists()) {
                return;
            }
            getLogger().info(file.getAbsolutePath());
            final BufferedReader seedReader = new BufferedReader(new FileReader(file));
            String seed = "";
            for (int i = 0; i <= seedNum; i++) {
                if (i == seedNum) {
                    seed = seedReader.readLine();
                } else {
                    seedReader.readLine();
                }
            }
            if (StringUtils.isEmpty(seed)) {
                seed = "";
            }
            config.set("LevelSeedNum", seedNum + 1);
            config.save(serverPath + "/plugins/MineHunt/config.yml");
            final File propertiesFile = new File(serverPath + "/server.properties");
            final InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(propertiesFile), Charsets.UTF_8);
            Properties server = new Properties();
            server.load(inputStreamReader);
            getLogger().info("读取到新的种子：" + seed);
            server.setProperty("level-seed", seed);
            server.store(new OutputStreamWriter(new FileOutputStream(propertiesFile), StandardCharsets.UTF_8), "propeties,write:level-seed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

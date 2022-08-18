package net.mcxk.minihunt.replay;

import me.jumper251.replay.api.ReplayAPI;
import net.mcxk.minihunt.game.Game;
import org.bukkit.Bukkit;

import java.util.UUID;

public class GameRecord {
    public static final UUID ROUND_UNIQUE_ID = UUID.randomUUID();

    public static void record(net.mcxk.minihunt.game.Game game) {
        if (!Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            return;
        }
        ReplayAPI.getInstance().registerReplaySaver(new MHRecordSaver(game));
        ReplayAPI.getInstance().recordReplay(ROUND_UNIQUE_ID.toString().replace("-", ""), Bukkit.getConsoleSender());
    }

    public static void stop(Game game) {
        if (!Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            return;
        }
        ReplayAPI.getInstance().stopReplay(ROUND_UNIQUE_ID.toString().replace("-", ""), true);
    }
}

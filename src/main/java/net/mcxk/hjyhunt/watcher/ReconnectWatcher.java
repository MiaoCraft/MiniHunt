package net.mcxk.hjyhunt.watcher;

import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.game.GameStatus;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ReconnectWatcher {
    private final HJYHunt plugin = HJYHunt.getInstance();

    /**
     * 超时的判定时间（毫秒）
     */
    private final int reJoinTime = HJYHunt.getInstance().getConfig().getInt("reJoinTime") * 1000;

    public ReconnectWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
                    return;
                }
                List<Player> removing = new ArrayList<>();
                plugin.getGame().getReconnectTimer().forEach((key, value) -> {
                    if (System.currentTimeMillis() - value > reJoinTime) {
                        removing.add(key);
                    }
                });
                // 将超时未重连的玩家从队伍中移除
                removing.forEach(player -> {
                    plugin.getGame().getReconnectTimer().remove(player);
                    if (player.isOnline()) {
                        return;
                    }
                    plugin.getGame().playerLeft(player);
                });
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}

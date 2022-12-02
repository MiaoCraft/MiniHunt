package net.mcxk.minihunt.watcher;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.GameStatus;
import org.bukkit.scheduler.BukkitRunnable;

public class ReconnectWatcher {
    private final MiniHunt plugin = MiniHunt.getInstance();

    /**
     * 超时的判定时间（毫秒）
     */
    private final int reJoinTime = MiniHunt.getInstance().getConfig().getInt("reJoinTime") * 1000;

    public ReconnectWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
                    return;
                }
                // 将超时未重连的玩家从队伍中移除
                plugin.getGame().getReconnectTimer().forEach((key, value) -> {
                    if (System.currentTimeMillis() - value > reJoinTime) {
                        plugin.getGame().getReconnectTimer().remove(key);
                        if (key.isOnline()) {
                            return;
                        }
                        plugin.getGame().playerLeft(key);
                    }
                });
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}

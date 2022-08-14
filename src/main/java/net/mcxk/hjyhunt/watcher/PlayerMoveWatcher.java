package net.mcxk.hjyhunt.watcher;

import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class PlayerMoveWatcher {

    private final HJYHunt plugin = HJYHunt.getInstance();
    private boolean runnerNether = false;
    private boolean runnerTheEnd = false;

    public PlayerMoveWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                HJYHunt.getInstance().getGame().getInGamePlayers().forEach(player -> {
                    World.Environment environment = player.getWorld().getEnvironment();
                    if (environment != World.Environment.NORMAL) {
                        Optional<net.mcxk.hjyhunt.game.PlayerRole> role = HJYHunt.getInstance().getGame().getPlayerRole(player);
                        if (role.isPresent()) {
                            if (role.get() == PlayerRole.RUNNER) {
                                if (!runnerNether && environment == World.Environment.NETHER) {
                                    runnerNether = true;
                                    Bukkit.broadcastMessage(String.format("%s逃亡者 %s 已到达 下界 维度！", ChatColor.YELLOW, player.getName()));
                                    plugin.getGame().setFirstEnterNetherPlayer(player);
                                }
                                if (!runnerTheEnd && environment == World.Environment.THE_END) {
                                    runnerTheEnd = true;
                                    Bukkit.broadcastMessage(String.format("%s逃亡者 %s 已到达 末地 维度！", ChatColor.YELLOW, player.getName()));
                                }
                            }
                        }
                    }

                });
            }
        }.runTaskTimer(HJYHunt.getInstance(), 0, 80);
    }
}

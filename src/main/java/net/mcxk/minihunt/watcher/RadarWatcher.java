package net.mcxk.minihunt.watcher;

import lombok.Getter;
import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.game.PlayerRole;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class RadarWatcher {
    private final MiniHunt plugin = MiniHunt.getInstance();
    @Getter
    private final int warnDistance = plugin.getConfig().getInt("WarnDistance");
    @Getter
    private final int hunterWarnDistance = plugin.getConfig().getInt("HunterWarnDistance");

    private final String warnDistanceClose = plugin.getConfig().getString("WarnDistanceClose");

    private final String hunterWarnDistanceClose = plugin.getConfig().getString("HunterWarnDistanceClose");

    public RadarWatcher() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
                    return;
                }
                List<Player> runners = GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.RUNNER);
                List<Player> hunters = GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER);
                for (Player runner : runners) {
                    for (Player hunter : hunters) {
                        if (hunter.getWorld() != runner.getWorld() || runner.getGameMode() == GameMode.SPECTATOR) {
                            continue;
                        }
                        double distance = hunter.getLocation().distance(runner.getLocation());
                        if (distance < warnDistance && StringUtils.isNotEmpty(warnDistanceClose)) {
                            TextComponent textComponent = new TextComponent(String.format(warnDistanceClose, Math.round(distance)));
                            textComponent.setColor(ChatColor.RED);
                            runner.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
                        }
                        if (distance < hunterWarnDistance && StringUtils.isNotEmpty(hunterWarnDistanceClose)) {
                            TextComponent textComponent = new TextComponent(String.format(hunterWarnDistanceClose, hunterWarnDistance));
                            textComponent.setColor(ChatColor.RED);
                            hunter.spigot().sendMessage(ChatMessageType.ACTION_BAR, textComponent);
                        }
                    }
                }
            }
        }.runTaskTimer(MiniHunt.getInstance(), 0, 20);
    }
}

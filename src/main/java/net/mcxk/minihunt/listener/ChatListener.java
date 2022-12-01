package net.mcxk.minihunt.listener;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.game.PlayerRole;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;


public class ChatListener implements Listener {
    private final MiniHunt plugin = MiniHunt.getInstance();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent event) {
        if (MiniHunt.getInstance().getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        Optional<PlayerRole> role = MiniHunt.getInstance().getGame().getPlayerRole(event.getPlayer());
        if (!role.isPresent()) {
            event.setFormat(ChatColor.GRAY + plugin.getConfig().getString("ObserverName") + " " + event.getPlayer().getDisplayName() + " " + event.getMessage());
            return;
        }

        if (event.getMessage().startsWith("#")) {
            event.setCancelled(true);
            if (role.get() == PlayerRole.HUNTER) {
                GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.sendMessage(ChatColor.GRAY + "[TEAM] " + event.getPlayer().getDisplayName() + ": " + ChatColor.RESET + event.getMessage()));
            }
        } else {
            if (role.get() == PlayerRole.HUNTER) {
                event.setFormat(ChatColor.RED + plugin.getConfig().getString("HunterName") + " " + event.getPlayer().getDisplayName() + " " + ChatColor.RESET + event.getMessage());
            } else if (role.get() == PlayerRole.RUNNER) {
                event.setFormat(ChatColor.GREEN + plugin.getConfig().getString("RunnerName") + " " + event.getPlayer().getDisplayName() + " " + ChatColor.RESET + event.getMessage());
            }
        }
    }
}

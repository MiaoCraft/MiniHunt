package net.mcxk.hjyhunt.listener;

import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.game.GameStatus;
import net.mcxk.hjyhunt.game.PlayerRole;
import net.mcxk.hjyhunt.util.GetPlayerAsRole;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Optional;


public class ChatListener implements Listener {
    private final HJYHunt plugin = HJYHunt.getInstance();

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent event) {
        if (HJYHunt.getInstance().getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        Optional<net.mcxk.hjyhunt.game.PlayerRole> role = HJYHunt.getInstance().getGame().getPlayerRole(event.getPlayer());
        if (!role.isPresent()) {
            event.setFormat(ChatColor.GRAY + plugin.getConfig().getString("ObserverName") + " " + event.getPlayer().getDisplayName() + " " + event.getMessage());
            return;
        }

        if (event.getMessage().startsWith("#")) {
            event.setCancelled(true);
            if (role.get() == net.mcxk.hjyhunt.game.PlayerRole.HUNTER) {
                GetPlayerAsRole.getPlayersAsRole(net.mcxk.hjyhunt.game.PlayerRole.HUNTER).forEach(p -> p.sendMessage(ChatColor.GRAY + "[TEAM] " + event.getPlayer().getDisplayName() + ": " + ChatColor.RESET + event.getMessage()));
            }
        } else {
            if (role.get() == net.mcxk.hjyhunt.game.PlayerRole.HUNTER) {
                event.setFormat(ChatColor.RED + plugin.getConfig().getString("HunterName") + " " + event.getPlayer().getDisplayName() + " " + ChatColor.RESET + event.getMessage());
            } else if (role.get() == PlayerRole.RUNNER) {
                event.setFormat(ChatColor.GREEN + plugin.getConfig().getString("RunnerName") + " " + event.getPlayer().getDisplayName() + " " + ChatColor.RESET + event.getMessage());
            }
        }
    }
}

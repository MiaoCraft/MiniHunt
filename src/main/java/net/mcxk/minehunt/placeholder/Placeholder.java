package net.mcxk.minehunt.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Placeholder extends PlaceholderExpansion {

    private final MineHunt plugin;

    public Placeholder(MineHunt plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "Minehunt";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        Optional<PlayerRole> role = MineHunt.getInstance().getGame().getPlayerRole(player);
        if ("rule".equals(identifier) && role.isPresent()) {
            if (role.get() == PlayerRole.HUNTER) {
                return (ChatColor.RED + plugin.getConfig().getString("HunterName"));
            }
            if (role.get() == PlayerRole.RUNNER) {
                return (ChatColor.GREEN + plugin.getConfig().getString("RunnerName"));
            }
            if (role.get() == PlayerRole.WAITING) {
                return (ChatColor.GRAY + plugin.getConfig().getString("WaitingName"));
            }
            return (ChatColor.GRAY + plugin.getConfig().getString("ObserverName"));
        }
        return null;
    }
}




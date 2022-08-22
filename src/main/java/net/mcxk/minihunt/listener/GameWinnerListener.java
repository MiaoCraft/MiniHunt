package net.mcxk.minihunt.listener;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.game.GameStop;
import net.mcxk.minihunt.game.PlayerRole;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;
import java.util.Optional;

public class GameWinnerListener implements Listener {
    private final MiniHunt plugin = MiniHunt.getInstance();
    private String dragonKiller = "Magic";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void playerDeath(PlayerDeathEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        Optional<PlayerRole> role = plugin.getGame().getPlayerRole(event.getEntity());
        final Player killerPlayer = event.getEntity().getKiller();
        if (role.isPresent()) {
            final PlayerRole playerRole = role.get();
            if (playerRole == PlayerRole.RUNNER) {
                assert killerPlayer != null;
                try {
                    String finalKiller = killerPlayer.getName();
                    plugin.getGame().getGameEndingData().setRunnerKiller(finalKiller);
                } catch (Exception ignored) {
                    plugin.getGame().getGameEndingData().setRunnerKiller(null);
                }
                event.getEntity().setGameMode(GameMode.SPECTATOR);
                if (GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).stream().allMatch(p -> p.getGameMode() == GameMode.SPECTATOR)) {
                    GameStop.stop(PlayerRole.HUNTER, event.getEntity().getLocation().add(0, 3, 0));
                    // 避免玩家死亡
                    event.getEntity().setHealth(20);
                }
                // 逃亡者首次击杀猎人
            } else if (playerRole == PlayerRole.HUNTER
                    && Objects.nonNull(killerPlayer)
                    && Objects.isNull(plugin.getGame().getFirstKillPlayer())) {
                // 记录逃亡者首次击杀猎人
                plugin.getGame().setFirstKillPlayer(event.getEntity());
                Bukkit.broadcastMessage(String.format("%s逃亡者%s首次击杀猎人%s！", ChatColor.YELLOW, killerPlayer.getName(), event.getEntity().getName()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entityDeath(EntityDamageByEntityEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }
        if (event.getDamager() instanceof Player) {
            Optional<PlayerRole> role = MiniHunt.getInstance().getGame().getPlayerRole(((Player) event.getDamager()));
            if (role.isPresent() && role.get() == PlayerRole.HUNTER) {
                event.setCancelled(true);
                event.getEntity().sendMessage(ChatColor.RED + "猎人是末影龙的好伙伴，你不可以对龙造成伤害！");
                return;
            }
        }
        dragonKiller = event.getDamager().getName();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void entityDeath(EntityDamageByBlockEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }
        if (event.getDamager() == null) {
            return;
        }
        dragonKiller = event.getDamager().getType().name();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void entityDeath(EntityDeathEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        if (event.getEntityType() != EntityType.ENDER_DRAGON) {
            return;
        }
        plugin.getGame().getGameEndingData().setDragonKiller(dragonKiller);
        GameStop.stop(PlayerRole.RUNNER, new Location(event.getEntity().getLocation().getWorld(), 0, 85, 0));
    }
}

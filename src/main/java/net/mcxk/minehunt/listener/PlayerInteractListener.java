package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Objects;
import java.util.Optional;

/**
 * @author xiaolin
 */
public class PlayerInteractListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void clickXJB(PlayerInteractEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void damageXJB(EntityDamageEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void runXJB(FoodLevelChangeEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
        }
    }

    /**
     * 主要是统计展示的对队友输出最多的玩家
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void teamDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        if (event.getDamager() instanceof EnderDragon) {
            teamDamageEndDragon(event.getFinalDamage(), (Player) event.getDamager());
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player1 = (Player) event.getEntity();
        // 只处理玩家对玩家的伤害
        Player player2 = (Player) event.getDamager();
        if (player1.getUniqueId().equals(player2.getUniqueId())) {
            return;
        }

        Optional<PlayerRole> player1Role = plugin.getGame().getPlayerRole(player1);
        Optional<PlayerRole> player2Role = plugin.getGame().getPlayerRole(player2);
        if (player1Role.isPresent() && player2Role.isPresent()) {
            // 角色类型相同
            if (player1Role.get() != player2Role.get()) {
                if (player2Role.get() == PlayerRole.HUNTER
                        && Objects.isNull(plugin.getGame().getFirstTeamPlayer())) {
                    // 记录首次攻击到逃亡者玩家
                    plugin.getGame().setFirstTeamPlayer(player1);
                    Bukkit.broadcastMessage(String.format("%s猎人%s首次攻击到逃亡者%s！", ChatColor.YELLOW, player2.getName(), player1.getName()));
                }
                double historyDamage = plugin.getGame().getTeamDamageData().getOrDefault(player2, 0.0d);
                // 累计造成的伤害
                historyDamage += event.getFinalDamage();
                plugin.getGame().getTeamDamageData().put(player2, historyDamage);
            } else if (!plugin.getGame().isFriendsHurt()) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * 玩家对末影龙的伤害
     *
     * @param finalDamage 伤害数值
     * @param player      玩家
     */
    private void teamDamageEndDragon(double finalDamage, Player player) {
        final PlayerRole role = plugin.getGame().getRoleMapping().get(player);
        if (Objects.isNull(role) || role != PlayerRole.RUNNER) {
            return;
        }
        Double historyDamage = plugin.getGame()
                .getTeamDamageEndDragonData()
                .getOrDefault(player, 0.0D);
        plugin.getGame()
                .getTeamDamageEndDragonData().put(player, historyDamage + finalDamage);
    }


}

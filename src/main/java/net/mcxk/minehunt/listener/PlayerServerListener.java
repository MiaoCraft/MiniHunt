package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import net.mcxk.minehunt.util.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

/**
 * 监听玩家进入服务器
 */
public class PlayerServerListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();

    private final static BaseComponent[] SELECT_INTENTION_ROLE = new BaseComponent[]{
            Util.buildTextComponent("请选择您意向的角色：", false, ChatColor.YELLOW),
            Util.buildTextComponent("逃亡者", true, ChatColor.RED, "/minehunt want runner"),
            Util.buildTextComponent(" - ", false, ChatColor.WHITE),
            Util.buildTextComponent("猎人(默认)", true, ChatColor.GREEN, "/minehunt want hunter"),
            Util.buildTextComponent(" - ", false, ChatColor.WHITE),
            Util.buildTextComponent("观战", true, ChatColor.GRAY, "/minehunt want waiting"),
    };

    private final static BaseComponent[] CONFIRM_PREPARE = new BaseComponent[]{
            Util.buildTextComponent("请确认是否开始游戏：", false, ChatColor.YELLOW),
            Util.buildTextComponent("等待(默认)", true, ChatColor.RED, "/minehunt prepare false"),
            Util.buildTextComponent(" - ", false, ChatColor.WHITE),
            Util.buildTextComponent("准备", true, ChatColor.GREEN, "/minehunt prepare true"),
    };


    /**
     * 监听进入游戏的玩家
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (plugin.getGame().getStatus() == GameStatus.WAITING_PLAYERS) {
            if (plugin.getGame().playerJoining(player)) {
                plugin.getGame().getIntentionRoleMapping().put(player, PlayerRole.HUNTER);
                if (plugin.getGame().isSelectTeam()) {
                    player.spigot().sendMessage(SELECT_INTENTION_ROLE);
                }
                if (plugin.getGame().isConfirmPrepare()) {
                    plugin.getGame().getPlayerPrepare().put(player, false);
                    player.spigot().sendMessage(CONFIRM_PREPARE);
                    player.setGlowing(true);
                } else {
                    player.setGlowing(false);
                    plugin.getGame().getPlayerPrepare().put(player, true);
                }
                player.setGameMode(GameMode.ADVENTURE);
            } else {
                player.sendMessage("当前游戏已满人，已自动加入观战者队列");
                plugin.getGame().getIntentionRoleMapping().put(player, PlayerRole.WAITING);
            }
        } else {
            //处理玩家重连
            if (plugin.getGame().getInGamePlayers().stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()))) {
                plugin.getGame().getInGamePlayers().removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
                plugin.getGame().getInGamePlayers().add(player);

                for (Map.Entry<Player, PlayerRole> playerPlayerRoleEntry : plugin.getGame().getRoleMapping().entrySet()) {
                    if (playerPlayerRoleEntry.getKey().getUniqueId().equals(player.getUniqueId())) {
                        PlayerRole role = playerPlayerRoleEntry.getValue();
                        plugin.getGame().getRoleMapping().remove(playerPlayerRoleEntry.getKey());
                        plugin.getGame().getRoleMapping().put(player, role);
                        break;
                    }
                }

                if (plugin.getGame().getInGamePlayers().contains(player)) {
                    Bukkit.broadcastMessage(org.bukkit.ChatColor.GREEN + "玩家 " + player.getName() + " 已重新连接");
                    plugin.getGame().getReconnectTimer().entrySet().removeIf(set -> set.getKey().getUniqueId().equals(player.getUniqueId()));
                }

            } else {
                player.setGameMode(GameMode.SPECTATOR);
                player.sendMessage("游戏已经开始，您现在处于观战状态");
            }
        }
    }

    /**
     * 监听退出游戏的玩家
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        player.setGlowing(false);
        plugin.getGame().getIntentionRoleMapping().remove(player);
        plugin.getGame().getPlayerPrepare().remove(player);
        // 处理正在游戏的玩家
        if (plugin.getGame().getStatus().equals(GameStatus.GAME_STARTED)
                && plugin.getGame().getInGamePlayers().contains(player)) {
            plugin.getGame().playerLeaving(player);
        } else {
            plugin.getGame().getInGamePlayers().remove(player);
        }
    }

}

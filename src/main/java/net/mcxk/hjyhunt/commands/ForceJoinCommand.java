package net.mcxk.hjyhunt.commands;

import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.game.Game;
import net.mcxk.hjyhunt.util.GetPlayerAsRole;
import net.mcxk.hjyhunt.util.SendMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/11
 * @apiNote
 */
public class ForceJoinCommand {
    private ForceJoinCommand() {
    }

    public static boolean forceJoin(String type, CommandSender sender, Game game) {
        if (!sender.hasPermission(net.mcxk.hjyhunt.game.ConstantCommand.HJY_HUNT_ADMIN) || !(sender instanceof Player)) {
            SendMessage.sendMessage(String.format("%s你没有执行该命令的权限。", ChatColor.RED), sender);
            return true;
        }
        Player player = (Player) sender;
        game.getInGamePlayers().add(player);
        if (net.mcxk.hjyhunt.game.ConstantCommand.HUNTER.equalsIgnoreCase(type)) {
            GetPlayerAsRole.getRoleMapping().put(player, net.mcxk.hjyhunt.game.PlayerRole.HUNTER);
        } else {
            GetPlayerAsRole.getRoleMapping().put(player, net.mcxk.hjyhunt.game.PlayerRole.RUNNER);
        }
        player.setGameMode(GameMode.SURVIVAL);
        Bukkit.broadcastMessage(HJYHunt.messageHead + "玩家 " + sender.getName() + " 强制加入了游戏！ 身份：" + type);
        return true;
    }
}

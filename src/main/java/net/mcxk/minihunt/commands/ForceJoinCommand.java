package net.mcxk.minihunt.commands;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.ConstantCommand;
import net.mcxk.minihunt.game.Game;
import net.mcxk.minihunt.game.PlayerRole;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import net.mcxk.minihunt.util.SendMessage;
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
        if (!(sender instanceof Player)) {
            sender.sendMessage("请在游戏中输入此命令!");
            return true;
        }
        if (!sender.hasPermission(ConstantCommand.MINI_HUNT_ADMIN)) {
            SendMessage.sendMessage(String.format("%s你没有执行该命令的权限。", ChatColor.RED), sender);
            return true;
        }
        Player player = (Player) sender;
        game.getInGamePlayers().add(player);
        if (ConstantCommand.HUNTER.equalsIgnoreCase(type)) {
            GetPlayerAsRole.getRoleMapping().put(player, PlayerRole.HUNTER);
        } else {
            GetPlayerAsRole.getRoleMapping().put(player, PlayerRole.RUNNER);
        }
        player.setGameMode(GameMode.SURVIVAL);
        Bukkit.broadcastMessage(MiniHunt.messageHead + "玩家 " + sender.getName() + " 强制加入了游戏!  身份：" + type);
        return true;
    }
}

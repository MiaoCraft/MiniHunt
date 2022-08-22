package net.mcxk.minihunt.commands;

import net.mcxk.minihunt.game.ConstantCommand;
import net.mcxk.minihunt.game.Game;
import net.mcxk.minihunt.util.SendMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/11
 * @apiNote
 */
public class ForceStartCommand {
    private ForceStartCommand() {
    }

    public static boolean forceStart(CommandSender sender, Game game) {
        if (!sender.hasPermission(ConstantCommand.MINI_HUNT_ADMIN)) {
            SendMessage.sendMessage(String.format("%s你没有执行该命令的权限。", ChatColor.RED), sender);
            return true;
        }
        if (game.getInGamePlayers().isEmpty()) {
            SendMessage.sendMessage(String.format("%s游戏中没有玩家，无法开始游戏。", ChatColor.RED), sender);
            return true;
        }
        if (game.getStatus() != net.mcxk.minihunt.game.GameStatus.WAITING_PLAYERS) {
            SendMessage.sendMessage(String.format("%s游戏已开始！", ChatColor.RED), sender);
            return true;
        }
        game.start();
        return true;
    }
}

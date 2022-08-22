package net.mcxk.minihunt.commands;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.ConstantCommand;
import net.mcxk.minihunt.game.Game;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.util.SendMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/11
 * @apiNote
 */
public class ResetCDCommand {
    private ResetCDCommand() {
    }

    public static boolean resetCD(CommandSender sender, Game game) {
        if (!sender.hasPermission(ConstantCommand.MINI_HUNT_ADMIN)) {
            SendMessage.sendMessage(String.format("%s你没有执行该命令的权限。", ChatColor.RED), sender);
            return true;
        }
        if (game.getStatus() != GameStatus.WAITING_PLAYERS) {
            SendMessage.sendMessage(String.format("%s游戏未开始！", ChatColor.RED), sender);
            return true;
        }
        MiniHunt.getInstance().getCountDownWatcher().resetCountdown();
        return true;
    }
}

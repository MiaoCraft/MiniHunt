package net.mcxk.hjyhunt.commands;

import net.mcxk.hjyhunt.game.Game;
import net.mcxk.hjyhunt.util.SendMessage;
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
        if (!sender.hasPermission(net.mcxk.hjyhunt.game.ConstantCommand.HJY_HUNT_ADMIN)) {
            SendMessage.sendMessage(String.format("%s你没有执行该命令的权限。", ChatColor.RED), sender);
            return true;
        }
        if (game.getStatus() != net.mcxk.hjyhunt.game.GameStatus.WAITING_PLAYERS) {
            SendMessage.sendMessage(String.format("%s游戏未开始！", ChatColor.RED), sender);
            return true;
        }
        net.mcxk.hjyhunt.HJYHunt.getInstance().getCountDownWatcher().resetCountdown();
        return true;
    }
}

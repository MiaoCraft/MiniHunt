package net.mcxk.minihunt.commands;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.ConstantCommand;
import net.mcxk.minihunt.game.Game;
import net.mcxk.minihunt.util.SendMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author LingMuQingYu, Crsuh2er0
 * @since 2022/8/10 21:53
 */
public class MiniHuntCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length < 1) {
            return false;
        }
        final MiniHunt miniHunt = MiniHunt.getInstance();
        final Game game = miniHunt.getGame();

        switch (args[0]) {
            case ConstantCommand.ABOUT:
                AboutCommand.about(sender);
                return true;
            case ConstantCommand.HELP:
                HelpCommand.help(sender);
                return true;
            case ConstantCommand.WANT:
                return ChangeRoleCommand.changeRole(sender, args[1], game);
            case ConstantCommand.HUNTER:
            case ConstantCommand.RUNNER:
                return ForceJoinCommand.forceJoin(args[0], sender, game);
            case ConstantCommand.RESET_COUNTDOWN:
                return ResetCDCommand.resetCD(sender, game);
            case ConstantCommand.PLAYERS:
                return GetPlayersCommand.getPlayers(game, sender);
            case ConstantCommand.FORCE_START:
                return ForceStartCommand.forceStart(sender, game);
            default:
                SendMessage.sendMessage(String.format("%s未知命令！输入/minihunt help查看指令列表。", ChatColor.RED), sender);
                return true;
        }
    }
}

package net.mcxk.minihunt.commands;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.Game;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.game.PlayerRole;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import net.mcxk.minihunt.util.SendMessage;
import net.mcxk.minihunt.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/11
 * @apiNote
 */
public class GetPlayersCommand {
    private GetPlayersCommand() {
    }

    public static boolean getPlayers(@NotNull Game game, CommandSender sender) {
        if (game.getStatus() != GameStatus.GAME_STARTED) {
            SendMessage.sendMessage(String.format("%s游戏未开始! ", ChatColor.RED), sender);
        } else {
            getPlayersMethod(sender);
        }
        return true;
    }

    public static void getPlayersMethod(@NotNull CommandSender sender) {
        sender.sendMessage(MiniHunt.messageHead + ChatColor.YELLOW + ">猎人AND逃亡者<");
        sender.sendMessage(ChatColor.RED + "猎人: " + GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList()));
        sender.sendMessage(ChatColor.GREEN + "逃亡者: " + Util.list2String(GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList())));
    }
}

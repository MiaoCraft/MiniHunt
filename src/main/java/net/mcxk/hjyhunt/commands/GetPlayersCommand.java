package net.mcxk.hjyhunt.commands;

import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.game.Game;
import net.mcxk.hjyhunt.game.PlayerRole;
import net.mcxk.hjyhunt.util.GetPlayerAsRole;
import net.mcxk.hjyhunt.util.SendMessage;
import net.mcxk.hjyhunt.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/11
 * @apiNote
 */
public class GetPlayersCommand {
    private GetPlayersCommand() {
    }

    public static boolean getPlayers(Game game, CommandSender sender) {
        if (game.getStatus() != net.mcxk.hjyhunt.game.GameStatus.WAITING_PLAYERS) {
            SendMessage.sendMessage(String.format("%s游戏未开始！", ChatColor.RED), sender);
            return true;
        }
        Bukkit.broadcastMessage(HJYHunt.messageHead + ChatColor.YELLOW + ">猎人AND逃亡者<");
        Bukkit.broadcastMessage(ChatColor.RED + "猎人: " + GetPlayerAsRole.getPlayersAsRole(net.mcxk.hjyhunt.game.PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList()));
        Bukkit.broadcastMessage(ChatColor.GREEN + "逃亡者: " + Util.list2String(GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList())));
        return true;
    }

}

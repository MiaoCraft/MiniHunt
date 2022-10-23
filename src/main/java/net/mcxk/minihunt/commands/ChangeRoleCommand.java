package net.mcxk.minihunt.commands;

import net.mcxk.minihunt.game.ConstantCommand;
import net.mcxk.minihunt.game.Game;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.game.PlayerRole;
import net.mcxk.minihunt.util.SendMessage;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/11
 * @apiNote
 */
public class ChangeRoleCommand {
    private ChangeRoleCommand() {
    }

    public static boolean changeRole(CommandSender sender, String arg, @NotNull Game game) {
        // 只有在游戏未开始时才能改变角色
        if (!GameStatus.WAITING_PLAYERS.equals(game.getStatus()) || !(sender instanceof Player)) {
            SendMessage.sendMessage(String.format("%s游戏已开始！", ChatColor.RED), sender);
            return true;
        }
        Player player = (Player) sender;
        final PlayerRole playerRole = game.getIntentionRoleMapping().get(player);
        // 判断由观战转为游戏玩家时，如果游戏人数已经达到最大游戏人数，不能变更游戏模式
        if (PlayerRole.WAITING.equals(playerRole) &&
                !ConstantCommand.WAITING.equals(arg) &&
                game.getInGamePlayers().size() >= game.getMaxPlayers()) {
            SendMessage.sendMessage(String.format("当前游戏已满人，只能选择%s观战%s队列！", ChatColor.GRAY, ChatColor.WHITE), sender);
            return true;
        }
        switch (arg) {
            case ConstantCommand.HUNTER:
                game.getIntentionRoleMapping().put(player, PlayerRole.HUNTER);
                game.getInGamePlayers().add(player);
                SendMessage.sendMessage(String.format("您选择了%s猎人%s！", ChatColor.GREEN, ChatColor.WHITE), sender);
                SendMessage.sendMessage("当意愿猎人人数过多时，将从中随机抽取部分逃亡者！", sender);
                return true;
            case ConstantCommand.RUNNER:
                game.getIntentionRoleMapping().put(player, PlayerRole.RUNNER);
                game.getInGamePlayers().add(player);
                SendMessage.sendMessage(String.format("您选择了%s逃亡者%s！", ChatColor.RED, ChatColor.WHITE), sender);
                SendMessage.sendMessage("当意愿逃亡者人数过多时，将从中随机抽取部分猎人！", sender);
                return true;
            case ConstantCommand.WAITING:
                game.getIntentionRoleMapping().put(player, PlayerRole.WAITING);
                game.getInGamePlayers().remove(player);
                SendMessage.sendMessage(String.format("您选择了%s观战%s！", ChatColor.GRAY, ChatColor.WHITE), sender);
                SendMessage.sendMessage("您将不会参与到游戏中！", sender);
                return true;
            default:
                SendMessage.sendMessage(String.format("%s未知命令！用法：/minihunt want <runner/hunter/waiting>", ChatColor.RED), sender);
                return true;
        }
    }
}

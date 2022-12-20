package net.mcxk.minihunt.game;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.util.GameEnd;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/12
 * @apiNote
 */
public class LeaveEnding {
    static Game game = new Game();

    private LeaveEnding() {
    }

    public static void leaveEnd(@NotNull PlayerRole winner) {
        switch (winner) {
            case HUNTER:
                for (Player p : GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER)) {
                    GetPlayerAsRole.getRoleMapping().remove(p);
                    GetPlayerAsRole.getRoleMapping().put(p, PlayerRole.WAITING);
                }
                break;
            case RUNNER:
                for (Player p : GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER)) {
                    GetPlayerAsRole.getRoleMapping().remove(p);
                    GetPlayerAsRole.getRoleMapping().put(p, PlayerRole.WAITING);
                }
                break;
            default:
        }
        if (game.isJudgeWinnerWhenLeaveEnd()) {
            GameStop.stop(winner);
        } else {
            Bukkit.broadcastMessage("由于比赛的一方所有人全部掉线，游戏被迫终止。");
            Bukkit.broadcastMessage("服务器将会在 60 秒钟后重新启动。");
            Bukkit.getScheduler().runTaskLater(MiniHunt.getInstance(), GameEnd::startEnd, 20);
        }
    }
}

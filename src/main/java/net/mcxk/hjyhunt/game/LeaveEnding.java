package net.mcxk.hjyhunt.game;

import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.util.GameEnd;
import org.bukkit.Bukkit;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/12
 * @apiNote
 */
public class LeaveEnding {
    static Game game = new Game();

    private LeaveEnding() {
    }

    public static void leaveEnd(PlayerRole winner) {
        if (game.isJudgeWinnerWhenLeaveEnd()) {
            GameStop.stop(winner);
        }
        Bukkit.broadcastMessage("由于比赛的一方所有人全部掉线，游戏被迫终止。");
        Bukkit.broadcastMessage("服务器将会在 60 秒钟后重新启动。");
        Bukkit.getScheduler().runTaskLater(HJYHunt.getInstance(), GameEnd::startEnd, 20);
    }
}

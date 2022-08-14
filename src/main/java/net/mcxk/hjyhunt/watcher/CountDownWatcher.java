package net.mcxk.hjyhunt.watcher;

import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.game.Game;
import net.mcxk.hjyhunt.game.GameStatus;
import net.mcxk.hjyhunt.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;

/**
 * @author xiaolin
 */
public class CountDownWatcher {
    private static final int SHORTER = 5;

    private final EnumMap<PlayerRole, String> roleNameMap = new EnumMap<>(net.mcxk.hjyhunt.game.PlayerRole.class);
    private final BukkitTask bukkitTask;
    private int remains = HJYHunt.getInstance().getGame().getCountdown();

    public CountDownWatcher() {
        roleNameMap.put(net.mcxk.hjyhunt.game.PlayerRole.HUNTER, ChatColor.GREEN + "猎人");
        roleNameMap.put(net.mcxk.hjyhunt.game.PlayerRole.RUNNER, ChatColor.RED + "逃亡者");
        roleNameMap.put(net.mcxk.hjyhunt.game.PlayerRole.WAITING, ChatColor.GRAY + "旁观");
        bukkitTask = Bukkit.getScheduler().runTaskTimer(HJYHunt.getInstance(), new CountDownWatcherRunnable(), 0, 20);
    }

    public void resetCountdown() {
        this.remains = HJYHunt.getInstance().getGame().getCountdown();
        Bukkit.broadcastMessage("倒计时已被重置");
    }

    private class CountDownWatcherRunnable implements Runnable {
        @Override
        public void run() {
            Game game = HJYHunt.getInstance().getGame();
            if (game.getStatus() != GameStatus.WAITING_PLAYERS) {
                Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
            }

            if (game.getInGamePlayers().size() < game.getMinPlayers()) {
                String title;
                title = ChatColor.AQUA + "" + game.getInGamePlayers().size() + " " + ChatColor.WHITE + "/ " + ChatColor.AQUA + game.getMinPlayers();
                Bukkit.getOnlinePlayers().forEach(p -> {
                    final net.mcxk.hjyhunt.game.PlayerRole playerRole = game.getIntentionRoleMapping().get(p);
                    if (net.mcxk.hjyhunt.game.PlayerRole.WAITING.equals(playerRole)) {
                        p.sendTitle(title, String.format("正在等待更多玩家加入游戏....[%s旁观%s]", ChatColor.GRAY, ChatColor.WHITE), 0, 40, 0);
                    } else {
                        String subtitle = String.format("正在等待更多玩家加入游戏....[%s%s]",
                                roleNameMap.get(game.getIntentionRoleMapping().get(p)),
                                ChatColor.WHITE);
                        p.sendTitle(title, subtitle, 0, 40, 0);
                    }
                });
                remains = HJYHunt.getInstance().getGame().getCountdown();
            } else {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    final int size = game.getInGamePlayers().size();
                    String subTitle = String.format("游戏即将开始... [%s/%s]", size, game.getMaxPlayers());
                    if (game.isConfirmPrepare()) {
                        subTitle = String.format("所有玩家准备就绪，游戏即将开始... [%s/%s]", size, size);
                    }
                    p.sendTitle(ChatColor.GOLD.toString() + remains, subTitle, 0, 40, 0);
                    p.playSound(p.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1.0f, 1.0f);
                });
                remains--;
            }

            if (remains < 0) {
                Bukkit.getOnlinePlayers().forEach(p ->
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f));
                game.start();
                Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
            }

            if (!game.isConfirmPrepare() && game.getInGamePlayers().size() >= game.getMaxPlayers() && remains > SHORTER) {
                Bukkit.broadcastMessage("玩家到齐，倒计时缩短！");
            }
        }
    }
}

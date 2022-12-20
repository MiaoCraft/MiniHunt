package net.mcxk.minihunt.watcher;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.Game;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.game.PlayerRole;
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

    private final EnumMap<PlayerRole, String> roleNameMap = new EnumMap<>(PlayerRole.class);
    private final BukkitTask bukkitTask;
    private int remains = MiniHunt.getInstance().getGame().getCountdown();

    public CountDownWatcher() {
        roleNameMap.put(PlayerRole.HUNTER, ChatColor.RED + "猎人");
        roleNameMap.put(PlayerRole.RUNNER, ChatColor.GREEN + "逃亡者");
        roleNameMap.put(PlayerRole.WAITING, ChatColor.GRAY + "旁观");
        bukkitTask = Bukkit.getScheduler().runTaskTimer(MiniHunt.getInstance(), new CountDownWatcherRunnable(), 0, 20);
    }

    public void resetCountdown() {
        this.remains = MiniHunt.getInstance().getGame().getCountdown();
        Bukkit.broadcastMessage("倒计时已被重置");
    }

    private class CountDownWatcherRunnable implements Runnable {
        @Override
        public void run() {
            Game game = MiniHunt.getInstance().getGame();
            if (game.getStatus() != GameStatus.WAITING_PLAYERS) {
                Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
            }
            String title = ChatColor.AQUA + "" + game.getInGamePlayers().size() + " " + ChatColor.WHITE + "/ " + ChatColor.AQUA + game.getMinPlayers();
            if (game.getInGamePlayers().size() < game.getMinPlayers()) {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    final PlayerRole playerRole = game.getIntentionRoleMapping().get(p);
                    if (PlayerRole.WAITING.equals(playerRole)) {
                        p.sendTitle(title, String.format("正在等待更多玩家加入游戏....[%s旁观%s]", ChatColor.GRAY, ChatColor.WHITE), 0, 40, 0);
                    } else {
                        String subtitle = String.format("正在等待更多玩家加入游戏....[%s%s]",
                                roleNameMap.get(game.getIntentionRoleMapping().get(p)),
                                ChatColor.WHITE);
                        p.sendTitle(title, subtitle, 0, 40, 0);
                    }
                });
                remains = MiniHunt.getInstance().getGame().getCountdown();
            } else {
                Bukkit.getOnlinePlayers().forEach(p -> {
                    final PlayerRole playerRole = game.getIntentionRoleMapping().get(p);
                    final int size = game.getInGamePlayers().size();
                    String subTitle;
                    if (PlayerRole.WAITING.equals(playerRole)) {
                        subTitle = String.format("游戏即将开始....[%s/%s][%s旁观%s]", size, game.getMaxPlayers(), ChatColor.GRAY, ChatColor.WHITE);
                    } else if (PlayerRole.HUNTER.equals(playerRole)) {
                        subTitle = String.format("游戏即将开始....[%s/%s][%s猎人%s]", size, game.getMaxPlayers(), ChatColor.RED, ChatColor.WHITE);
                    } else {
                        subTitle = String.format("游戏即将开始....[%s/%s][%s逃亡者%s]", size, game.getMaxPlayers(), ChatColor.GREEN, ChatColor.WHITE);
                    }
                    p.sendTitle(title, subTitle, 0, 40, 0);
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
                Bukkit.broadcastMessage("玩家到齐，倒计时缩短! ");
            }
        }
    }
}

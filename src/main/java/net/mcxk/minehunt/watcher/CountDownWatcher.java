package net.mcxk.minehunt.watcher;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.Game;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xiaolin
 */
public class CountDownWatcher {
    private int remains = MineHunt.getInstance().getGame().getCountdown();
    private final Map<PlayerRole, String> roleNameMap = new HashMap<>();
    private final Map<Boolean, String> prepareMap = new HashMap<>();
    private final BukkitTask bukkitTask;
    private final static int SHORTER = 5;

    public CountDownWatcher() {
        roleNameMap.put(PlayerRole.HUNTER, ChatColor.GREEN + "猎人");
        roleNameMap.put(PlayerRole.RUNNER, ChatColor.RED + "逃亡者");
        roleNameMap.put(PlayerRole.WAITING, ChatColor.GRAY + "旁观");
        prepareMap.put(false, ChatColor.RED + "未准备");
        prepareMap.put(true, ChatColor.GREEN + "准备就绪");
        bukkitTask = Bukkit.getScheduler().runTaskTimer(MineHunt.getInstance(), new CountDownWatcherRunnable(), 0, 20);
    }

    private class CountDownWatcherRunnable implements Runnable {
        @Override
        public void run() {
            Game game = MineHunt.getInstance().getGame();
            if (game.getStatus() != GameStatus.WAITING_PLAYERS) {
                Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
            }
            if (remains <= 0) {
                Bukkit.getOnlinePlayers().forEach(p ->
                        p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f));
                game.start();
                Bukkit.getScheduler().cancelTask(bukkitTask.getTaskId());
            }
            if (game.getInGamePlayers().size() < game.getMinPlayers()) {
                String title;
                if (game.isConfirmPrepare()) {
                    title = ChatColor.AQUA + "" + game.getPlayerPrepare().values().stream().filter(r -> r).count() + " " + ChatColor.WHITE + "/ " + ChatColor.AQUA + game.getInGamePlayers().size();
                } else {
                    title = ChatColor.AQUA + "" + game.getInGamePlayers().size() + " " + ChatColor.WHITE + "/ " + ChatColor.AQUA + game.getMinPlayers();
                }
                Bukkit.getOnlinePlayers().forEach(p -> {
                    final PlayerRole playerRole = game.getIntentionRoleMapping().get(p);
                    if (PlayerRole.WAITING.equals(playerRole)) {
                        p.sendTitle(title, String.format("正在等待更多玩家加入游戏....[%s旁观]", ChatColor.WHITE), 0, 40, 0);
                    } else {
                        String subtitle = String.format("正在等待更多玩家加入游戏....[%s%s][%s%s]",
                                prepareMap.get(game.getPlayerPrepare().get(p)),
                                ChatColor.WHITE,
                                roleNameMap.get(game.getIntentionRoleMapping().get(p)),
                                ChatColor.WHITE);
                        p.sendTitle(title, subtitle, 0, 40, 0);
                    }
                });
                remains = MineHunt.getInstance().getGame().getCountdown();
            } else if (game.isConfirmPrepare() && !game.getPlayerPrepare().values().stream().allMatch(r -> r)) {
                String title = ChatColor.AQUA + "" + game.getPlayerPrepare().values().stream().filter(r -> r).count() + " " + ChatColor.WHITE + "/ " + ChatColor.AQUA + game.getInGamePlayers().size();
                Bukkit.getOnlinePlayers().forEach(p -> {
                    final PlayerRole playerRole = game.getIntentionRoleMapping().get(p);
                    if (PlayerRole.WAITING.equals(playerRole)) {
                        p.sendTitle(title, String.format("正在等待玩家准备就绪....[%s旁观]", ChatColor.WHITE), 0, 40, 0);
                    } else {
                        final Boolean prepare = game.getPlayerPrepare().get(p);
                        String subtitle = String.format(prepare ? "正在等待玩家准备就绪....[%s%s][%s%s]" : "请确认准备....[%s%s][%s%s]",
                                prepareMap.get(prepare),
                                ChatColor.WHITE,
                                roleNameMap.get(playerRole),
                                ChatColor.WHITE);
                        p.sendTitle(title, subtitle, 0, 40, 0);
                    }
                });
                remains = MineHunt.getInstance().getGame().getCountdown();
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
            if (!game.isConfirmPrepare() && game.getInGamePlayers().size() >= game.getMaxPlayers()) {
                if (remains > SHORTER) {
                    remains = SHORTER;
                    Bukkit.broadcastMessage("玩家到齐，倒计时缩短！");
                }
            }
        }
    }

    public void resetCountdown() {
        this.remains = MineHunt.getInstance().getGame().getCountdown();
        Bukkit.broadcastMessage("倒计时已被重置");
    }
}

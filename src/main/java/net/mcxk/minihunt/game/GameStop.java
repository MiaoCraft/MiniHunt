package net.mcxk.minihunt.game;

import lombok.Getter;
import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.replay.GameRecord;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import net.mcxk.minihunt.util.MusicPlayer;
import net.mcxk.minihunt.util.StatisticsBaker;
import net.mcxk.minihunt.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/12
 * @apiNote
 */
public class GameStop {
    static MiniHunt plugin = MiniHunt.getInstance();
    @Getter
    static Game game = new Game();

    private GameStop() {
    }

    public static void stop(PlayerRole winner, Location location) {
        game.getInGamePlayers().stream().filter(Player::isOnline).forEach(player -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(location.clone().add(0, 3, 0));
            player.teleport(Util.lookAt(player.getEyeLocation(), location));
        });
        game.setStatus(GameStatus.ENDED);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "游戏结束! 服务器将在 60 秒后重新启动！");
        String runnerNames = Util.list2String(GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList()));
        String hunterNames = Util.list2String(GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList()));

        if (winner == PlayerRole.HUNTER) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "胜利者：猎人");
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "恭喜：" + hunterNames);
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).forEach(player -> player.sendTitle(ChatColor.GOLD + "胜利", "成功击败了逃亡者", 0, 2000, 0));
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).forEach(player -> player.sendTitle(ChatColor.RED + "游戏结束", "不幸阵亡", 0, 2000, 0));
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + "胜利者：逃亡者");
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "恭喜：" + runnerNames);
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).forEach(player -> player.sendTitle(ChatColor.GOLD + "胜利", "成功战胜了末影龙", 0, 2000, 0));
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).forEach(player -> player.sendTitle(ChatColor.RED + "游戏结束", "未能阻止末影龙死亡", 0, 2000, 0));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            try {
                GameRecord.stop(game);
                Bukkit.broadcastMessage("游戏录制已保存：" + GameRecord.ROUND_UNIQUE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            new MusicPlayer().playEnding();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bukkit.getOnlinePlayers().stream().filter(p -> !game.getInGamePlayers().contains(p)).forEach(p -> p.sendTitle(ChatColor.RED + "游戏结束", "The End", 0, 2000, 0));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            // 开始结算阶段
            StatisticsBaker baker = new StatisticsBaker();
            // 计算输出最多的玩家
            game.getGameEndingData().setDamageOutput(baker.getDamageMaster());
            game.getGameEndingData().setDamageReceive(baker.getDamageTakenMaster());
            game.getGameEndingData().setWalkMaster(baker.getWalkingMaster());
            game.getGameEndingData().setJumpMaster(baker.getJumpMaster());
            game.getGameEndingData().setTeamKiller(baker.getTeamBadGuy());
            game.getGameEndingData().setTeamEndDragon(baker.getTeamEndDragonBadGuy());
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, game::sendEndingAnimation, (long) 20 * 10);
        }, (long) 20 * 10);
    }

    public static void stop(PlayerRole winner) {
        game.getInGamePlayers().stream().filter(Player::isOnline).forEach(player -> player.setGameMode(GameMode.SPECTATOR));
        game.setStatus(GameStatus.ENDED);
        Bukkit.broadcastMessage(ChatColor.YELLOW + "游戏结束! 服务器将在 60 秒后重新启动！");
        String runnerNames = Util.list2String(GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList()));
        String hunterNames = Util.list2String(GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList()));

        if (winner == PlayerRole.HUNTER) {
            Bukkit.broadcastMessage(ChatColor.GOLD + "胜利者：猎人");
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "恭喜：" + hunterNames);
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).forEach(player -> player.sendTitle(ChatColor.GOLD + "胜利", "成功击败了逃亡者", 0, 2000, 0));
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).forEach(player -> player.sendTitle(ChatColor.RED + "游戏结束", "不幸阵亡", 0, 2000, 0));
        } else {
            Bukkit.broadcastMessage(ChatColor.GOLD + "胜利者：逃亡者");
            Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "恭喜：" + runnerNames);
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).forEach(player -> player.sendTitle(ChatColor.GOLD + "胜利", "成功战胜了末影龙", 0, 2000, 0));
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).forEach(player -> player.sendTitle(ChatColor.RED + "游戏结束", "未能阻止末影龙死亡", 0, 2000, 0));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            try {
                GameRecord.stop(game);
                Bukkit.broadcastMessage("游戏录制已保存：" + GameRecord.ROUND_UNIQUE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            new MusicPlayer().playEnding();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bukkit.getOnlinePlayers().stream().filter(p -> !game.getInGamePlayers().contains(p)).forEach(p -> p.sendTitle(ChatColor.RED + "游戏结束", "The End", 0, 2000, 0));
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            // 开始结算阶段
            StatisticsBaker baker = new StatisticsBaker();
            // 计算输出最多的玩家
            game.getGameEndingData().setDamageOutput(baker.getDamageMaster());
            game.getGameEndingData().setDamageReceive(baker.getDamageTakenMaster());
            game.getGameEndingData().setWalkMaster(baker.getWalkingMaster());
            game.getGameEndingData().setJumpMaster(baker.getJumpMaster());
            game.getGameEndingData().setTeamKiller(baker.getTeamBadGuy());
            game.getGameEndingData().setTeamEndDragon(baker.getTeamEndDragonBadGuy());
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, game::sendEndingAnimation, (long) 20 * 10);
        }, (long) 20 * 10);
    }
}

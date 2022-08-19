package net.mcxk.minihunt.game;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.replay.GameRecord;
import net.mcxk.minihunt.util.GameEnd;
import net.mcxk.minihunt.util.GameEndingData;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import net.mcxk.minihunt.util.Util;
import net.mcxk.minihunt.watcher.PlayerMoveWatcher;
import net.mcxk.minihunt.watcher.RadarWatcher;
import net.mcxk.minihunt.watcher.ReconnectWatcher;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Game {
    /**
     * 团队内伤害数据
     */
    @Getter
    private final Map<Player, Double> teamDamageData = Maps.newConcurrentMap();
    /**
     * 逃亡者攻击末影龙伤害数据
     */
    @Getter
    private final Map<Player, Double> teamDamageEndDragonData = Maps.newConcurrentMap();
    private final MiniHunt plugin = MiniHunt.getInstance();
    /**
     * 正在进行游戏的玩家
     * 线程安全
     */
    @Getter
    private final Set<Player> inGamePlayers = Sets.newCopyOnWriteArraySet();
    /**
     * 玩家重连计时器
     */
    @Getter
    private final Map<Player, Long> reconnectTimer = Maps.newConcurrentMap();
    /**
     * 玩家重连计数器
     */
    @Getter
    private final Map<Player, Byte> reconnectCount = Maps.newConcurrentMap();
    /**
     * 进度管理器
     */
    @Getter
    private final net.mcxk.minihunt.game.GameProgressManager progressManager = new GameProgressManager();
    /**
     * 结算统计信息
     */
    @Getter
    private final GameEndingData gameEndingData = new GameEndingData();
    private final Map<World, Difficulty> difficultyMap = Maps.newConcurrentMap();
    /**
     * 玩家意向角色
     * 线程安全
     */
    @Getter
    private final Map<Player, net.mcxk.minihunt.game.PlayerRole> intentionRoleMapping = Maps.newConcurrentMap();
    /**
     * 是否游戏结束后踢出所有玩家
     */
    @Getter
    private final boolean autoKick = plugin.getConfig().getBoolean("AutoKick");
    /**
     * 是否允许选择队伍
     */
    @Getter
    private final boolean selectTeam = plugin.getConfig().getBoolean("SelectTeam");
    /**
     * 是否需要手动确认准备
     */
    @Getter
    private final boolean confirmPrepare = plugin.getConfig().getBoolean("ConfirmPrepare");
    /**
     * 最小准备就绪玩家数量（-1所有）
     */
    @Getter
    private final int countdown = plugin.getConfig().getInt("Countdown");
    private final int L0Player = plugin.getConfig().getInt("L0Player");
    private final int minPlayers = L0Player;
    private final int L0Runner = plugin.getConfig().getInt("L0Runner");
    private final int L1Player = plugin.getConfig().getInt("L1Player");
    private final int L1Runner = plugin.getConfig().getInt("L1Runner");
    private final int L2Player = plugin.getConfig().getInt("L2Player");
    private final int L2Runner = plugin.getConfig().getInt("L2Runner");
    private final int L3Player = plugin.getConfig().getInt("L3Player");
    @Getter
    private final int maxPlayers = L3Player;
    private final int L3Runner = plugin.getConfig().getInt("L3Runner");
    private final int XRandom = plugin.getConfig().getInt("XRandom");
    private final int XBasic = plugin.getConfig().getInt("XBasic");
    private final int YRandom = plugin.getConfig().getInt("YRandom");
    private final int YBasic = plugin.getConfig().getInt("YBasic");
    @Getter
    private final boolean friendsHurt = plugin.getConfig().getBoolean("FriendsHurt");
    private final boolean endWhenAllLeave = plugin.getConfig().getBoolean("endWhenAllLeave");
    @Getter
    private final boolean judgeWinnerWhenLeaveEnd = plugin.getConfig().getBoolean("judgeWinnerWhenLeaveEnd");
    Random random = new Random();
    /**
     * 首次攻击到逃亡者玩家
     */
    @Getter
    @Setter
    private Player firstTeamPlayer = null;
    /**
     * 首次击杀到猎人玩家
     */
    @Getter
    @Setter
    private Player firstKillPlayer = null;
    /**
     * 首次获得全套铁甲猎人
     */
    @Getter
    @Setter
    private Player firstAllArmourPlayer = null;
    /**
     * 首次进入地狱的逃亡者
     */
    @Getter
    @Setter
    private Player firstEnterNetherPlayer = null;
    @Getter
    @Setter
    private net.mcxk.minihunt.game.GameStatus status = net.mcxk.minihunt.game.GameStatus.WAITING_PLAYERS;
    /**
     * 玩家角色
     * 线程安全
     */
    @Getter
    private boolean compassUnlocked = plugin.getConfig().getBoolean("CompassUnlocked");

    public void switchCompass(boolean unlocked) {
        if (this.compassUnlocked == unlocked) {
            return;
        }
        this.compassUnlocked = unlocked;
        if (unlocked) {
            GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.HUNTER).forEach(p -> p.getInventory().addItem(new ItemStack(Material.COMPASS, 1)));
            Bukkit.broadcastMessage(ChatColor.YELLOW + "猎人已解锁追踪指南针！逃亡者的位置已经暴露！");
        } else {
            GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.HUNTER).forEach(p -> p.getInventory().remove(Material.COMPASS));
            Bukkit.broadcastMessage(ChatColor.YELLOW + "猎人的追踪指南针被破坏失效，需要重新解锁！");
        }
        // 清除合成的指南针
        GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.RUNNER).forEach(p -> p.getInventory().remove(Material.COMPASS));
    }

    /**
     * 获取玩家角色
     *
     * @param player 玩家
     * @return 可能是Empty（玩家不属于游戏中的玩家）否则返回玩家角色
     */
    public Optional<net.mcxk.minihunt.game.PlayerRole> getPlayerRole(Player player) {
        if (status == net.mcxk.minihunt.game.GameStatus.WAITING_PLAYERS) {
            return Optional.of(net.mcxk.minihunt.game.PlayerRole.WAITING);
        }
        if (!GetPlayerAsRole.getRoleMapping().containsKey(player)) {
            return Optional.empty();
        }
        return Optional.of(GetPlayerAsRole.getRoleMapping().get(player));
    }

    public boolean playerJoining(Player player) {
        if (status != GameStatus.WAITING_PLAYERS && (Objects.equals(getPlayerRole(player), Optional.of(PlayerRole.RUNNER)) || Objects.equals(getPlayerRole(player), Optional.of(PlayerRole.HUNTER)))) {
            reconnectTimer.remove(player);
            if (!reconnectCount.containsKey(player)) {
                reconnectCount.put(player, (byte) 1);
            }
            reconnectCount.put(player, (byte) (reconnectCount.get(player) + 1));
            if (reconnectCount.get(player) > 3) {
                player.setHealth(0);
                Bukkit.broadcastMessage(String.format("%s%s 因为重连次数过多已死亡！", ChatColor.RED, player.getName()));
            }
        }
        if (inGamePlayers.size() < maxPlayers) {
            inGamePlayers.add(player);
            return true;
        }
        return false;
    }

    public void playerLeaving(Player player) {
        if (status == net.mcxk.minihunt.game.GameStatus.WAITING_PLAYERS) {
            this.inGamePlayers.remove(player);
        } else {
            if (endWhenAllLeave) {
                if (GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.RUNNER).isEmpty()) {
                    LeaveEnding.leaveEnd(PlayerRole.HUNTER);
                }
                if (GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.HUNTER).isEmpty()) {
                    LeaveEnding.leaveEnd(PlayerRole.RUNNER);
                }
            } else {
                this.reconnectTimer.put(player, System.currentTimeMillis());
            }
        }
    }

    public void playerLeft(Player player) {
        GetPlayerAsRole.getRoleMapping().remove(player);
        this.inGamePlayers.remove(player);

        if (GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.RUNNER).isEmpty()) {
            LeaveEnding.leaveEnd(PlayerRole.HUNTER);
        } else if (GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.HUNTER).isEmpty()) {
            LeaveEnding.leaveEnd(PlayerRole.RUNNER);
        } else {
            Bukkit.broadcastMessage("玩家：" + player.getName() + " 因长时间未能重新连接回对战而被从列表中剔除");
            Bukkit.broadcastMessage(ChatColor.GREEN + "猎人: " + Util.list2String(GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList())));
            Bukkit.broadcastMessage(ChatColor.RED + "逃亡者: " + Util.list2String(GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList())));
        }
    }

    public void start() {
        if (status != net.mcxk.minihunt.game.GameStatus.WAITING_PLAYERS) {
            return;
        }
        status = net.mcxk.minihunt.game.GameStatus.GAME_STARTED;
        if (Bukkit.getPluginManager().isPluginEnabled("AdvancedReplay")) {
            Bukkit.broadcastMessage("请稍等，正在启动游戏录制...");
            try {
                GameRecord.record(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Bukkit.broadcastMessage("请稍后，系统正在随机分配玩家身份...");

        List<Player> noRolesPlayers = new ArrayList<>(inGamePlayers);
        Map<Player, PlayerRole> roleMapTemp = new HashMap<>(16);

        int runners = 1;
        if (inGamePlayers.size() >= L0Player) {
            runners = L0Runner;
        }
        if (inGamePlayers.size() >= L1Player) {
            runners = L1Runner;
        }
        if (inGamePlayers.size() >= L2Player) {
            runners = L2Runner;
        }
        if (inGamePlayers.size() >= L3Player) {
            runners = L3Runner;
        }
        List<Player> intentionRunners = new ArrayList<>();
        if (selectTeam) {
            intentionRunners = intentionRoleMapping.entrySet()
                    .stream()
                    .filter(e -> PlayerRole.RUNNER.equals(e.getValue()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }
        while (roleMapTemp.size() < runners) {
            if (selectTeam && !intentionRunners.isEmpty()) {
                Player selected = intentionRunners.get(random.nextInt(intentionRunners.size()));
                roleMapTemp.put(selected, PlayerRole.RUNNER);
                noRolesPlayers.remove(selected);
                intentionRunners.remove(selected);
            } else {
                Player selected = noRolesPlayers.get(random.nextInt(noRolesPlayers.size()));
                roleMapTemp.put(selected, PlayerRole.RUNNER);
                noRolesPlayers.remove(selected);
            }
        }
        noRolesPlayers.forEach(p -> roleMapTemp.put(p, PlayerRole.HUNTER));
        GetPlayerAsRole.setRoleMapping(new ConcurrentHashMap<>(roleMapTemp));
        Bukkit.broadcastMessage("正在将逃亡者随机传送到远离猎人的位置...");
        // 先给第一个runner找个位置 bug
        Location airDropLoc = airDrop(GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).get(0).getWorld().getSpawnLocation());
        // 再把其他runner传送过去
        GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER).forEach(runner -> runner.teleport(airDropLoc));
        GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.teleport(p.getWorld().getSpawnLocation()));
        Bukkit.broadcastMessage("设置游戏规则...");
        inGamePlayers.forEach(p -> {
            p.setGameMode(GameMode.SURVIVAL);
            p.setFoodLevel(40);
            p.setHealth(20);
            p.setExp(0.0f);
            p.setCompassTarget(p.getWorld().getSpawnLocation());
            p.getInventory().clear();
        });
        if (compassUnlocked) {
            GetPlayerAsRole.getPlayersAsRole(PlayerRole.HUNTER).forEach(p -> p.getInventory().addItem(new ItemStack(Material.COMPASS, 1)));
        }
        switchWorldRuleForReady(true);
        Bukkit.broadcastMessage("游戏开始！");
        Bukkit.broadcastMessage(ChatColor.AQUA + "欢迎来到 " + ChatColor.GREEN + plugin.getName() + " " + ChatColor.AQUA + "!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "在本游戏中，将会有 " + ChatColor.YELLOW + runners + ChatColor.AQUA + " 名玩家扮演逃亡者，其余玩家扮演猎人");
        Bukkit.broadcastMessage(ChatColor.RED + "猎人需要阻止逃亡者击杀末影龙或击杀逃亡者以取得胜利。");
        Bukkit.broadcastMessage(ChatColor.GREEN + "逃亡者需要在猎人的追杀下击败末影龙以取得胜利。逃亡者无法复活且由于任何原因死亡均会导致猎人胜利。");
        Bukkit.broadcastMessage(ChatColor.AQUA + "在游戏过程中，当你解锁特定的游戏阶段时，全体玩家将会获得阶段奖励，可能是特定物品也可能是增益效果。");
        Bukkit.broadcastMessage(String.format("%s%s猎人可以右键刷新指南针，来更好的追踪逃亡者。", ChatColor.RED, ChatColor.BOLD));
        Bukkit.broadcastMessage(ChatColor.AQUA + "猎人可以通过合成指南针来定位逃亡者的方向；逃亡者可以通过合成指南针摧毁猎人的指南针。");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "祝君好运，末地见！");
        Bukkit.broadcastMessage(ChatColor.GREEN + "猎人: " + Util.list2String(GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.HUNTER).stream().map(Player::getName).collect(Collectors.toList())));
        Bukkit.broadcastMessage(ChatColor.RED + "逃亡者: " + Util.list2String(GetPlayerAsRole.getPlayersAsRole(net.mcxk.minihunt.game.PlayerRole.RUNNER).stream().map(Player::getName).collect(Collectors.toList())));
        this.registerWatchers();
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.GAME_STARTING, null);
    }

    public void switchWorldRuleForReady(boolean ready) {
        if (!ready) {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.DO_FIRE_TICK, false);
                world.setGameRule(GameRule.MOB_GRIEFING, false);
                difficultyMap.put(world, world.getDifficulty());
                world.setDifficulty(Difficulty.PEACEFUL);
            });
        } else {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
                world.setGameRule(GameRule.DO_FIRE_TICK, true);
                world.setGameRule(GameRule.MOB_GRIEFING, true);
                world.setDifficulty(difficultyMap.getOrDefault(world, Difficulty.NORMAL));
            });
        }
    }

    @SneakyThrows
    void sendEndingAnimation() {
        double maxCanCost = 20000d;
        int needShows = 0;
        if (StringUtils.isNotBlank(gameEndingData.getDamageOutput())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getDragonKiller())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getDamageReceive())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getStoneAgePassed())) {
            needShows++;
        }

        if (StringUtils.isNotBlank(gameEndingData.getRunnerKiller())) {
            needShows++;
        }

        if (StringUtils.isNotBlank(gameEndingData.getWalkMaster())) {
            needShows++;
        }
        if (StringUtils.isNotBlank(gameEndingData.getJumpMaster())) {
            needShows++;
        }
        if (needShows != 0) {
            maxCanCost /= needShows;
        }

        int sleep = (int) maxCanCost;

        if (StringUtils.isNotBlank(gameEndingData.getDragonKiller())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GOLD + plugin.getConfig().getString("DragonKiller"), gameEndingData.getDragonKiller(), 0, 20000, 0));
            Thread.sleep(sleep);
        }

        if (StringUtils.isNotBlank(gameEndingData.getRunnerKiller())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.RED + plugin.getConfig().getString("RunnerKiller"), gameEndingData.getRunnerKiller(), 0, 20000, 0));
            Thread.sleep(sleep);
        }

        if (StringUtils.isNotBlank(gameEndingData.getDamageOutput())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.AQUA + plugin.getConfig().getString("DamageOutPut"), gameEndingData.getDamageOutput(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getDamageReceive())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.LIGHT_PURPLE + plugin.getConfig().getString("DamageReceive"), gameEndingData.getDamageReceive(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getTeamKiller())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.DARK_RED + plugin.getConfig().getString("TeamKiller"), gameEndingData.getTeamKiller(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getTeamEndDragon())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.DARK_RED + plugin.getConfig().getString("TeamEndDragon"), gameEndingData.getTeamEndDragon(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getWalkMaster())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.YELLOW + plugin.getConfig().getString("WalkMaster"), gameEndingData.getWalkMaster(), 0, 20000, 0));
            Thread.sleep(sleep);
        }
        if (StringUtils.isNotBlank(gameEndingData.getJumpMaster())) {
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GRAY + plugin.getConfig().getString("JumpMaster"), gameEndingData.getJumpMaster(), 0, 20000, 0));
            Thread.sleep(sleep);
        }

        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GREEN + plugin.getConfig().getString("EndText1"), plugin.getConfig().getString("EndText2"), 0, 20000, 0));
        Thread.sleep(sleep);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GREEN + plugin.getConfig().getString("ServerName"), plugin.getConfig().getString("ServerGame"), 0, 20000, 0));
        Thread.sleep(sleep);
        Bukkit.getOnlinePlayers().forEach(Player::resetTitle);
        Bukkit.getScheduler().runTaskLater(plugin, GameEnd::startEnd, 20);
    }


    private void registerWatchers() {
        new RadarWatcher();
        new ReconnectWatcher();
        new PlayerMoveWatcher();
    }

    /**
     * 来自ManHunt插件的代码
     */
    private Location airDrop(Location spawnpoint) {
        Location loc = spawnpoint.clone();
        loc = new Location(loc.getWorld(), loc.getBlockX(), 0, loc.getBlockZ());
        loc.add((double) random.nextInt(XRandom) + XBasic, 0, (double) random.nextInt(YRandom) + YBasic);
        final World world = loc.getWorld();
        if (Objects.isNull(world)) {
            throw new RuntimeException("所在世界获取失败！");
        }
        // 获取当前位置的地表
        loc = world.getHighestBlockAt(loc).getLocation();
        // 指定位置放置玻璃
        loc.getBlock().setType(Material.GLASS);
        // 然后设置人的位置在玻璃上一格
        loc.setY(loc.getY() + 1);
        return loc;
    }

    public int getMinPlayers() {
        return minPlayers;
    }
}

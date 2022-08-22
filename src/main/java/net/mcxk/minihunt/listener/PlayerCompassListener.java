package net.mcxk.minihunt.listener;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.GameStatus;
import net.mcxk.minihunt.game.PlayerRole;
import net.mcxk.minihunt.util.GetPlayerAsRole;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PlayerCompassListener implements Listener {
    private final MiniHunt plugin = MiniHunt.getInstance();

    private final List<List<Material>> detectionArmours = Arrays.asList(
            Arrays.asList(Material.IRON_HELMET, Material.DIAMOND_HELMET),
            Arrays.asList(Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE),
            Arrays.asList(Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS),
            Arrays.asList(Material.IRON_BOOTS, Material.DIAMOND_BOOTS));

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void craftCompass(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() != Material.COMPASS) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        Optional<PlayerRole> role = plugin.getGame().getPlayerRole(player);
        if (!role.isPresent()) {
            return;
        }
        if (role.get() == PlayerRole.HUNTER) {
            // 猎人合成，解锁
            plugin.getGame().switchCompass(true);
        } else if (role.get() == PlayerRole.RUNNER) {
            // 逃亡者合成，锁回去
            plugin.getGame().switchCompass(false);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void respawnGivenCompass(PlayerRespawnEvent event) {
        if (plugin.getGame().getStatus() == GameStatus.GAME_STARTED && plugin.getGame().isCompassUnlocked()) {
            Optional<PlayerRole> role = plugin.getGame().getPlayerRole(event.getPlayer());
            if (role.isPresent() && role.get() == PlayerRole.HUNTER) {
                event.getPlayer().getInventory().addItem(new ItemStack(Material.COMPASS));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void inventoryOpenCompass(InventoryOpenEvent event) {
        inventoryCompass(event, event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void inventoryCloseCompass(InventoryCloseEvent event) {
        inventoryCompass(event, event.getPlayer());
    }

    /**
     * 判断猎人是否全套铁甲
     */
    public void inventoryCompass(InventoryEvent event, HumanEntity humanEntity) {
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player player = (Player) humanEntity;
        Optional<PlayerRole> role = plugin.getGame().getPlayerRole(player);
        if (!role.isPresent() ||
                role.get() != PlayerRole.HUNTER ||
                Objects.nonNull(plugin.getGame().getFirstAllArmourPlayer())) {
            return;
        }
        // 检查玩家是否打开背包
        if (event.getInventory().getType().equals(InventoryType.CRAFTING)
                || event.getInventory().getType().equals(InventoryType.WORKBENCH)) {
            Boolean[] armours = new Boolean[]{false, false, false, false};
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (Objects.isNull(itemStack)) {
                    continue;
                }
                // 判断itemStack是否是铁甲
                int index = 0;
                for (List<Material> armour : detectionArmours) {
                    if (armour.contains(itemStack.getType())) {
                        armours[index] = true;
                    }
                    index++;
                }
            }
            // 全套铁甲以上护甲
            if (Arrays.stream(armours).allMatch(r -> r)) {
                plugin.getGame().setFirstAllArmourPlayer(player);
                Bukkit.broadcastMessage(String.format("%s猎人%s最快达成全套铁甲以上护具！", org.bukkit.ChatColor.YELLOW, player.getName()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void deathDropRemoveCompass(PlayerDeathEvent event) {
        event.getDrops().removeIf(itemStack -> itemStack.getType() == Material.COMPASS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void clickCompass(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() == null || event.getItem().getType() != Material.COMPASS) {
            return;
        }
        if (!plugin.getGame().isCompassUnlocked()) {
            event.getPlayer().setCompassTarget(event.getPlayer().getWorld().getSpawnLocation());
            event.getPlayer().sendMessage("你的队伍还没有解锁指南针，请先合成一个来解锁。");
        }
        List<Player> runners = GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER);
        if (runners.isEmpty()) {
            event.getPlayer().sendMessage("追踪失败，所有逃亡者均已离线等待重连中...");
        }
        Player closestRunner = null;
        double minDistance = Double.MAX_VALUE;
        double distance;
        for (Player runner : GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER)) {
            distance = event.getPlayer().getLocation().distance(runner.getLocation());
            if (runner.getWorld() == event.getPlayer().getWorld() && runner.getGameMode() != GameMode.SPECTATOR && distance < minDistance) {
                closestRunner = runner;
                minDistance = distance;
            }
        }

        if (closestRunner == null) {
            event.getPlayer().sendMessage("追踪失败，没有任何逃亡者和您所处的世界相同");
        } else {
            TextComponent component = new TextComponent("成功探测到距离您最近的逃亡者！正在追踪: %s".replace("%s", closestRunner.getName()));
            component.setColor(ChatColor.AQUA);
            if (event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {
                event.getPlayer().setCompassTarget(closestRunner.getLocation());
            } else {
                CompassMeta compassMeta = (CompassMeta) event.getItem().getItemMeta();
                if (compassMeta == null) {
                    event.getPlayer().sendMessage("错误：指南针损坏，请联系服务器管理员报告BUG.");
                } else {
                    compassMeta.setLodestone(closestRunner.getLocation());
                    // 如果为true，则目标位置必须有Lodestone才有效；因此设为false 这貌似也是ManiHunt中的一个BUG
                    compassMeta.setLodestoneTracked(false);
                    event.getItem().setItemMeta(compassMeta);
                }
            }
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        }
    }
}

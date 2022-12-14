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
import org.jetbrains.annotations.NotNull;

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
            // ?????????????????????
            plugin.getGame().switchCompass(true);
        } else if (role.get() == PlayerRole.RUNNER) {
            // ???????????????????????????
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
     * ??????????????????????????????
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
        // ??????????????????????????????
        if (event.getInventory().getType().equals(InventoryType.CRAFTING)
                || event.getInventory().getType().equals(InventoryType.WORKBENCH)) {
            Boolean[] armours = new Boolean[]{false, false, false, false};
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (Objects.isNull(itemStack)) {
                    continue;
                }
                // ??????itemStack???????????????
                int index = 0;
                for (List<Material> armour : detectionArmours) {
                    if (armour.contains(itemStack.getType())) {
                        armours[index] = true;
                    }
                    index++;
                }
            }
            // ????????????????????????
            if (Arrays.stream(armours).allMatch(r -> r)) {
                plugin.getGame().setFirstAllArmourPlayer(player);
                Bukkit.broadcastMessage(String.format("%s??????%s????????????????????????????????????! ", org.bukkit.ChatColor.YELLOW, player.getName()));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void deathDropRemoveCompass(@NotNull PlayerDeathEvent event) {
        event.getDrops().removeIf(itemStack -> itemStack.getType() == Material.COMPASS);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void clickCompass(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (Objects.isNull(event.getItem()) || event.getItem().getType() != Material.COMPASS) {
            return;
        }
        if (!plugin.getGame().isCompassUnlocked()) {
            event.getPlayer().setCompassTarget(event.getPlayer().getWorld().getSpawnLocation());
            event.getPlayer().sendMessage("?????????????????????????????????????????????????????????????????????");
        }
        List<Player> runners = GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER);
        if (runners.isEmpty()) {
            event.getPlayer().sendMessage("?????????????????????????????????????????????????????????...");
        }
        Player closestRunner = null;
        double minDistance = Double.MAX_VALUE;
        double distance;
        for (Player runner : GetPlayerAsRole.getPlayersAsRole(PlayerRole.RUNNER)) {
            if (event.getPlayer().getWorld() != runner.getWorld() || runner.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            distance = event.getPlayer().getLocation().distance(runner.getLocation());
            if (runner.getGameMode() != GameMode.SPECTATOR && distance < minDistance) {
                closestRunner = runner;
                minDistance = distance;
            }
        }

        if (Objects.isNull(closestRunner)) {
            event.getPlayer().sendMessage("???????????????????????????????????????????????????????????????");
        } else {
            TextComponent component = new TextComponent("??????????????????????????????????????????! ????????????: %s".replace("%s", closestRunner.getName()));
            component.setColor(ChatColor.AQUA);
            if (event.getPlayer().getWorld().getEnvironment() == World.Environment.NORMAL) {
                event.getPlayer().setCompassTarget(closestRunner.getLocation());
            } else {
                CompassMeta compassMeta = (CompassMeta) event.getItem().getItemMeta();
                if (Objects.isNull(compassMeta)) {
                    event.getPlayer().sendMessage("????????????????????????????????????????????????????????????BUG.");
                } else {
                    compassMeta.setLodestone(closestRunner.getLocation());
                    // ?????????true???????????????????????????Lodestone????????????????????????false ???????????????ManiHunt????????????BUG
                    compassMeta.setLodestoneTracked(false);
                    event.getItem().setItemMeta(compassMeta);
                }
            }
            event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
        }
    }
}

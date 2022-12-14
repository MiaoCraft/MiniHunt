package net.mcxk.minihunt.listener;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.GameProgress;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ProgressDetectingListener implements Listener {
    private final MiniHunt plugin = MiniHunt.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void craftCompass(@NotNull CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() != Material.COMPASS) {
            return;
        }
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.COMPASS_UNLOCKED);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void getIron(@NotNull FurnaceExtractEvent event) {
        if (event.getItemType() != Material.IRON_INGOT) {
            return;
        }
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.IRON_MINED);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void breakStone(@NotNull BlockBreakEvent event) {
        if (event.getBlock().getType() != Material.STONE) {
            return;
        }
        plugin.getGame().getProgressManager().unlockProgress(GameProgress.STONE_AGE);
        plugin.getGame().getGameEndingData().setStoneAgePassed(event.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void changeDim(@NotNull PlayerPortalEvent event) {
        NetherJudger(event.getTo(), event.getPlayer());
    }

    private void NetherJudger(Location to, Player player) {
        if (Objects.requireNonNull(Objects.requireNonNull(to).getWorld()).getEnvironment() == World.Environment.NETHER) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.ENTER_NETHER, player);
            return;
        }
        if (to.getWorld().getEnvironment() == World.Environment.THE_END) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.ENTER_END);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void teleport(@NotNull PlayerTeleportEvent event) {
        NetherJudger(event.getTo(), event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void pickup(@NotNull EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().getType() == Material.ENDER_PEARL) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.GET_ENDER_PEARL);
            return;
        }
        if (event.getItem().getItemStack().getType() == Material.BLAZE_ROD) {
            plugin.getGame().getProgressManager().unlockProgress(GameProgress.GET_BLAZE_ROD);
        }
    }


}

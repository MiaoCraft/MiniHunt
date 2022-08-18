package net.mcxk.minihunt.util;

import lombok.SneakyThrows;
import net.mcxk.minihunt.MiniHunt;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;


public class MusicPlayer {
    @SneakyThrows
    public void playEnding() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.MUSIC_DISC_WAIT, 1.0f, 1.0f));
            }
        }.runTaskLater(MiniHunt.getInstance(), 1);
    }
}

package net.mcxk.minihunt.util;

import net.mcxk.minihunt.MiniHunt;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/12
 * @apiNote
 */
public class SendMessage {
    private SendMessage() {
    }

    public static void sendMessage(String message, @NotNull CommandSender sender) {
        sender.sendMessage(MiniHunt.messageHead + message);
    }
}

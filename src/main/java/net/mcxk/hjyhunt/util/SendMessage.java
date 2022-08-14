package net.mcxk.hjyhunt.util;

import net.mcxk.hjyhunt.HJYHunt;
import org.bukkit.command.CommandSender;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/12
 * @apiNote
 */
public class SendMessage {
    private SendMessage() {
    }

    public static void sendMessage(String message, CommandSender sender) {
        sender.sendMessage(HJYHunt.messageHead + message);
    }
}

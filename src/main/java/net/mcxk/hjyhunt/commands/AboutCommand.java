package net.mcxk.hjyhunt.commands;

import net.mcxk.hjyhunt.HJYHunt;
import org.bukkit.command.CommandSender;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/10
 * @apiNote
 */
public class AboutCommand {
    private AboutCommand() {
    }

    public static void about(CommandSender sender) {
        // 禁止删除本行版权声明
        // 墨守吐槽：如果有人想在我这搞分支就顺着往下写就好了~
        sender.sendMessage("§a" + HJYHunt.pluginName + "§3v" + HJYHunt.pluginVersion);
        sender.sendMessage("https://github.com/MiaoCraft/" + HJYHunt.pluginName);
        sender.sendMessage("Copyright - Minecraft of gamerteam. 版权所有.");
        sender.sendMessage("使用该插件应遵循§eAGPL-3.0§r协议");
        sender.sendMessage("Fork by MossCG 这是墨守的分支版本~");
        sender.sendMessage("Fork by LingMuQingYu 这是凌慕轻语的分支版本~");
        sender.sendMessage("Fork by §aC§br§cs§du§eh§a2§be§cr§d0 §r这是§b泠§d辰§r的分支版本~");
    }
}

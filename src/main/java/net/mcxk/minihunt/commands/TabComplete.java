package net.mcxk.minihunt.commands;

import net.mcxk.minihunt.game.ConstantCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Crsuh2er0
 * &#064;date 2022/8/11
 * @apiNote
 */
public class TabComplete implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        final String[] miniHunt = new String[]{"about", "hunter", "runner", "rstcd", "players", "start", "want", "help"};
        final String[] want = new String[]{"hunter", "runner", "waiting"};

        if (args.length <= 1) {
            return Arrays.asList(miniHunt);
        }

        if (args.length < 3 && ConstantCommand.WANT.equalsIgnoreCase(args[0])) {
            return Arrays.asList(want);
        }
        return Collections.emptyList();
    }
}

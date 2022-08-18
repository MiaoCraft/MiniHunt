package net.mcxk.minihunt.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class Util {
    /**
     * Convert strList to String. E.g "Foo, Bar"
     *
     * @param strList Target list
     * @return str
     */
    @NotNull
    public static String list2String(@NotNull List<String> strList) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < strList.size(); i++) {
            builder.append(strList.get(i));
            if (i + 1 != strList.size()) {
                builder.append(", ");
            }
        }
        return builder.toString();
    }

    /**
     * Returns loc with modified pitch/yaw angles so it faces lookat
     *
     * @param loc    The location a players head is
     * @param lookat The location they should be looking
     * @return The location the player should be facing to have their crosshairs on the location
     * lookAt Kudos to bergerkiller for most of this function
     */
    public static @NotNull Location lookAt(@NotNull Location loc, @NotNull Location lookat) {
        // Clone the loc to prevent applied changes to the input loc
        loc = loc.clone();
        // Values of change in distance (make it relative)
        double dx = lookat.getX() - loc.getX();
        double dy = lookat.getY() - loc.getY();
        double dz = lookat.getZ() - loc.getZ();
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                loc.setYaw((float) (1.5 * Math.PI));
            } else {
                loc.setYaw((float) (0.5 * Math.PI));
            }
            loc.setYaw(loc.getYaw() - (float) Math.atan(dz / dx));
        } else if (dz < 0) {
            loc.setYaw((float) Math.PI);
        }
        // Get the distance from dx/dz
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        float pitch = (float) -Math.atan(dy / dxz);
        // Set values, convert to degrees
        // Minecraft yaw (vertical) angles are inverted (negative)
        loc.setYaw(-loc.getYaw() * 180f / (float) Math.PI + 360);
        // But pitch angles are normal
        loc.setPitch(pitch * 180f / (float) Math.PI);
        return loc;
    }

    /**
     * 快捷构建发送消息组件工具类
     *
     * @param text   消息内容
     * @param isBold 是否粗体
     * @param color  字体颜色
     * @return 消息组件
     */
    public static TextComponent buildTextComponent(String text, Boolean isBold, ChatColor color) {
        return buildTextComponent(text, isBold, color, null);
    }

    /**
     * 快捷构建发送消息组件工具类
     *
     * @param text    消息内容
     * @param isBold  是否粗体
     * @param color   字体颜色
     * @param command 点击触发指令
     * @return 消息组件
     */
    public static TextComponent buildTextComponent(String text, Boolean isBold, ChatColor color, String command) {
        final TextComponent textComponent = new TextComponent(text);
        if (Objects.nonNull(isBold)) {
            textComponent.setBold(isBold);
        }
        if (Objects.nonNull(color)) {
            textComponent.setColor(color);
        }
        if (StringUtils.isNotEmpty(command)) {
            textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        }
        return textComponent;
    }

}

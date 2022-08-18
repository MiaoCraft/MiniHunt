package net.mcxk.minihunt.util;

import net.mcxk.minihunt.MiniHunt;
import net.mcxk.minihunt.game.Game;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.Map;

public class StatisticsBaker {
    private final Game game = MiniHunt.getInstance().getGame();

    public String getDamageMaster() {
        Map.Entry<String, Double> result = getHighest(Statistic.DAMAGE_DEALT);
        if (result.getValue() == 0.0d) {
            return "";
        }
        return result.getKey() + " 对其他生物造成了 " + result.getValue().intValue() / 10.0 + " 点伤害";
    }

    public String getDamageTakenMaster() {
        Map.Entry<String, Double> result = getHighest(Statistic.DAMAGE_TAKEN);
        if (result.getValue() == 0.0d) {
            return "";
        }
        return result.getKey() + " 共受到了 " + result.getValue().intValue() / 10.0 + " 点伤害";
    }

    public String getWalkingMaster() {
        Map.Entry<String, Double> result = getHighest(Statistic.WALK_ONE_CM);
        if (result.getValue() == 0.0d) {
            return "";
        }
        return result.getKey() + " 旅行了 " + Math.round(result.getValue().intValue() / 100.0) + " 米";
    }


    public String getJumpMaster() {
        Map.Entry<String, Double> result = getHighest(Statistic.JUMP);
        if (result.getValue() == 0.0d) {
            return "";
        }
        return "生命不息，空格不停 " + result.getKey() + " 共跳跃了 " + result.getValue().intValue() + " 次";
    }

    public String getTeamBadGuy() {
        Player maxPlayer = null;
        double maxDamage = 0.0d;
        for (Map.Entry<Player, Double> playerDoubleEntry : MiniHunt.getInstance().getGame().getTeamDamageData().entrySet()) {
            if (playerDoubleEntry.getValue() < maxDamage) {
                continue;
            }
            maxPlayer = playerDoubleEntry.getKey();
            maxDamage = playerDoubleEntry.getValue().intValue();
        }
        if (maxPlayer == null) {
            return "";
        }
        return maxPlayer.getName() + " 对敌方造成了 " + maxDamage + " 点伤害";
    }

    public String getTeamEndDragonBadGuy() {
        Player maxPlayer = null;
        double maxDamage = 0.0d;
        for (Map.Entry<Player, Double> playerDoubleEntry : MiniHunt.getInstance().getGame().getTeamDamageEndDragonData().entrySet()) {
            if (playerDoubleEntry.getValue() < maxDamage) {
                continue;
            }
            maxPlayer = playerDoubleEntry.getKey();
            maxDamage = playerDoubleEntry.getValue().intValue();
        }
        if (maxPlayer == null) {
            return "";
        }
        return maxPlayer.getName() + " 对末影龙造成了 " + maxDamage + " 点伤害";
    }


    public Map.Entry<String, Double> getHighest(Statistic statistic) {
        Player playerMax = null;
        double dataMax = 0.0d;
        for (Player filtering : game.getInGamePlayers()) {
            if (playerMax == null) {
                playerMax = filtering;
                dataMax = filtering.getStatistic(statistic);
                continue;
            }
            double data = filtering.getStatistic(statistic);
            if (dataMax < data) {
                playerMax = filtering;
                dataMax = data;
            }
        }
        if (playerMax == null) {
            return new AbstractMap.SimpleEntry<>("Null", 0.0d);
        }
        return new AbstractMap.SimpleEntry<>(playerMax.getName(), dataMax);
    }
}

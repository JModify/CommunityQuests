package me.wonka01.ServerQuests.questcomponents.rewards;

import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.ServerQuests;
import org.bukkit.OfflinePlayer;

public abstract class ScalableReward implements Reward {

    @Getter @Setter protected double amount;
    protected ServerQuests plugin;

    public ScalableReward(ServerQuests plugin, double amount) {
        this.plugin = plugin;
        this.amount = amount;
    }

    public abstract void giveScaledReward(OfflinePlayer player, int scaleFactor);
}

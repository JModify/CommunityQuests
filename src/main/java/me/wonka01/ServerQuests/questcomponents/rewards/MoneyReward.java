package me.wonka01.ServerQuests.questcomponents.rewards;

import me.knighthat.apis.utils.Colorization;
import me.wonka01.ServerQuests.ServerQuests;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class MoneyReward extends ScalableReward implements Reward, Colorization {

    public MoneyReward(ServerQuests plugin, double amount) {
        super(plugin, amount);
    }

    public void giveRewardToPlayer(OfflinePlayer player, double rewardPercentage) {
        if(amount <= 0) {
            return;
        }
        Economy economy = JavaPlugin.getPlugin(ServerQuests.class).getEconomy();
        if (economy == null) {
            return;
        }
        double weightedAmount = rewardPercentage * amount;
        economy.depositPlayer(player, weightedAmount);

        if (player.isOnline()) {
            String message = "- " + weightedAmount + " " + economy.currencyNamePlural();
            ((Player) player).sendMessage(color(message));
        }
    }

    @Override
    public void giveScaledReward(OfflinePlayer offlinePlayer, int scaleFactor) {
        if(amount <= 0) {
            return;
        }
        Economy economy = JavaPlugin.getPlugin(ServerQuests.class).getEconomy();
        if (economy == null) {
            return;
        }
        double scaledAmount = amount / scaleFactor;
        double scaledAmountRounded = Math.round(scaledAmount * 100.0) / 100.0;
        economy.depositPlayer(offlinePlayer, scaledAmountRounded);

        if (offlinePlayer.isOnline()) {
            String message = "- " + scaledAmountRounded + " " + economy.currencyNamePlural();

            Player player = offlinePlayer.getPlayer();
            ((Player) offlinePlayer).sendMessage(color(message));
        }
    }
}

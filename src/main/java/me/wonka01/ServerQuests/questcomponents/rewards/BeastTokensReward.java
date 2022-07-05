package me.wonka01.ServerQuests.questcomponents.rewards;

import me.knighthat.apis.utils.Colorization;
import me.wonka01.ServerQuests.ServerQuests;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.MessageFormat;

public class BeastTokensReward extends ScalableReward implements Reward, Colorization {

    public BeastTokensReward(ServerQuests plugin, double tokens) {
        super(plugin, tokens);
    }

    @Override
    public void giveRewardToPlayer(OfflinePlayer offlinePlayer, double rewardPercentage) {
        if(amount <= 0) {
            return;
        }
        double weightedAmount = rewardPercentage * amount;
        int weightedAmountWhole = (int) Math.round(weightedAmount);

        if (weightedAmountWhole <= 0) {
            return;
        }

        plugin.getBeastTokensHook().addTokensToPlayer(offlinePlayer, weightedAmountWhole);

        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            String message = MessageFormat.format("- &a{0} {1}", weightedAmountWhole, plugin.getMessages().string("beastToken"));
            player.sendMessage(color(message));
        }
    }

    @Override
    public void giveScaledReward(OfflinePlayer offlinePlayer, int scaleFactor) {
        if(amount <= 0) {
            return;
        }

        int scaledAmount = (int) Math.round(amount / scaleFactor);
        if (scaledAmount <= 0) {
            return;
        }

        plugin.getBeastTokensHook().addTokensToPlayer(offlinePlayer, scaledAmount);

        if (offlinePlayer.isOnline()) {
            Player player = offlinePlayer.getPlayer();
            String message = MessageFormat.format("- &a{0} {1}", scaledAmount, plugin.getMessages().string("beastToken"));
            player.sendMessage(color(message));
        }
    }

}

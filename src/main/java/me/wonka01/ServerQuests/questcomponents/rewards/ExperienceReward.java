package me.wonka01.ServerQuests.questcomponents.rewards;

import lombok.Getter;
import me.knighthat.apis.utils.Colorization;
import me.wonka01.ServerQuests.ServerQuests;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.MessageFormat;

public class ExperienceReward extends ScalableReward implements Reward, Colorization {

    public ExperienceReward(ServerQuests plugin, double experience) {
        super(plugin, experience);
    }

    public void giveRewardToPlayer(OfflinePlayer offlinePlayer, double rewardPercentage) {
        if (!offlinePlayer.isOnline() || amount <= 0) return;

        Player player = offlinePlayer.getPlayer();
        int exp = (int) (rewardPercentage * amount);

        if (exp <= 0) return;

        player.giveExp(exp);

        ServerQuests plugin = JavaPlugin.getPlugin(ServerQuests.class);
        String message = MessageFormat.format("- &a{0} {1}", exp, plugin.getMessages().message("experience"));
        player.sendMessage(color(message));
    }

    @Override
    public void giveScaledReward(OfflinePlayer offlinePlayer, int scaleFactor) {
        if (!offlinePlayer.isOnline() || amount <= 0) return;

        Player player = offlinePlayer.getPlayer();

        int scaledAmount = (int) Math.round(amount / scaleFactor);

        if (scaledAmount <= 0) {
            return;
        }

        player.giveExp(scaledAmount);

        ServerQuests plugin = JavaPlugin.getPlugin(ServerQuests.class);
        String message = MessageFormat.format("- &a{0} {1}", scaledAmount, plugin.getMessages().message("experience"));
        player.sendMessage(color(message));
    }
}

package me.modify.townyquests.rewards;

import com.modify.fundamentum.text.PlugLogger;
import lombok.NonNull;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.questcomponents.players.BasePlayerComponent;
import me.wonka01.ServerQuests.questcomponents.players.PlayerData;
import me.wonka01.ServerQuests.questcomponents.players.SortByContributions;
import me.wonka01.ServerQuests.questcomponents.rewards.Reward;
import me.wonka01.ServerQuests.questcomponents.rewards.ScalableReward;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class CompetitiveRewardHandler {

    private ServerQuests plugin;
    private RewardMode rewardMode;
    private int benchmark;

    public CompetitiveRewardHandler(ServerQuests plugin) {
        this.plugin = plugin;
        this.rewardMode = RewardMode.DEFAULT;
        this.benchmark = 5;
    }

    public void loadConfigurations() {
        this.rewardMode = RewardMode.match(plugin.getConfig().getString("rewardDistribution.competitive.mode", "default"));
        this.benchmark = plugin.getConfig().getInt("rewardDistribution.competitive.benchmark", 5);
    }

    public void distributeRewards(BasePlayerComponent basePlayerComponent, int questGoal) {
        Map<UUID, PlayerData> playerMap = basePlayerComponent.getPlayerMap();
        List<Reward> rewardList = basePlayerComponent.getRewardsList();

        switch (rewardMode) {
            case DEFAULT -> {
                basePlayerComponent.handleCoopRewardDistribution(questGoal);
            }
            case RANKED -> {
                Map<UUID, PlayerData> sortedMap = new TreeMap<>(new SortByContributions(playerMap));
                sortedMap.putAll(playerMap);
                int participatedCount = sortedMap.size();

                int adjustedBenchmark = Math.min(participatedCount, benchmark);

                List<UUID> keys = new ArrayList<>(sortedMap.keySet());
                for (int i = 0; i < adjustedBenchmark; i++) {
                    OfflinePlayer user = Bukkit.getOfflinePlayer(keys.get(i));

                    if (user.isOnline()) {
                        Player onlineUser = (Player) user;
                        if (rewardList.size() > 0) {
                            String rewardTitle = plugin.getMessages().message("rewardsTitle");
                            onlineUser.sendMessage(rewardTitle);
                        }
                    }

                    for (Reward reward : rewardList) {
                        if (reward instanceof ScalableReward scalableReward) {
                            scalableReward.giveScaledReward(user, adjustedBenchmark);
                        } else {
                            reward.giveRewardToPlayer(user, -1);
                        }
                    }
                }
            }
        }
    }

    private enum RewardMode {
        DEFAULT, RANKED;

        /**
         * Attempts to match given string with a valid reward mode.
         * If the provided string is valid, DEFAULT will be returned.
         * @param var string to attempt match with a reward mode
         * @return valid reward mode match or DEFAULT if the given variable cannot be found.
         */
        public static RewardMode match(@NonNull String var) {
            for (RewardMode type : values())
                if (type.name().equalsIgnoreCase(var.toUpperCase()))
                    return type;

            return DEFAULT;
        }
    }



}

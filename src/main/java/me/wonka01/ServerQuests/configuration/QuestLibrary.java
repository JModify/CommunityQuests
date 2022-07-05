package me.wonka01.ServerQuests.configuration;

import com.modify.fundamentum.text.PlugLogger;
import lombok.Getter;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.enums.ObjectiveType;
import me.wonka01.ServerQuests.questcomponents.rewards.Reward;
import me.wonka01.ServerQuests.questcomponents.rewards.*;
import me.wonka01.ServerQuests.util.ObjectiveTypeUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class QuestLibrary {

    @Getter private HashMap<String, QuestModel> questList;

    private ServerQuests plugin;

    public QuestLibrary(ServerQuests plugin) {
        this.plugin = plugin;
    }

    public QuestModel getQuestModelById(String questId) {
        return questList.get(questId);
    }

    public void loadQuestConfiguration(ConfigurationSection serverQuestConfig) {
        HashMap<String, QuestModel> map = new HashMap<>();
        if (serverQuestConfig == null) {
            questList = map;
            return;
        }

        for (String questId : serverQuestConfig.getKeys(false)) {
            ConfigurationSection section = serverQuestConfig.getConfigurationSection(questId);
            QuestModel model = loadQuestFromConfig(section);

            map.put(questId, model);
        }
        questList = map;
    }

    private QuestModel loadQuestFromConfig(ConfigurationSection section) {

        String questId = section.getName();
        String displayName = section.getString("displayName");
        List<String> description = section.getStringList("description");

        int timeToCompleteCoop = section.getInt("timeToCompleteCoop", 0);
        int timeToCompleteComp = section.getInt("timeToCompleteComp", 0);

        List<String> mobNames = section.getStringList("entities");
        List<String> itemNames = section.getStringList("materials");
        String displayItem = section.getString("displayItem", "");

        ObjectiveType objectiveType = ObjectiveTypeUtil.parseEventTypeFromString(section.getString("type"));
        int goal = section.getInt("goal", -1);

        ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
        ArrayList<Reward> rewards = getRewardsFromConfig(rewardsSection);

        return new QuestModel(questId, displayName, description, timeToCompleteCoop, timeToCompleteComp, goal,
            objectiveType, mobNames, rewards, itemNames, displayItem);
    }

    private ArrayList<Reward> getRewardsFromConfig(ConfigurationSection section) {
        ArrayList<Reward> rewards = new ArrayList<>();
        if (section == null) {
            return rewards;
        }

        for (String key : section.getKeys(false)) {
            Reward reward;
            if (key.equalsIgnoreCase("money")) {
                double amount = section.getDouble("money");
                reward = new MoneyReward(plugin, amount);
                rewards.add(reward);

            } else if (key.equalsIgnoreCase("experience")) {
                int amount = section.getInt("experience");
                reward = new ExperienceReward(plugin, amount);
                rewards.add(reward);

            } else if (key.equalsIgnoreCase("commands")) {
                List<String> commands = section.getStringList("commands");
                for (String command : commands) {
                    Reward commandReward = new CommandReward(command);
                    rewards.add(commandReward);
                }
            } else if (key.equalsIgnoreCase("items")) {
                List<?> itemRewards = section.getList(key);
                for (Object item : itemRewards) {
                    try {
                        LinkedHashMap<?, ?> map = (LinkedHashMap) item;
                        int amount = (Integer) map.get("amount");
                        String material = (String) map.get("material");
                        String itemName = (String) map.get("displayName");
                        reward = new ItemReward(amount, material, itemName);
                        rewards.add(reward);
                    } catch (Exception ex) {
                        JavaPlugin.getPlugin(ServerQuests.class).getLogger().info("Item reward failed to load due to invalid configuration");
                    }
                }
            } else if (key.equalsIgnoreCase("tokens")) {
                if (plugin.getBeastTokensHook().isHooked()) {
                    int amount = section.getInt("tokens");
                    reward = new BeastTokensReward(plugin, amount);
                    rewards.add(reward);
                } else {
                    PlugLogger.logError("Failed to recognize a beast token reward from a configured quest in file. BeastTokens plugin not installed.");
                }
            }
        }
        return rewards;
    }

    public Set<String> getAllQuestKeys() {
        return questList.keySet();
    }
}

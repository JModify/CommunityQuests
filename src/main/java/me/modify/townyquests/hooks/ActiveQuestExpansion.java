package me.modify.townyquests.hooks;

import com.modify.fundamentum.text.ColorUtil;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.QuestController;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ActiveQuestExpansion extends PlaceholderExpansion {

    private ServerQuests plugin;

    public ActiveQuestExpansion(ServerQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "quests";
    }

    @Override
    public @NotNull String getAuthor() {
        return "modify";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        if (params.equals("message")) {

            if (!plugin.getAutoQuest().isEnabled()) {
                return "Placeholder unavailable. AutoQuest is not enabled.";
            }

            ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();
            List<QuestController> activeQuestList = activeQuests.getActiveQuestsList();

            if (activeQuestList.size() > 1) {
                return "Placeholder unavailable. More than 1 quest is currently active.";
            }

            Optional<QuestController> optionalActiveQuest = activeQuestList.stream().findFirst();

            String message;
            if (optionalActiveQuest.isPresent()) {
                message = plugin.getConfig().getString("placeholder.questActive", "Error, message could not be read from configuration.");

                QuestController activeQuest = optionalActiveQuest.get();

                if (message.contains("{QUEST}")) {
                    message = message.replace("{QUEST}", ChatColor.stripColor(activeQuest.getQuestData().getDisplayName()));
                }

                if (message.contains("{TIME}")) {
                    message = message.replace("{TIME}", String.valueOf(plugin.getAutoQuest().getTimer().getTimeRemaining()));
                }

            } else {
                message = plugin.getConfig().getString("placeholder.noQuestsActive", "Error, message could not be read from configuration.");
            }

            return ColorUtil.format(message);
        }

        return null;
    }

}

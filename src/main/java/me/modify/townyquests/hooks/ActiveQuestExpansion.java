package me.modify.townyquests.hooks;

import com.modify.fundamentum.text.ColorUtil;
import com.modify.fundamentum.text.PlugLogger;
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

            QuestController activeQuest = plugin.getAutoQuest().getTimer().getActiveControlledQuest();

            String message;
            if (activeQuest != null) {
                message = plugin.getConfig().getString("placeholder.questActive", "Error, message could not be read from configuration.");

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

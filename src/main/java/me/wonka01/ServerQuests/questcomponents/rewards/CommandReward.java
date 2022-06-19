package me.wonka01.ServerQuests.questcomponents.rewards;

import com.modify.fundamentum.text.PlugLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Arrays;

public class CommandReward implements Reward {

    private String command;

    public CommandReward(String command) {
        this.command = command;
    }

    public void giveRewardToPlayer(OfflinePlayer player, double rewardPercentage) {
        if (player.isOnline() && player.getName() != null) {

            // TownyQuests edit
            if (isBeastTokenCommand(command)) {
                String[] parts = command.split(" ");
                if (parts.length == 4) {
                    if (parts[1].equals("add") || parts[1].equals("give")) {

                        double amount = 0;
                        try {
                            amount = Double.parseDouble(parts[3]);
                        } catch (NumberFormatException e) {
                            PlugLogger.logInfo("Unable to scale command reward amount, amount value not a number");
                        }

                        parts[3] = String.valueOf(Math.round(amount * rewardPercentage));
                        command = String.join(" ", parts);
                    }
                }
            }

            String commandToRun = command.replaceAll("player", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), commandToRun);
        }
    }

    /**
     * Checks if the command being run is a BeastToken command
     * @param command command to check
     * @return true if it is a beast token command, else false.
     */
    private boolean isBeastTokenCommand(String command) {
        String[] beastTokenAliases = {"bta", "beasttokenadmin", "beasttokensadmin",
            "beasttokens", "beasttoken"};

        return Arrays.stream(beastTokenAliases).anyMatch(command::startsWith);
    }
}

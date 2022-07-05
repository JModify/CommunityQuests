package me.modify.townyquests.hooks;

import com.modify.fundamentum.text.PlugLogger;
import lombok.Getter;
import lombok.Setter;
import me.mraxetv.beasttokens.BeastTokensAPI;
import me.wonka01.ServerQuests.ServerQuests;
import org.bukkit.OfflinePlayer;

public class BeastTokensHook {

    @Getter @Setter private boolean hooked = false;

    public ServerQuests plugin;

    public BeastTokensHook(ServerQuests plugin) {
        this.plugin = plugin;
    }

    public void check(){
        if (plugin.getServer().getPluginManager().getPlugin("BeastTokens") != null) {
            setHooked(true);
            PlugLogger.logInfo("BeastTokens detected. Plugin successfully hooked.");
        }
    }

    public void addTokensToPlayer(OfflinePlayer player, double amount) {
        if (hooked) {
            BeastTokensAPI.getTokensManager().addTokens(player, amount);
        } else {
            plugin.getDebugger().sendDebugInfo("Failed to give player " + player.getUniqueId() + " beast tokens. Plugin not installed.");
        }
    }
}

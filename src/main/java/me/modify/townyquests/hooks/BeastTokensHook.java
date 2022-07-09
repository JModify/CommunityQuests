package me.modify.townyquests.hooks;

import com.modify.fundamentum.text.PlugLogger;
import lombok.Getter;
import lombok.Setter;
import me.mraxetv.beasttokens.BeastTokensAPI;
import me.wonka01.ServerQuests.ServerQuests;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

    public void addTokensToPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (hooked) {
            if (offlinePlayer.isOnline()) {
                Player onlinePlayer = offlinePlayer.getPlayer();
                BeastTokensAPI.getTokensManager().addTokens(onlinePlayer, amount);
            } else {
                BeastTokensAPI.getTokensManager().addTokens(offlinePlayer, amount);;
            }
        } else {
            plugin.getDebugger().sendDebugInfo("Failed to give player " + offlinePlayer.getUniqueId() + " beast tokens. Plugin not installed.");
        }
    }
}

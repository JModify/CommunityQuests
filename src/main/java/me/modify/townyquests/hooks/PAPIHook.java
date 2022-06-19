package me.modify.townyquests.hooks;

import com.modify.fundamentum.text.PlugLogger;
import me.wonka01.ServerQuests.ServerQuests;
import org.bukkit.Bukkit;

public class PAPIHook {

    private boolean hooked = false;

    public ServerQuests plugin;

    public PAPIHook(ServerQuests plugin) {
        this.plugin = plugin;
    }

    public void check(){
        if (Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            setHooked(true);
            PlugLogger.logInfo("PlaceholderAPI detected. Plugin successfully hooked.");
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    private void setHooked(boolean hooked) {
        this.hooked = hooked;
    }

    public void registerExpansion() {
        if (hooked) {
            new ActiveQuestExpansion(plugin).register();
        }
    }

}

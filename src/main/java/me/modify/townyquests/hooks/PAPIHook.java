package me.modify.townyquests.hooks;

import com.modify.fundamentum.text.PlugLogger;
import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.ServerQuests;
import org.bukkit.Bukkit;

public class PAPIHook {

    @Getter @Setter private boolean hooked = false;

    public ServerQuests plugin;

    public PAPIHook(ServerQuests plugin) {
        this.plugin = plugin;
    }

    public void check(){
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            setHooked(true);
            PlugLogger.logInfo("PlaceholderAPI detected. Plugin successfully hooked.");
        }
    }

    public void registerExpansion() {
        if (hooked) {
            new ActiveQuestExpansion(plugin).register();
        }
    }

}

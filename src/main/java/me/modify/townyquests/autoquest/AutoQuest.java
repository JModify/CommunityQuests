package me.modify.townyquests.autoquest;

import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.ServerQuests;

public class AutoQuest {

    /** Whether AutoQuest is enabled or not */
    @Getter @Setter private boolean enabled;

    /** Timer controlling auto questing */
    @Getter private AutoQuestTimer timer;

    /**
     * Constructs a new auto quest object
     * @param plugin ServerQuests plugin instance
     */
    public AutoQuest(ServerQuests plugin) {
        this.timer = new AutoQuestTimer(plugin);
        this.enabled = true;
    }
}

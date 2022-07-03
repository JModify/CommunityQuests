package me.modify.townyquests.autoquest;

import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.questcomponents.QuestController;

import java.util.ArrayList;
import java.util.List;

public class AutoQuest {

    /** Whether AutoQuest is enabled or not */
    @Getter @Setter private boolean enabled;

    /** The default duration of a cooperative quest the plugin uses when timeToCompleteCoop value is not set */
    @Getter @Setter int defaultDurationCoop;

    /** The default duration of a competitive quest the plugin uses when timeToCompleteComp value is not set */
    @Getter @Setter int defaultDurationComp;

    /** Names of quests which are auto quest example. See config file comments for more information */
    @Getter @Setter List<String> autoQuestExempt;

    /** Timer controlling auto questing */
    @Getter private AutoQuestTimer timer;

    /**
     * Constructs a new auto quest object
     * @param plugin ServerQuests plugin instance
     */
    public AutoQuest(ServerQuests plugin) {
        this.timer = new AutoQuestTimer(plugin);
        this.enabled = true;
        this.autoQuestExempt = new ArrayList<>();
    }
}

package me.modify.townyquests;

import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.configuration.QuestModel;
import me.wonka01.ServerQuests.enums.EventType;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.QuestController;

import java.util.*;

public class AutoQuestTimer implements Runnable {

    /** Whether the auto questing timer is enabled or not. Enabled by default */
    @Getter @Setter private static boolean enabled = true;

    /** Interval in ticks for which this timer will repeat. 5 minutes by default */
    @Getter @Setter private static long interval = 6000L;

    private final ServerQuests plugin;

    public AutoQuestTimer(ServerQuests plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (AutoQuestTimer.isEnabled()) {
            ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();
            List<QuestController> activeQuestList = activeQuests.getActiveQuestsList();

            String prevQuestId = null;
            if (!activeQuestList.isEmpty()) {
                Optional<QuestController> activeQuest = activeQuestList.stream().findFirst();
                prevQuestId = String.valueOf(activeQuest.get().getQuestId());
            }

            EventType randomEventType = EventType.getRandomEventType();
            QuestModel randomQuestModel = getRandomQuestModel(prevQuestId);

            activeQuests.beginNewQuest(randomQuestModel, randomEventType);
        }
    }

    /**
     * Retrieves a random quest model, ensuring this new random quest
     * is not equal to the previously active quest.
     * @param prevQuestId previously active quest or null if none.
     * @return a random quest model.
     */
    private QuestModel getRandomQuestModel(String prevQuestId) {
        Set<String> allQuestKeys = plugin.getQuestLibrary().getAllQuestKeys();
        List<String> allQuestKeysList = new ArrayList<>(allQuestKeys);

        int size = allQuestKeysList.size();
        int randIdx = new Random().nextInt(size);

        String randomQuestId = allQuestKeysList.get(randIdx);

        // If this random quest id is equal to the previous active quest
        // get a new random quest model and return that.
        if (randomQuestId != null) {
            if (randomQuestId.equals(prevQuestId)) {
                return getRandomQuestModel(prevQuestId);
            }
        }

        return plugin.getQuestLibrary().getQuestModelById(randomQuestId);
    }
}

package me.wonka01.ServerQuests.questcomponents;

import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.configuration.QuestModel;
import me.wonka01.ServerQuests.enums.EventType;
import me.wonka01.ServerQuests.util.EventTypeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Singleton class that stores all active quests running on the server
public class ActiveQuests {

    private static ActiveQuests activeQuestsInstance;
    @Getter @Setter private static int questLimit;

    private List<QuestController> activeQuestsList = new ArrayList<>();

    public ActiveQuests() {
        activeQuestsInstance = this;
    }

    public static ActiveQuests getActiveQuestsInstance() {
        return activeQuestsInstance;
    }

    public void endQuest(UUID questId) {
        BarManager.closeBar(questId);
        QuestController controller = getQuestById(questId);
        controller.getQuestBar().removeBossBar();
        activeQuestsList.remove(getQuestById(questId));
        if (activeQuestsList.size() > 0) {
            BarManager.startShowingPlayersBar(activeQuestsList.get(0).getQuestId());
        }
    }

    /**
     * TownyEdit
     * Begins a new quest.
     * @param questModel quest model to begin
     * @param eventType quest type (collaborative or competitive)
     * @return quest controller of the quest which was started or null if quest limit is reached.
     */
    public QuestController beginNewQuest(QuestModel questModel, EventType eventType, boolean autoQuest) {
        if (activeQuestsList.size() >= questLimit) return null;

        EventTypeHandler typeHandler = new EventTypeHandler(eventType);
        QuestController controller = typeHandler.createQuestController(questModel, autoQuest);
        activeQuestsList.add(controller);
        controller.broadcast("questStartMessage");
        BarManager.startShowingPlayersBar(controller.getQuestId());
        return controller;
    }

    public void beginQuestFromSave(QuestController controller) {
        activeQuestsList.add(controller);
        BarManager.startShowingPlayersBar(controller.getQuestId());
    }

    public List<QuestController> getActiveQuestsList() {
        if (activeQuestsList == null) {
            activeQuestsList = new ArrayList<>();
        }
        return activeQuestsList;
    }

    public QuestController getQuestById(UUID questId) {
        for (QuestController controller : activeQuestsList) {
            if (controller.getQuestId().equals(questId)) {
                return controller;
            }
        }
        return null;
    }
}

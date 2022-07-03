package me.wonka01.ServerQuests.util;

import lombok.NonNull;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.configuration.QuestModel;
import me.wonka01.ServerQuests.enums.EventType;
import me.wonka01.ServerQuests.questcomponents.*;
import me.wonka01.ServerQuests.questcomponents.players.BasePlayerComponent;
import me.wonka01.ServerQuests.questcomponents.players.PlayerData;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public class EventTypeHandler {
    private final EventType eventType;

    public EventTypeHandler(EventType eventType) {
        this.eventType = eventType;
    }

    public EventTypeHandler(String type) {
        eventType = type.equalsIgnoreCase("comp") ? EventType.COMPETITIVE : EventType.COLLAB;
    }

    public QuestController createQuestController(@NonNull QuestModel model, boolean autoQuest) {

        if (eventType == EventType.COLLAB) {
            return createController(model, null, 0, model.getCompleteTimeCoop(), autoQuest);
        }
        return createController(model, null, 0, model.getCompleteTimeComp(), autoQuest);
    }

    public QuestController createControllerFromSave(@NonNull QuestModel model, @NonNull Map<UUID, PlayerData> players,
                                                    int completed, int timeLeft, boolean autoQuest) {
        return createController(model, players, completed, timeLeft, autoQuest);
    }

    private @NonNull QuestController createController(@NonNull QuestModel model, @Nullable Map<UUID, PlayerData> players, int completed, int timeLeft, boolean autoQuest) {

        ServerQuests plugin = JavaPlugin.getPlugin(ServerQuests.class);

        QuestBar bar = new QuestBar(model.getDisplayName());
        if (completed > 0) {
            bar.updateBarProgress((double) completed / model.getQuestGoal());
        }

        BasePlayerComponent pComponent = new BasePlayerComponent(model.getRewards());
        if (players != null) {
            pComponent = new BasePlayerComponent(model.getRewards(), players);
        }

        QuestData data = getQuestData(model, completed, pComponent, timeLeft, autoQuest);
        EventConstraints event = new EventConstraints(model.getItemNames(), model.getMobNames());

        return new QuestController(plugin, data, bar, pComponent, event, model.getObjective());
    }

    private QuestData getQuestData(QuestModel questModel, int amountComplete, BasePlayerComponent playerComponent, int timeLeft, boolean autoQuest) {

        if (eventType == EventType.COMPETITIVE) {
            return new CompetitiveQuestData(questModel.getQuestGoal(), questModel.getDisplayName(),
                questModel.getDescription(), playerComponent, questModel.getQuestId(), amountComplete, timeLeft, questModel.getDisplayItem(), autoQuest);
        } else {
            return new QuestData(questModel.getQuestGoal(), questModel.getDisplayName(),
                questModel.getDescription(), questModel.getQuestId(), amountComplete, timeLeft, questModel.getDisplayItem(), autoQuest);
        }
    }
}

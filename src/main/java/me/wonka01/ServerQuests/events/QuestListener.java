package me.wonka01.ServerQuests.events;

import me.wonka01.ServerQuests.enums.ObjectiveType;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.QuestController;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class QuestListener {
    protected ActiveQuests activeQuests;

    public QuestListener(ActiveQuests activeQuests) {
        this.activeQuests = activeQuests;
    }

    protected boolean updateQuest(QuestController controller, Player player, double amount) {
        controller.updateQuest(amount, player);
        if (controller.getQuestData().isGoalComplete()) {
            controller.endQuest();
            return true;
        }
        return false;
    }

    protected List<QuestController> tryGetControllersOfEventType(ObjectiveType type) {
        List<QuestController> controllers = new ArrayList<>();

        for (QuestController controller : activeQuests.getActiveQuestsList()) {
            if (controller.getObjectiveType() == type) {
                controllers.add(controller);
            }
        }
        return controllers;
    }
}

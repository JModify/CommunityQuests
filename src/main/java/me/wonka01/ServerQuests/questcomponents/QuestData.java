package me.wonka01.ServerQuests.questcomponents;

import lombok.Getter;
import org.bukkit.Material;

import java.util.List;
import java.util.Objects;

public class QuestData {

    private String questType;
    private int questGoal;
    private double amountCompleted;
    private int questDuration;

    private String displayName;
    private List<String> description;

    @Getter
    private Material displayItem;

    boolean autoQuest;

    public QuestData(int questGoal, String displayName, List<String> description, String questType, int amountCompleted, int questDuration, Material displayItem, boolean autoQuest) {
        this.questGoal = questGoal;
        this.questDuration = questDuration;
        this.amountCompleted = amountCompleted;
        this.displayName = displayName;
        this.description = description;
        this.questType = questType;
        this.displayItem = displayItem;
        this.autoQuest = autoQuest;
    }

    public boolean isAutoQuest() {
        return this.autoQuest;
    }

    public double getAmountCompleted() {
        return amountCompleted;
    }

    public double getPercentageComplete() {
        return (amountCompleted / (double) questGoal);
    }

    public void addToQuestProgress(double amountToIncrease) {
        amountCompleted += amountToIncrease;
    }

    public void decreaseDuration(int amountToDecrease) {
        questDuration -= amountToDecrease;
    }

    // Always false if no goal is set and the quest is using a timer...
    public boolean isGoalComplete() {
        return (hasGoal() && amountCompleted >= questGoal);
    }

    public boolean hasGoal() {
        return questGoal > 0;
    }

    public int getQuestGoal() {
        return questGoal;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public String getQuestType() {
        return questType;
    }

    public int getQuestDuration() {
        return questDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuestData questData = (QuestData) o;
        return questGoal == questData.questGoal && Double.compare(questData.amountCompleted, amountCompleted) == 0
            && questDuration == questData.questDuration && autoQuest == questData.autoQuest
            && Objects.equals(questType, questData.questType) && Objects.equals(displayName, questData.displayName)
            && Objects.equals(description, questData.description) && displayItem == questData.displayItem;
    }

    @Override
    public int hashCode() {
        return Objects.hash(questType, questGoal, amountCompleted, questDuration, displayName, description, displayItem, autoQuest);
    }
}

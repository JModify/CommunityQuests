package me.wonka01.ServerQuests.configuration;

import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.enums.ObjectiveType;
import me.wonka01.ServerQuests.questcomponents.rewards.Reward;
import me.wonka01.ServerQuests.util.ObjectiveTypeUtil;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@Getter
public class QuestModel {

    private String questId;
    private String displayName;
    private List<String> description;

    @Setter private int completeTimeCoop;
    @Setter private int completeTimeComp;

    private int questGoal;
    private ObjectiveType objective;
    private List<String> mobNames;
    private ArrayList<Reward> rewards;
    private List<String> itemNames;
    private Material displayItem;

    public QuestModel(String questId, String displayName, List<String> description,
                      int completeTimeCoop, int completeTimeComp, int questGoal, ObjectiveType objective,
                      List<String> mobNames, ArrayList<Reward> rewards, List<String> itemNames, String displayItem) {
        this.questId = questId;
        this.displayName = displayName;
        this.description = description;

        this.completeTimeCoop = completeTimeCoop;
        this.completeTimeComp = completeTimeComp;

        this.questGoal = questGoal;
        this.objective = objective;
        this.mobNames = mobNames;
        this.rewards = rewards;
        this.itemNames = itemNames;
        this.displayItem = Material.getMaterial(displayItem.toUpperCase());
        if(this.displayItem == null) {
            this.displayItem = ObjectiveTypeUtil.getEventTypeDefaultMaterial(objective);
        }
    }
}

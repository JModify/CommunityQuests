package me.wonka01.ServerQuests.events;

import com.modify.fundamentum.text.PlugLogger;
import me.wonka01.ServerQuests.enums.ObjectiveType;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.CompetitiveQuestData;
import me.wonka01.ServerQuests.questcomponents.QuestController;
import me.wonka01.ServerQuests.questcomponents.players.PlayerData;
import me.wonka01.ServerQuests.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiEvent extends QuestListener {

    public GuiEvent(ActiveQuests activeQuests) {
        super(activeQuests);
    }

    public boolean tryAddItemsToQuest(ItemStack itemsToAdd, Player player) {
        List<QuestController> controllers = tryGetControllersOfEventType(ObjectiveType.GUI);

        boolean isItemUsed = false;
        for (QuestController controller : controllers) {

            List<String> materials = controller.getEventConstraints().getMaterialNames();

            int goal = controller.getQuestData().getQuestGoal();
            int completed = (int)controller.getQuestData().getAmountCompleted();
            if (materials.isEmpty() || MaterialUtil.containsMaterial(itemsToAdd.getType().toString(), materials)) {
                if (goal > 0 && completed + itemsToAdd.getAmount() > goal) {
                    int difference = completed + itemsToAdd.getAmount() - goal;
                    ItemStack itemsToReturn = new ItemStack(itemsToAdd.getType(), difference);
                    player.getInventory().addItem(itemsToReturn);
                }

                updateQuest(controller, player, itemsToAdd.getAmount());
                isItemUsed = true;
            }
        }
        return isItemUsed;
    }

    /**
     * Attempts to donate a list of items to any quest which needs donations.
     * @param itemsToAdd list of items to add.
     * @param player player to add return items too.
     * @return true if any items in the list were returned to the player, else false
     */
    public boolean tryDonateItemsToQuest(List<ItemStack> itemsToAdd, Player player) {
        List<QuestController> guiControllers = tryGetControllersOfEventType(ObjectiveType.GUI);

        List<ItemStack> rejectedItems = new ArrayList<>();

        if (guiControllers.isEmpty()) {
            rejectedItems.addAll(itemsToAdd);
        }

        for (QuestController controller : guiControllers) {
            List<String> materials = controller.getEventConstraints().getMaterialNames();
            int goal = controller.getQuestData().getQuestGoal();
            int completed = (int) controller.getQuestData().getAmountCompleted();

            if (controller.isCompetitive()) {
                Map<UUID, PlayerData> playerDataMap = controller.getPlayerComponent().getPlayerMap();
                PlayerData playerData = playerDataMap.get(player.getUniqueId());

                completed = (int) playerData.getAmountContributed();
            }

            for (ItemStack item : itemsToAdd) {
                if (materials.isEmpty() || MaterialUtil.containsMaterial(item.getType().toString(), materials)) {
                    if (!controller.getQuestData().isGoalComplete()) {
                        if (completed + item.getAmount() > goal) {
                            int overflow = (completed + item.getAmount()) - goal;
                            ItemStack itemsToReturn = new ItemStack(item.getType(), overflow);
                            rejectedItems.add(itemsToReturn);
                        }

                        updateQuest(controller, player, item.getAmount());
                    } else {
                        rejectedItems.add(item);
                    }
                } else {
                    rejectedItems.add(item);
                }
            }
        }

        for (ItemStack rejectedItem : rejectedItems) {
            player.getInventory().addItem(rejectedItem);
        }

        return !rejectedItems.isEmpty();
    }
}

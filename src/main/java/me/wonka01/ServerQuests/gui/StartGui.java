package me.wonka01.ServerQuests.gui;

import lombok.NonNull;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.configuration.QuestLibrary;
import me.wonka01.ServerQuests.configuration.QuestModel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.ArrayList;
import java.util.List;

public class StartGui extends BaseGui implements InventoryHolder, Listener {

    private final Inventory inventory;
    private final TypeGui typeGui;
    private final ServerQuests plugin;

    public StartGui(ServerQuests plugin, TypeGui typeGui) {

        this.plugin = plugin;
        this.inventory = createInventory();
        this.typeGui = typeGui;
    }

    private @NonNull Inventory createInventory() {

        String title = plugin.getMessages().string("startQuest");
        return Bukkit.createInventory(this, 27, title);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void initializeItems() {
        inventory.clear();

        List<String> keys = new ArrayList<>(getQuestLibrary().getAllQuestKeys());
        for (int i = 0; i < keys.size(); i++) {

            String key = keys.get(i);
            QuestModel model = getQuestLibrary().getQuestModelById(key);

            ArrayList<String> lore = new ArrayList<>(model.getDescription());

            if (model.getQuestGoal() > 0) {
                String goal = plugin.getMessages().string("goal");
                lore.add(goal + ": &c" + model.getQuestGoal());
            }

            if (model.getCompleteTimeCoop() > 0) {
                String durationCoop = plugin.getMessages().string("durationCoop");
                lore.add(durationCoop+ ": &c" + model.getCompleteTimeCoop() + "s");
            }

            if (model.getCompleteTimeComp() > 0) {
                String durationCoop = plugin.getMessages().string("durationComp");
                lore.add(durationCoop+ ": &c" + model.getCompleteTimeComp() + "s");
            }

            String clickToStart = plugin.getMessages().string("clickToStart");
            lore.add(clickToStart);

            inventory.setItem(i, createGuiItem(model.getDisplayItem(), model.getDisplayName(),
                lore.toArray(new String[0])));
        }
    }

    private @NonNull QuestLibrary getQuestLibrary() {
        return plugin.getQuestLibrary();
    }

    public void openInventory(Player p) {
        p.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        if (!clickEventCheck(e, this)) {
            return;
        }
        Player player = (Player) e.getWhoClicked();

        int clickedSlot = e.getRawSlot();
        QuestModel model = null;
        int count = 0;

        for (String modelKeys : getQuestLibrary().getAllQuestKeys()) {
            if (clickedSlot == count) {
                model = getQuestLibrary().getQuestModelById(modelKeys);
                break;
            }
            count++;
        }

        if (model == null) {
            return;
        }
        typeGui.openInventory(player, model);
    }
}

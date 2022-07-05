package me.modify.townyquests.menu;

import com.modify.fundamentum.menu.Menu;
import com.modify.fundamentum.menu.MenuItem;
import com.modify.fundamentum.text.PlugLogger;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.events.GuiEvent;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.QuestController;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DonateQuestMenu implements InventoryHolder {

    private Inventory inventory;

    private final int ITEM_SLOT = 22;
    private final ServerQuests plugin;

    public GuiEvent eventHandler;

    private final Player player;
    private final String menuName;
    private final int slots;

    public DonateQuestMenu(ServerQuests plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.menuName = plugin.getMessages().string("donateMenu");
        this.slots = 27;
        this.eventHandler = new GuiEvent(ActiveQuests.getActiveQuestsInstance());
    }

    public void open() {
        this.inventory = Bukkit.createInventory(this, slots, menuName);
        this.setMenuItems();
        this.player.openInventory(this.inventory);
    }

    public void setMenuItems() {

        List<String> lore = new ArrayList<>();
        lore.add("&7Place all items you with to donate in any of the slots below.");
        lore.add("&7Invalid items you try to donate for the active quest will be returned.");
        lore.add("&7Any overflow of items for the given quest goal will also be returned.");

        MenuItem glass = new MenuItem("&cDonation Help", Material.YELLOW_STAINED_GLASS_PANE, false, lore);
        for(int i = 0; i < 27; i++) {
            if (i > 8 && i < 18) continue;
            inventory.setItem(i, glass.get());
        }
    }

    public void handleMenuClose(InventoryCloseEvent e) {
        List<ItemStack> itemsToDonate = new ArrayList<>();
        for (int i = 9; i < 18; i++) {
            ItemStack item = inventory.getItem(i);

            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            itemsToDonate.add(item);
        }


        if (eventHandler.tryDonateItemsToQuest(itemsToDonate, player)) {
            String cantDonate = plugin.getMessages().message("cantDonateItem");
            player.sendMessage(cantDonate);
        }
    }

    public void handleMenuClick(InventoryClickEvent e) {
        int slot = e.getSlot();
        int[] validSlots = {9, 10, 11, 12, 13, 14, 15, 16, 17};

        boolean valid = false;
        for (int i : validSlots) {
            if (i == slot) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            e.setCancelled(true);
        }
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}

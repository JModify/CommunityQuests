package me.modify.townyquests.listeners;

import com.modify.fundamentum.menu.Menu;
import me.modify.townyquests.menu.DonateQuestMenu;
import me.wonka01.ServerQuests.gui.DonateQuestGui;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        if (e.getClickedInventory() == null || e.getCurrentItem() == null) return;

        InventoryHolder holder = e.getClickedInventory().getHolder();

        if (holder instanceof DonateQuestMenu menu) {
            menu.handleMenuClick(e);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();

        InventoryHolder holder = e.getInventory().getHolder();

        if (holder instanceof DonateQuestMenu menu) {
            menu.handleMenuClose(e);
        }
    }
}

package ru.vlad.announcer.manager;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.vlad.announcer.AnnouncerPlugin;
import ru.vlad.announcer.data.Template;

import java.util.ArrayList;
import java.util.List;

public class GUIManager implements Listener {
    private final AnnouncerPlugin plugin;
    private final TemplateManager templates;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public GUIManager(AnnouncerPlugin plugin, TemplateManager templates) {
        this.plugin = plugin; this.templates = templates;
    }

    public void openMain(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, "📜 Панель объявлений");
        inv.setItem(10, makeItem(Material.PAPER, "§aСоздать шаблон"));
        inv.setItem(12, makeItem(Material.CHEST, "§eСписок шаблонов"));
        inv.setItem(14, makeItem(Material.CLOCK, "§6Таймеры"));
        inv.setItem(16, makeItem(Material.REDSTONE, "§cСобытия"));
        p.openInventory(inv);
    }

    public void openList(Player p) {
        List<Template> list = new ArrayList<>(templates.list());
        int rows = Math.max(9, ((list.size()+1 + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, rows, "📄 Шаблоны");
        int slot = 0;
        for (Template t : list) {
            ItemStack it = makeItem(Material.PAPER, "§f" + t.id());
            ItemMeta m = it.getItemMeta();
            List<String> lore = new ArrayList<>();
            lore.add("§7Тип: §e" + t.type());
            lore.add("§7Время (для титула): §e" + t.titleTime());
            lore.add("§7Вкл: " + (t.enabled() ? "§aДа" : "§cНет"));
            lore.add("§7ЛКМ — отправить | ПКМ — редактировать | Шифт+ЛКМ — удалить");
            m.setLore(lore);
            it.setItemMeta(m);
            inv.setItem(slot++, it);
        }
        inv.setItem(rows-1, makeItem(Material.BARRIER, "§cЗакрыть"));
        p.openInventory(inv);
    }

    private ItemStack makeItem(Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta meta = it.getItemMeta();
        meta.setDisplayName(name);
        it.setItemMeta(meta);
        return it;
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getView().title().equals("📜 Панель объявлений")) {
            e.setCancelled(true);
            Player p = (Player)e.getWhoClicked();
            int slot = e.getSlot();
            if (slot == 10) { plugin.chatEditor().startCreateSession(p); }
            if (slot == 12) { openList(p); }
            if (slot == 14) { p.sendMessage("§6Таймеры — в разработке (используйте конфиг или команды)"); }
            if (slot == 16) { p.sendMessage("§6События управляются в config.yml"); }
        }
        if (e.getView().title().startsWith("📄 Шаблоны")) {
            e.setCancelled(true);
            Player p = (Player)e.getWhoClicked();
            ItemStack cur = e.getCurrentItem();
            if (cur == null) return;
            if (cur.getType() == Material.BARRIER) { p.closeInventory(); return; }
            String id = cur.getItemMeta().getDisplayName().replace("§f", "");
            if (e.isLeftClick() && !e.isShiftClick()) {
                templates.broadcastTemplate(id, "all", p);
                p.sendMessage("§aОтправлено: " + id);
            } else if (e.isRightClick()) {
                plugin.chatEditor().startEditSession(p, id);
            } else if (e.isShiftClick()) {
                templates.remove(id);
                p.sendMessage("§cШаблон удалён: " + id);
                openList(p);
            }
        }
    }
}

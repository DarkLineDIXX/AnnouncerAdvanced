package ru.vlad.announcer.manager;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.vlad.announcer.AnnouncerPlugin;

import java.util.HashMap;
import java.util.Map;

public class ChatEditorManager implements Listener {
    private final AnnouncerPlugin plugin;
    private final TemplateManager templates;
    private final MiniMessage mm = MiniMessage.miniMessage();

    private record EditSession(String id, boolean creating, String audience, String type, int time) {}
    private final Map<java.util.UUID, EditSession> sessions = new HashMap<>();

    public ChatEditorManager(AnnouncerPlugin plugin, TemplateManager templates, GUIManager gui) {
        this.plugin = plugin; this.templates = templates;
    }

    public void startCreateSession(Player p) {
        sessions.put(p.getUniqueId(), new EditSession("", true, "all", "all", 4));
        p.sendMessage("§aРежим создания шаблона. Введите в чат ID шаблона (латиница, без пробелов):");
    }
    public void startEditSession(Player p, String id) {
        if (templates.get(id) == null) { p.sendMessage("§cШаблон не найден"); return; }
        sessions.put(p.getUniqueId(), new EditSession(id, false, templates.get(id).audience(), templates.get(id).type(), templates.get(id).titleTime()));
        p.sendMessage("§aРедактирование шаблона §f" + id + "§a. Введите новый текст (MiniMessage), или напишите 'skip' чтобы оставить:");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!sessions.containsKey(p.getUniqueId())) return;
        e.setCancelled(true);
        EditSession s = sessions.remove(p.getUniqueId());
        if (s.creating()) {
            String id = e.getMessage().trim().toLowerCase().replaceAll("[^a-z0-9_-]","_");
            p.sendMessage("§aID: " + id + " создан. Теперь введите текст (MiniMessage) для шаблона:");
            // next stage: put new partial session with id stored and wait for text
            sessions.put(p.getUniqueId(), new EditSession(id, true, "all", "all", 4));
            // store by sending again: we need a two-stage flow. For simplicity, ask user to run /announce add <id> <text> or continue conversation.
            // Here we simplify: if user typed id, ask to run command /announce add <id> <type> <audience> <time> <text>
            p.sendMessage("§7Для удобства: напиши: /announce add " + id + " <chat|actionbar|title|all> <audience> <time> <text>\nПример: /announce add " + id + " all all 4 <gradient:gold:red>Текст</gradient>");
            return;
        } else {
            String id = s.id();
            String msg = e.getMessage();
            if (msg.equalsIgnoreCase("skip")) msg = templates.get(id).text();
            templates.addOrUpdate(id, msg, s.type(), s.time(), true, s.audience());
            p.sendMessage("§aШаблон " + id + " обновлён.");
        }
    }
}

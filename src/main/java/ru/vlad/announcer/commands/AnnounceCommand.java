package ru.vlad.announcer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.vlad.announcer.AnnouncerPlugin;
import ru.vlad.announcer.manager.GUIManager;
import ru.vlad.announcer.manager.TemplateManager;

import java.util.ArrayList;
import java.util.Set;

public class AnnounceCommand implements CommandExecutor {
    private final AnnouncerPlugin plugin;
    private final TemplateManager templates;
    private final GUIManager gui;

    public AnnounceCommand(AnnouncerPlugin plugin, TemplateManager templates, GUIManager gui) {
        this.plugin = plugin; this.templates = templates; this.gui = gui;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("announcegui")) {
            if (!(sender instanceof Player)) { sender.sendMessage("Только игрокам"); return true; }
            Player p = (Player) sender;
            if (!p.hasPermission("announcer.gui") && !p.isOp()) { p.sendMessage("Нет прав"); return true; }
            gui.openMain(p); return true;
        }

        if (!sender.hasPermission("announcer.use") && !sender.isOp()) { sender.sendMessage("Нет прав"); return true; }
        if (args.length == 0) {
            sender.sendMessage("Использование: /announce <шаблон> [chat|actionbar|title|all]");
            // Получаем Set<String> с идентификаторами
            Set<String> ids = templates.ids();
            sender.sendMessage("Шаблоны: " + String.join(", ", ids));
            return true;
        }
        String id = args[0]; String mode = args.length>1?args[1]:"all";
        Player p = sender instanceof Player ? (Player)sender : null;
        templates.broadcastTemplate(id, mode, p);
        sender.sendMessage("Отправлено: " + id);
        return true;
    }
}

package ru.vlad.announcer;



import net.kyori.adventure.platform.bukkit.BukkitAudiences;

import org.bukkit.plugin.java.JavaPlugin;

import ru.vlad.announcer.manager.*;

import ru.vlad.announcer.listener.EventListener;

import ru.vlad.announcer.commands.AnnounceCommand;



public final class AnnouncerPlugin extends JavaPlugin {

    private static AnnouncerPlugin instance;

    private BukkitAudiences audiences;



    private TemplateManager templateManager;

    private GUIManager guiManager;

    private ChatEditorManager chatEditor;

    private TimerManager timerManager;



    @Override

    public void onEnable() {

        instance = this;

        audiences = getAudiences();



        saveDefaultConfig();



        templateManager = new TemplateManager(this);

        templateManager.loadFromConfig();



        guiManager = new GUIManager(this, templateManager);

        chatEditor = new ChatEditorManager(this, templateManager, guiManager);

        timerManager = new TimerManager(this, templateManager);

        timerManager.loadTimersFromConfig();



        getServer().getPluginManager().registerEvents(new EventListener(this, templateManager), this);

        getServer().getPluginManager().registerEvents(guiManager, this);

        getServer().getPluginManager().registerEvents(chatEditor, this);



        getCommand("announce").setExecutor(new AnnounceCommand(this, templateManager, guiManager));

        getCommand("announcegui").setExecutor(new AnnounceCommand(this, templateManager, guiManager));

        getCommand("announcereload").setExecutor((s, c, l, args) -> {

            templateManager.loadFromConfig();

            s.sendMessage("§aAnnouncer: шаблоны перезагружены.");

            return true;

        });



        getLogger().info("AnnouncerAdvanced v1.0 включён");

    }



    @Override

    public void onDisable() {

        if (audiences != null) { audiences.close(); audiences = null; }

        timerManager.cancelAll();

        instance = null;

    }



    private BukkitAudiences getAudiences() {

        try {

            return (BukkitAudiences) Class.forName("io.papermc.paper.adventure.PaperAudiences").getMethod("of", JavaPlugin.class).invoke(null, this);

        } catch (Exception e) {

            getLogger().severe("Не удалось получить экземпляр BukkitAudiences: " + e.getMessage());

            return null;

        }

    }



    public static AnnouncerPlugin getInstance() { return instance; }

    public BukkitAudiences audiences() { return audiences; }

    public TemplateManager templates() { return templateManager; }

    public GUIManager gui() { return guiManager; }

    public ChatEditorManager chatEditor() { return chatEditor; }

    public TimerManager timerManager() { return timerManager; }

}

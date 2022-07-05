package me.wonka01.ServerQuests;

import com.modify.fundamentum.Fundamentum;
import com.modify.fundamentum.util.PlugDebugger;
import lombok.Getter;
import lombok.NonNull;
import me.modify.townyquests.autoquest.AutoQuest;
import me.modify.townyquests.hooks.BeastTokensHook;
import me.modify.townyquests.hooks.PAPIHook;
import me.modify.townyquests.listeners.InventoryListener;
import me.modify.townyquests.rewards.CompetitiveRewardHandler;
import me.wonka01.ServerQuests.commands.CommandManager;
import me.knighthat.apis.files.Config;
import me.knighthat.apis.files.Messages;
import me.wonka01.ServerQuests.configuration.JsonQuestSave;
import me.wonka01.ServerQuests.configuration.QuestLibrary;
import me.wonka01.ServerQuests.events.*;
import me.wonka01.ServerQuests.gui.*;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.BarManager;
import me.wonka01.ServerQuests.questcomponents.QuestBar;
import me.wonka01.ServerQuests.questcomponents.players.BasePlayerComponent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;

public class ServerQuests extends JavaPlugin {

    //Configuration
    @Getter private final @NonNull Config newConfig = new Config(this);
    @Getter private final @NonNull Messages messages = new Messages(this);

    public QuestLibrary questLibrary;

    // Vault
    @Getter
    private Economy economy;

    // GUI's
    private StartGui startGui;
    private StopGui stopGui;
    private ViewGui viewGui;
    private ActiveQuests activeQuests;
    private JsonQuestSave jsonSave;

    // Hooks
    @Getter private PAPIHook papiHook;
    @Getter private BeastTokensHook beastTokensHook;


    // TownyQuests fork
    @Getter private PlugDebugger debugger;
    @Getter private AutoQuest autoQuest;
    @Getter CompetitiveRewardHandler competitiveRewardHandler;

    @Override
    public void onEnable() {

        Fundamentum.setPlugin(this);

        loadConfig();
        loadConfigurationLimits();

        new CommandManager(this);

        debugger = new PlugDebugger();
        autoQuest = new AutoQuest(this);
        competitiveRewardHandler = new CompetitiveRewardHandler(this);
        loadTownyQuests();
        handleHooks();

        loadQuestLibraryFromConfig();
        loadSaveData();

        if (!setupEconomy()) {
            getLogger().info("Warning! No economy plugin found, a cash reward can not be added to a quest in Towny Quests.");
        }

        loadGuis();
        registerOtherListeners();
        registerQuestListeners();

        getLogger().info("Plugin is enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin is disabled");
        jsonSave.saveQuestsInProgress();
        BarManager.closeBar();
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    private void loadSaveData() {
        jsonSave = new JsonQuestSave(getDataFolder(), this);
        if (jsonSave.getOrCreateQuestFile()) {
            jsonSave.readAndInitializeQuests();
            BarManager.initializeDisplayBar();
        }
    }

    private void loadQuestLibraryFromConfig() {
        ConfigurationSection serverQuestSection = getConfig().getConfigurationSection("Quests");
        questLibrary = new QuestLibrary(this);
        questLibrary.loadQuestConfiguration(serverQuestSection);
        this.activeQuests = new ActiveQuests();
    }

    /**
     * TownyQuests Edit
     * All hooks the plugin has to make connection too is handled here.
     * Only to be run on server start.
     */
    private void handleHooks() {
        papiHook = new PAPIHook(this);
        papiHook.check();
        papiHook.registerExpansion();

        beastTokensHook = new BeastTokensHook(this);
        beastTokensHook.check();
    }

    /**
     * TownyQuests Edit
     * All towny quest related loading is executed here.
     * This should be run upon plugin reload and server start.
     */
    private void loadTownyQuests() {
        boolean isDebug = getConfig().getBoolean("debug", false);
        debugger.setDebugMode(isDebug);
        competitiveRewardHandler.loadConfigurations();
        loadAutoQuest();
    }

    /**
     * TownyQuestsEdit
     * All AutoQuest related loading is executed here.
     */
    private void loadAutoQuest() {
        boolean autoQuestEnabled = getConfig().getBoolean("autoQuest.enabled", true);

        int defaultDurationCoopSeconds = getConfig().getInt("autoQuest.defaultDurationCoop", 60);
        int defaultDurationCompSeconds = getConfig().getInt("autoQuest.defaultDurationComp", 60);

        int autoQuestDelaySeconds = getConfig().getInt("autoQuest.delay", 60);

        List<String> autoQuestExempt = getConfig().getStringList("autoQuest.exemptQuests");

        autoQuest.setEnabled(autoQuestEnabled);
        autoQuest.setDefaultDurationCoop(defaultDurationCoopSeconds);
        autoQuest.setDefaultDurationComp(defaultDurationCompSeconds);
        autoQuest.getTimer().setDelay(autoQuestDelaySeconds);
        autoQuest.setAutoQuestExempt(autoQuestExempt);

        if (autoQuestEnabled) {
            long ONE_SECOND_IN_TICKS = 20;

            // Start the repeating task which automatically quests
            if (!autoQuest.getTimer().isRunning()) {
                autoQuest.getTimer().setTaskId(getServer().getScheduler().scheduleSyncRepeatingTask(this, autoQuest.getTimer(),
                    0, ONE_SECOND_IN_TICKS));
            }
        } else {
            // If this method is being run on reload, and the auto quest timer was previously
            // active but is not currently, then the timer must be cancelled from the bukkit scheduler.
            if (autoQuest.getTimer().isRunning()) {
                autoQuest.getTimer().cancelTask();
            }
        }
    }

    // TownyQuests edit
    private void loadConfigurationLimits() {
        int questLimit = getConfig().getInt("questLimit");
        String barColor = getConfig().getString("barColor").toUpperCase(Locale.ROOT);
        int leaderBoardLimit = getConfig().getInt("leaderBoardSize", 5);
        boolean disableBossBar = getConfig().getBoolean("disableBossBar", false);

        BarManager.setDisableBossBar(disableBossBar);
        BasePlayerComponent.setLeaderBoardSize(leaderBoardLimit);
        QuestBar.barColor = barColor;
        ActiveQuests.setQuestLimit(questLimit);
    }

    private void loadGuis() {
        TypeGui typeGui = new TypeGui(this);
        typeGui.initializeItems();

        viewGui = new ViewGui(this);
        startGui = new StartGui(this, typeGui);
        startGui.initializeItems();
        stopGui = new StopGui(this);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(typeGui, this);
        manager.registerEvents(startGui, this);
        manager.registerEvents(stopGui, this);
        manager.registerEvents(viewGui, this);
    }

    public void reloadConfiguration() {
        reloadConfig();
        saveConfig();
        ConfigurationSection serverQuestSection = getConfig().getConfigurationSection("Quests");
        questLibrary = new QuestLibrary(this);
        questLibrary.loadQuestConfiguration(serverQuestSection);
        loadConfigurationLimits();

        //TownyQuests edit
        loadTownyQuests();

        messages.reload();
        loadGuis();
    }

    public QuestLibrary getQuestLibrary() {
        return questLibrary;
    }

    public StartGui getStartGui() {
        return startGui;
    }

    public StopGui getStopGui() {
        return stopGui;
    }

    public ViewGui getViewGui() {
        return viewGui;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    private void registerQuestListeners() {
        getServer().getPluginManager().registerEvents(new BarManager(), this);
        getServer().getPluginManager().registerEvents(new BreakEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new CatchFishEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new KillPlayerEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new MobKillEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new ProjectileKillEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new PlaceEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new ShearEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new TameEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new MilkCowEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new CraftItemQuestEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new ConsumeItemQuestEvent(activeQuests), this);
        getServer().getPluginManager().registerEvents(new EnchantItemQuestEvent(activeQuests), this);
    }

    private void registerOtherListeners() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new InventoryListener(), this);
    }
}

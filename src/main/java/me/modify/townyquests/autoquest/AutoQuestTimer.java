package me.modify.townyquests.autoquest;

import com.modify.fundamentum.text.PlugLogger;
import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.configuration.QuestModel;
import me.wonka01.ServerQuests.enums.EventType;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.QuestController;

import java.util.*;

public class AutoQuestTimer implements Runnable {

    /** Instance of the plugin */
    private final ServerQuests plugin;

    /** The task id for this repeating timer on the bukkit scheduler */
    @Getter @Setter private int taskId;

    /** Duration in minutes which a given quest should last for. 2 minutes by default */
    @Getter @Setter private int duration;

    /** Delay in minutes between quests ending and then starting again. 2 minutes by default */
    @Getter @Setter private int delay;

    /** Internal data variable */
    private int counter;

    /** Internal data variable used to keep track of whether the timer is in delay mode */
    private TimerState timerState;

    /**
     * Internal data variable used to keep track of the previously active quest.
     * Implemented to ensure no two quests can be queued sequentially.
     */
    private String prevQuestDisplayName;

    /**
     * Constructs a new AutoQuestTimer.
     * @param plugin ServerQuests instance.
     */
    public AutoQuestTimer(ServerQuests plugin) {
        this.plugin = plugin;
        this.taskId = -1;
        this.duration = 2;
        this.delay = 2;
        this.counter = 0;
        this.timerState = TimerState.READY;
        this.prevQuestDisplayName = null;
    }

    @Override
    public void run() {
        if (plugin.getAutoQuest().isEnabled()) {
            switch(timerState) {

                // Delay is active before a new quest is started.
                case DELAYING -> {
                    plugin.getDebugger().sendDebugInfo("Triggered DELAYING state. +1 added to counter.");
                    counter += 1;

                    // If the counter is >= to the delay time. A new quest is started
                    // and the state of the timer is changed to the DURATING state.
                    if (counter >= delay) {
                        plugin.getDebugger().sendDebugInfo("DELAYING state completed. New quest started. State changed to DURATING");
                        startNewRandomQuest();
                        counter = 0;
                        timerState = TimerState.DURATING;
                    }
                }

                // State where time is decreasing in duration for the active quest.
                case DURATING -> {
                    plugin.getDebugger().sendDebugInfo("Triggered DURATING state. +1 added to counter.");
                    counter += 1;

                    // If the counter is >= to the duration of a given auto quest,
                    // the active quest must be ended and further logic checks which state
                    // the timer must then change too
                    if (counter >= duration) {
                        plugin.getDebugger().sendDebugInfo("DURATING state completed. Quest ended");
                        endActiveQuest();
                        counter = 0;

                        // Special case where there is no delay
                        // In this case, the state of the timer is not changed,
                        // it will indefinitely stay in the DURATING state and quests and start
                        // instantaneously.
                        if (delay == 0) {
                            plugin.getDebugger().sendDebugInfo("Delay is equal to 0 minutes. Next quest started. State still in DURATING.");
                            startNewRandomQuest();
                        } else {
                            plugin.getDebugger().sendDebugInfo("Timer state changed to DELAYING.");
                            timerState = TimerState.DELAYING;
                        }
                    }
                }

                // Case only reachable when server first starts.
                case READY -> {
                    if (!isConfigEmpty()) {
                        plugin.getDebugger().sendDebugInfo("READY state triggered, config is not empty.");
                        if (isQuestRunning()) {
                            plugin.getDebugger().sendDebugInfo("Quest is already running, changing state to DURATING.");
                            timerState = TimerState.DURATING;
                        } else {
                            plugin.getDebugger().sendDebugInfo("No quests running, changing state to DELAYING");
                            timerState = TimerState.DELAYING;
                        }
                    } else {
                        PlugLogger.logInfo("AutoQuest unavailable. No quests present in config file.");
                    }
                }
            }
        }
    }

    /**
     * Returns the time remaining if a quest is currently active.
     * If no quest is active, -1 is returned.
     * @return time remaining for active quest, or -1 if no quest is active.
     */
    public int getTimeRemaining() {
        if (timerState == TimerState.DURATING) {
            return duration - counter;
        } else {
            return -1;
        }
    }

    private boolean isConfigEmpty() {
        ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();
        List<QuestController> activeQuestList = activeQuests.getActiveQuestsList();

        return activeQuestList.isEmpty();
    }

    private boolean isQuestRunning() {
        ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();
        List<QuestController> activeQuestList = activeQuests.getActiveQuestsList();

        Optional<QuestController> optionalActiveQuest = activeQuestList.stream().findFirst();

        return optionalActiveQuest.isPresent();
    }

    /**
     * Randomly starts a new quest from the list configured in config.
     * If there are no quests configured, no quest will be started.
     */
    private void startNewRandomQuest() {
        ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();

        EventType randomEventType = EventType.getRandomEventType();
        QuestModel randomQuestModel = getRandomQuestModel();

        if (randomQuestModel != null) {
            activeQuests.beginNewQuest(randomQuestModel, randomEventType);
        }
    }

    /**
     * Ends the currently active quest.
     *
     * Special case:
     * If the special case as commented below is executed, the prevQuestDisplayName
     * variable will not be set and so may be null at any given time. If it is null
     * there is no guarantee that the next quest started is not the same as the previous.
     * Very confusing, but shouldn't have to worry about this if you configure
     * auto quest to be enabled when first starting the server.
     *
     */
    private void endActiveQuest() {
        ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();
        List<QuestController> activeQuestList = activeQuests.getActiveQuestsList();

        if (!activeQuestList.isEmpty()) {

            // Special case: The case where this occurs is rare, server owner must have started more than 1 quest when auto quest
            // was not active, then activated auto quest then reloaded the plugin using the /cq reload command.
            if (activeQuestList.size() > 1) {
                activeQuestList.forEach(QuestController::endQuest);
                return;
            }

            Optional<QuestController> activeQuest = activeQuestList.stream().findFirst();
            prevQuestDisplayName = String.valueOf(activeQuest.get().getQuestData().getDisplayName());
            activeQuest.get().endQuest();
        }
    }

    /**
     * Retrieves a random quest model, ensuring this new random quest
     * is not equal to the previously active quest.
     * @return a random quest model.
     */
    private QuestModel getRandomQuestModel() {
        Set<String> allQuestKeys = plugin.getQuestLibrary().getAllQuestKeys();
        List<String> allQuestKeysList = new ArrayList<>(allQuestKeys);

        if (allQuestKeysList.isEmpty()) {
            plugin.getDebugger().sendDebugInfo("AutoQuest unavailable. No quests present in config file.");
            return null;
        }

        int size = allQuestKeysList.size();
        int randIdx = new Random().nextInt(size);

        String randomQuestKey = allQuestKeysList.get(randIdx);
        QuestModel randomQuest = plugin.getQuestLibrary().getQuestModelById(randomQuestKey);

        // If there is only 1 quest configured this must execute otherwise this method will
        // recursively try and find a different quest to the previous for an indefinite amount of time.
        if (allQuestKeysList.size() == 1) {
            return randomQuest;
        }

        if (prevQuestDisplayName != null) {
            if (randomQuest.getDisplayName().equals(prevQuestDisplayName)) {
                getRandomQuestModel();
            }
        }

        return randomQuest;
    }

    /**
     * Cancels this timer object from the bukkit scheduler.
     */
    public void cancelTask() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    /**
     * Checks if this timer object is still running on the bukkit schedular.
     * This is tracked through the taskId value. If taskId is -1, then it is not running,
     * if it is anything other than -1, than it is running.
     * @return true if timer is running on bukkit scheduler
     */
    public boolean isRunning() {
        return taskId != -1;
    }

    /**
     * Represents the current state of an AutoQuest timer.
     */
    private enum TimerState {
        /**
         * AutoQuest timer in it's delaying state means that a quest has ended,
         * and a delay is currently running before the next quest can start
         */
        DELAYING,

        /**
         * AutoQuest timer in its durating state means that a quest is currently
         * active and its duration is decreasing in time
         */
        DURATING,

        /**
         * AutoQuest timer in its ready state means that the timer is ready to start a new quest.
         * This state is only reachable when the server is first started.
         */
        READY
    }
}

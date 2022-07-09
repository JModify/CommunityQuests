package me.modify.townyquests.autoquest;

import com.modify.fundamentum.text.PlugLogger;
import lombok.Getter;
import lombok.Setter;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.configuration.QuestModel;
import me.wonka01.ServerQuests.enums.EventType;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.QuestController;
import me.wonka01.ServerQuests.questcomponents.QuestData;

import java.sql.Array;
import java.util.*;

public class AutoQuestTimer implements Runnable {

    /** Instance of the plugin */
    private final ServerQuests plugin;

    /** Internal data variable */
    private int counter;

    /** Internal data variable used to keep track of whether the timer is in delay mode */
    private TimerState timerState;

    /** Currently active quest which auto quest is controlling.*/
    private QuestController activeQuest;

    /** Previously active quest which auto quest controlled */
    private QuestController previouslyActiveQuest;

    /** The task id for this repeating timer on the bukkit scheduler */
    @Getter @Setter private int taskId;

    /** Delay in seconds between quests ending and then starting again. 2 minutes by default */
    @Getter @Setter private int delay;

    /**
     * Constructs a new AutoQuestTimer.
     * @param plugin ServerQuests instance.
     */
    public AutoQuestTimer(ServerQuests plugin) {
        this.plugin = plugin;
        this.taskId = -1;
        delay = 2;
        counter = 0;
        timerState = TimerState.READY;
        activeQuest = null;
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
                        activeQuest = startNewRandomQuest();
                        counter = 0;
                        timerState = TimerState.DURATING;
                    }
                }

                // State where time is decreasing in duration for the active quest.
                case DURATING -> {

                    // Was the active quest ended through forceful means OR of goal completion.
                    if (wasActiveQuestForcefullyEnded() || activeQuest.getQuestData().isGoalComplete()) {
                        previouslyActiveQuest = activeQuest;

                        if (delay == 0) {
                            plugin.getDebugger().sendDebugInfo("Delay is equal to 0 minutes. Next quest started. State still in DURATING.");
                            activeQuest = startNewRandomQuest();
                        } else {
                            plugin.getDebugger().sendDebugInfo("Timer state changed to DELAYING.");
                            activeQuest = null;
                            timerState = TimerState.DELAYING;
                        }

                        return;
                    }

                    activeQuest.getQuestData().decreaseDuration(1);

                    // If the counter is >= to the duration of a given auto quest,
                    // the active quest must be ended and further logic checks which state
                    // the timer must then change too
                    if (activeQuest.getQuestData().getQuestDuration() <= 0) {
                        plugin.getDebugger().sendDebugInfo("DURATING state completed. Quest ended");
                        previouslyActiveQuest = endActiveQuest();

                        // Special case where there is no delay
                        // In this case, the state of the timer is not changed,
                        // it will indefinitely stay in the DURATING state and quests and start
                        // instantaneously.
                        if (delay == 0) {
                            plugin.getDebugger().sendDebugInfo("Delay is equal to 0 minutes. Next quest started. State still in DURATING.");
                            activeQuest = startNewRandomQuest();
                        } else {
                            plugin.getDebugger().sendDebugInfo("Timer state changed to DELAYING.");
                            activeQuest = null;
                            timerState = TimerState.DELAYING;
                        }
                    }
                }

                // Case only reachable when server first starts.
                case READY -> {
                    if (!isConfigEmpty()) {
                        plugin.getDebugger().sendDebugInfo("READY state triggered, config is not empty.");

                        QuestController possibleActiveQuest = getActiveQuest();
                        if (possibleActiveQuest != null) {
                            plugin.getDebugger().sendDebugInfo("Quest is already running, changing state to DURATING.");
                            activeQuest = possibleActiveQuest;
                            timerState = TimerState.DURATING;
                        } else {
                            if (delay != 0) {
                                plugin.getDebugger().sendDebugInfo("No quests running, changing state to DELAYING");
                                timerState = TimerState.DELAYING;
                            } else {
                                activeQuest = startNewRandomQuest();
                                timerState = TimerState.DURATING;
                            }
                        }
                    } else {
                        PlugLogger.logError("AutoQuest unavailable. No quests present in config file.");
                        cancelTask();
                    }
                }
            }
        }
    }

    /**
     * Returns the time remaining in seconds if a quest is currently active.
     * If no quest is active, -1 is returned.
     * @return time remaining for active quest, or -1 if no quest is active.
     */
    public int getTimeRemaining() {
        if (timerState == TimerState.DURATING) {
            int questDuration = activeQuest.getQuestData().getQuestDuration();
            return questDuration - counter;
        } else {
            return -1;
        }
    }

    /**
     * Determines whether the plugin's config file contains
     * any quest configurations.
     * @return true if the file is empty, else false.
     */
    private boolean isConfigEmpty() {
        Set<String> allQuestKeys = plugin.getQuestLibrary().getAllQuestKeys();
        return allQuestKeys.isEmpty();
    }

    /**
     * Retrieves an active quest in the active quest list which is marked as auto quest.
     * Intended to be used upon server start or reload.
     * @return currently active auto quest, or null if none.
     */
    public QuestController getActiveQuest() {
        ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();
        List<QuestController> activeQuestsList = activeQuests.getActiveQuestsList();
        Optional<QuestController> questController = activeQuestsList.stream().filter(quest -> quest.getQuestData().isAutoQuest()).findFirst();
        return questController.orElse(null);
    }

    public QuestController getActiveControlledQuest() {
        return this.activeQuest;
    }

    /**
     * Randomly starts a new quest from the list configured in config.
     * If there are no quests configured, no quest will be started.
     */
    private QuestController startNewRandomQuest() {
        ActiveQuests activeQuests = ActiveQuests.getActiveQuestsInstance();

        EventType randomEventType = EventType.getRandomEventType();
        QuestModel randomQuestModel = findNextQuestModel();

        if (randomQuestModel != null) {
            return activeQuests.beginNewQuest(randomQuestModel, randomEventType, true);
        }
        return null;
    }

    /**
     * Ends the currently active quest.
     *
     * @return QuestController of the quest that was just ended, or null if no active quests.
     */
    private QuestController endActiveQuest() {
        return activeQuest != null ? activeQuest.endQuest() : null;
    }

    /**
     * Determines whether the active quest has been forcefully ended
     * through the use of /townyquests stop.
     * @return true if forcefully stopped, else false.
     */
    public boolean wasActiveQuestForcefullyEnded() {
        return !ActiveQuests.getActiveQuestsInstance().getActiveQuestsList().contains(activeQuest);
    }

    /**
     * AutoQuest algorithm which determines which quest should be activated next.
     *
     * In summary, this algorithm considers three conditions:
     * 1. Quests which are exempt from auto quest selection.
     * 2. The previously active quest (if any).
     * 3. Quests which are already active on the server through manual activation.
     *
     * The algorithm will do everything it can to avoid activating quests which meet
     * any of these conditions. Where a given condition cannot be passed, the algorithm
     * will then select a quest which meets the condition preceding it. For example a
     * quest passes conditions 1 & 2 and reaches condition 3, but does not pass this condition,
     * a selection will be made form all the quests which passed condition 2.
     *
     * NOTE: Any selection made from a group of quest models which the algorithm
     * decides to choose from is done randomly.
     *
     * Special cases - if any of these cases are true, null is returned.
     * - All quests were marked as exempt from auto quest selection in config.
     * - There are no configured quests in the config file.
     * - The server is currently at its quest limit.
     *
     * @return QuestModel of the next quest AutoQuest should select, or null if any of the
     *         special cases are true.
     */
    private QuestModel findNextQuestModel() {
        List<QuestController> activeQuests = ActiveQuests.getActiveQuestsInstance().getActiveQuestsList();

        Set<String> allQuestKeys = plugin.getQuestLibrary().getAllQuestKeys();
        List<String> allQuestKeysList = new ArrayList<>(allQuestKeys);

        // Executed if the server has reached the limit for active quests (set in config).
        if (activeQuests.size() >= ActiveQuests.getQuestLimit()) {
            PlugLogger.logWarning("Failed to make valid auto quest selection. Quest limit reached.");
            return null;
        }

        // Almost unreachable but just in case of infinite recursion in the small chance this is the case
        if (allQuestKeysList.isEmpty()) {
            PlugLogger.logWarning("AutoQuest unavailable. No quests present in config file.");
            return null;
        }

        // If there is only 1 quest configured this must execute otherwise this method will
        // recursively try and find a different quest to the previous for an indefinite amount of time.
        if (allQuestKeysList.size() == 1) {
            PlugLogger.logWarning("Quest configuration contains only 1 quest. AutoQuest algorithm will not work as expected.");
            return plugin.getQuestLibrary().getQuestModelById(allQuestKeysList.get(0));
        }

        /*
         * EXEMPTION CHECK
         */
        // Condition which returns true if a given quest is not on the AutoQuest accept list.
        // otherwise, returns false if it is on the exempt list.
        Condition autoQuestExemptCondition = model -> {
            if (!plugin.getAutoQuest().getAutoQuestExempt().contains(model.getQuestId())) {
                return true;
            }
            return false;
        };

        List<QuestModel> exemptCheckPassed = getQuestModelsFromCondition(0, allQuestKeysList, new ArrayList<>(), autoQuestExemptCondition);

        // This case will be true if all quests have been marked as exempt.
        if (exemptCheckPassed.isEmpty()) {
            PlugLogger.logWarning("Failed to make valid auto quest selection. " +
                "All quests have been marked as exempt from auto quest selection.");
            return null;
        }

        // If there is a previously active quest, which aren't the previously active quest.
        List<QuestModel> previouslyActiveCheckPassed;
        if (previouslyActiveQuest != null) {
            /*
             * PREVIOUSLY ACTIVE QUEST CHECK
             */
            // Condition which returns true if a given quest passed the exemption check
            // and was not the previously active quest.
            Condition previouslyActiveCondition = model -> {
                if (!exemptCheckPassed.contains(model)) return false;

                if (isEqual(previouslyActiveQuest, model)) {
                    return false;
                }

                return true;
            };
            previouslyActiveCheckPassed = getQuestModelsFromCondition(0, allQuestKeysList, new ArrayList<>(), previouslyActiveCondition);
        } else {
            // If there is no previously active quest, just create a copy of all quests which passed the exempt check.
            previouslyActiveCheckPassed = new ArrayList<>(exemptCheckPassed);
        }

        // This case will be true if previous checks have returned no suitable quest.
        if (previouslyActiveCheckPassed.isEmpty()) {
            PlugLogger.logWarning("AutoQuest algorithm limited with valid quest selection. Selection may be unexpected.");
            return getRandomModelFromList(exemptCheckPassed);
        }

        /*
         * ALREADY MANUALLY RUNNING CHECK
         */
        Condition manuallyRunningCondition = model -> {
            if (!previouslyActiveCheckPassed.contains(model)) return false;

            boolean passed = true;
            for (QuestController activeQuest : activeQuests) {
                if (isEqual(activeQuest, model)) {
                    passed = false;
                }
            }
            return passed;
        };


        List<QuestModel> manuallyRunningCheckPassed = getQuestModelsFromCondition(0, allQuestKeysList, new ArrayList<>(), manuallyRunningCondition);
        if (manuallyRunningCheckPassed.isEmpty()) {
            PlugLogger.logWarning("AutoQuest algorithm limited with valid quest selection. Selection may be unexpected.");
            return getRandomModelFromList(previouslyActiveCheckPassed);
        }

        plugin.getDebugger().sendDebugInfo("Successfully retrieved random auto quest which satisfy all conditions.");
        return getRandomModelFromList(manuallyRunningCheckPassed);
    }

    /**
     * Recursive function which constructs a list of all quest models which meet a given condition.
     * @param index starting index
     * @param allQuestKeys keys of all possible quests to check.
     * @param questModels starting list of quest models - this list is added too recursively then returned.
     * @param condition condition of which quests can be added to the returned list of quest models.
     * @return list of quest models which meet the given condition, order is not randomized.
     */
    private List<QuestModel> getQuestModelsFromCondition(int index, List<String> allQuestKeys, List<QuestModel> questModels, Condition condition) {
        String randomQuestKey = allQuestKeys.get(index);

        QuestModel model = plugin.getQuestLibrary().getQuestModelById(randomQuestKey);
        if (condition.accept(model)) {
            questModels.add(model);
        }

        index += 1;
        if (index >= allQuestKeys.size()) {
            return questModels;
        }

        return getQuestModelsFromCondition(index, allQuestKeys, questModels, condition);
    }

    /**
     * Retrieves a random quest model from a list of quest models.
     * @param models list of models to randomly select one from.
     * @return quest model which was selected
     */
    private QuestModel getRandomModelFromList(List<QuestModel> models) {
        int randIdx = new Random().nextInt(models.size());
        return models.get(randIdx);
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
     * Only current way to compare equality of model and controller is
     * by comparing the display names of the type quests
     * @param controller quest controller to compare
     * @param model quest model to compare
     * @return true if equal, else false.
     */
    private boolean isEqual(QuestController controller, QuestModel model) {
        QuestData data = controller.getQuestData();
        return (model.getDisplayName().equals(data.getDisplayName()));
    }

    /** Condition which accepts QuestModels that meet it. */
    @FunctionalInterface
    private interface Condition {

        /**
         * Accept that the given quest model meets this condition.
         * @param model model to accept
         * @return true if accepted, else false.
         */
        boolean accept(QuestModel model);
    }
}

package me.modify.townyquests.autoquest;

/**
 * Represents the current state of an AutoQuest timer.
 */
public enum TimerState {
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

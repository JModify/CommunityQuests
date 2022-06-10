package me.wonka01.ServerQuests.enums;

import java.util.Random;

public enum EventType {

    COLLAB,
    COMPETITIVE;

    public static EventType getRandomEventType() {
        return values()[new Random().nextInt(values().length)];
    }
}

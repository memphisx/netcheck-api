package com.bompotis.netcheck.scheduler.batch.notification.client;

public enum MessagePriority {
    LOWEST(-2), LOW(-1), QUIET(-1), NORMAL(0), HIGH(1), EMERGENCY(2);
    private final int priority;
    
    MessagePriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return String.valueOf(this.priority);
    }
}
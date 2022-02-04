package dev.africa.pandaware.utils.logger.writer.impl;

import org.tinylog.core.LogEntry;
import org.tinylog.core.LogEntryValue;
import org.tinylog.writers.Writer;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;

public class NotificationWriter implements Writer {
    public NotificationWriter(Map<String, String> properties) {
    }

    @Override
    public Collection<LogEntryValue> getRequiredLogEntryValues() {
        return EnumSet.of(LogEntryValue.LEVEL, LogEntryValue.MESSAGE);
    }

    @Override
    public void write(LogEntry logEntry) {
//        Notification.Type notificationType;
//        switch (logEntry.getLevel()) {
//            case WARN:
//                notificationType = Notification.Type.WARNING;
//                break;
//            case ERROR:
//                notificationType = Notification.Type.ERROR;
//                break;
//            default:
//                notificationType = Notification.Type.SUCCESS;
//        }
//
//        Stitch.NOTIFICATION_MANAGER.addNotification(notificationType, logEntry.getMessage(), 3000);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}

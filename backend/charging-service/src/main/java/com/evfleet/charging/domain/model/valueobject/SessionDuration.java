package com.evfleet.charging.domain.model.valueobject;

import lombok.Value;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

@Value
public class SessionDuration implements Serializable {
    Duration duration;

    public static SessionDuration between(LocalDateTime start, LocalDateTime end) {
        return new SessionDuration(Duration.between(start, end));
    }

    public long toMinutes() {
        return duration.toMinutes();
    }

    public long toHours() {
        return duration.toHours();
    }

    @Override
    public String toString() {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%dh %dm", hours, minutes);
    }
}

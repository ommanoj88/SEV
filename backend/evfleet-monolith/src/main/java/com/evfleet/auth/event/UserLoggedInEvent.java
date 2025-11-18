package com.evfleet.auth.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

/**
 * Event published when a user logs in
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Getter
public class UserLoggedInEvent extends DomainEvent {

    private final Long userId;
    private final String email;

    public UserLoggedInEvent(Object source, Long userId, String email) {
        super(source, userId, null);
        this.userId = userId;
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("UserLoggedInEvent[userId=%d, email=%s]",
                userId, email);
    }
}

package com.evfleet.auth.event;

import com.evfleet.common.event.DomainEvent;
import lombok.Getter;

/**
 * Event published when a new user registers
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Getter
public class UserRegisteredEvent extends DomainEvent {

    private final Long userId;
    private final String email;
    private final String name;
    private final Long companyId;

    public UserRegisteredEvent(Object source, Long userId, String email, String name, Long companyId) {
        super(source, userId, companyId);
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.companyId = companyId;
    }

    @Override
    public String toString() {
        return String.format("UserRegisteredEvent[userId=%d, email=%s, name=%s]",
                userId, email, name);
    }
}

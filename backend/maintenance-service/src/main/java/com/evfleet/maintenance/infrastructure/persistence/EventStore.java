package com.evfleet.maintenance.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventStore {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void appendEvent(String aggregateId, String aggregateType, String eventType,
                           Object eventData, int version) {
        try {
            String eventJson = objectMapper.writeValueAsString(eventData);
            String sql = "INSERT INTO event_store (event_id, aggregate_id, aggregate_type, " +
                        "event_type, event_data, version, timestamp) VALUES (?, ?, ?, ?, ?::jsonb, ?, ?)";

            jdbcTemplate.update(sql,
                    UUID.randomUUID().toString(),
                    aggregateId,
                    aggregateType,
                    eventType,
                    eventJson,
                    version,
                    LocalDateTime.now()
            );

            log.info("Event appended: {} for aggregate: {}", eventType, aggregateId);
        } catch (Exception e) {
            log.error("Failed to append event", e);
            throw new RuntimeException("Event store append failed", e);
        }
    }

    public List<StoredEvent> loadEvents(String aggregateId) {
        String sql = "SELECT * FROM event_store WHERE aggregate_id = ? ORDER BY version ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new StoredEvent(
                rs.getString("event_id"),
                rs.getString("aggregate_id"),
                rs.getString("aggregate_type"),
                rs.getString("event_type"),
                rs.getString("event_data"),
                rs.getInt("version"),
                rs.getTimestamp("timestamp").toLocalDateTime()
        ), aggregateId);
    }

    public record StoredEvent(
            String eventId,
            String aggregateId,
            String aggregateType,
            String eventType,
            String eventData,
            int version,
            LocalDateTime timestamp
    ) {}
}

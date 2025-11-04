package com.evfleet.driver.domain.model.valueobject;

import lombok.Value;

@Value
public class PerformanceScore {
    int score;

    public PerformanceScore(int score) {
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Performance score must be between 0 and 100");
        }
        this.score = score;
    }

    public static PerformanceScore initial() {
        return new PerformanceScore(80);
    }

    public PerformanceScore adjust(int delta) {
        int newScore = Math.max(0, Math.min(100, this.score + delta));
        return new PerformanceScore(newScore);
    }

    public String getRating() {
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 60) return "AVERAGE";
        return "NEEDS_IMPROVEMENT";
    }
}

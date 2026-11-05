package io.bmatch.gateway.dto;

import java.time.OffsetDateTime;

// Averages of the per-game analytics (bmatch-analytics, table game_analytics) shown on profiles.
// Scores are 0..1 or null when no analysed game measured them. When the player has rated games only
// those are averaged; otherwise the averages come from debug games and `debugOnly` is true.
public record PlayerStats(long games, long ratedGames, boolean debugOnly,
                          Double memory, Double accuracy, Double speed, Double streaks, Double consistency,
                          Double performance, Integer lastMmrDelta, Boolean lastRated, OffsetDateTime lastAnalysedAt) {
    public static PlayerStats empty() {
        return new PlayerStats(0, 0, false, null, null, null, null, null, null, null, null, null);
    }
}

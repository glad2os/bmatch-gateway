package io.bmatch.gateway.services;

import io.bmatch.gateway.dto.PlayerStats;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
public class PlayerStatsService {
    // Rated games when there are any, otherwise every analysed game (debug ones).
    private static final String STATS = """
            WITH own AS (SELECT * FROM game_analytics WHERE player_id = $1),
                 counts AS (SELECT count(*) AS games, count(*) FILTER (WHERE rated) AS rated_games FROM own),
                 used AS (SELECT own.* FROM own, counts WHERE own.rated OR counts.rated_games = 0),
                 last AS (SELECT mmr_delta, rated, computed_at FROM own ORDER BY computed_at DESC LIMIT 1)
            SELECT counts.games, counts.rated_games,
                   (SELECT avg((metrics -> 'memory' ->> 'score')::float8) FROM used) AS memory,
                   (SELECT avg((metrics -> 'accuracy' ->> 'score')::float8) FROM used) AS accuracy,
                   (SELECT avg((metrics -> 'speed' ->> 'score')::float8) FROM used) AS speed,
                   (SELECT avg((metrics -> 'streaks' ->> 'score')::float8) FROM used) AS streaks,
                   (SELECT avg((metrics -> 'consistency' ->> 'score')::float8) FROM used) AS consistency,
                   (SELECT avg(performance)::float8 FROM used) AS performance,
                   last.mmr_delta, last.rated, last.computed_at
            FROM counts LEFT JOIN last ON true
            """;

    private final DatabaseClient databaseClient;

    public PlayerStatsService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<PlayerStats> statsOf(long userId) {
        return databaseClient.sql(STATS)
                .bind("$1", userId)
                .map((row, meta) -> {
                    long games = row.get("games", Long.class);
                    long rated = row.get("rated_games", Long.class);
                    return new PlayerStats(games, rated, games > 0 && rated == 0,
                            row.get("memory", Double.class), row.get("accuracy", Double.class),
                            row.get("speed", Double.class), row.get("streaks", Double.class),
                            row.get("consistency", Double.class), row.get("performance", Double.class),
                            row.get("mmr_delta", Integer.class), row.get("rated", Boolean.class),
                            row.get("computed_at", OffsetDateTime.class));
                })
                .one()
                .defaultIfEmpty(PlayerStats.empty());
    }
}

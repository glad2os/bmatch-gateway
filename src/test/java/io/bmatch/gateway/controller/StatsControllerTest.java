package io.bmatch.gateway.controller;

import io.bmatch.gateway.dto.PlayerStats;
import io.bmatch.gateway.services.PlayerStatsService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

class StatsControllerTest {
    @Test
    void returnsTheAveragesOfAPlayer() {
        PlayerStatsService service = mock(PlayerStatsService.class);
        when(service.statsOf(7L)).thenReturn(Mono.just(new PlayerStats(3, 2, false,
                0.8, 0.7, 0.6, 0.5, 0.9, 0.72, 12, true, null)));
        WebTestClient client = WebTestClient.bindToController(new StatsController(service)).build();

        client.get().uri("/api/user/public/7/stats").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.games").isEqualTo(3)
                .jsonPath("$.ratedGames").isEqualTo(2)
                .jsonPath("$.memory").isEqualTo(0.8)
                .jsonPath("$.lastMmrDelta").isEqualTo(12)
                .jsonPath("$.debugOnly").isEqualTo(false);
    }

    @Test
    void aPlayerWithoutAnalysedGamesGetsEmptyStats() {
        PlayerStatsService service = mock(PlayerStatsService.class);
        when(service.statsOf(8L)).thenReturn(Mono.just(PlayerStats.empty()));
        WebTestClient client = WebTestClient.bindToController(new StatsController(service)).build();

        client.get().uri("/api/user/public/8/stats").exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.games").isEqualTo(0)
                .jsonPath("$.memory").doesNotExist();
    }

    @Test
    void rejectsNonNumericIds() {
        WebTestClient client = WebTestClient.bindToController(new StatsController(mock(PlayerStatsService.class))).build();
        client.get().uri("/api/user/public/abc/stats").exchange().expectStatus().isNotFound();
    }
}

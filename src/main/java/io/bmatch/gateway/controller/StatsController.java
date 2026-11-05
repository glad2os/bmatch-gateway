package io.bmatch.gateway.controller;

import io.bmatch.gateway.dto.PlayerStats;
import io.bmatch.gateway.services.PlayerStatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

// Game statistics of a player, public like the profile itself (/api/user/public/** is open).
@RestController
@RequestMapping("/api/user/public")
public class StatsController {
    private final PlayerStatsService statsService;

    public StatsController(PlayerStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/{id:\\d+}/stats")
    public Mono<ResponseEntity<PlayerStats>> getStats(@PathVariable long id) {
        return statsService.statsOf(id).map(ResponseEntity::ok);
    }
}

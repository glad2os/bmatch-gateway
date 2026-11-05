package io.bmatch.gateway.helpers.profile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

// Profile links are stored as a JSON array in users.links.
public final class ProfileLinks {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProfileLinks() {
    }

    public static List<String> decode(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }

    public static String encode(List<String> links) {
        if (links == null || links.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(links);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid links", e);
        }
    }
}

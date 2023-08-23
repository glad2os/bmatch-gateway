package io.bmatch.gateway.helpers.authority;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GrantedAuthorityDeserializer extends JsonDeserializer<List<GrantedAuthority>> {

    @Override
    public List<GrantedAuthority> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<GrantedAuthority> authorities = new ArrayList<>();
        JsonNode nodes = p.getCodec().readTree(p);

        for (JsonNode node : nodes) {
            authorities.add(new SimpleGrantedAuthority(node.get("authority").asText()));
        }

        return authorities;
    }
}

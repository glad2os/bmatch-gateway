package io.bmatch.gateway.dto;

import java.util.List;

// What anyone can see on a shared profile link: no email, birthday or account flags.
public record PublicProfile(Long id, String username, Integer mmr, String firstName, String lastName,
                            List<String> links, String background) {
    public static PublicProfile of(UserProfile profile) {
        return new PublicProfile(profile.getId(), profile.getUsername(), profile.getMmr(), profile.getFirstName(),
                profile.getLastName(), profile.getLinks() == null ? List.of() : profile.getLinks(), profile.getBackground());
    }
}

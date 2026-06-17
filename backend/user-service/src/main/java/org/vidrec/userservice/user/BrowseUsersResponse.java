package org.vidrec.userservice.user;

import java.util.List;
import java.util.UUID;

/**
 * Paginated list of discoverable creators for the "Browse creators" page. Each
 * entry carries just what the follow UI needs: identity, online presence, and
 * the user's declared preferred categories. Upload count is fetched separately
 * by the client from video-service.
 */
public record BrowseUsersResponse(
    List<UserSummary> users,
    int page,
    int size,
    long totalElements
) {
    public record UserSummary(
        UUID userId,
        String username,
        String displayName,
        String profilePictureUrl,
        boolean isOnline,
        List<String> categories
    ) {}
}

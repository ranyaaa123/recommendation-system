package org.vidrec.userservice.follow;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vidrec.userservice.shared.exception.ApiException;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<Void> follow(
        @PathVariable UUID userId,
        @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
        Authentication authentication
    ) {
        followService.follow(currentUserId(headerUserId, authentication), userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<Void> unfollow(
        @PathVariable UUID userId,
        @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
        Authentication authentication
    ) {
        followService.unfollow(currentUserId(headerUserId, authentication), userId);
        return ResponseEntity.noContent().build();
    }

    // Open (no owner check) so the recommendation-service can fetch any user's
    // follow graph at scoring time. Returns only followee IDs — not sensitive.
    @GetMapping("/{userId}/following")
    public ResponseEntity<List<UUID>> following(@PathVariable UUID userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    private UUID currentUserId(String headerUserId, Authentication authentication) {
        String resolved = null;
        if (headerUserId != null && !headerUserId.isBlank()) {
            resolved = headerUserId;
        } else if (authentication != null && authentication.getPrincipal() != null) {
            resolved = authentication.getPrincipal().toString();
        }
        if (resolved == null || resolved.isBlank()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing authenticated user context.", List.of());
        }
        return UUID.fromString(resolved);
    }
}

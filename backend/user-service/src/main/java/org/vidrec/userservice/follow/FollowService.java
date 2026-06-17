package org.vidrec.userservice.follow;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vidrec.userservice.shared.exception.ApiException;
import org.vidrec.userservice.user.UserRepository;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(UUID followerId, UUID followeeId) {
        if (followerId.equals(followeeId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CANNOT_FOLLOW_SELF", "You cannot follow yourself.", List.of());
        }
        if (!userRepository.existsById(followeeId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "No user with this ID.", List.of());
        }
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            return;
        }
        followRepository.save(Follow.builder()
            .followerId(followerId)
            .followeeId(followeeId)
            .build());
    }

    @Transactional
    public void unfollow(UUID followerId, UUID followeeId) {
        followRepository.deleteByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    @Transactional(readOnly = true)
    public List<UUID> getFollowing(UUID followerId) {
        return followRepository.findByFollowerId(followerId).stream()
            .map(Follow::getFolloweeId)
            .toList();
    }
}

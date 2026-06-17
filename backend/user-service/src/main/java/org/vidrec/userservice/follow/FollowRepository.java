package org.vidrec.userservice.follow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    void deleteByFollowerIdAndFolloweeId(UUID followerId, UUID followeeId);
    List<Follow> findByFollowerId(UUID followerId);
    long countByFolloweeId(UUID followeeId);
    long countByFollowerId(UUID followerId);
}

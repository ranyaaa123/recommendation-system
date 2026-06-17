package org.vidrec.recommendationservice.recommendation;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vidrec.recommendationservice.interaction.Interaction;
import org.vidrec.recommendationservice.interaction.InteractionRepository;

/**
 * Scores candidate videos by how much the people a user follows have engaged
 * with them. For each candidate the score is the summed positive interaction
 * score across all followees, so a video several followees watched or liked
 * ranks above one nobody you follow has touched. Returns an empty map when the
 * user follows no one, which keeps the recommender on its original blend.
 */
@Service
@RequiredArgsConstructor
public class SocialScorer {

    private final UserServiceClient userServiceClient;
    private final InteractionRepository interactionRepository;

    @Transactional(readOnly = true)
    public Map<String, Double> scoreCandidates(UUID userId, List<String> candidateVideoIds) {
        if (candidateVideoIds == null || candidateVideoIds.isEmpty()) {
            return Map.of();
        }

        List<UUID> followees = userServiceClient.getFollowing(userId);
        if (followees.isEmpty()) {
            return Map.of();
        }

        Map<String, Double> scores = new LinkedHashMap<>();
        for (Interaction interaction : interactionRepository.findByUserIdInAndVideoIdIn(followees, candidateVideoIds)) {
            double score = interaction.getScore() == null ? 0.0 : interaction.getScore();
            if (score <= 0.0) {
                continue;
            }
            scores.merge(interaction.getVideoId(), score, Double::sum);
        }
        return scores;
    }
}

package org.vidrec.recommendationservice.recommendation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.vidrec.recommendationservice.content.ContentBasedService;

@Service
@RequiredArgsConstructor
public class HybridScorer {

    // Default blend, used when the user follows nobody.
    private static final double SVD_WEIGHT = 0.6;
    private static final double CONTENT_WEIGHT = 0.4;

    // Social blend, used once the user follows at least one person so that what
    // their followees watch carries real weight in the ranking.
    private static final double SOCIAL_SVD_WEIGHT = 0.45;
    private static final double SOCIAL_CONTENT_WEIGHT = 0.25;
    private static final double SOCIAL_WEIGHT = 0.30;

    private final SvdClient svdClient;
    private final ContentBasedService contentBasedService;
    private final SocialScorer socialScorer;

    public List<String> score(UUID userId, List<String> candidateVideoIds, int limit) {
        if (candidateVideoIds.isEmpty()) {
            return List.of();
        }

        Map<String, Double> svdScores = svdClient.predictBatch(userId.toString(), candidateVideoIds);
        Map<String, Double> contentScores = contentBasedService.scoreCandidates(userId, candidateVideoIds, false);
        Map<String, Double> socialScores = socialScorer.scoreCandidates(userId, candidateVideoIds);

        Map<String, Double> normalizedSvd = normalize(candidateVideoIds, svdScores);
        Map<String, Double> normalizedContent = normalize(candidateVideoIds, contentScores);
        Map<String, Double> normalizedSocial = normalize(candidateVideoIds, socialScores);

        boolean social = !socialScores.isEmpty();
        double svdWeight = social ? SOCIAL_SVD_WEIGHT : SVD_WEIGHT;
        double contentWeight = social ? SOCIAL_CONTENT_WEIGHT : CONTENT_WEIGHT;
        double socialWeight = social ? SOCIAL_WEIGHT : 0.0;

        return candidateVideoIds.stream()
            .sorted((left, right) -> Double.compare(
                blendedScore(right, normalizedSvd, normalizedContent, normalizedSocial, svdWeight, contentWeight, socialWeight),
                blendedScore(left, normalizedSvd, normalizedContent, normalizedSocial, svdWeight, contentWeight, socialWeight)
            ))
            .limit(limit)
            .toList();
    }

    private double blendedScore(String videoId, Map<String, Double> svdScores, Map<String, Double> contentScores,
                                Map<String, Double> socialScores, double svdWeight, double contentWeight, double socialWeight) {
        return svdWeight * svdScores.getOrDefault(videoId, 0.0)
            + contentWeight * contentScores.getOrDefault(videoId, 0.0)
            + socialWeight * socialScores.getOrDefault(videoId, 0.0);
    }

    private Map<String, Double> normalize(List<String> candidateVideoIds, Map<String, Double> scores) {
        double min = candidateVideoIds.stream()
            .mapToDouble(videoId -> scores.getOrDefault(videoId, 0.0))
            .min()
            .orElse(0.0);
        double max = candidateVideoIds.stream()
            .mapToDouble(videoId -> scores.getOrDefault(videoId, 0.0))
            .max()
            .orElse(0.0);

        Map<String, Double> normalized = new HashMap<>();
        if (Double.compare(min, max) == 0) {
            candidateVideoIds.forEach(videoId -> normalized.put(videoId, 0.5));
            return normalized;
        }

        candidateVideoIds.forEach(videoId -> {
            double value = scores.getOrDefault(videoId, 0.0);
            normalized.put(videoId, (value - min) / (max - min));
        });
        return normalized;
    }
}

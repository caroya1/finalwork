package com.dianping.ai.strategy;

import com.dianping.ai.client.ShopClient;
import com.dianping.ai.util.IntentParser;
import com.dianping.ai.util.ParsedIntent;
import com.dianping.common.dto.ShopDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AiRecommendStrategy implements RecommendStrategy {

    private static final Logger logger = LoggerFactory.getLogger(AiRecommendStrategy.class);

    private static final int MAX_RESULTS = 10;
    private static final double RELEVANCE_WEIGHT = 0.6;
    private static final double RATING_WEIGHT = 0.4;

    private final IntentParser intentParser;
    private final ShopClient shopClient;

    private final ThreadLocal<String> queryHolder = new ThreadLocal<>();

    public AiRecommendStrategy(IntentParser intentParser, ShopClient shopClient) {
        this.intentParser = intentParser;
        this.shopClient = shopClient;
    }

    public void setQuery(String query) {
        queryHolder.set(query);
    }

    @Override
    public String getName() {
        return "ai";
    }

    @Override
    public List<ShopDTO> recommend(Long userId, String city, Double longitude, Double latitude, Integer size) {
        String query = queryHolder.get();
        queryHolder.remove();

        int resultSize = size != null ? Math.min(size, MAX_RESULTS) : MAX_RESULTS;

        return recommendWithQuery(query, city, userId, resultSize);
    }

    public List<ShopDTO> recommendWithQuery(String query, String city, Long userId, int size) {
        int resultSize = Math.min(size, MAX_RESULTS);

        if (query == null || query.trim().isEmpty()) {
            logger.debug("Empty query, returning empty results");
            return Collections.emptyList();
        }

        if (city == null || city.trim().isEmpty()) {
            logger.debug("City is required for AI recommendation");
            return Collections.emptyList();
        }

        try {
            ParsedIntent intent = intentParser.parse(query);
            logger.info("Parsed intent for query '{}': {}", query, intent);

            if (!intent.hasAnyPreference()) {
                logger.debug("No preferences extracted, returning popular shops");
                return getFallbackShops(city, resultSize);
            }

            List<ShopDTO> shops = searchShops(city, intent);

            if (shops.isEmpty()) {
                logger.debug("No matching shops found, returning popular shops");
                return getFallbackShops(city, resultSize);
            }

            List<ShopDTO> sortedShops = sortAndScore(shops, intent);

            return sortedShops.stream()
                    .limit(resultSize)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("AI recommendation failed for query: {}", query, e);
            return getFallbackShops(city, resultSize);
        }
    }

    private List<ShopDTO> searchShops(String city, ParsedIntent intent) {
        List<ShopDTO> shops = new ArrayList<>();

        try {
            List<ShopDTO> cityShops = shopClient.listByCity(city);
            if (cityShops == null || cityShops.isEmpty()) {
                return Collections.emptyList();
            }

            for (ShopDTO shop : cityShops) {
                if (matchesIntent(shop, intent)) {
                    shops.add(shop);
                }
            }

            return shops;
        } catch (Exception e) {
            logger.error("Failed to search shops for city: {}", city, e);
            return Collections.emptyList();
        }
    }

    private boolean matchesIntent(ShopDTO shop, ParsedIntent intent) {
        if (intent.getCuisine() != null && shop.getCategory() != null) {
            if (!matchesCategory(shop.getCategory(), intent.getCuisine())) {
                return false;
            }
        }

        if (intent.getPriceRange() != null) {
            if (!matchesPriceRange(shop, intent.getPriceRange())) {
                return false;
            }
        }

        if (intent.getScene() != null && shop.getTags() != null) {
            if (!matchesScene(shop.getTags(), intent.getScene())) {
                return false;
            }
        }

        if (intent.getLocation() != null && shop.getAddress() != null) {
            if (!matchesLocation(shop.getAddress(), intent.getLocation())) {
                return false;
            }
        }

        return true;
    }

    private boolean matchesCategory(String shopCategory, String intentCuisine) {
        String lowerCategory = shopCategory.toLowerCase();
        String lowerCuisine = intentCuisine.toLowerCase();

        return lowerCategory.contains(lowerCuisine) || lowerCuisine.contains(lowerCategory);
    }

    private boolean matchesPriceRange(ShopDTO shop, String priceRange) {
        return true;
    }

    private boolean matchesScene(String shopTags, String intentScene) {
        String lowerTags = shopTags.toLowerCase();
        String lowerScene = intentScene.toLowerCase();

        return lowerTags.contains(lowerScene);
    }

    private boolean matchesLocation(String shopAddress, String intentLocation) {
        String lowerAddress = shopAddress.toLowerCase();
        String lowerLocation = intentLocation.toLowerCase();

        return lowerAddress.contains(lowerLocation);
    }

    private List<ShopDTO> sortAndScore(List<ShopDTO> shops, ParsedIntent intent) {
        return shops.stream()
                .map(shop -> calculateRelevanceScore(shop, intent))
                .sorted(Comparator.comparing(ShopDTO::getHotScore).reversed())
                .collect(Collectors.toList());
    }

    private ShopDTO calculateRelevanceScore(ShopDTO shop, ParsedIntent intent) {
        double relevanceScore = intent.getConfidence();

        double ratingScore = 0.0;
        if (shop.getRating() != null) {
            ratingScore = shop.getRating() / 5.0;
        }

        double combinedScore = (relevanceScore * RELEVANCE_WEIGHT) + (ratingScore * RATING_WEIGHT);
        shop.setHotScore(combinedScore);

        return shop;
    }

    private List<ShopDTO> getFallbackShops(String city, int size) {
        try {
            List<ShopDTO> shops = shopClient.listByCity(city);
            if (shops == null || shops.isEmpty()) {
                return Collections.emptyList();
            }

            return shops.stream()
                    .sorted(Comparator.comparing(
                            s -> s.getRating() != null ? s.getRating() : 0.0,
                            Comparator.reverseOrder()))
                    .limit(size)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get fallback shops for city: {}", city, e);
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isSupported(Long userId) {
        return true;
    }
}

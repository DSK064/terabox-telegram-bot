package com.lc.gbs.retail.client.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lc.gbs.retail.client.model.FeatureConfigResponse;
import com.lc.gbs.retail.client.service.RetailConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class FeatureConfigUtility {
    private static final Logger log = LogManager.getLogger(FeatureConfigUtility.class);
    private final RetailConfigService retailConfigService;
    private final ObjectMapper objectMapper;

    public FeatureConfigUtility(RetailConfigService retailConfigService) {
        this.retailConfigService = retailConfigService;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * This utility method is to get feature config response for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @return Feature Config response from retail config service
     */
    private FeatureConfigResponse getValue(String featureKey, String sessionType, String brand, String shopId) {
        log.info("Calling getValue with {}, {}, {}, {}", featureKey, sessionType, brand, shopId);
        List<FeatureConfigResponse> featureConfigResponses = retailConfigService.getFeatureConfigResponse();
        if (!CollectionUtils.isEmpty(featureConfigResponses)) {
            List<FeatureConfigResponse> configResponses = featureConfigResponses.stream().filter(featureConfigResponse ->
                            featureConfigResponse.getBrand().equalsIgnoreCase("ALL")
                            && featureConfigResponse.getFeatureKey().equalsIgnoreCase(featureKey)).toList();
            if (!CollectionUtils.isEmpty(configResponses)) {
                AtomicReference<FeatureConfigResponse> featureConfigFinalResponse = new AtomicReference<>(configResponses.get(0));
                featureConfigResponses.stream().filter(featureConfigResponse ->
                        !featureConfigResponse.getBrand().equalsIgnoreCase("ALL")
                                && featureConfigResponse.getFeatureKey().equalsIgnoreCase(featureKey)
                                && featureConfigResponse.getSessionType().equalsIgnoreCase(sessionType)
                                && featureConfigResponse.getBrand().equalsIgnoreCase(brand))
                        .forEach(featureConfigResponse -> {
                            if (featureConfigResponse.getShopIds().contains(shopId)) {
                                featureConfigFinalResponse.set(featureConfigResponse);
                            }
                        });
                log.info("completed getValue with response {}, {}, {}, {}", featureKey, sessionType, brand, shopId);
                return featureConfigFinalResponse.get();
            }
        }
        return null;
    }


    /**
     * This utility method is to check feature is enabled or not for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc..
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @return Feature Config response from retail config service
     */
    public boolean getBooleanConfig(String featureKey, String sessionType, String brand, String shopId) {
        FeatureConfigResponse featureConfigResponse = getValue(featureKey, sessionType, brand, shopId);
        return featureConfigResponse != null && Boolean.parseBoolean(featureConfigResponse.getIsEnabled());
    }

    /**
     * This utility method is to check feature is enabled or not for the ShopId and brand based on feature key and session type
     * If the eventStartTime is before the system config last_updated_time, then return false else return true
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc..
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @param eventStartTime  - the time when a particular event started
     * @return Feature Config response from retail config service
     */
    public boolean getBooleanConfig(String featureKey, String sessionType, String brand, String shopId, ZonedDateTime eventStartTime) {
        FeatureConfigResponse featureConfigResponse = getValue(featureKey, sessionType, brand, shopId);
        return featureConfigResponse != null && (Boolean.parseBoolean(featureConfigResponse.getIsEnabled()) && eventStartTime.isAfter(featureConfigResponse.getLastUpdatedTime()));
    }


    /**
     * This utility method is to get threshold value configured for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc..
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @return Feature Config response from retail config service
     */
    public Long getNumberConfig(String featureKey, String sessionType, String brand, String shopId) {
        FeatureConfigResponse featureConfigResponse = getValue(featureKey, sessionType, brand, shopId);
        return featureConfigResponse != null && !featureConfigResponse.getValue().equalsIgnoreCase("N/A")
                ? Long.valueOf(featureConfigResponse.getValue()) : null;
    }

    /**
     * This utility method is to get threshold value configured for the ShopId and brand based on feature key and session type and exclusively used for NDP
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc..
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @param triggerType - Trigger Type
     * @return Feature Config response from retail config service
     */
    public Long getNumberConfig(String featureKey, String sessionType, String brand, String shopId, String triggerType) {
        FeatureConfigResponse featureConfigResponse = getValue(featureKey, sessionType, brand, shopId);
        if (featureConfigResponse != null && !featureConfigResponse.getValue().equalsIgnoreCase("N/A")) {
            String ndpTriggerFields = featureConfigResponse.getNdpTriggerField();
            List<String> ndpTriggerFieldList = ndpTriggerFields != null ? Arrays.asList(ndpTriggerFields.split(",")) : Collections.emptyList();
            log.info("Feature config client : NDP trigger value for shop {}, brand {}, featureKey {}, ndpTriggerFieldList {}, wallet transaction type {}",
                    shopId, brand, featureKey, ndpTriggerFieldList, triggerType);
            return ndpTriggerFieldList.contains(triggerType) ? Long.valueOf(featureConfigResponse.getValue()) : null;
        }
        return null;
    }

    public boolean checkFeatureEnable(String featureKey) {
        log.info("started checkFeatureEnable method with : {}", featureKey);
        List<FeatureConfigResponse> featureConfigResponses = retailConfigService.getFeatureConfigResponse();
        if (!CollectionUtils.isEmpty(featureConfigResponses)) {
            List<FeatureConfigResponse> featureLevelList = featureConfigResponses.stream()
                    .filter(feature -> (featureKey.equalsIgnoreCase(feature.getFeatureKey())
                            && Boolean.parseBoolean(feature.getIsEnabled())))
                    .toList();
            AtomicBoolean canPoll = new AtomicBoolean(false);
            if (!CollectionUtils.isEmpty(featureLevelList)) {
                featureLevelList.forEach(feature -> {
                    boolean isAllBrands = "ALL".equalsIgnoreCase(feature.getBrand());
                    if (isAllBrands || !CollectionUtils.isEmpty(feature.getShopIds())) {
                        canPoll.set(true);
                    }
                });
            }
            log.info("completed checkFeatureEnable method with : {} and enable : {} ", featureKey, Optional.of(canPoll.get()));
            return canPoll.get();
        }
        return false;
    }

    /**
     * This utility method is to get threshold value configured in double value for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc..
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @return Feature Config response from retail config service
     */
    public Double getDoubleConfig(String featureKey, String sessionType, String brand, String shopId) {
        FeatureConfigResponse featureConfigResponse = getValue(featureKey, sessionType, brand, shopId);
        return featureConfigResponse != null && !featureConfigResponse.getValue().equalsIgnoreCase("N/A")
                ? Double.valueOf(featureConfigResponse.getValue()) : null;
    }


    /**
     * This utility method is to get threshold value configured in double value for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc..
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @return T  object which is configured in system config
     */
    public <T> Optional<T> getJsonConfig(String featureKey, String sessionType, String brand, String shopId, Class<T> classType) {
        FeatureConfigResponse featureConfigResponse = getValue(featureKey, sessionType, brand, shopId);
        String jsonValue = featureConfigResponse != null && !featureConfigResponse.getValue().equalsIgnoreCase("N/A")
                ? featureConfigResponse.getValue() : null;
        if (Objects.nonNull(jsonValue)) {
            try {
                return Optional.of(objectMapper.readValue(jsonValue, classType));
            } catch (JsonProcessingException e) {
                log.error("Invalid Json type {} with Object: {}", classType, jsonValue);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * This utility method is to get feature config response for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @return Feature Config response from retail config service
     */
    private List<FeatureConfigResponse> getJsonConfigs(String featureKey, String sessionType, String brand) {
        log.info("Calling getValue with {}, {}, {}", featureKey, sessionType, brand);
        List<FeatureConfigResponse> featureConfigResponses = retailConfigService.getFeatureConfigResponse();
        if (!CollectionUtils.isEmpty(featureConfigResponses)) {
            log.info("completed getValue with response {}, {}, {}", featureKey, sessionType, brand);
            return featureConfigResponses.stream().filter(featureConfigResponse ->
                                    featureConfigResponse.getFeatureKey().equalsIgnoreCase(featureKey)
                                    && featureConfigResponse.getSessionType().equalsIgnoreCase(sessionType)
                                    && (featureConfigResponse.getBrand().equalsIgnoreCase(brand) || featureConfigResponse.getBrand().equalsIgnoreCase("ALL")))
                    .toList();
        }
        return List.of();
    }

    /**
     * This utility method is to get threshold value configured in double value for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc.
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @return List<T> object which is configured in system config
     */
    public <T> List<T> getJsonConfigs(String featureKey, String sessionType, String brand, Class<T> classType) {
        List<FeatureConfigResponse> responseList = getJsonConfigs(featureKey, sessionType, brand);
        return responseList.stream()
                .filter(configResponse -> !configResponse.getValue().equalsIgnoreCase("N/A"))
                .map(configResponse -> {
                    try {
                        return objectMapper.readValue(configResponse.getValue(), classType);
                    } catch (Exception e) {
                        log.error("Invalid Json type {}", classType);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}

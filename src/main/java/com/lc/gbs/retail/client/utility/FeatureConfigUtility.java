package com.lc.gbs.retail.client.utility;

import com.lc.gbs.retail.client.model.FeatureConfigResponse;
import com.lc.gbs.retail.client.service.RetailConfigService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Component
public class FeatureConfigUtility {

    private static final Logger log = LogManager.getLogger(FeatureConfigUtility.class);

    @Autowired
    private RetailConfigService retailConfigService;

    /**
     * This utility method is to get feature config response for the ShopId and brand based on feature key and session type
     * @param featureKey - requires feature key name from retail config service feature keys ex : ndp_add_funds_key etc
     * @param sessionType - requires Session type value ex : NDP (Anonymous,LoggedIn) , ALL
     * @param brand - Brand name
     * @param shopId - Shop Id
     * @return Feature Config response from retail config service
     */
    private FeatureConfigResponse getValue(String featureKey, String sessionType, String brand, String shopId) {
        log.info("Calling getFeatureEnabledResponse with {}, {}, {}, {}", featureKey, sessionType, brand, shopId);
        List<FeatureConfigResponse> featureConfigResponses = retailConfigService.getFeatureConfigResponse();
        if (!featureConfigResponses.isEmpty()) {
            AtomicReference<FeatureConfigResponse> featureConfigFinalResponse = new AtomicReference<>(featureConfigResponses.stream().filter(featureConfigResponse ->
                            featureConfigResponse.getBrand().equalsIgnoreCase("ALL")
                                    && featureConfigResponse.getFeatureKey().equalsIgnoreCase(featureKey))
                    .collect(Collectors.toList()).get(0));
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
            return featureConfigFinalResponse.get();
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
        return featureConfigResponse != null ? Boolean.parseBoolean(featureConfigResponse.getIsEnabled()) : false;
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
        if(featureConfigResponse != null && !featureConfigResponse.getValue().equalsIgnoreCase("N/A")){
            String ndpTriggerFields = featureConfigResponse.getNdpTriggerField();
            List<String> ndpTriggerFieldList = ndpTriggerFields != null ? Arrays.asList(ndpTriggerFields.split(",")) : Collections.emptyList();
            return ndpTriggerFieldList.contains(triggerType) ? Long.valueOf(featureConfigResponse.getValue()) : null;
        }
        return null;
    }

}

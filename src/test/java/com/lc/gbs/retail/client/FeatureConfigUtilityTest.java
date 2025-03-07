package com.lc.gbs.retail.client;

import com.lc.gbs.retail.client.model.FeatureConfigResponse;
import com.lc.gbs.retail.client.service.RetailConfigService;
import com.lc.gbs.retail.client.utility.FeatureConfigUtility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeatureConfigUtilityTest {

    @Mock
    private RetailConfigService retailConfigService;

    @InjectMocks
    private FeatureConfigUtility featureConfigUtility;


    @Test
    public void featureConfigUtilityTest(){
        List<FeatureConfigResponse> featureConfigResponseList = getMockFeatureConfigs();
        when(retailConfigService.getFeatureConfigResponse()).thenReturn(featureConfigResponseList);
        assertEquals(true,featureConfigUtility.getBooleanConfig("auto_vt_key","ALL","LADBROKEUK","1000"));
        assertEquals(false,featureConfigUtility.getBooleanConfig("shop_alert_key","ALL","LADBROKEUK","1000"));
        assertEquals(true,featureConfigUtility.getBooleanConfig("shop_alert_key","ALL","LADBROKEUK","1004"));
        assertEquals(null,featureConfigUtility.getNumberConfig("ndp_bet_key","Anonymous","LADBROKEUK","1001"));
        assertEquals(200,featureConfigUtility.getNumberConfig("ndp_bet_key","Anonymous","CORAL","1003"));
        assertEquals(null,featureConfigUtility.getNumberConfig("ndp_funds_key","Anonymous","CORAL","1001"));
        assertEquals(300,featureConfigUtility.getNumberConfig("ndp_funds_key","Anonymous","LADBROKEUK","1005"));
        assertEquals(null,featureConfigUtility.getNumberConfig("ndp_funds_key","Anonymous","LADBROKEUK","1001","Physical Cash"));
        assertEquals(150,featureConfigUtility.getNumberConfig("ndp_funds_key","Anonymous","LADBROKEUK","2004","Physical Cash"));
        assertEquals(false, featureConfigUtility.getBooleanConfig("payment_tracking","ALL","LADBROKEUK","2024", ZonedDateTime.of(
                2024, 11, 3, 12, 20, 59,
                90000, ZoneId.systemDefault())));
        assertEquals(true, featureConfigUtility.getBooleanConfig("payment_tracking","ALL","LADBROKEUK","2024", ZonedDateTime.of(
                2024, 12, 25, 12, 20, 59,
                90000, ZoneId.systemDefault())));
    }

    @Test
    public void testCheckForBatchPolling() {
        List<FeatureConfigResponse> featureConfigResponseList = getMockFeatureConfigs();
        featureConfigResponseList.get(0).setFeatureKey("live_balance_alert_time");
        featureConfigResponseList.get(1).setFeatureKey("live_balance_alert_time");
        when(retailConfigService.getFeatureConfigResponse()).thenReturn(featureConfigResponseList);
        assertTrue(featureConfigUtility.checkFeatureEnable("live_balance_alert_time"));
    }

    private List<FeatureConfigResponse> getMockFeatureConfigs() {
        List<FeatureConfigResponse> featureConfigResponseList = new ArrayList<>();
        Set<String> shopIds = new HashSet<>();
        shopIds.add("1001");
        shopIds.add("1002");
        shopIds.add("1003");
        shopIds.add("1004");
        shopIds.add("1005");
        FeatureConfigResponse featureConfigResponse1 = FeatureConfigResponse.builder()
                .featureKey("auto_vt_key")
                .brand("ALL")
                .sessionType("ALL")
                .isEnabled("TRUE")
                .value(null)
                .build();
        featureConfigResponseList.add(featureConfigResponse1);
        FeatureConfigResponse featureConfigResponse2 = FeatureConfigResponse.builder()
                .featureKey("shop_alert_key")
                .brand("ALL")
                .sessionType("ALL")
                .isEnabled("FALSE")
                .shopIds(shopIds)
                .build();
        FeatureConfigResponse featureConfigResponse2_1 = FeatureConfigResponse.builder()
                .featureKey("shop_alert_key")
                .brand("LADBROKEUK")
                .sessionType("ALL")
                .isEnabled("TRUE")
                .shopIds(shopIds)
                .build();
        featureConfigResponseList.add(featureConfigResponse2);
        featureConfigResponseList.add(featureConfigResponse2_1);
        FeatureConfigResponse featureConfigResponse3 = FeatureConfigResponse.builder()
                .featureKey("ndp_bet_key")
                .brand("ALL")
                .sessionType("Anonymous")
                .isEnabled("FALSE")
                .value("N/A")
                .build();
        FeatureConfigResponse featureConfigResponse3_1 = FeatureConfigResponse.builder()
                .featureKey("ndp_bet_key")
                .brand("CORAL")
                .sessionType("Anonymous")
                .isEnabled("TRUE")
                .shopIds(shopIds)
                .value("200")
                .build();
        featureConfigResponseList.add(featureConfigResponse3);
        featureConfigResponseList.add(featureConfigResponse3_1);
        FeatureConfigResponse featureConfigResponse4 = FeatureConfigResponse.builder()
                .featureKey("ndp_funds_key")
                .brand("LADBROKEUK")
                .sessionType("Anonymous")
                .isEnabled("TRUE")
                .shopIds(shopIds)
                .value("300")
                .build();
        featureConfigResponseList.add(featureConfigResponse4);
        shopIds = new HashSet<>();
        shopIds.add("2001");
        shopIds.add("2002");
        shopIds.add("2003");
        shopIds.add("2004");
        shopIds.add("2005");
        FeatureConfigResponse featureConfigResponse5 = FeatureConfigResponse.builder()
                .featureKey("ndp_funds_key")
                .brand("LADBROKEUK")
                .sessionType("Anonymous")
                .isEnabled("TRUE")
                .ndpTriggerField("Physical Cash,Manager loaded cash,Bet Funds Added")
                .shopIds(shopIds)
                .value("150")
                .build();
        featureConfigResponseList.add(featureConfigResponse5);
        FeatureConfigResponse featureConfigResponse6 = FeatureConfigResponse.builder()
                .featureKey("ndp_funds_key")
                .brand("ALL")
                .sessionType("Anonymous")
                .isEnabled("FALSE")
                .value("N/A")
                .build();
        featureConfigResponseList.add(featureConfigResponse6);
        FeatureConfigResponse featureConfigResponse7 = FeatureConfigResponse.builder()
                .featureKey("payment_tracking")
                .brand("ALL")
                .sessionType("ALL")
                .isEnabled("TRUE")
                .value(null)
                .shopIds(new HashSet<>(){{add("2024");}})
                .lastUpdatedTime(ZonedDateTime.of(
                        2024, 12, 3, 12, 20, 59,
                        90000, ZoneId.systemDefault()))
                .build();
        featureConfigResponseList.add(featureConfigResponse7);
        return featureConfigResponseList;
    }

    @Test
    void testGetJsonConfigWithFeatureKeyWithBrand() {
        record PresetConfig(String name, String brand, int order, Criteria criteria) {
            record Criteria(List<String> category,List<String> typeIds, List<String> typeNames, String typeNamesInclude) {}
        }
        String jsonValue = """ 
                {
                		"name": "SIS Virtuals",
                		"brand": "LADBROKES",
                		"order": 6,
                		"criteria": {
                			"category": [
                				"Greyhound Racing",
                				"Horse Racing"
                			],
                			"typeIds": [
                				"virtual"
                			],
                			"typeNames": [
                				"Portman Park",
                				"Sprint Valley",
                				"Steepledowns",
                				"Brushwood",
                				"Millersfield"
                			],
                			"typeNamesInclude": "INCLUDE"
                		}
                	}
                """;
        ArrayList<FeatureConfigResponse> featureConfigResponseList = new ArrayList<>();
        FeatureConfigResponse.FeatureConfigResponseBuilder featureKeyResult = FeatureConfigResponse.builder()
                .brand("LADBROKES")
                .featureKey("preset_config_key");
        FeatureConfigResponse.FeatureConfigResponseBuilder sessionTypeResult = featureKeyResult
                .lastUpdatedTime(ZonedDateTime.now())
                .ndpTriggerField("false")
                .sessionType("ALL");
        FeatureConfigResponse buildResult = sessionTypeResult.shopIds(new HashSet<>()).value(jsonValue).build();
        featureConfigResponseList.add(buildResult);
        when(retailConfigService.getFeatureConfigResponse()).thenReturn(featureConfigResponseList);
        Class<PresetConfig> classType = PresetConfig.class;

        // Act
        List<PresetConfig> actualJsonConfig = featureConfigUtility.getJsonConfigs("preset_config_key", "ALL", "LADBROKES",
                classType);

        // Assert
        verify(retailConfigService).getFeatureConfigResponse();
        assertEquals(1,actualJsonConfig.size());
        assertTrue( actualJsonConfig.get(0).criteria().category().contains("Greyhound Racing"));
    }

}

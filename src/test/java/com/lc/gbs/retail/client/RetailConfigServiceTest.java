package com.lc.gbs.retail.client;

import com.lc.gbs.retail.client.model.FeatureConfigResponse;
import com.lc.gbs.retail.client.service.RetailConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RetailConfigServiceTest {

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private RetailConfigService retailConfigService;

    @Test
    public void getFeatureConfigResponseTest() {
        String retailConfigServiceUrl = "http://test/api/retail/shoplist";
        List<FeatureConfigResponse> expectedResponse = getMockFeatureConfigs();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(retailConfigServiceUrl);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(uriBuilder.build().toUri())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(MediaType.APPLICATION_JSON)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(new ParameterizedTypeReference<List<FeatureConfigResponse>>() {})).thenReturn(Mono.just(expectedResponse));
        retailConfigService.setRetailConfigServiceUrl(retailConfigServiceUrl);
        retailConfigService.setFeatureKeys("auto_vt_key,shop_alert_key");
        List<FeatureConfigResponse> result = retailConfigService.getFeatureConfigResponse();
        assertEquals(expectedResponse, result);
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
        featureConfigResponseList.add(featureConfigResponse2);
        FeatureConfigResponse featureConfigResponse3 = FeatureConfigResponse.builder()
                .featureKey("ndp_bet_key")
                .brand("ALL")
                .sessionType("Anonymous")
                .isEnabled("FALSE")
                .value("N/A")
                .build();
        featureConfigResponseList.add(featureConfigResponse3);
        FeatureConfigResponse featureConfigResponse4 = FeatureConfigResponse.builder()
                .featureKey("ndp_funds_key")
                .brand("LADBROKEUK")
                .sessionType("Anonymous")
                .isEnabled("TRUE")
                .shopIds(shopIds)
                .value("300")
                .build();
        featureConfigResponseList.add(featureConfigResponse4);
        return featureConfigResponseList;
    }

}

package com.lc.gbs.retail.client.service;

import com.lc.gbs.retail.client.model.FeatureConfigResponse;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.List;

@Service
@Data
public class RetailConfigService {

    private static final Logger log = LogManager.getLogger(RetailConfigService.class);

    @Autowired
    private WebClient webClient;

    @Value("${retail.config.service.url}")
    private String retailConfigServiceUrl ;

    /**
     * This is the aggregate method to get list of all feature configs shop list from retail config service
     * Based on parent scheduler cron job value this cache get refreshed and get updated from retail service
     * @return List of feature config responses
     */
    @Cacheable(value = "configCache")
    public List<FeatureConfigResponse> getFeatureConfigResponse() {
        List<FeatureConfigResponse> systemConfigResponses = null;
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(retailConfigServiceUrl);
            log.info("getFeatureConfigResponse from retail config service started with endpoint = {}", retailConfigServiceUrl);
            systemConfigResponses = webClient.get().uri(builder.build().toUri()).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON)
                    .retrieve().bodyToMono(new ParameterizedTypeReference<List<FeatureConfigResponse>>() {}).block();
            log.info("getFeatureConfigResponse from retail config service completed  with response ");
            return systemConfigResponses;
        } catch (WebClientResponseException wcre) {
            log.error("getFeatureConfigResponse from retail config service with error {} ", wcre.getResponseBodyAsString(), wcre);
        } catch (Exception ex) {
            log.error("getFeatureConfigResponse from retail config service failed with error {}", ex.getMessage());
        }
        return systemConfigResponses;
    }


}

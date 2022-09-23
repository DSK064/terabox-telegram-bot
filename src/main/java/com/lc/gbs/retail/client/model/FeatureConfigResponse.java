package com.lc.gbs.retail.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FeatureConfigResponse {
    private String featureKey;
    private String sessionType;
    private String brand;
    private String isEnabled;
    private String value;
    private Set<String> shopIds;
}

package com.lc.gbs.retail.client.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@ConditionalOnProperty(name = "enable.featureconfig.shoplist.cache.scheduling", matchIfMissing = false)
public class ScheduleEnablingConfig {}
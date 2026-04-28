package com.ledger.ledgerservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.application.thread-pool")
public class ThreadPoolProperty {

    private RequestLogProperty requestLog;
    private BusinessLogProperty businessLog;
    private StatisticsProperty statistics;

    @Getter
    @Setter
    public static class RequestLogProperty {

        private Integer poolMaxSize;
        private Integer poolCoreSize;
        private Integer queueCapacity;
        private Integer keepAliveSeconds;
        private String threadNamePrefix;

    }

    @Getter
    @Setter
    public static class BusinessLogProperty {

        private Integer poolMaxSize;
        private Integer poolCoreSize;
        private Integer queueCapacity;
        private Integer keepAliveSeconds;
        private String threadNamePrefix;

    }

    @Getter
    @Setter
    public static class StatisticsProperty {

        private Integer poolMaxSize;
        private Integer poolCoreSize;
        private Integer queueCapacity;
        private Integer keepAliveSeconds;
        private String threadNamePrefix;

    }

    @Getter
    @Setter
    public static class NotificationProperty {

        private Integer poolMaxSize;
        private Integer poolCoreSize;
        private Integer queueCapacity;
        private Integer keepAliveSeconds;
        private String threadNamePrefix;

    }

    @Getter
    @Setter
    public static class ExportProperty {

        private Integer poolMaxSize;
        private Integer poolCoreSize;
        private Integer queueCapacity;
        private Integer keepAliveSeconds;
        private String threadNamePrefix;

    }

}

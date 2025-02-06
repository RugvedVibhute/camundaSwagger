package dev.rugved.camundaSwagger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "queries")
public class QueryConfig {

    private Map<String, String> wbsHeader;
    private Map<String, String> networkElementType; // Add this

    public Map<String, String> getWbsHeader() {
        return wbsHeader;
    }

    public void setWbsHeader(Map<String, String> wbsHeader) {
        this.wbsHeader = wbsHeader;
    }

    public Map<String, String> getNetworkElementType() { // Add this getter
        return networkElementType;
    }

    public void setNetworkElementType(Map<String, String> networkElementType) { // Add this setter
        this.networkElementType = networkElementType;
    }
}

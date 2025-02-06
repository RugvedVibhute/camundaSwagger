package dev.rugved.camundaSwagger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "queries")
public class QueryConfig {

    private Map<String, String> wbsHeader;
    private Map<String, String> networkElementType;
    private Map<String, String> ntuNniSfpOrAaSfp;
    private Map<String, String> ntuType; // Add this

    public Map<String, String> getWbsHeader() {
        return wbsHeader;
    }

    public void setWbsHeader(Map<String, String> wbsHeader) {
        this.wbsHeader = wbsHeader;
    }

    public Map<String, String> getNetworkElementType() {
        return networkElementType;
    }

    public void setNetworkElementType(Map<String, String> networkElementType) {
        this.networkElementType = networkElementType;
    }

    public Map<String, String> getNtuNniSfpOrAaSfp() {
        return ntuNniSfpOrAaSfp;
    }

    public void setNtuNniSfpOrAaSfp(Map<String, String> ntuNniSfpOrAaSfp) {
        this.ntuNniSfpOrAaSfp = ntuNniSfpOrAaSfp;
    }

    public Map<String, String> getNtuType() { // Add this getter
        return ntuType;
    }

    public void setNtuType(Map<String, String> ntuType) { // Add this setter
        this.ntuType = ntuType;
    }
}

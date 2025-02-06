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
    private Map<String, String> ntuType;
    private Map<String, String> skuId; // Add this

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

    public Map<String, String> getNtuType() {
        return ntuType;
    }

    public void setNtuType(Map<String, String> ntuType) {
        this.ntuType = ntuType;
    }

    public Map<String, String> getSkuId() { // Add this getter
        return skuId;
    }

    public void setSkuId(Map<String, String> skuId) { // Add this setter
        this.skuId = skuId;
    }
}

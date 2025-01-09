package dev.rugved.camundaSwagger.entity;

public class OtherAddress {

    private String soldToAddressRole;
    private String soldToAddressId;
    private String networkSiteAddressRole;
    private String networkSiteAddressId;
    private String additionalPartnerAddressRole;
    private String additionalPartnerAddressId;

    // Getters and Setters
    public String getSoldToAddressRole() {
        return soldToAddressRole;
    }

    public void setSoldToAddressRole(String soldToAddressRole) {
        this.soldToAddressRole = soldToAddressRole;
    }

    public String getSoldToAddressId() {
        return soldToAddressId;
    }

    public void setSoldToAddressId(String soldToAddressId) {
        this.soldToAddressId = soldToAddressId;
    }

    public String getNetworkSiteAddressRole() {
        return networkSiteAddressRole;
    }

    public void setNetworkSiteAddressRole(String networkSiteAddressRole) {
        this.networkSiteAddressRole = networkSiteAddressRole;
    }

    public String getNetworkSiteAddressId() {
        return networkSiteAddressId;
    }

    public void setNetworkSiteAddressId(String networkSiteAddressId) {
        this.networkSiteAddressId = networkSiteAddressId;
    }

    public String getAdditionalPartnerAddressRole() {
        return additionalPartnerAddressRole;
    }

    public void setAdditionalPartnerAddressRole(String additionalPartnerAddressRole) {
        this.additionalPartnerAddressRole = additionalPartnerAddressRole;
    }

    public String getAdditionalPartnerAddressId() {
        return additionalPartnerAddressId;
    }

    public void setAdditionalPartnerAddressId(String additionalPartnerAddressId) {
        this.additionalPartnerAddressId = additionalPartnerAddressId;
    }
}


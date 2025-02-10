package dev.rugved.camundaSwagger.util;

public class Constants {

    public static final String JOB_TYPE_FETCH_WBS_DETAILS = "fetchWBSDetails";
    public static final String JOB_TYPE_SHIP_TO_ADDRESS = "shipToAddress";
    public static final String JOB_TYPE_HARDWARE_TO_BE_SHIPPED = "HardwareToBeShipped";
    public static final String JOB_TYPE_FETCHVARIABLES = "fetchVariables";

    public static final String STATE_OR_PROVINCE = "stateOrProvince";
    public static final String WBS_HEADER = "wbsHeader";
    public static final String CUSTOMER_TYPE = "customerType";
    public static final String CUSTOMER_SUBTYPE = "customerSubType";
    public static final String ERROR_MESSAGE = "errorMessage";

    public static final String SHIP_TO_ADDRESSES = "shipToAddresses";
    public static final String SHIP_TO_ADDRESS_ID = "shipToAddressId";
    public static final String SHIP_TO_ADDRESS_ROLE = "shipToAddressRole";

    public static final String OTHER_ADDRESSES = "otherAddresses";
    public static final String SOLD_TO_ADDRESS_ROLE = "soldToAddressRole";
    public static final String SOLD_TO_ADDRESS_ID = "soldToAddressId";
    public static final String NETWORK_SITE_ADDRESS_ROLE = "networkSiteAddressRole";
    public static final String NETWORK_SITE_ADDRESS_ID = "networkSiteAddressId";
    public static final String ADDITIONAL_PARTNER_ADDRESS_ROLE = "additionalPartnerAddressRole";
    public static final String ADDITIONAL_PARTNER_ADDRESS_ID = "additionalPartnerAddressId";

    // HardwareToBeShipped Constants
    public static final String NETWORK_ELEMENT = "networkElement";

    public static final String DISTANCE = "distance";
    public static final String NTU_REQUIRED_REQUEST = "NTURequired";
    public static final String NTU_REQUIRED = "ntuRequired";
    public static final String NTU_SIZE = "ntuSize";
    public static final String UNI_PORT_CAPACITY_REQUEST = "UNIPortCapacity";
    public static final String UNI_PORT_CAPACITY = "uniPortCapacity";
    public static final String UNI_INTERFACE_TYPE = "uniInterfaceType";

    public static final String NTU_TYPE = "ntuType";
    public static final String NTU_TYPE_SKU_ID = "ntuTypeSkuId";
    public static final String NTU_NNI_SFP = "ntuNniSfp";
    public static final String NTU_NNI_SFP_SKU_ID = "ntuNniSfpSkuId";
    public static final String AA_SFP = "aaSfp";
    public static final String AA_SFP_SKU_ID = "aaSfpSkuId";
    public static final String AA_UNI_SFP = "aaUniSfp";
    public static final String SKU_ID = "skuId";

    public static final String RELATED_PARTY = "relatedParty";
    public static final String CONTACT_MEDIUM = "contactMedium";
    public static final String CHARACTERISTIC = "characteristic";
    public static final String SHIPPING_ORDER_ITEM = "shippingOrderItem";
    public static final String SHIPMENT = "shipment";
    public static final String SHIPMENT_ITEM = "shipmentItem";
    public static final String PRODUCT = "product";
    public static final String PRODUCT_CHARACTERISTIC = "productCharacteristic";
    public static final String INTERFACE_TYPE = "InterfaceType";
    public static final String SHIPPING_ORDER_CHARACTERISTIC = "shippingOrderCharacteristic";
    public static final String INSTALLATION_METHOD = "InstallationMethod";

    public static final String VENDOR_TYPE = "vendorType";
    public static final String DISTANCE_RANGES = "distanceRanges";

    public static final String FIND_BY_NETWORK_ELEMENT = "findByNetworkElement";
    public static final String FIND_NTU_NNI_SFP = "findNtuNniSfp";
    public static final String FIND_AA_SFP = "findAaSfp";
    public static final String FIND_BY_NTU_SIZE = "findByNtuSize";
    public static final String FIND_ADDRESSES_BY_STATE = "findAddressesByState";
    public static final String FIND_OTHER_ADDRESSES = "findOtherAddresses";
    public static final String FIND_BY_AA_UNI_SFP = "findByAaUniSfp";
    public static final String FIND_AA_UNI_SFP = "findAaUniSfp";
    public static final String FIND_BY_STATE_OR_PROVINCE = "findByStateOrProvince";


    private Constants() {
        // Prevent instantiation
    }
}

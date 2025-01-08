import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExtractStateOrProvince {
    public static void main(String[] args) throws Exception {
        String var = "{...}"; // Replace with your JSON string

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(var);

        JsonNode relatedParty = rootNode.path("relatedParty");
        String stateOrProvinceValue = null;

        if (relatedParty.isArray()) {
            for (JsonNode party : relatedParty) {
                JsonNode contactMedium = party.path("contactMedium");
                if (contactMedium.isArray() && contactMedium.size() > 0) {
                    JsonNode stateOrProvince = contactMedium.get(0)
                            .path("characteristic")
                            .path("stateOrProvince");
                    if (!stateOrProvince.isMissingNode()) {
                        stateOrProvinceValue = stateOrProvince.asText();
                        break; // Exit loop once found
                    }
                }
            }
        }

        if (stateOrProvinceValue != null) {
            System.out.println("State or Province: " + stateOrProvinceValue);
        } else {
            System.out.println("State or Province not found.");
        }
    }
}

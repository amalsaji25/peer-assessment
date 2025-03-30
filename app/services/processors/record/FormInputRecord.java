package services.processors.record;

import java.util.HashMap;
import java.util.Map;

public class FormInputRecord implements InputRecord {

    private final Map<String, String> formData = new HashMap<>();

    public FormInputRecord(Map<String, String[]> formParams) {
        formParams.forEach((key, value) -> {
             if(value != null && value.length > 0) {
                 formData.put(key, value[0]);
             }
         });
    }

    @Override
    public String get(String key) {
        return formData.getOrDefault(key, "");
    }

    @Override
    public boolean isMapped(String key) {
        return formData.containsKey(key);
    }

    @Override
    public String toString() {
        return "FormInputRecord{" +
                "data=" + formData +
                '}';
    }
}

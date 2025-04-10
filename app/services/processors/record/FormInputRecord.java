package services.processors.record;

import java.util.HashMap;
import java.util.Map;

/**
 * FormInputRecord is a class that implements the InputRecord interface. It provides methods to
 * retrieve values from form data and check if a key is mapped.
 */
public class FormInputRecord implements InputRecord {

  private final Map<String, String> formData = new HashMap<>();

  public FormInputRecord(Map<String, String[]> formParams) {
    formParams.forEach(
        (key, value) -> {
          if (value != null && value.length > 0) {
            formData.put(key, value[0]);
          }
        });
  }

  /**
   * Retrieves the value associated with the specified key from the form data.
   *
   * @param key the key whose associated value is to be returned
   * @return the value associated with the specified key
   */
  @Override
  public String get(String key) {
    return formData.getOrDefault(key, "");
  }

  /**
   * Checks if the specified key is mapped in the form data.
   *
   * @param key the key to check
   * @return true if the key is mapped, false otherwise
   */
  @Override
  public boolean isMapped(String key) {
    return formData.containsKey(key);
  }

  /**
   * Returns a string representation of the FormInputRecord object.
   *
   * @return a string representation of the FormInputRecord object
   */
  @Override
  public String toString() {
    return "FormInputRecord{" + "data=" + formData + '}';
  }
}

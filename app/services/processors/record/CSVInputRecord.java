package services.processors.record;

import org.apache.commons.csv.CSVRecord;

/**
 * CSVInputRecord is a class that implements the InputRecord interface. It provides methods to
 * retrieve values from a CSVRecord object and check if a key is mapped.
 */
public class CSVInputRecord implements InputRecord {

  private final CSVRecord record;

  public CSVInputRecord(CSVRecord record) {
    this.record = record;
  }

  /**
   * Retrieves the value associated with the specified key from the CSVRecord.
   *
   * @param key the key whose associated value is to be returned
   * @return the value associated with the specified key
   */
  @Override
  public String get(String key) {
    return record.get(key);
  }

  /**
   * Checks if the specified key is mapped in the CSVRecord.
   *
   * @param key the key to check
   * @return true if the key is mapped, false otherwise
   */
  @Override
  public boolean isMapped(String key) {
    return record.isMapped(key);
  }
}

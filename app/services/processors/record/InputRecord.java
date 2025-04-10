package services.processors.record;

/**
 * InputRecord is an interface that defines the contract for input records. It provides methods to
 * retrieve values associated with specific keys and to check if a key is mapped.
 */
public interface InputRecord {
  String get(String key);

  boolean isMapped(String key);
}

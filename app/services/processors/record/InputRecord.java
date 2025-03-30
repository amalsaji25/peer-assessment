package services.processors.record;

public interface InputRecord {
    String get(String key);
    boolean isMapped(String key);
}

package services.processors.record;

import org.apache.commons.csv.CSVRecord;

public class CSVInputRecord implements InputRecord {

    private final CSVRecord record;

    public CSVInputRecord(CSVRecord record) {
        this.record = record;
    }

    @Override
    public String get(String key) {
        return record.get(key);
    }

    @Override
    public boolean isMapped(String key) {
        return record.isMapped(key);
    }
}

package models.enums;

/**
 * FileType is an enumeration that represents different file types supported by the application.
 * Currently, it only supports CSV files.
 */
public enum FileType {
  CSV("csv");

  private final String extension;

  FileType(String extension) {
    this.extension = extension;
  }

    public static boolean isValidFileExtension(String fileName) {
        for (FileType fileType : FileType.values()) {
            if (fileName.toLowerCase().endsWith("." + fileType.getExtension())) {
                return true;
            }
        }
        return false;
    }

  public String getExtension() {
    return extension;
  }
}

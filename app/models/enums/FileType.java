package models.enums;

public enum FileType {
  CSV("csv");

  private final String extension;

  FileType(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

    public static boolean isValidFileExtension(String fileName) {
        for (FileType fileType : FileType.values()) {
            if (fileName.toLowerCase().endsWith("." + fileType.getExtension())) {
                return true;
            }
        }
        return false;
    }
}

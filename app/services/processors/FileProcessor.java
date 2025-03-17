package services.processors;

import play.mvc.Result;

import java.nio.file.Path;
import java.util.concurrent.CompletionStage;

public interface FileProcessor {
    CompletionStage<Result> processFile(Path filePath, String fileType);
}

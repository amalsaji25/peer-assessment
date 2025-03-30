package repository.core;

import models.dto.Context;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public interface Repository <T>{

    CompletionStage<Map<String, Object>> saveAll(List<T> records, Context context);

}

package com.wpss.wordpresssass.task.domain;

import java.util.List;
import java.util.Optional;

public interface AsyncTaskRepository {

    AsyncTask save(AsyncTask task);

    int update(AsyncTask task);

    Optional<AsyncTask> findByIdAndTenantId(Long id, Long tenantId);

    List<AsyncTask> findExecutableTasks(String workerId, int limit);
}

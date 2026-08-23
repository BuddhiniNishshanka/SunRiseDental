package com.sunrisedental.dao;

import com.sunrisedental.model.AuditLog;
import java.util.List;

public interface IAuditLogDAO {
    boolean logAction(AuditLog log);
    List<AuditLog> findRecentLogs(int limit);
}

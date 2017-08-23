package com.chickling.kmanager.core.db;

import java.util.List;

import com.chickling.kmanager.model.OffsetHistory;
import com.chickling.kmanager.model.OffsetHistoryQueryParams;
import com.chickling.kmanager.model.OffsetInfo;

/**
 * 
 * @author Hulva Luva.H
 *
 */
public interface OffsetDB<T> {
  T getDB();

  void insert(long timestamp, OffsetInfo offsetInfo);

  void batchInsert(List<OffsetInfo> offsetInfoList);

  OffsetHistory offsetHistory(String group, String topic);

  OffsetHistory offsetHistory(OffsetHistoryQueryParams params);

  void close();

  boolean check();
}

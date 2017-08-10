package com.chickling.kmonitor.core.db;

import java.util.List;

import com.chickling.kmonitor.model.OffsetHistory;
import com.chickling.kmonitor.model.OffsetHistoryQueryParams;
import com.chickling.kmonitor.model.OffsetInfo;

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

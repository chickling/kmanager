package com.chickling.kmonitor.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmonitor.initialize.SystemManager;
import com.chickling.kmonitor.model.OffsetHistory;
import com.chickling.kmonitor.model.OffsetHistoryQueryParams;

/**
 * @author Hulva Luva.H
 *
 */
@RestController
@RequestMapping("/query")
public class QueryController {
	private static Logger LOG = LoggerFactory.getLogger(QueryController.class);

	@RequestMapping(value = "", method = RequestMethod.POST)
	public OffsetHistory queryOffsetHistoryWithOpt(@RequestBody OffsetHistoryQueryParams params) {
		OffsetHistory offsetHistory = null;
		try {
			offsetHistory = SystemManager.db.offsetHistory(params);
		} catch (Exception e) {
			LOG.warn("offsetHistory Ops~" + e.getMessage());
		}
		return offsetHistory;
	}
}

package com.chickling.kmanager.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.initialize.SystemManager;
import com.chickling.kmanager.model.OffsetHistory;
import com.chickling.kmanager.model.OffsetHistoryQueryParams;

/**
 * @author Hulva Luva.H
 *
 */
@RestController
@CrossOrigin(origins = "*")
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

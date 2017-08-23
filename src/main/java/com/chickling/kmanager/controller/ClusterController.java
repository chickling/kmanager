package com.chickling.kmanager.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chickling.kmanager.model.BrokerInfo;
import com.chickling.kmanager.utils.ZKUtils;

/**
 * 
 * @author Hulva Luva.H
 *
 */
@RestController
@CrossOrigin(origins = "*")
public class ClusterController {

	@RequestMapping(value = "/cluster", method = RequestMethod.GET)
	public List<BrokerInfo> getCluster() throws Exception {
		return ZKUtils.getBrokers();
	}
}

package org.ekstep.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ekstep.searchindex.elasticsearch.ElasticSearchUtil;
import org.ekstep.searchindex.util.CompositeSearchConstants;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import org.ekstep.common.controller.BaseController;
import org.ekstep.common.dto.Response;
import org.ekstep.common.dto.ResponseParams;
import org.ekstep.common.dto.ResponseParams.StatusType;
import org.ekstep.common.exception.ResponseCode;

@Controller
@RequestMapping("health")
public class HealthCheckController extends BaseController {
	ElasticSearchUtil es = new ElasticSearchUtil();

	@RequestMapping(value = "", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<Response> search() {
		String name = "search-service";
		String apiId = name + ".health";
		List<Map<String, Object>> checks = new ArrayList<Map<String, Object>>();
		Response response = new Response();

		boolean index = false;
		try {
			index = es.isIndexExists(CompositeSearchConstants.COMPOSITE_SEARCH_INDEX);
			if (index == true) {
				checks.add(getResponseData(response, true, "", ""));
			} else {
				checks.add(getResponseData(response, false, "404", "Elastic Search index is not avaialable"));
			}
		} catch (Exception e) {
			checks.add(getResponseData(response, false, "503", e.getMessage()));
		}
		response.put("checks", checks);
		return getResponseEntity(response, apiId, null);
	}

	public Map<String, Object> getResponseData(Response response, boolean res, String error, String errorMsg) {
		ResponseParams params = new ResponseParams();
		String err = error;
		Map<String, Object> esCheck = new HashMap<String, Object>();
		esCheck.put("name", "ElasticSearch");
		if (res == true && err.isEmpty()) {
			params.setErr("0");
			params.setStatus(StatusType.successful.name());
			params.setErrmsg("Operation successful");
			response.setParams(params);
			response.put("healthy", true);
			esCheck.put("healthy", true);
		} else {
			params.setStatus(StatusType.failed.name());
			params.setErrmsg(errorMsg);
			response.setResponseCode(ResponseCode.SERVER_ERROR);
			response.setParams(params);
			response.put("healthy", false);
			esCheck.put("healthy", false);
			esCheck.put("err", err);
			esCheck.put("errmsg", errorMsg);
		}
		return esCheck;
	}

}
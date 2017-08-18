package com.chickling.kmanager.controller.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.chickling.kmanager.initialize.SystemManager;

/**
 * @author Hulva Luva.H
 * @since 2017-07-13
 *
 */
@Component
public class IsSystemReadyFilter implements Filter {
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String url = null;
		if (request instanceof HttpServletRequest) {
			url = ((HttpServletRequest) request).getServletPath();
		}
		if (!SystemManager.IS_SYSTEM_READY.get() && !url.contains("/css") && !url.contains("/fonts") && !url.contains("/images")
				&& !url.contains("/scripts") && !SystemManager.excludePath.contains(url)) {
			JSONObject result = new JSONObject();
			result.put("isSystemReady", false);
			response.getOutputStream().write(result.toString().getBytes());
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {
	}

}

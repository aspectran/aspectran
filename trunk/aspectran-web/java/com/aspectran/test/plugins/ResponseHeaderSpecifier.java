package com.aspectran.test.plugins;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.var.ArgumentMap;
import com.aspectran.web.activity.WebTranslet;

/**
 *
 * <p>Created: 2008. 06. 11 오후 3:44:05</p>
 *
 */
public class ResponseHeaderSpecifier {
	
	public void execute(WebTranslet translet, ArgumentMap arguments) throws Exception {
		HttpServletRequest request = translet.getHttpServletRequest();
		HttpServletResponse response = translet.getHttpServletResponse();

		for(Map.Entry<String, Object> entry : arguments.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue().toString();
			
			if(name.equals("filename")) {
				String userAgent = request.getHeader("USER-AGENT");
				
				if(userAgent.indexOf("MSIE") != -1) {
					value = new String(value.getBytes("EUC-KR"), "ISO-8859-1");
					if(userAgent.indexOf("MSIE 5.5") != -1) {
						value = "filename=\"" + value + "\";";
					} else {
						value = "attachment; filename=\"" + value + "\";";
					}
				} else {
					value = new String(value.getBytes("UTF-8"), "ISO-8859-1");
					value = "attachment; filename=\"" + value + "\";";
				}

				response.setHeader("Content-Disposition", value);
				
				System.out.println("=====================================");
				System.out.println(userAgent);
				System.out.println(entry);
				System.out.println("=====================================");
			} else {
				System.out.println(entry);
				response.setHeader(name, value);
			}
		}
	}

}

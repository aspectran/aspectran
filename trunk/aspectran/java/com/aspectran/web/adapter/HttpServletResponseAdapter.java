package com.aspectran.web.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.variable.ValueObjectMap;
import com.aspectran.core.activity.variable.token.ItemTokenExpression;
import com.aspectran.core.activity.variable.token.ItemTokenExpressor;
import com.aspectran.core.activity.variable.token.Token;
import com.aspectran.core.activity.variable.token.TokenExpression;
import com.aspectran.core.activity.variable.token.TokenExpressor;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.rule.RedirectResponseRule;

/**
 * The Class HttpServletResponseAdapter.
 * 
 * @author Gulendol
 * @since 2011. 3. 13.
 */
public class HttpServletResponseAdapter extends AbstractResponseAdapter implements ResponseAdapter {

	/** The Constant QUESTION_CHAR. */
	private static final char QUESTION_CHAR = '?';

	/** The Constant AMPERSAND_CHAR. */
	private static final char AMPERSAND_CHAR = '&';

	/** The Constant EQUAL_CHAR. */
	private static final char EQUAL_CHAR = '=';

	/**
	 * Instantiates a new http servlet response adapter.
	 *
	 * @param response the response
	 */
	public HttpServletResponseAdapter(HttpServletResponse response) {
		super(response);
	}
	
	public String getCharacterEncoding() {
		return ((HttpServletResponse)adaptee).getCharacterEncoding();
	}
	
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		((HttpServletResponse)adaptee).setCharacterEncoding(characterEncoding);
	}
	
	public String getContentType() {
		return ((HttpServletResponse)adaptee).getContentType();
	}

	public void setContentType(String contentType) {
		((HttpServletResponse)adaptee).setContentType(contentType);
	}
	
	public OutputStream getOutputStream() throws IOException {
		return ((HttpServletResponse)adaptee).getOutputStream();
	}
	
	public Writer getWriter() throws IOException {
		return ((HttpServletResponse)adaptee).getWriter();
	}
	
	public void redirect(String requestUri) throws IOException {
		((HttpServletResponse)adaptee).sendRedirect(requestUri);
	}
	
	public String redirect(CoreActivity activity, RedirectResponseRule redirectResponseRule) throws IOException {
		String characterEncoding = ((HttpServletResponse)adaptee).getCharacterEncoding();
		String url = null;
		int questionPos = -1;
		
		Token[] urlTokens = redirectResponseRule.getUrlTokens();
		//TokenValueHandler handler = new ParameterValueEncoder(characterEncoding);

		if(urlTokens != null && urlTokens.length > 0) {
			TokenExpressor expressor = new TokenExpression(activity);
			//expressor.setTokenValueHandler(handler);
			url = expressor.expressAsString(urlTokens);
		} else {
			url = redirectResponseRule.getUrl();
		}

		StringBuilder sb = new StringBuilder(256);

		if(url != null) {
			sb.append(url);
			questionPos = url.indexOf(QUESTION_CHAR);
		} else if(redirectResponseRule.getTransletName() != null) {
			sb.append(redirectResponseRule.getTransletName());
		}
		
		if(redirectResponseRule.getParameterItemRuleMap() != null) {
			ItemTokenExpressor expressor = new ItemTokenExpression(activity);
			//expressor.setTokenValueHandler(handler);
			ValueObjectMap valueMap = expressor.express(redirectResponseRule.getParameterItemRuleMap());

			if(valueMap != null && valueMap.size() > 0) {
				if(questionPos == -1)
					sb.append(QUESTION_CHAR);

				String name = null;
				Object value = null;
				
				for(Map.Entry<String, Object> entry : valueMap.entrySet()) {
					if(name != null)
						sb.append(AMPERSAND_CHAR);

					name = entry.getKey();
					value = entry.getValue();

					if(!redirectResponseRule.isExcludeNullParameter() || value != null) {
						sb.append(name).append(EQUAL_CHAR);

						if(value != null) {
							value = URLEncoder.encode(value.toString(), characterEncoding);
							sb.append(value.toString());
						}
					}
				}
			}
		}

		url = sb.toString();
		redirect(url);
		
		return url;
	}
}
/*
final class ParameterValueEncoder implements TokenValueHandler {

	private String characterEncoding;

	public ParameterValueEncoder(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public Object handle(TokenType tokenType, Object value) {
		if(tokenType == TokenType.TEXT)
			return value;

		if(characterEncoding != null && value instanceof String) {
			try {
				return URLEncoder.encode(value.toString(), characterEncoding);
			} catch(UnsupportedEncodingException e) {
			}
		}

		return value;
	}
}
*/
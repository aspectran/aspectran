package com.aspectran.web.adapter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.aspectran.core.activity.AspectranActivity;
import com.aspectran.core.adapter.AbstractResponseAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.rule.RedirectResponseRule;
import com.aspectran.core.token.Token;
import com.aspectran.core.token.expression.ItemTokenExpression;
import com.aspectran.core.token.expression.TokenExpression;
import com.aspectran.core.token.expression.TokenValueHandler;
import com.aspectran.core.type.TokenType;
import com.aspectran.core.var.ValueMap;

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
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractResponseAdapter#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return ((HttpServletResponse)adaptee).getCharacterEncoding();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractResponseAdapter#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
		((HttpServletResponse)adaptee).setCharacterEncoding(characterEncoding);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractResponseAdapter#getContentType()
	 */
	public String getContentType() {
		return ((HttpServletResponse)adaptee).getContentType();
	}

	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractResponseAdapter#setContentType(java.lang.String)
	 */
	public void setContentType(String contentType) {
		((HttpServletResponse)adaptee).setContentType(contentType);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		return ((HttpServletResponse)adaptee).getOutputStream();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#getWriter()
	 */
	public Writer getWriter() throws IOException {
		return ((HttpServletResponse)adaptee).getWriter();
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.ResponseAdapter#redirect(java.lang.String)
	 */
	public void redirect(String requestUri) throws IOException {
		((HttpServletResponse)adaptee).sendRedirect(requestUri);
	}
	
	/* (non-Javadoc)
	 * @see org.jhlabs.translets.adapter.AbstractResponseAdapter#redirect(org.jhlabs.translets.activity.Activity, org.jhlabs.translets.context.rule.RedirectResponseRule)
	 */
	public String redirect(AspectranActivity activity, RedirectResponseRule redirectResponseRule) throws IOException {
		String characterEncoding = ((HttpServletResponse)adaptee).getCharacterEncoding();
		
		Token[] urlTokens = redirectResponseRule.getUrlTokens();
		TokenValueHandler handler = new ParameterValueEncoder(characterEncoding);

		if(urlTokens != null && urlTokens.length > 0) {
			TokenExpression expressor = new TokenExpression(activity);
			expressor.setTokenValueHandler(handler);
			String url = expressor.express(urlTokens);
			redirect(url);
			return url;
		}

		StringBuilder sb = new StringBuilder(256);

		if(redirectResponseRule.getTransletName() != null) {
			sb.append(redirectResponseRule.getTransletName());
		} else if(redirectResponseRule.getUrl() != null) {
			sb.append(redirectResponseRule.getUrl());
		}

		if(redirectResponseRule.getParameterItemRuleMap() != null) {
			ItemTokenExpression expressor = new ItemTokenExpression(activity);
			expressor.setTokenValueHandler(handler);
			ValueMap valueMap = expressor.express(redirectResponseRule.getParameterItemRuleMap());

			if(valueMap != null && valueMap.size() > 0) {
				sb.append(QUESTION_CHAR);

				String name = null;
				Object value = null;
				
				for(Map.Entry<String, Object> entry : valueMap.entrySet()) {
					if(name != null)
						sb.append(AMPERSAND_CHAR);

					name = entry.getKey();
					value = entry.getValue();

					if(redirectResponseRule.getExcludeNullParameters() != Boolean.TRUE || value != null) {
						sb.append(name).append(EQUAL_CHAR);

						if(value != null) {
							sb.append(value.toString());
						}
					}
				}
			}
		}

		String url = sb.toString();
		redirect(url);
		
		return url;
	}
}

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

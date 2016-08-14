/**
 * Copyright 2008-2016 Juho Jeong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.console.activity;

import com.aspectran.console.adapter.ConsoleRequestAdapter;
import com.aspectran.console.adapter.ConsoleResponseAdapter;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.TokenType;

/**
 * The Class ConsoleActivity.
 *
 * @author Juho Jeong
 * @since 2016. 1. 18.
 */
public class ConsoleActivity extends CoreActivity {
	
	/**
	 * Instantiates a new ConsoleActivity.
	 *
	 * @param context the current ActivityContext
	 * @param sessionAdapter the session adapter
	 */
	public ConsoleActivity(ActivityContext context, SessionAdapter sessionAdapter) {
		super(context);
		
		setSessionAdapter(sessionAdapter);
	}

	@Override
	protected void adapt() throws AdapterException {
		try {
			RequestAdapter requestAdapter = new ConsoleRequestAdapter(this);
			requestAdapter.setCharacterEncoding(determineRequestCharacterEncoding());
			setRequestAdapter(requestAdapter);

			ResponseAdapter responseAdapter = new ConsoleResponseAdapter(this);
			setResponseAdapter(responseAdapter);
		} catch(Exception e) {
			throw new AdapterException("Failed to adapt for the Console Activity.", e);
		}
	}

	/**
	 * Parses the declared parameters.
	 */
	@Override
	protected void parseDeclaredParameters() {
		ItemRuleMap parameterItemRuleMap = getRequestRule().getParameterItemRuleMap();

		if(parameterItemRuleMap != null) {
			System.out.println("Required Parameters:");

			for(ItemRule itemRule : parameterItemRuleMap.values()) {
				Token[] tokens = itemRule.getTokens();
				if(tokens == null) {
					tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
				}

				System.out.printf("  @%s: %s", itemRule.getName(), TokenParser.toString(tokens));
				System.out.println();
			}

			System.out.println("Input Parameters:");

			for(ItemRule itemRule : parameterItemRuleMap.values()) {
				Token[] tokens = itemRule.getTokens();

				if(tokens != null && tokens.length > 0) {
					for(Token token : tokens) {
						if(token.getType() == TokenType.PARAMETER) {
							System.out.printf("  $%s: ", token.getName());
							if(token.getValue() != null) {
								System.out.printf("(%s)", token.getValue());
							}
							String input = System.console().readLine();
							if(input != null && !input.isEmpty()) {
								getRequestAdapter().setParameter(token.getName(), input);
							}
						}
					}
				} else {
					System.out.printf("  $%s: ", itemRule.getName());
					String input = System.console().readLine();
					if(input != null && !input.isEmpty()) {
						getRequestAdapter().setParameter(itemRule.getName(), input);
					}
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		ConsoleActivity activity = new ConsoleActivity(getActivityContext(), getSessionAdapter());
		activity.setIncluded(true);
		return (T)activity;
	}

}

/**
 * Copyright 2008-2017 Juho Jeong
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
import com.aspectran.console.inout.ConsoleInout;
import com.aspectran.console.service.ConsoleAspectranService;
import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.context.expr.token.Token;
import com.aspectran.core.context.expr.token.TokenParser;
import com.aspectran.core.context.rule.ItemRule;
import com.aspectran.core.context.rule.ItemRuleList;
import com.aspectran.core.context.rule.ItemRuleMap;
import com.aspectran.core.context.rule.type.TokenType;
import com.aspectran.core.util.StringUtils;

/**
 * The Class ConsoleActivity.
 *
 * @since 2016. 1. 18.
 */
public class ConsoleActivity extends CoreActivity {

	private final ConsoleAspectranService service;

	private final ConsoleInout consoleInout;

	/**
	 * Instantiates a new ConsoleActivity.
	 *
	 * @param service the console aspectran service
	 */
	public ConsoleActivity(ConsoleAspectranService service) {
		super(service.getActivityContext());
		setSessionAdapter(service.getSessionAdapter());

		this.service = service;
		this.consoleInout = service.getConsoleInout();
	}

	@Override
	protected void adapt() throws AdapterException {
		try {
			RequestAdapter requestAdapter = new ConsoleRequestAdapter();
			requestAdapter.setCharacterEncoding(consoleInout.getEncoding());
			setRequestAdapter(requestAdapter);

			ResponseAdapter responseAdapter = new ConsoleResponseAdapter(consoleInout);
			responseAdapter.setCharacterEncoding(consoleInout.getEncoding());
			setResponseAdapter(responseAdapter);
		} catch (Exception e) {
			throw new AdapterException("Could not adapt to console application activity.", e);
		}
	}

	@Override
	protected void parseRequest() {
		receiveRequiredParameters();

		super.parseRequest();
	}

	/**
	 * Receive required input parameters.
	 */
	private void receiveRequiredParameters() {
		ItemRuleMap parameterItemRuleMap = getRequestRule().getParameterItemRuleMap();

		if (parameterItemRuleMap != null) {
			ItemRuleList parameterItemRules = new ItemRuleList(parameterItemRuleMap.values());

			consoleInout.writeLine("Required parameters:");

			for (ItemRule itemRule : parameterItemRules) {
				Token[] tokens = itemRule.getTokens();
				if (tokens == null) {
					tokens = new Token[] { new Token(TokenType.PARAMETER, itemRule.getName()) };
				}

				String madatoryMarker = itemRule.isMandatory() ? "*" : " ";
				consoleInout.writeLine("  %s %s: %s", madatoryMarker, itemRule.getName(), TokenParser.toString(tokens));
			}

			consoleInout.writeLine("Enter values for each parameter:");

			ItemRuleList missingItemRules = receiveRequiredParameters(parameterItemRules);

			if (missingItemRules != null) {
				consoleInout.writeLine("Enter the missing values for each parameter:");

				ItemRuleList missingItemRules2 = receiveRequiredParameters(missingItemRules);

				if (missingItemRules2 != null && missingItemRules.size() == missingItemRules2.size()) {
					String[] itemNames = missingItemRules2.getItemNames();
					String missingParamNames = StringUtils.joinCommaDelimitedList(itemNames);
					consoleInout.writeLine("Missing required parameters: %s", missingParamNames);

					terminate();
				}
			}
		}
	}

	private ItemRuleList receiveRequiredParameters(ItemRuleList parameterItemRules) {
		ItemRuleList missingItemRules = new ItemRuleList(parameterItemRules.size());

		for (ItemRule itemRule : parameterItemRules) {
			Token[] tokens = itemRule.getTokens();
			int inputCount = 0;

			if (tokens != null && tokens.length > 0) {
				for (Token token : tokens) {
					if (token.getType() == TokenType.PARAMETER) {
						String input = consoleInout.readLine("    %s: ", token.stringify());
						if (input != null && !input.isEmpty()) {
							getRequestAdapter().setParameter(token.getName(), input);
							inputCount++;
						}
					}
				}
			} else {
				String input = consoleInout.readLine("    $%s: ", itemRule.getName());
				if (input != null && !input.isEmpty()) {
					getRequestAdapter().setParameter(itemRule.getName(), input);
					inputCount++;
				}
			}

			if (itemRule.isMandatory() && inputCount == 0) {
				missingItemRules.add(itemRule);
			}
		}

		return (missingItemRules.isEmpty() ? null : missingItemRules);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Activity> T newActivity() {
		ConsoleActivity activity = new ConsoleActivity(service);
		activity.setIncluded(true);
		return (T)activity;
	}

}

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
package com.aspectran.embedded.activity;

import com.aspectran.core.activity.AdapterException;
import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.RequestAdapter;
import com.aspectran.core.adapter.ResponseAdapter;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;
import com.aspectran.embedded.adapter.EmbeddedRequestAdapter;
import com.aspectran.embedded.adapter.EmbeddedResponseAdapter;

public class EmbeddedActivity extends CoreActivity {

	/**
	 * Instantiates a new embedded activity.
	 *
	 * @param context the current ActivityContext
	 * @param sessionAdapter the session adapter
	 */
	public EmbeddedActivity(ActivityContext context, SessionAdapter sessionAdapter) {
		super(context);
		setSessionAdapter(sessionAdapter);
	}

	@Override
	protected void adapt() throws AdapterException {
		try {
			RequestAdapter requestAdapter = new EmbeddedRequestAdapter();
			requestAdapter.setCharacterEncoding(resolveRequestCharacterEncoding());
			setRequestAdapter(requestAdapter);

			ResponseAdapter responseAdapter = new EmbeddedResponseAdapter();
			setResponseAdapter(responseAdapter);
		} catch(Exception e) {
			throw new AdapterException("Failed to adapt for Embedded Activity.", e);
		}
	}


}

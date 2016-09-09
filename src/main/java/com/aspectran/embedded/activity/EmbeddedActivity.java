package com.aspectran.embedded.activity;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.adapter.SessionAdapter;
import com.aspectran.core.context.ActivityContext;

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

}

/*
 *  Copyright (c) 2010 Jeong Ju Ho, All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aspectran.web.activity;

import com.aspectran.core.activity.CoreActivity;
import com.aspectran.core.activity.CoreTransletImpl;

/**
 * The Class AspectranWebTranslet.
 */
public class WebTransletImpl extends CoreTransletImpl implements WebTranslet {
	
	/**
	 * Instantiates a new aspectran web translet.
	 *
	 * @param activity the activity
	 */
	public WebTransletImpl(CoreActivity activity) {
		super(activity);
	}
	
}

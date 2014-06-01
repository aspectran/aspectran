/*
 *  Copyright (c) 2009 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.activity;

import com.aspectran.core.activity.request.RequestException;
import com.aspectran.core.context.ActivityContext;

/**
 * <p>Created: 2014. 06. 01 오후 11:00:00</p>
 */
public class CoreActivityImpl extends AbstractCoreActivity implements CoreActivity {

	public CoreActivityImpl(ActivityContext context) {
		super(context);
	}
	
	public void init(String transletName) throws CoreActivityException {
		setTransletInterfaceClass(CoreTranslet.class);
		setTransletImplementClass(CoreTransletImpl.class);
	}
	
	protected void request(CoreTranslet translet) throws RequestException {
	}
	
	public CoreActivity newCoreActivity() {
		CoreActivityImpl activity = new CoreActivityImpl(getActivityContext());
		return activity;
	}

}

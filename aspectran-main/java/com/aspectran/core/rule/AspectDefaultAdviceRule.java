/*
 *  Copyright (c) 2008 Jeong Ju Ho, All rights reserved.
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
package com.aspectran.core.rule;

import java.util.Map;

import com.aspectran.core.activity.process.ActionList;
import com.aspectran.core.activity.response.DispatchResponse;
import com.aspectran.core.activity.response.ForwardResponse;
import com.aspectran.core.activity.response.RedirectResponse;
import com.aspectran.core.activity.response.transform.AbstractTransform;
import com.aspectran.core.type.AspectAdviceType;
import com.aspectran.core.type.AspectranSettingType;

/**
 * <p>Created: 2008. 04. 01 오후 11:19:28</p>
 */
public class AspectDefaultAdviceRule {

	private String aspectId;
	
	private AspectAdviceType aspectAdviceType;

	private Map<AspectranSettingType, String> settings;


}

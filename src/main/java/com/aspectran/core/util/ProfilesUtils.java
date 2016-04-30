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
package com.aspectran.core.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for Aspectran Profiles.
 */
public class ProfilesUtils {

	private static final String ACTIVE_PROFILES_PROPERTY_NAME = "aspectran.profiles.active";

	public static String[] getActiveProfilesFromSystem() {
		String profilesProp = SystemUtils.getProperty(ACTIVE_PROFILES_PROPERTY_NAME);
		if(profilesProp != null) {
			String[] profiles = StringUtils.tokenize(StringUtils.trimAllWhitespace(profilesProp), ",");
			if(profiles != null && profiles.length > 0) {
				return profiles;
			}
		}
		return null;
	}

	public static boolean acceptsProfiles(String[] activeProfiles, String... profiles) {
		if(profiles == null || profiles.length == 0)
			return true;

		if(activeProfiles == null || activeProfiles.length == 0)
			return false;

		for(String profile : profiles) {
			String p = StringUtils.trimAllWhitespace(profile);
			if(!p.isEmpty()) {
				if(p.charAt(0) != '!') {
					for(String activeProfile : activeProfiles) {
						if(p.equals(activeProfile))
							return true;
					}
				} else {
					String p2 = p.substring(1);
					for(String activeProfile : activeProfiles) {
						if(p2.equals(activeProfile))
							return false;
					}
					return true;
				}
			}
		}

		return false;
	}

	public static String[] validateProfiles(String[] profiles) {
		if(profiles == null || profiles.length == 0)
			return null;

		List<String> list = new ArrayList<>(profiles.length);
		for(String profile : profiles) {
			String p = StringUtils.trimAllWhitespace(profile);
			if(!p.isEmpty() && !list.contains(p)) {
				list.add(p);
			}
		}

		if(list.size() == 0)
			return null;

		return list.toArray(new String[list.size()]);
	}

	public static String[] split(String profiles) {
		return validateProfiles(StringUtils.tokenize(profiles, ","));
	}

	public static String join(String[] profiles) {
		return String.join(",", profiles);
	}
	
}

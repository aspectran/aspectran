/*
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
package com.aspectran.core.context.env;

import java.util.LinkedHashSet;
import java.util.Set;

import com.aspectran.core.util.StringUtils;
import com.aspectran.core.util.SystemUtils;

public abstract class AbstractEnvironment implements Environment {

	private static final String ACTIVE_PROFILES_PROPERTY_NAME = "aspectran.profiles.active";
	
	private static final String DEFAULT_PROFILES_PROPERTY_NAME = "aspectran.profiles.default";

	private final Set<String> activeProfiles = new LinkedHashSet<>();

	private final Set<String> defaultProfiles = new LinkedHashSet<>();

	@Override
	public String[] getActiveProfiles() {
		return activeProfiles.toArray(new String[activeProfiles.size()]);
	}

	private Set<String> doGetActiveProfiles() {
		synchronized (activeProfiles) {
			if (activeProfiles.isEmpty()) {
				setActiveProfiles(getProfilesFromSystemProperty(ACTIVE_PROFILES_PROPERTY_NAME));
			}
			return activeProfiles;
		}
	}

	public void setActiveProfiles(String profiles) {
		setActiveProfiles(StringUtils.splitCommaDelimitedString(profiles));
	}
	
	public void setActiveProfiles(String[] profiles) {
		synchronized (activeProfiles) {
			if (!activeProfiles.isEmpty()) {
				activeProfiles.clear();
			}
			if (profiles != null) {
				for (String profile : profiles) {
					if (profile.contains(",")) {
						addActiveProfile(profile);
					} else {
						addProfile(activeProfiles, profile);
					}
				}
			}
		}
	}
	
	public void addActiveProfile(String profile) {
		String[] profiles = StringUtils.splitCommaDelimitedString(profile);
		doGetActiveProfiles();
		if (profiles.length > 0) {
			synchronized (activeProfiles) {
				for (String p : profiles) {
					addProfile(activeProfiles, p);
				}
			}
		}
	}

	@Override
	public String[] getDefaultProfiles() {
		return defaultProfiles.toArray(new String[defaultProfiles.size()]);
	}

	private Set<String> doGetDefaultProfiles() {
		synchronized (defaultProfiles) {
			if (defaultProfiles.isEmpty()) {
				setActiveProfiles(getProfilesFromSystemProperty(DEFAULT_PROFILES_PROPERTY_NAME));
			}
			return defaultProfiles;
		}
	}

	public void setDefaultProfiles(String profiles) {
		setDefaultProfiles(StringUtils.splitCommaDelimitedString(profiles));
	}

	public void setDefaultProfiles(String[] profiles) {
		synchronized (defaultProfiles) {
			if (!defaultProfiles.isEmpty()) {
				defaultProfiles.clear();
			}
			if (profiles != null) {
				for (String profile : profiles) {
					addProfile(defaultProfiles, profile);
				}
			}
		}
	}
	
	public void addDefaultProfile(String profile) {
		String[] profiles = StringUtils.splitCommaDelimitedString(profile);
		doGetDefaultProfiles();
		if (profiles.length > 0) {
			synchronized (defaultProfiles) {
				for (String p : profiles) {
					addProfile(defaultProfiles, p);
				}
			}
		}
	}
	
	private void addProfile(Set<String> profiles, String profile) {
		profile = StringUtils.trimWhitespace(profile);
		if (profile.isEmpty()) {
			throw new IllegalArgumentException("Invalid profile [" + profile + "]; must contain text.");
		}
		if (profile.charAt(0) == '!') {
			throw new IllegalArgumentException("Invalid profile [" + profile + "]; must not begin with ! operator.");
		}
		profiles.add(profile);
	}

	@Override
	public boolean acceptsProfiles(String... profiles) {
		if (profiles == null || profiles.length == 0) {
			return true;
		}

		for (String profile : profiles) {
			if (StringUtils.hasLength(profile) && profile.charAt(0) == '!') {
				if (!isActiveProfile(profile)) {
					return true;
				}
			} else {
				if (isActiveProfile(profile)) {
					return true;
				}
			}
		}

		return false;
	}
	
	private boolean isActiveProfile(String profile) {
		Set<String> currentActiveProfiles = doGetActiveProfiles();
		return (currentActiveProfiles.contains(profile)
				|| (currentActiveProfiles.isEmpty() && doGetDefaultProfiles().contains(profile)));
	}
	
	private String[] getProfilesFromSystemProperty(String propName) {
		String profilesProp = SystemUtils.getProperty(propName);
		if (profilesProp != null) {
			String[] profiles = StringUtils.splitCommaDelimitedString(profilesProp);
			if (profiles != null && profiles.length > 0) {
				return profiles;
			}
		}
		return null;
	}

}

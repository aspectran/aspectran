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

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

/**
 * This class enables a good and consistent toString() to be built for any class or object.
 * 
 * @author Juho Jeong
 * @since 2016. 2. 11.
 */
public class ToStringBuilder {
	
	private StringBuilder sb;
	
	public ToStringBuilder() {
		this(32);
	}
	
	public ToStringBuilder(int capacity) {
		this.sb = new StringBuilder(capacity);
		this.sb.append("{");
	}

	public void append(String name, Object value) {
		if(value != null) {
			appendName(name);
			append(value);
		}
	}
	
	public void appendForce(String name, Object value) {
		appendName(name);
		append(value);
	}
	
	public void append(String name, boolean value) {
		if(value) {
			appendName(name);
			this.sb.append(Boolean.toString(value));
		}
	}
	
	public void appendForce(String name, boolean value) {
		appendName(name);
		this.sb.append(Boolean.toString(value));
	}
	
	public void appendEqual(String name, Object value, Object compare) {
		if(value != null && value.equals(compare)) {
			appendName(name);
			append(value);
		}
	}
	
	public void appendNotEqual(String name, Object value, Object compare) {
		if(value != null && !value.equals(compare)) {
			appendName(name);
			append(value);
		}
	}
	
	public void appendSize(String name, Object object) {
		if(object != null) {
			appendName(name);
			if(object instanceof Map<?, ?>) {
				this.sb.append(((Map<?, ?>)object).size());
			} else if(object instanceof Collection<?>) {
				this.sb.append(((Collection<?>)object).size());
			} else if(object.getClass().isArray()) {
				this.sb.append(Array.getLength(object));
			} else if(object instanceof CharSequence) {
				this.sb.append(((CharSequence)object).length());
			}
		}
	}
	
	public void append(Map<?, ?> map) {
		if(map != null) {
			this.sb.append("{");
			int len = this.sb.length();
			for(Map.Entry<?, ?> entry : ((Map<?, ?>)map).entrySet()) {
				Object key = entry.getKey();
				Object value = entry.getValue();
				if(value != null) {
					if(this.sb.length() > len)
						addpendComma();
					appendName(key);
					append(value);
				}
			}
			this.sb.append("}");
		}
	}
	
	public void append(Collection<?> list) {
		if(list != null) {
			this.sb.append("[");
			int len = this.sb.length();
			for(Object o : ((Collection<?>)list)) {
				if(this.sb.length() > len)
					addpendComma();
				append(o);
			}
			this.sb.append("]");
		}
	}
	
	private void append(Object object) {
//		if(object instanceof Map<?, ?>) {
//			append((Map<?, ?>)object);
//		} else if(object instanceof Collection<?>) {
//			append((Collection<?>)object);
//		} else if(object instanceof Token[]) {
//			for(Token t : (Token[])object) {
//				this.sb.append(t.stringify());
//			}
//		} else if(object instanceof Parameters) {
//			this.sb.append(((Parameters)object).describe(false));
//		} else if(object instanceof ToStringBuilder) {
//			this.sb.append(((ToStringBuilder)object).getStringBuilder());
//		} else if(object instanceof CharSequence) {
//			this.sb.append(((CharSequence)object));
//		} else if(object != null && object.getClass().isArray()) {
//			this.sb.append("[");
//			int len = Array.getLength(object);
//			for(int i = 0; i < len; i++) {
//				if(i > 0)
//					addpendComma();
//				append(Array.get(object, i));
//			}
//			this.sb.append("]");
//		} else {
//			this.sb.append(object);
//		}
	}
	
	public void appendName(Object name) {
		if(this.sb.length() > 1)
			addpendComma();
		this.sb.append(name).append("=");
	}
	
	private void addpendComma() {
		this.sb.append(", ");
	}
	
	protected StringBuilder getStringBuilder() {
		return this.sb;
	}
	
	@Override
	public String toString() {
		this.sb.append("}");
		return this.sb.toString();
	}

}

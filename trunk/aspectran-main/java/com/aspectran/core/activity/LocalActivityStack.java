package com.aspectran.core.activity;

import com.aspectran.core.util.ArrayStack;

public class LocalActivityStack {

	/** Ensure serialization compatibility */
	static final long serialVersionUID = 2973567907988650303L;

	public static ThreadLocal<LocalActivityStack> local = new ThreadLocal<LocalActivityStack>() {
		@Override
		protected LocalActivityStack initialValue() {
			return new LocalActivityStack();
		}
	};

	private ArrayStack stack = new ArrayStack(2);

	private CoreActivity push(CoreActivity activity) {
		return (CoreActivity)stack.push(activity);
	}

	private CoreActivity peek() {
		return (CoreActivity)stack.peek();
	}

	private CoreActivity pop() {
		return (CoreActivity)stack.pop();
	}
	
	private int size() {
		return stack.size();
	}
	
	public static CoreActivity pushActivity(CoreActivity activity) {
		return local.get().push(activity);
	}
	
	public static CoreActivity peekActivity() {
		return local.get().peek();
	}
	
	public static CoreActivity popActivity() {
		CoreActivity activity = local.get().pop();
		
		if(local.get().size() == 0)
			local.remove();
		
		return activity;
	}
	
}
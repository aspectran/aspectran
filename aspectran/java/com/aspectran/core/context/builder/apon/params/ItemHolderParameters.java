package com.aspectran.core.context.builder.apon.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefine;
import com.aspectran.core.util.apon.Parameters;

public class ItemHolderParameters extends AbstractParameters implements Parameters {

	public static final ParameterDefine item;
	
	private static final ParameterDefine[] parameterDefines;
	
	static {
		item = new ParameterDefine("item", ItemParameters.class, true, true);
		
		parameterDefines = new ParameterDefine[] {
				item
		};
	}
	
	public ItemHolderParameters() {
		super(parameterDefines);
	}
	
	public ItemHolderParameters(String text) {
		super(parameterDefines, text);
	}
	
}

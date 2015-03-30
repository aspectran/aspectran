package com.aspectran.example.common;

import com.aspectran.core.activity.Activity;
import com.aspectran.core.activity.CoreTranslet;


public class MyTransletImpl extends CoreTranslet implements MyTranslet {

	public MyTransletImpl(Activity activity) {
		super(activity);
	}

}

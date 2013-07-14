/**
 * 
 */
package com.aspectran.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.aspectran.translet.activity.Translet;

/**
 *
 * <p>Created: 2008. 05. 27 오전 1:03:24</p>
 *
 */
public class TestBean {

	private String variable1 = "a~~~~~~\\~~~~~~~~~~~";
	
	private String variable2 = "b~~~~~~~~'~~~\"~~~~~~";
	
	private String string;
	
	private List<Integer> list;
	
	private Map<String, Object> map;
	
	public TestBean() {
		
	}
	
	public TestBean(String string, ArrayList<Integer> list, LinkedHashMap<String, Object> map) {
		this.string = string;
		this.list = list;
		this.map = map;
	}
	
	
	/**
	 * @return the variable1
	 */
	public String getVariable1() {
		return variable1;
	}

	/**
	 * @param variable1 the variable1 to set
	 */
	public void setVariable1(String variable1) {
		this.variable1 = variable1;
	}

	/**
	 * @return the variable2
	 */
	public String getVariable2() {
		return variable2;
	}

	/**
	 * @param variable2 the variable2 to set
	 */
	public void setVariable2(String variable2) {
		this.variable2 = variable2;
	}

	public Object testAction(SuperTranslet translet) {
		List<Object> list = new ArrayList<Object>();
		
		list.add("aaaaaa");
		list.add("bbbbbb");
		list.add("cccccc");
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("ddd", "111111");
		map.put("eee", "222222");
		map.put("fff", "333333");
		
		list.add(map);
		
		TestBean bean = new TestBean();
		
		list.add(bean);
		
		String[] aaa = { "11111111111111111111", "2222222222222222222222" };
		
		map.put("fff1111111111111111111111", aaa);
		
//		if(true)
//			throw new IllegalArgumentException("ddddddd");
		
		return list;
	}
	
	public Object testAction2(SuperTranslet translet) {
		return this;
	}
	
	public Object testXmlAction(SuperTranslet translet) {
		return this;
	}
	
	
	
}

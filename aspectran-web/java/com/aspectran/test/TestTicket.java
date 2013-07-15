package com.aspectran.test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.aspectran.web.activity.WebTranslet;

/**
 *
 * @author Gulendol
 *
 * <p>Created: 2008. 06. 12 오전 5:55:28</p>
 *
 */
public class TestTicket {

	private String string;
	
	private List<Integer> list;
	
	private Map<String, Object> map;
	
	public TestTicket() {
		
	}
	
	//public TestTicket(String string, ArrayList<Integer> list, LinkedHashMap<String, String> map) {
	public TestTicket(String string, List<Integer> list, Map<String, Object> map) {
		this.string = string;
		this.list = list;
		this.map = map;
	}
	
	public String getTestString() {
		return "hi-ticket, I'm glad to meet you.";
	}
	
	public boolean check(WebTranslet translet) {
		return true;
	}
	
	public Date getDate() {
		return new Date();
	}
	
	public int[] getInt() {
		int[] ii = new int[2];
		ii[0] = 111111;
		ii[1] = 222222222;
		
		return ii;
	}
}

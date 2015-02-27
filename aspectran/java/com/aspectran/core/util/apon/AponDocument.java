package com.aspectran.core.util.apon;


public class AponDocument {

	public static final char CURLY_BRACKET_OPEN = '{';

	public static final char CURLY_BRACKET_CLOSE = '}';
	
	public static final char SQUARE_BRACKET_OPEN = '[';
	
	public static final char SQUARE_BRACKET_CLOSE = ']';

	public static final char ROUND_BRACKET_OPEN = '(';
	
	public static final char ROUND_BRACKET_CLOSE = ')';
	
	public static final char TEXT_LINE_START = '|';
		
	public static final char NAME_VALUE_SEPARATOR = ':';
	
	public static final char COMMENT_LINE_START = '#';
	
	public static final char NO_CONTROL_CHAR = 0;
	
	public static final char QUOTE_CHAR = '"';
	
	public static final char ESCAPE_CHAR = '\\';
	
	public static final String NULL = "null";
	
	public static final String TRUE = "true";
	
	public static final String FALSE = "false";
	
	public static String unescape(String value) {
		int vlen = value.length();

		if(value == null || vlen == 0 || value.indexOf(ESCAPE_CHAR) == -1)
			return value;

		char b = value.charAt(0);
		char c = 0;

		StringBuilder sb = new StringBuilder(vlen);

		for(int i = 1; i < vlen; i++) {
			c = value.charAt(i);
			
			if(b == ESCAPE_CHAR) {
				switch(c) {
				case ESCAPE_CHAR:
				case QUOTE_CHAR:
					sb.append(c);
					break;
				case 'b':
					sb.append('\b');
					break;
				case 't':
					sb.append('\t');
					break;
				case 'n':
					sb.append('\n');
					break;
				case 'f':
					sb.append('\f');
					break;
				case 'r':
					sb.append('\r');
					break;
				default:
					sb.append(b);
				}
			} else {
				sb.append(b);
			}
			
			b = c;
		}
		
		if(c != 0)
			sb.append(c);
		
		return sb.toString();
	}
	
}

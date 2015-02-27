package com.aspectran.core.util.apon;


public class AponFormat {

	protected static final char CURLY_BRACKET_OPEN = '{';

	protected static final char CURLY_BRACKET_CLOSE = '}';
	
	protected static final char SQUARE_BRACKET_OPEN = '[';
	
	protected static final char SQUARE_BRACKET_CLOSE = ']';

	protected static final char ROUND_BRACKET_OPEN = '(';
	
	protected static final char ROUND_BRACKET_CLOSE = ')';
	
	protected static final char TEXT_LINE_START = '|';
		
	protected static final char NAME_VALUE_SEPARATOR = ':';
	
	protected static final char COMMENT_LINE_START = '#';
	
	protected static final char NO_CONTROL_CHAR = 0;
	
	protected static final char QUOTE_CHAR = '"';
	
	protected static final char ESCAPE_CHAR = '\\';
	
	protected static final char SPACE_CHAR = ' ';
	
	protected static final String NULL = "null";
	
	protected static final String TRUE = "true";
	
	protected static final String FALSE = "false";
	
	protected static String unescape(String value) {
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

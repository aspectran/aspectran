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

	protected static final char NEXT_LINE_CHAR = '\n';

	protected static final char SPACE_CHAR = ' ';
	
	protected static final String NULL = "null";
	
	protected static final String TRUE = "true";
	
	protected static final String FALSE = "false";
	
	public static String escape(String value) {
		if(value == null)
			return null;
		
		int vlen = value.length();

		if(vlen == 0)
			return value;

		StringBuilder sb = new StringBuilder(vlen);
		char c;
		String t;

		for(int i = 0; i < vlen; i++) {
			c = value.charAt(i);

			switch(c) {
			case '\\':
			case '"':
				sb.append('\\');
				sb.append(c);
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\r':
				sb.append("\\r");
				break;
			default:
				if(c < ' ' || (c >= '\u0080' && c < '\u00a0') || (c >= '\u2000' && c < '\u2100')) {
					t = "000" + Integer.toHexString(c);
					sb.append("\\u" + t.substring(t.length() - 4));
				} else {
					sb.append(c);
				}
			}
		}
		
		return sb.toString();
	}
	
	public static String unescape(String value) {
		if(value == null)
			return null;
		
		int vlen = value.length();

		if(vlen == 0 || value.indexOf(ESCAPE_CHAR) == -1)
			return value;

		StringBuilder sb = new StringBuilder(vlen);
		char c;
		
		for(int i = 0; i < vlen; i++) {
			c = value.charAt(i);
			
			if(c == ESCAPE_CHAR) {
				if(++i < vlen)
					c = value.charAt(i);
				else
					c = 0;

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
					return null;
				}
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}
	
}

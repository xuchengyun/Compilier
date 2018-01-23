/**
* Initial code for the Scanner for the class project in COP5556 Programming Language Principles 
* at the University of Florida, Spring 2018.
* 
* This software is solely for the educational benefit of students 
* enrolled in the course during the Spring 2018 semester.  
* 
* This software, and any software derived from it,  may not be shared with others or posted to public web sites,
* either during the course or afterwards.
* 
*  @Beverly A. Sanders, 2018
*/

package cop5556sp18;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cop5556fa17.Scanner.LexicalException;
import cop5556fa17.Scanner.State;

public class Scanner {

	@SuppressWarnings("serial")
	public static class LexicalException extends Exception {

		int pos;

		public LexicalException(String message, int pos) {
			super(message);
			this.pos = pos;
		}

		public int getPos() {
			return pos;
		}
	}

	public static enum Kind {
		IDENTIFIER(""), INTEGER_LITERAL(""), BOOLEAN_LITERAL(""), FLOAT_LITERAL(""),
		KW_Z("Z")/* Z */, KW_default_width("default_width")/* default_width */, KW_default_height("default_height")/* default_height */, 
		KW_width("width") /* width */, KW_height("height") /* height*/, KW_show("show")/*show*/, KW_write("write") /* write */, KW_to("to") /* to */,
		KW_input("input") /* input */, KW_from("from") /* from */, KW_cart_x("cart_x")/* cart_x*/, KW_cart_y("cart_y")/* cart_y */, 
		KW_polar_a("polar_a")/* polar_a*/, KW_polar_r("polar_r")/* polar_r*/, KW_abs("abs")/* abs */, KW_sin("sin")/* sin*/, KW_cos("cos")/* cos */, 
		KW_atan("atan")/* atan */, KW_log("log")/* log */, KW_image("image")/* image */, KW_int("int")/* int */, KW_float("float") /* float */, 
		KW_boolean("boolean")/* boolean */, KW_filename("filename")/* filename */, KW_red("red") /* red */, KW_blue("blue") /* blue */, 
		KW_green("green") /* green */, KW_alpha("alpha") /* alpha*/, KW_while("while") /* while */, KW_if("if") /* if */, OP_ASSIGN(":=")/* := */, 
		OP_EXCLAMATION("!")/* ! */, OP_QUESTION("?")/* ? */, OP_COLON(":")/* : */, OP_EQ("==")/* == */, OP_NEQ("!=")/* != */, 
		OP_GE(">=")/* >= */, OP_LE("<=")/* <= */, OP_GT(">")/* > */, OP_LT("<")/* < */, OP_AND("&")/* & */, OP_OR("|")/* | */, 
		OP_PLUS("+")/* +*/, OP_MINUS("-")/* - */, OP_TIMES("*")/* * */, OP_DIV("/")/* / */, OP_MOD("%")/* % */, OP_POWER("**")/* ** */, 
		OP_AT("@")/* @ */, LPAREN("(")/*( */, RPAREN(")")/* ) */, LSQUARE("[")/* [ */, RSQUARE("]")/* ] */, LBRACE("{") /*{ */, 
		RBRACE("}") /* } */, LPIXEL("<<") /* << */, RPIXEL(">>") /* >> */, SEMI(";")/* ; */, COMMA(",")/* , */, DOT(".") /* . */, EOF("");
		
		final String value;
		
		Kind(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return this.value;
		}

	    @Override
	    public String toString() {
	        return this.getValue();
	    }
	}
	
	
    

	/**
	 * Class to represent Tokens.
	 * 
	 * This is defined as a (non-static) inner class which means that each Token
	 * instance is associated with a specific Scanner instance. We use this when
	 * some token methods access the chars array in the associated Scanner.
	 * 
	 * @author Beverly Sanders
	 *
	 */
	public class Token {
		public final Kind kind;
		public final int pos; // position of first character of this token in the input. Counting starts at 0
								// and is incremented for every character.
		public final int length; // number of characters in this token
		

	    
		public Token(Kind kind, int pos, int length) {
			super();
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		public String getText() {
			return String.copyValueOf(chars, pos, length);
		}

		/**
		 * precondition: This Token's kind is INTEGER_LITERAL
		 * 
		 * @returns the integer value represented by the token
		 */
		public int intVal() {
			assert kind == Kind.INTEGER_LITERAL;
			return Integer.valueOf(String.copyValueOf(chars, pos, length));
		}

		/**
		 * precondition: This Token's kind is FLOAT_LITERAL]
		 * 
		 * @returns the float value represented by the token
		 */
		public float floatVal() {
			assert kind == Kind.FLOAT_LITERAL;
			return Float.valueOf(String.copyValueOf(chars, pos, length));
		}

		/**
		 * precondition: This Token's kind is BOOLEAN_LITERAL
		 * 
		 * @returns the boolean value represented by the token
		 */
		public boolean booleanVal() {
			assert kind == Kind.BOOLEAN_LITERAL;
			return getText().equals("true");
		}

		/**
		 * Calculates and returns the line on which this token resides. The first line
		 * in the source code is line 1.
		 * 
		 * @return line number of this Token in the input.
		 */
		public int line() {
			return Scanner.this.line(pos) + 1;
		}

		/**
		 * Returns position in line of this token.
		 * 
		 * @param line.
		 *            The line number (starting at 1) for this token, i.e. the value
		 *            returned from Token.line()
		 * @return
		 */
		public int posInLine(int line) {
			return Scanner.this.posInLine(pos, line - 1) + 1;
		}

		/**
		 * Returns the position in the line of this Token in the input. Characters start
		 * counting at 1. Line termination characters belong to the preceding line.
		 * 
		 * @return
		 */
		public int posInLine() {
			return Scanner.this.posInLine(pos) + 1;
		}

		public String toString() {
			int line = line();
			return "[" + kind + "," + String.copyValueOf(chars, pos, length) + "," + pos + "," + length + "," + line
					+ "," + posInLine(line) + "]";
		}

		/**
		 * Since we override equals, we need to override hashCode, too.
		 * 
		 * See
		 * https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#hashCode--
		 * where it says, "If two objects are equal according to the equals(Object)
		 * method, then calling the hashCode method on each of the two objects must
		 * produce the same integer result."
		 * 
		 * This method, along with equals, was generated by eclipse
		 * 
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((kind == null) ? 0 : kind.hashCode());
			result = prime * result + length;
			result = prime * result + pos;
			return result;
		}

		/**
		 * Override equals so that two Tokens are equal if they have the same Kind, pos,
		 * and length.
		 * 
		 * This method, along with hashcode, was generated by eclipse.
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Token other = (Token) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (kind != other.kind)
				return false;
			if (length != other.length)
				return false;
			if (pos != other.pos)
				return false;
			return true;
		}

		/**
		 * used in equals to get the Scanner object this Token is associated with.
		 * 
		 * @return
		 */
		private Scanner getOuterType() {
			return Scanner.this;
		}

	}// Token

	/**
	 * Array of positions of beginning of lines. lineStarts[k] is the pos of the
	 * first character in line k (starting at 0).
	 * 
	 * If the input is empty, the chars array will have one element, the synthetic
	 * EOFChar token and lineStarts will have size 1 with lineStarts[0] = 0;
	 */
	int[] lineStarts;

	int[] initLineStarts() {
		ArrayList<Integer> lineStarts = new ArrayList<Integer>();
		int pos = 0;

		for (pos = 0; pos < chars.length; pos++) {
			lineStarts.add(pos);
			char ch = chars[pos];
			while (ch != EOFChar && ch != '\n' && ch != '\r') {
				pos++;
				ch = chars[pos];
			}
			if (ch == '\r' && chars[pos + 1] == '\n') {
				pos++;
			}
		}
		// convert arrayList<Integer> to int[]
		return lineStarts.stream().mapToInt(Integer::valueOf).toArray();
	}

	int line(int pos) {
		int line = Arrays.binarySearch(lineStarts, pos);
		if (line < 0) {
			line = -line - 2;
		}
		return line;
	}

	public int posInLine(int pos, int line) {
		return pos - lineStarts[line];
	}

	public int posInLine(int pos) {
		int line = line(pos);
		return posInLine(pos, line);
	}

	/**
	 * Sentinal character added to the end of the input characters.
	 */
	static final char EOFChar = 128;

	/**
	 * The list of tokens created by the scan method.
	 */
	final ArrayList<Token> tokens;

	/**
	 * An array of characters representing the input. These are the characters from
	 * the input string plus an additional EOFchar at the end.
	 */
	final char[] chars;

	/**
	 * position of the next token to be returned by a call to nextToken
	 */
	private int nextTokenPos = 0;

	Scanner(String inputString) {
		int numChars = inputString.length();
		this.chars = Arrays.copyOf(inputString.toCharArray(), numChars + 1); // input string terminated with null char
		chars[numChars] = EOFChar;
		tokens = new ArrayList<Token>();
		lineStarts = initLineStarts();
	}



	public static HashMap<String, Kind> keyword = new HashMap<String, Kind>();
    static {
        for(Kind k: Kind.values()){
		    String str = k.toString();
		    if(str.matches("^[a-zA-Z_]+$")){
		    	keyword.put(str, k);
		    }
		}
    }

	private enum State {START, AFTER_LT, AFTER_ST, AFTER_EXCL, AFTER_COLON, AFTER_EQ, AFTER_DOT, AFTER_AS, IN_DIGIT, IN_IDENTIFIER};  //TODO:  this is incomplete

	 
	 //TODO: Modify this to deal with the entire lexical specification
	public Scanner scan() throws LexicalException {
		int pos = 0;
		State state = State.START;
		int startPos = 0;
		while (pos < chars.length) {
			char ch = chars[pos];
			switch(state) {
				case START: {
					startPos = pos;
					switch (ch) {
						case ' ': {}
						case '\n':
						case '\r':
						case '\t':
						case '\f': {pos++;}break;
						case EOFChar: {tokens.add(new Token(Kind.EOF, startPos, 0)); pos++;} break;
						case ';': {tokens.add(new Token(Kind.SEMI, startPos, pos - startPos + 1)); pos++;} break;
						case '(': {tokens.add(new Token(Kind.LPAREN, startPos, pos - startPos + 1)); pos++;} break;
						case ')': {tokens.add(new Token(Kind.RPAREN, startPos, pos - startPos + 1)); pos++;} break;
						case '[': {tokens.add(new Token(Kind.LSQUARE, startPos, pos - startPos + 1)); pos++;} break;
						case ']': {tokens.add(new Token(Kind.RSQUARE, startPos, pos - startPos + 1)); pos++;} break;
						case '{': {tokens.add(new Token(Kind.LBRACE, startPos, pos - startPos + 1)); pos++;} break;
						case '}': {tokens.add(new Token(Kind.RBRACE, startPos, pos - startPos + 1)); pos++;} break;
						case '?': {tokens.add(new Token(Kind.OP_QUESTION, startPos, pos - startPos + 1)); pos++;} break;
						case '&': {tokens.add(new Token(Kind.OP_AND, startPos, pos - startPos + 1)); pos++;} break;
						case '|': {tokens.add(new Token(Kind.OP_OR, startPos, pos - startPos + 1)); pos++;} break;
						case '+': {tokens.add(new Token(Kind.OP_PLUS, startPos, pos - startPos + 1)); pos++;} break;
						case '-': {tokens.add(new Token(Kind.OP_MINUS, startPos, pos - startPos + 1)); pos++;} break;
						case '/': {tokens.add(new Token(Kind.OP_DIV, startPos, pos - startPos + 1)); pos++;} break;
						case '@': {tokens.add(new Token(Kind.OP_AT, startPos, pos - startPos + 1)); pos++;} break;
						
						case '<': {state = State.AFTER_LT; pos++;} break;
						case '>': {state = State.AFTER_ST; pos++;} break;
						case '.': {state = State.AFTER_DOT; pos++;} break;
						case '!': {state = State.AFTER_EXCL; pos++;} break;
						case ':': {state = State.AFTER_COLON; pos++;} break;
						case '=': {state = State.AFTER_EQ; pos++;} break;
						case '*': {state = State.AFTER_AS; pos++;} break;
						
						default: {
							if(Character.isDigit(ch)){
								state = State.IN_DIGIT;
								pos++;
							}
							else if(Character.isLetter(ch)){
								state = State.IN_IDENTIFIER;
								pos++;
							}
							else if(Character.isWhitespace(ch)){
								pos++;
							}
							else{
								throw new LexicalException("Invalid character " + ch + "at pos " + pos, pos);
							}
							error(pos, 0, 0, "undefined state");
						}
					}//switch ch
				}
				break; 
				case IN_IDENTIFIER: {
//					if 
				}
				default: {
					if(Character.isDigit(ch)){
						state = State.IN_DIGIT;
						pos ++;
					}
					else if(Character.isJavaIdentifierStart(ch)){
						state = State.IN_IDENTIFIER;
						pos ++;
					}
					else if(Character.isWhitespace(ch)){
						pos ++;
					}
					else{
						throw new LexicalException("Invalid character " + ch + "at pos " + pos, pos);
					}
					error(pos, 0, 0, "undefined state");
				}
			}// switch state
		} // while
			
		return this;
	}


	private void error(int pos, int line, int posInLine, String message) throws LexicalException {
		String m = (line + 1) + ":" + (posInLine + 1) + " " + message;
		throw new LexicalException(m, pos);
	}

	/**
	 * Returns true if the internal iterator has more Tokens
	 * 
	 * @return
	 */
	public boolean hasTokens() {
		return nextTokenPos < tokens.size();
	}

	/**
	 * Returns the next Token and updates the internal iterator so that the next
	 * call to nextToken will return the next token in the list.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return
	 */
	public Token nextToken() {
		return tokens.get(nextTokenPos++);
	}

	/**
	 * Returns the next Token, but does not update the internal iterator. This means
	 * that the next call to nextToken or peek will return the same Token as
	 * returned by this methods.
	 * 
	 * It is the callers responsibility to ensure that there is another Token.
	 * 
	 * Precondition: hasTokens()
	 * 
	 * @return next Token.
	 */
	public Token peek() {
		return tokens.get(nextTokenPos);
	}

	/**
	 * Resets the internal iterator so that the next call to peek or nextToken will
	 * return the first Token.
	 */
	public void reset() {
		nextTokenPos = 0;
	}

	/**
	 * Returns a String representation of the list of Tokens and line starts
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Tokens:\n");
		for (int i = 0; i < tokens.size(); i++) {
			sb.append(tokens.get(i)).append('\n');
		}
		sb.append("Line starts:\n");
		for (int i = 0; i < lineStarts.length; i++) {
			sb.append(i).append(' ').append(lineStarts[i]).append('\n');
		}
		return sb.toString();
	}

}

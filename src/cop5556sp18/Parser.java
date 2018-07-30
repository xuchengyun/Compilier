package cop5556sp18;
/* *
 * Initial code for SimpleParser for the class project in COP5556 Programming Language Principles 
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


import cop5556sp18.Scanner.Token;
import cop5556sp18.Scanner.Kind;
import static cop5556sp18.Scanner.Kind.*;

import java.util.ArrayList;

import cop5556sp18.AST.*;


public class Parser {
	
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		Token t;

		public SyntaxException(Token t, String message) {
			super(message);
			this.t = t;
		}

	}



	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}


	public Program parse() throws SyntaxException {
		Program program = program();
		matchEOF();
		return program;
	}

	/*
	 * Program ::= Identifier Block
	 */
	public Program program() throws SyntaxException {
		Token first = t;
		Token name = match(IDENTIFIER);
		Block block = block();
		return new Program(first, name, block);
	}
	
	/*
	 * Block ::=  { (  (Declaration | Statement) ; )* }
	 */
	
	Kind[] firstDec = { KW_int, KW_boolean, KW_image, KW_float, KW_filename };
	Kind[] firstStatement = {KW_input, KW_write, KW_while, KW_if, KW_show, KW_sleep, IDENTIFIER, 
			/* TODO  correct this */  };
	Kind[] color = {KW_red, KW_blue, KW_green, KW_alpha};
	public Block block() throws SyntaxException {
		Token first = t;
		ArrayList<ASTNode> decsOrStatements = new ArrayList<ASTNode>();
		match(LBRACE);
		while (isKind(firstDec)|isKind(firstStatement) || isKind(color)) {
			if (isKind(firstDec)) {
				decsOrStatements.add(declaration());
			} else if (isKind(firstStatement) || isKind(color)) {
				decsOrStatements.add(statement());
			}
			match(SEMI);
		}
		match(RBRACE);
		return new Block(first, decsOrStatements);
	}
	
	public Declaration declaration() throws SyntaxException{
		//TODO
		Token first = t;
		Kind kind = t.kind;
		Token type = null;
		Expression e1 = null;
		Expression e2 = null;
		type = consume();
		Token name = match(IDENTIFIER);
		if (kind == KW_image) {
			if (isKind(LSQUARE)){
				match(LSQUARE);
				e1 = expression();
				match(COMMA);
				e2 = expression();
				match(RSQUARE);
				return new Declaration(first, type, name, e1, e2);
			} else {
				return new Declaration(first, type, name, e1, e2);
			}
		} else if (kind == KW_int || kind == KW_float || 
				kind == KW_boolean || kind == KW_filename) {
			return new Declaration(first, type, name, e1, e2);
		} else {
			throw new SyntaxException(t, "Expected type but " + kind);
		}
	}
	
	public Statement statement() throws SyntaxException{
		Kind kind = t.kind;
		if (kind == KW_input) {
			return statementInput();
		} else if (kind == KW_write) {
			return statementWrite();
		} else if (kind == IDENTIFIER || isKind(color)) {
			return statementAssignment();
		} else if (kind == KW_while) {
			return statementWhile();
		} else if (kind == KW_if) {
			return statementIf();
		} else if (kind == KW_show) {
			return statementShow();
		} else if (kind == KW_sleep) {
			return statementSleep();
		} else{
			throw new SyntaxException(t, "Expected statement kind but " + t + " , statement exception");
		}
		//TODO
	}
	
	public StatementInput statementInput() throws SyntaxException {
		Token first = t;
		match(KW_input);
		Token name = match(IDENTIFIER);
		match(KW_from);
		match(OP_AT);
		Expression e = expression();
		return new StatementInput(first, name, e);
	}
	
	public StatementWrite statementWrite() throws SyntaxException {
		Token first = t;
		match(KW_write);
		Token n1 = match(IDENTIFIER);
		match(KW_to);
		Token n2 = match(IDENTIFIER);
		return new StatementWrite(first, n1, n2);
	}
	
	public StatementAssign statementAssignment() throws SyntaxException {
		Token first = t;
		LHS lhs = lhs();
		match(OP_ASSIGN);
		Expression e = expression();
		return new StatementAssign(first, lhs, e);
	}
	
	public StatementWhile statementWhile() throws SyntaxException {
		Token first = t;
		match(KW_while);
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		return new StatementWhile(first, e, b);
	}
	
	public StatementIf statementIf() throws SyntaxException {
		Token first = t;
		match(KW_if);
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		return new StatementIf(first, e, b);
	}
	
	public StatementShow statementShow() throws SyntaxException {
		Token first = t;
		match(KW_show);
		Expression e = expression();
		return new StatementShow(first, e);
	}
	
	public StatementSleep statementSleep() throws SyntaxException {
		Token first = t;
		match(KW_sleep);
		Expression e = expression();
		return new StatementSleep(first, e);
	}
	
	public LHS lhs() throws SyntaxException {
		Token first = t;
		LHS lhs = null;
		if (isKind(IDENTIFIER)) {
			Token name = consume();
			if (isKind(LSQUARE)) {
				PixelSelector px = pixelSelector();
				lhs = new LHSPixel(first, name, px);
			} else {
				lhs = new LHSIdent(first, name);
			}
			
		} else if (isKind(color)) {
			Token color = consume();
			match(LPAREN);
			Token name = match(IDENTIFIER);
			PixelSelector px = pixelSelector();
			match(RPAREN);
			lhs = new LHSSample(first, name, px, color);
		} else {
			throw new SyntaxException(t, "Expected IDENTIFIER of COLOR, but " + t + " LHS exception");
		}
		return lhs;
	}
	
	public PixelSelector pixelSelector() throws SyntaxException {
		Token first = t;
		match(LSQUARE);
		Expression ex = expression();
		match(COMMA);
		Expression ey = expression();
		match(RSQUARE);
		return new PixelSelector(first, ex, ey);
	}
	
	public Expression expression() throws SyntaxException {
		Token first = t;
		Expression t = null;
		Expression f = null;
		Expression orex = orExpression();
		if (isKind(OP_QUESTION)){
			match(OP_QUESTION);
			t = expression();
			match(OP_COLON);
			f = expression();
			return new ExpressionConditional(first, orex, t, f);
		}
		return orex;
	}
	
	public Expression orExpression() throws SyntaxException{
		Token first = t;
		Expression e0 = andExpression();
		Expression e1 = null;
		Token op = null;
		while(isKind(OP_OR)) {
			op = match(OP_OR);
			e1 = andExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	public Expression andExpression() throws SyntaxException{
		Token first = t;
		Expression e0 = eqExpression();
		Expression e1 = null;
		Token op = null;
		while(isKind(OP_AND)) {
			op = match(OP_AND);
			e1 = eqExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	public Expression eqExpression() throws SyntaxException{
		Token first = t;
		Expression e0 = relExpression();
		Expression e1 = null;
		Token op = null;
		while(isKind(OP_EQ) || isKind(OP_NEQ)) {
			op = consume();
			e1 = relExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	public Expression relExpression() throws SyntaxException{
		Token first = t;
		Expression e0 = addExpression();
		Expression e1 = null;
		Token op = null;
		while(isKind(OP_GT) || isKind(OP_LT) || isKind(OP_GE) || isKind(OP_LE) ) {
			op = consume();
			e1 = addExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	public Expression addExpression() throws SyntaxException{
		Token first = t;
		Expression e0 = multExpression();
		Expression e1 = null;
		Token op = null;
		while(isKind(OP_PLUS) || isKind(OP_MINUS)) {
			op = consume();
			e1 = multExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	public Expression multExpression() throws SyntaxException{
		Token first = t;
		Expression e0 = powerExpression();
		Expression e1 = null;
		Token op = null;
		while(isKind(OP_TIMES) || isKind(OP_DIV) || isKind(OP_MOD)) {
			op = consume();
			e1 = powerExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	public Expression powerExpression() throws SyntaxException{
		Token first = t;
		Expression e0 = unaryExpression();
		Expression e1 = null;
		Token op = null;
		if (isKind(OP_POWER)) {
			op = consume();
			e1 = powerExpression();
			e0 = new ExpressionBinary(first, e0, op, e1);
		}
		return e0;
	}
	
	public Expression unaryExpression() throws SyntaxException {
		Token first = t;
		Expression e = null;
		Token op = null;
		if (isKind(OP_PLUS)) {
			op = consume();
			e = unaryExpression();
			return new ExpressionUnary(first, op, e);
		} else if (isKind(OP_MINUS)) {
			op = consume();
			e = unaryExpression();
			return new ExpressionUnary(first, op, e);
		} else {
			return unaryExpressionNotPlusMinus();
		}
	}
	
	public Expression unaryExpressionNotPlusMinus() throws SyntaxException {
		Token first = t;
		Expression e = null;
		Token op = null;
		if (isKind(OP_EXCLAMATION)) {
			op = consume();
			e = unaryExpression();
			return new ExpressionUnary(first, op, e);
		} else {
			return primary();
		}
	}
	
	Kind[] functionName = { KW_sin, KW_cos, KW_atan, KW_abs, KW_log, KW_cart_x, KW_cart_y,
			KW_polar_a, KW_polar_r, KW_int, KW_float, KW_width, KW_height, KW_red, KW_green,
			KW_blue, KW_alpha};

	public Expression primary() throws SyntaxException {
		Token first = t;
		System.out.println(t);
		if (isKind(INTEGER_LITERAL)) {
			Token intLiteral = consume();
			return new ExpressionIntegerLiteral(first, intLiteral);
		} else if (isKind(BOOLEAN_LITERAL)) {
			Token booleanLiteral = consume();
			return new ExpressionBooleanLiteral(first, booleanLiteral);
		} else if (isKind(FLOAT_LITERAL)) {
			Token floatLiteral = consume();
			return new ExpressionFloatLiteral(first, floatLiteral);
		} else if (isKind(IDENTIFIER)) {
			Token name = consume();
			// pixelSelector
			if (isKind(LSQUARE)) {
				PixelSelector ps= pixelSelector();
				return new ExpressionPixel(first, name, ps);
			} else {
				return new ExpressionIdent(first, name);
			}
		} else if (isKind(LPAREN)) {
			consume();
			Expression e = expression();
			match(RPAREN);
			return e;
		} else if (isKind(KW_Z) || isKind(KW_default_height) || isKind(KW_default_width)) {
			Token name = consume();
			return new ExpressionPredefinedName(first, name);
		} else if (isKind(LPIXEL)) {
			return pixelConstructor();
		} else {
			return functionApplication();
		}
	}
	
	public Expression functionApplication() throws SyntaxException {
		Token first = t;
		Token name = null;
		Expression e0 = null;
		Expression e1 = null;
		if (isKind(functionName)) {
			name = consume();
			if (isKind(LPAREN)) {
				consume();
				e0 = expression();
				match(RPAREN);
				return new ExpressionFunctionAppWithExpressionArg(first, name, e0);
			} else if (isKind(LSQUARE)) {
				consume();
				e0 = expression();
				match(COMMA);
				e1 = expression();
				match(RSQUARE);
				return new ExpressionFunctionAppWithPixel(first, name, e0, e1);
			} else {
				throw new SyntaxException(t, "EXPECTED LPAREN OR KSQUARE, BUT NOT");
			}
		} else {
			throw new SyntaxException(t, "EXPECTED Function, BUT " + t + "pos "  + t.pos);
		}
	}
	
	public Expression pixelConstructor() throws SyntaxException {
		Token first = t;
		consume();
		Expression alpha = expression();
		match(COMMA);
		Expression r = expression();
		match(COMMA);
		Expression g = expression();
		match(COMMA);
		Expression b = expression();
		match(RPIXEL);
		return new ExpressionPixelConstructor(first, alpha, r, g, b);
	}

	protected boolean isKind(Kind kind) {
		return t.kind == kind;
	}

	protected boolean isKind(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind)
				return true;
		}
		return false;
	}


	/**
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		Token tmp = t;
		if (isKind(kind)) {
			consume();
			return tmp;
		}
		throw new SyntaxException(t,"Expected " + kind + ", but " + t + " pos " + t.pos); //TODO  give a better error message!
	}


	private Token consume() throws SyntaxException {
		Token tmp = t;
		if (isKind(EOF)) {
			throw new SyntaxException(t,"Expeted " + tmp + ", but meet EOF"); //TODO  give a better error message!  
			//Note that EOF should be matched by the matchEOF method which is called only in parse().  
			//Anywhere else is an error. */
		}
		t = scanner.nextToken();
		return tmp;
	}


	/**
	 * Only for check at end of program. Does not "consume" EOF so no attempt to get
	 * nonexistent next Token.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (isKind(EOF)) {
			return t;
		}
		throw new SyntaxException(t,"Expeted EOF, but " + t); //TODO  give a better error message!
	}
	

}


package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.Parser;
import cop5556sp18.Scanner;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Program;
import cop5556sp18.TypeChecker.SemanticException;

public class TypeCheckerTest {

	/*
	 * set Junit to be able to catch exceptions
	 */
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	/**
	 * Prints objects in a way that is easy to turn on and off
	 */
	static final boolean doPrint = true;

	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}

	/**
	 * Scans, parses, and type checks the input string
	 * 
	 * @param input
	 * @throws Exception
	 */
	void typeCheck(String input) throws Exception {
		show(input);
		// instantiate a Scanner and scan input
		Scanner scanner = new Scanner(input).scan();
		show(scanner);
		// instantiate a Parser and parse input to obtain and AST
		Program ast = new Parser(scanner).parse();
		show(ast);
		// instantiate a TypeChecker and visit the ast to perform type checking and
		// decorate the AST.
		ASTVisitor v = new TypeChecker();
		ast.visit(v, null);
	}



	/**
	 * Simple test case with an almost empty program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void emptyProg() throws Exception {
		String input = "blockScope{if(true){ int x; }; int x; x := 5; show x;}";
		typeCheck(input);
	}

	@Test
	public void expression1() throws Exception {
		String input = "prog {show 3+4;}";
		typeCheck(input);
	}

	@Test
	public void expression2_fail() throws Exception {
		String input = "prog { show true+4; }"; //error, incompatible types in binary expression
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// Declaration
	@Test
	public void test_1() throws Exception {
		String input = "a {int i;}";
		typeCheck(input);
	}
	
	// Declaration.name not in SymbolTable.currentScope
	@Test
	public void test_2() throws Exception {
		String input = "a {int i; int i;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// Declaration.name not in SymbolTable.currentScope
	@Test
	public void test_3() throws Exception {
		String input = "a {int i; image i;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// Declaration
	@Test
	public void test_4() throws Exception {
		String input = "a {image i;}";
		typeCheck(input);
	}
	
	// Declaration
	@Test
	public void test_5() throws Exception {
		String input = "a {show 3;}";
		typeCheck(input);
	}
	
	// Expression0 == ε or (Expression0.type == integer and type == image)
	@Test
	public void test_6() throws Exception {
		String input = "a {image f[4.3, 7]; }";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// StatementInput
	@Test
	public void test_7() throws Exception {
		String input = "a {float i; input i from @ 50;}";
		typeCheck(input);
	}
	
	// StatementInput.dec  != null
	@Test
	public void test_8() throws Exception {
		String input = "a {input i from @ 50;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// Expression.type ==integer
	@Test
	public void test_9() throws Exception {
		String input = "a {float i; input i from @ 50.0;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// StatementWrite
	@Test
	public void test_10() throws Exception {
		String input = "p {image a; filename b; write a to b;}";
		typeCheck(input);
	}
	
	// StatementWrite.destDec != null
	@Test
	public void test_11() throws Exception {
		String input = "p {image a; write a to b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// destDec.type == filename
	@Test
	public void test_12() throws Exception {
		String input = "p {image a; boolean b; write a to b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// StatementAssign
	@Test
	public void test_13() throws Exception {
		String input = "p {boolean a; boolean b; a := b;}";
		typeCheck(input);
	}
	
	// LHS.type == Expression.type
	@Test
	public void test_14() throws Exception {
		String input = "p {boolean a; int b; a := b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// StatementWhile
	@Test
	public void test_15() throws Exception {
		String input = "p {int i; boolean b; while(b) {int j;};}";
		typeCheck(input);
	}
	
	// Expression.type == boolean
	@Test
	public void test_16() throws Exception {
		String input = "p {int i; filename b; while(b) {int j;};}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// StatementIf
	@Test
	public void test_17() throws Exception {
		String input = "p {int i; boolean b; if(b) {int j;};}";
		typeCheck(input);
	}
	
	// Expression.type == boolean
	@Test
	public void test_18() throws Exception {
		String input = "p {int i; filename b; if(b) {int j;};}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// StatementShow
	@Test
	public void test_19() throws Exception {
		String input = "p {int i; show i + 3;}";
		typeCheck(input);
	}
	
	// Expression.type ∈ {int, boolean, float, image}
	@Test
	public void test_20() throws Exception {
		String input = "p {filename f; show f;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// StatementSleep
	@Test
	public void test_21() throws Exception {
		String input = "p {int i; sleep i + 3;}";
		typeCheck(input);
	}
	
	// Expression.type == integer
	@Test
	public void test_22() throws Exception {
		String input = "p {float f; sleep f;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// LHSIdent
	@Test
	public void test_23() throws Exception {
		String input = "p {int ii; float i1; int i; image iii; i := 1 + 2 * 3 - 4 / 1;}";
		typeCheck(input);
	}
	
	// LHSIdent.dec != null
	@Test
	public void test_24() throws Exception {
		String input = "p {f := .3;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
		
	// LHSPixel
		@Test
		public void test_25() throws Exception {
			String input = "p {image a; int b; int x; int y; a[x, y] := b;}";
			typeCheck(input);
		}
		
	// LLHSPixel.dec.type == image
	@Test
	public void test_26() throws Exception {
		String input = "p {filename a; int b; int x; int y; a[x, y] := b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// LHSSample
	@Test
	public void test_27() throws Exception {
		String input = "p {image a; int b; green(a[1, 2]) := b;}";
		typeCheck(input);
	}
	
	// LHSSample.dec.type == image
	@Test
	public void test_28() throws Exception {
		String input = "p {filename a; int b; green(a[1, 2]) := b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// PixelSelector
	@Test
	public void test_29() throws Exception {
		String input = "p {image a; int b; a[1.1, 2.2] := b;}";
		typeCheck(input);
	}
	
	// Expression0.type == Expression1.type
	@Test
	public void test_30() throws Exception {
		String input = "p {image a; int b; a[1, 2.2] := b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// Expression0.type == integer or Expression0.type == float
	@Test
	public void test_31() throws Exception {
		String input = "p {image a; int b; boolean x; boolean y; a[x, y] := b;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionConditional
	@Test
	public void test_32() throws Exception {
		String input = "p {int i; i := true ? 1 : 2;}";
		typeCheck(input);
	}
	
	// Expression0 .type == boolean
	@Test
	public void test_33() throws Exception {
		String input = "p {int i; i := 0 ? 1 : 2;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// Expression1.type == Expression2 .type
	@Test
	public void test_34() throws Exception {
		String input = "p {int i; boolean b; i := b ? 1.0 : 2;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionConditional.type == Expression1.type
	@Test
	public void test_35() throws Exception {
		String input = "p {int i; boolean b; i := b ? 1.0 : 2.0;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionPixelConstructor
	@Test
	public void test_36() throws Exception {
		String input = "p {int i; i := <<1, 2, 3, 4>>;}";
		typeCheck(input);
	}
	
	// Expressionalpha.type == integer
	@Test
	public void test_37() throws Exception {
		String input = "p {int i; i := <<0.1, 2, 3, 4>>;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionPixel
	@Test
	public void test_38() throws Exception {
		String input = "p {int i; image j; i := j[55, 77];}";
		typeCheck(input);
	}
	
	// ExpressionPixel.dec.type == image
	@Test
	public void test_39() throws Exception {
		String input = "p {int i; int j; i := j[55, 77];}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionFunctionAppWithPixel
	@Test
	public void test_40() throws Exception {
		String input = "p {int i; i := cart_x[5.5, 7.7];}";
		typeCheck(input);
	}
	
	// Expression1 .type == float
	@Test
	public void test_41() throws Exception {
		String input = "p {int i; i := cart_x[5.5, 7];}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionFunctionAppWithPixel
	@Test
	public void test_42() throws Exception {
		String input = "p {float i; i := polar_r[5, 7];}";
		typeCheck(input);
	}
	
	// Expression0.type == integer
	@Test
	public void test_43() throws Exception {
		String input = "p {float i; i := polar_r[5.5, 7];}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_44() throws Exception {
		String input = "p {int i; i := abs(5+7);}";
		typeCheck(input);
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_45() throws Exception {
		String input = "p {float i; i := atan(100 + 3.5);}";
		typeCheck(input);
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_46() throws Exception {
		String input = "p {int i; image img; i := height(img);}";
		typeCheck(input);
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_47() throws Exception {
		String input = "p {int i; float j; j := float(i);}";
		typeCheck(input);
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_48() throws Exception {
		String input = "p {int i; float j; i := int(j);}";
		typeCheck(input);
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_49() throws Exception {
		String input = "p {int i; i := blue(.5);}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_50() throws Exception {
		String input = "p {float i; i := log(5);}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionFunctionAppWithExpressionArg
	@Test
	public void test_51() throws Exception {
		String input = "p {float i; boolean b; i := float(b);}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionBinary
	@Test
	public void test_52() throws Exception {
		String input = "p {int i; i := 3 % 10;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_53() throws Exception {
		String input = "p {float i; i := 7.8 ** 1.2;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_54() throws Exception {
		String input = "p {float i; i := 3 / 1.3;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_55() throws Exception {
		String input = "p {boolean i; boolean x; x := false; i := true | x;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_56() throws Exception {
		String input = "p {int i; i := 3 & 1;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_57() throws Exception {
		String input = "p {boolean i; i := 3 >= 1;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_58() throws Exception {
		String input = "p {boolean i; i := 3.123 != 1.3;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_59() throws Exception {
		String input = "p {boolean i; i := true < false;}";
		typeCheck(input);
	}
	
	// ExpressionBinary
	@Test
	public void test_60() throws Exception {
		String input = "p {float i; i := 0.1 % 5.9;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionBinary
	@Test
	public void test_61() throws Exception {
		String input = "p {float i; i := 4.4 & 3;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionBinary
	@Test
	public void test_62() throws Exception {
		String input = "p {boolean i; i := 100 == true;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	// ExpressionBinary
	@Test
	public void test_63() throws Exception {
		String input = "p {boolean i; filename f; image img; i := f > img;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
	@Test
	public void test_64() throws Exception {
		String input = "prog{image var1; red( var1[0.0,0]) := 5;}";
		thrown.expect(SemanticException.class);
		try {
			typeCheck(input);
		} catch (SemanticException e) {
			show(e);
			throw e;
		}
	}
	
}

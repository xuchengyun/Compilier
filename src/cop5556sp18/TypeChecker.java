package cop5556sp18;

import cop5556sp18.Scanner.Kind;
import cop5556sp18.Scanner.Token;
import cop5556sp18.AST.ASTNode;
import cop5556sp18.AST.ASTVisitor;
import cop5556sp18.AST.Block;
import cop5556sp18.AST.Declaration;
import cop5556sp18.AST.Expression;
import cop5556sp18.AST.ExpressionBinary;
import cop5556sp18.AST.ExpressionBooleanLiteral;
import cop5556sp18.AST.ExpressionConditional;
import cop5556sp18.AST.ExpressionFloatLiteral;
import cop5556sp18.AST.ExpressionFunctionAppWithExpressionArg;
import cop5556sp18.AST.ExpressionFunctionAppWithPixel;
import cop5556sp18.AST.ExpressionIdent;
import cop5556sp18.AST.ExpressionIntegerLiteral;
import cop5556sp18.AST.ExpressionPixel;
import cop5556sp18.AST.ExpressionPixelConstructor;
import cop5556sp18.AST.ExpressionPredefinedName;
import cop5556sp18.AST.ExpressionUnary;
import cop5556sp18.AST.LHS;
import cop5556sp18.AST.LHSIdent;
import cop5556sp18.AST.LHSPixel;
import cop5556sp18.AST.LHSSample;
import cop5556sp18.AST.PixelSelector;
import cop5556sp18.AST.Program;
import cop5556sp18.AST.StatementAssign;
import cop5556sp18.AST.StatementIf;
import cop5556sp18.AST.StatementInput;
import cop5556sp18.AST.StatementShow;
import cop5556sp18.AST.StatementSleep;
import cop5556sp18.AST.StatementWhile;
import cop5556sp18.AST.StatementWrite;
import cop5556sp18.Types.Type;

public class TypeChecker implements ASTVisitor {


	TypeChecker() {
	}

	@SuppressWarnings("serial")
	public static class SemanticException extends Exception {
		Token t;

		public SemanticException(Token t, String message) {
			super("line :" + t.line() + " pos :" + t.posInLine() + " error: "+  message);
			this.t = t;
		}
	}
	
	SymbolTable table = new SymbolTable();	
	
	// Name is only used for naming the output file. 
	// Visit the child block to type check program.
	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		program.block.visit(this, arg);
		return program.progName;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		table.enterScope();
		for (ASTNode decOrstats : block.decsOrStatements) {
			decOrstats.visit(this, arg);
		}
		table.leaveScope();
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = declaration.name;
		Kind decType = declaration.type;
		System.out.println("visit dec:" + declaration);
		System.out.println("current scope:" + table.currentScope);

		if (table.insert(name, declaration)) {
			System.out.println("table:" + table.table);

			Expression e0 = declaration.width;
			Expression e1 = declaration.height;

			if (e0 != null && e1 != null) {
				Type e0Type = (Type)e0.visit(this, arg);
				Type e1Type = (Type)e1.visit(this, arg);
				if (decType.equals(Kind.KW_image)) {
					if (!(e0Type == Type.INTEGER && e1Type == Type.INTEGER)) {
						String msg = "Illigal token is" + declaration.firstToken + " When declaration type is image, but height and width not both of them type are integer";
						throw new SemanticException(declaration.firstToken, msg);
					}
				} else {
					String msg = "Illigal token is" + declaration.firstToken + " When width and height exist, but KW is not image";
					throw new SemanticException(declaration.firstToken, msg);
				}

			} else if ((e0 == null && e1 != null) || (e0 != null && e1 == null)) {
				String msg = "Illigal token is" + declaration.firstToken + " not satisfy (expression0 == e) == (expression1 == e)";
				throw new SemanticException(declaration.firstToken, msg);
			}
		} else {
			String msg = "Illigal token is" + declaration.firstToken + " declaration has already exist";
			throw new SemanticException(declaration.firstToken, msg);
		}
		return null;
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Declaration sourceDec = table.lookup(statementWrite.sourceName);
		if (sourceDec == null) {
			String msg = "Illigal token is" + statementWrite.firstToken + " sourceDec is null";
			throw new SemanticException(statementWrite.firstToken, msg);		
		}

		if (!sourceDec.type.equals(Kind.KW_image)) {
			String msg = "Illigal token is" + statementWrite.firstToken + " sourceDec Type is not KW_IMAGE";
			throw new SemanticException(statementWrite.firstToken, msg);		
		}
		
		Declaration destDec = table.lookup(statementWrite.destName);
		if (destDec == null) {
			String msg = "Illigal token is" + statementWrite.firstToken + " destDec is null";
			throw new SemanticException(statementWrite.firstToken, msg);		
		}
	
		if (!destDec.type.equals(Kind.KW_filename)) {
			String msg = "Illigal token is" + statementWrite.firstToken + " destDec Type is not KW_IMAGE";
			throw new SemanticException(statementWrite.firstToken, msg);				
		}
		statementWrite.sourceDec = sourceDec;
		statementWrite.destDec = destDec;
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg) throws Exception {
		// TODO Auto-generated method stub
		System.out.println(statementInput.destName + ": nams");

		Declaration dec = table.lookup(statementInput.destName);
		statementInput.dec = dec;
		if(dec == null) {
			String msg = "Illigal token is" + statementInput.firstToken + " statementInput dec is null";
			throw new SemanticException(statementInput.firstToken, msg);				
		}
		
		Type eType = (Type)statementInput.e.visit(this, arg);
		if (!eType.equals(Type.INTEGER)) {
			String msg = "Illigal token is" + statementInput.firstToken + " statementInput expression is not INTEGER";
			throw new SemanticException(statementInput.firstToken, msg);				
		}
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = pixelSelector.ex;
		Expression e1 = pixelSelector.ey;
		Type e0Type = (Type)e0.visit(this, arg);
		Type e1Type = (Type)e1.visit(this, arg);
		
		System.out.println(e0Type + ":fa");
		System.out.println(e1Type);
		
		if (e0Type.equals(e1Type)) {
			// do nothing
		} else {
			String msg = "Illigal token is" + pixelSelector.firstToken + " two expression type is not same";
			throw new SemanticException(pixelSelector.firstToken, msg);				
		}
		if (e0Type.equals(Type.INTEGER) || e1Type.equals(Type.FLOAT)) {
			// do nothing d
		} else {
			String msg = "Illigal token is" + pixelSelector.firstToken + " type of e0 is not float";
			throw new SemanticException(pixelSelector.firstToken, msg);					
		}
		return null;
	}

	@Override
	public Object visitExpressionConditional(ExpressionConditional expressionConditional, Object arg) throws Exception {
		Expression e0 = expressionConditional.guard;
		Expression e1 = expressionConditional.trueExpression;
		Expression e2 = expressionConditional.falseExpression;
		Type e0Type = (Type)e0.visit(this, arg);
		Type e1Type = (Type)e1.visit(this, arg);
		Type e2Type = (Type)e2.visit(this, arg);
		if (e0Type.equals(Type.BOOLEAN)) {
			// do nothing
		} else {
			String msg = "Illigal token is" + expressionConditional.firstToken + " guard is not boolean";
			throw new SemanticException(expressionConditional.firstToken, msg);					
		}
		
		if (e1Type.equals(e2Type)) {
			// do nothing
		} else {
			String msg = "Illigal token is" + expressionConditional.firstToken + " the type of trueexpression and falseexpression are not same";
			throw new SemanticException(expressionConditional.firstToken, msg);						
		}
		expressionConditional.type = e1Type;
		return e1Type;
	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = expressionBinary.leftExpression;
		Expression e1 = expressionBinary.rightExpression;
		Type e0Type = (Type)e0.visit(this, arg);
		Type e1Type = (Type)e1.visit(this, arg);

		Kind op = expressionBinary.op;

		if (e0Type.equals(Type.INTEGER) && e1Type.equals(Type.INTEGER)) {
			if (op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_MINUS) || op.equals(Kind.OP_TIMES) || op.equals(Kind.OP_DIV) || op.equals(Kind.OP_MOD) || op.equals(Kind.OP_POWER) || op.equals(Kind.OP_AND) || op.equals(Kind.OP_OR)) {
				expressionBinary.type = Type.INTEGER;
				return Type.INTEGER;
			} else if (op.equals(Kind.OP_EQ)||op.equals(Kind.OP_NEQ)||op.equals(Kind.OP_GT)||op.equals(Kind.OP_GE)||op.equals(Kind.OP_LT)||op.equals(Kind.OP_LE)) {
				expressionBinary.type = Type.BOOLEAN;
				return Type.BOOLEAN;
			} else {
				String msg = "Illigal token is" + expressionBinary.firstToken + " When both types are Integer, the operator is illegal";
				throw new SemanticException(expressionBinary.firstToken, msg);						
			}
		} else if (e0Type.equals(Type.FLOAT) && e1Type.equals(Type.FLOAT)) {
			if (op.equals(Kind.OP_PLUS)||op.equals(Kind.OP_MINUS)||op.equals(Kind.OP_TIMES)||op.equals(Kind.OP_DIV)||op.equals(Kind.OP_POWER)) {
				expressionBinary.type = Type.FLOAT;
				return Type.FLOAT;
			} else if (op.equals(Kind.OP_EQ)||op.equals(Kind.OP_NEQ)||op.equals(Kind.OP_GT)||op.equals(Kind.OP_GE)||op.equals(Kind.OP_LT)||op.equals(Kind.OP_LE)) {
				expressionBinary.type = Type.BOOLEAN;
				return Type.BOOLEAN;
			} else {
				String msg = "Illigal token is" + expressionBinary.firstToken + " When both types are Float, the operator is illegal";
				throw new SemanticException(expressionBinary.firstToken, msg);						
			}
		} else if (e0Type.equals(Type.INTEGER) && e1Type.equals(Type.FLOAT)) {
			if (op.equals(Kind.OP_PLUS)||op.equals(Kind.OP_MINUS)||op.equals(Kind.OP_TIMES)||op.equals(Kind.OP_DIV)||op.equals(Kind.OP_POWER)) {
				expressionBinary.type = Type.FLOAT;
				return Type.FLOAT;
			} else {
				String msg = "Illigal token is" + expressionBinary.firstToken + " When one integer one float, the operator is illegal";
				throw new SemanticException(expressionBinary.firstToken, msg);						
			}
		} else if (e0Type.equals(Type.FLOAT) && e1Type.equals(Type.INTEGER)) {
			if (op.equals(Kind.OP_PLUS)||op.equals(Kind.OP_MINUS)||op.equals(Kind.OP_TIMES)||op.equals(Kind.OP_DIV)||op.equals(Kind.OP_POWER)) {
				expressionBinary.type = Type.FLOAT;
				return Type.FLOAT;
			} else {
				String msg = "Illigal token is" + expressionBinary.firstToken + " When one integer one float, the operator is illegal";
				throw new SemanticException(expressionBinary.firstToken, msg);		
			}
		} else if (e0Type.equals(Type.BOOLEAN) && e1Type.equals(Type.BOOLEAN)) {
			if (op.equals(Kind.OP_EQ)||op.equals(Kind.OP_NEQ)||op.equals(Kind.OP_GT)||op.equals(Kind.OP_GE)||op.equals(Kind.OP_LT)||op.equals(Kind.OP_LE)||op.equals(Kind.OP_AND)||op.equals(Kind.OP_OR)) {
				expressionBinary.type = Type.BOOLEAN;
				return Type.BOOLEAN;
			} else {
				String msg = "Illigal token is" + expressionBinary.firstToken + " When both are boolean, the operator is illegal";
				throw new SemanticException(expressionBinary.firstToken, msg);		
			}
		} else {
			String msg = "Illigal token is" + expressionBinary.firstToken + " The expression is illegal";
			throw new SemanticException(expressionBinary.firstToken, msg);		
		}
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = expressionUnary.expression;
		Type eType = (Type)e.visit(this, arg);
		expressionUnary.type = eType;
		return eType;
	}

	@Override
	public Object visitExpressionIntegerLiteral(ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionIntegerLiteral.type = Type.INTEGER;
		return Type.INTEGER;
	}

	@Override
	public Object visitBooleanLiteral(ExpressionBooleanLiteral expressionBooleanLiteral, Object arg) throws Exception {
		// TODO Auto-generated method stub
		expressionBooleanLiteral.type = Type.BOOLEAN;
		return Type.BOOLEAN;
	}

	@Override
	public Object visitExpressionPredefinedName(ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionPredefinedName.type = Type.INTEGER;
		return Type.INTEGER;
	}

	@Override
	public Object visitExpressionFloatLiteral(ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		expressionFloatLiteral.type = Type.FLOAT;
		return Type.FLOAT;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg, Object arg)
			throws Exception {
		Expression e = expressionFunctionAppWithExpressionArg.e;
		Type eType = (Type)e.visit(this, arg);
		Kind function = expressionFunctionAppWithExpressionArg.function;

		if (eType.equals(Type.INTEGER) && (function.equals(Kind.KW_abs) || function.equals(Kind.KW_red) || function.equals(Kind.KW_green) || function.equals(Kind.KW_blue) || function.equals(Kind.KW_alpha))) {
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			return Type.INTEGER;
		}
		else if (eType.equals(Type.INTEGER) && (function.equals(Kind.KW_float))) {
			expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			return Type.FLOAT;
		} else if (eType.equals(Type.INTEGER) && (function.equals(Kind.KW_int))) {
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			return Type.INTEGER;
		}
		else if (eType.equals(Type.FLOAT) && (function.equals(Kind.KW_abs) || function.equals(Kind.KW_sin) || function.equals(Kind.KW_cos) || function.equals(Kind.KW_atan) || function.equals(Kind.KW_log))) {
			expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			return Type.FLOAT;
		} 
		else if (eType.equals(Type.FLOAT) && (function.equals(Kind.KW_float))) {
			expressionFunctionAppWithExpressionArg.type = Type.FLOAT;
			return Type.FLOAT;
		} 
		else if (eType.equals(Type.FLOAT) && (function.equals(Kind.KW_int))) {
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			return Type.INTEGER;
		} 
		else if (eType.equals(Type.IMAGE) && (function.equals(Kind.KW_width))) {
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			return Type.INTEGER;
		} 
		else if (eType.equals(Type.IMAGE) && function.equals(Kind.KW_height)) {
			expressionFunctionAppWithExpressionArg.type = Type.INTEGER;
			return Type.INTEGER;
		} else {
			String msg = "Illigal token is" + expressionFunctionAppWithExpressionArg.firstToken + "binary exception";
			throw new SemanticException(expressionFunctionAppWithExpressionArg.firstToken, msg);
		}
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = expressionFunctionAppWithPixel.e0;
		Expression e1 = expressionFunctionAppWithPixel.e1;
		Kind name = expressionFunctionAppWithPixel.name;
		Type e0Type = (Type)e0.visit(this, arg);
		Type e1Type = (Type)e1.visit(this, arg);
		if (name.equals(Kind.KW_cart_x)  || name.equals(Kind.KW_cart_y)) {
			if (e0Type.equals(Type.FLOAT) && e1Type.equals(Type.FLOAT)) {
				expressionFunctionAppWithPixel.type = Type.INTEGER;
				return Type.INTEGER;
			} else {
				String msg = "Illigal token is" + expressionFunctionAppWithPixel.firstToken + "expression type illegal";
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken, msg);
			}
		}
		
		if (name.equals(Kind.KW_polar_a) || name.equals(Kind.KW_polar_r)) {
			if (e0Type.equals(Type.INTEGER) && e1Type.equals(Type.INTEGER)) {
				expressionFunctionAppWithPixel.type = Type.FLOAT;
				return Type.FLOAT;
			} else {
				String msg = "Illigal token is" + expressionFunctionAppWithPixel.firstToken + "expression type illegal";
				throw new SemanticException(expressionFunctionAppWithPixel.firstToken, msg);
			}
		}
		String msg = "Illigal token is" + expressionFunctionAppWithPixel.firstToken + "functionname illegal";
		throw new SemanticException(expressionFunctionAppWithPixel.firstToken, msg);	
	}

	@Override
	public Object visitExpressionPixelConstructor(ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		// TODO Auto-generated method stub
		Expression alpha = expressionPixelConstructor.alpha;
		Expression red = expressionPixelConstructor.red;
		Expression green = expressionPixelConstructor.green;
		Expression blue = expressionPixelConstructor.blue;
		Type alphaType = (Type)alpha.visit(this, arg);
		Type redType = (Type)red.visit(this, arg);
		Type greenType = (Type)green.visit(this, arg);
		Type blueType = (Type)blue.visit(this, arg);
		if (alphaType.equals(Type.INTEGER) && redType.equals(Type.INTEGER) && greenType.equals(Type.INTEGER) && blueType.equals(Type.INTEGER)) {
			expressionPixelConstructor.type = Type.INTEGER;
			return Type.INTEGER;
		} else {
			String msg = "Illigal token is" + expressionPixelConstructor.firstToken + "expression type illegal";
			throw new SemanticException(expressionPixelConstructor.firstToken, msg);	
		}
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws Exception {
		// TODO Auto-generated method stub
		LHS lhs = statementAssign.lhs;
		Expression e = statementAssign.e;
		Type lhsType = (Type)lhs.visit(this, arg);
		Type eType = (Type)e.visit(this, arg);

		if (lhsType != eType) {
			String msg = "Illigal token is" + statementAssign.firstToken + "LHS type noe equals expression type";
			throw new SemanticException(statementAssign.firstToken, msg);	
		}
		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = statementShow.e;
		Type eType = (Type)e.visit(this, arg);
		if (!(eType.equals(Type.INTEGER) || eType.equals(Type.FLOAT) || eType.equals(Type.BOOLEAN) || eType.equals(Type.IMAGE))) {
			String msg = "Illigal token is" + statementShow.firstToken + "expression type not legal";
			throw new SemanticException(statementShow.firstToken, msg);	
		} 
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = expressionPixel.name;
		PixelSelector pixelSelector = expressionPixel.pixelSelector;
		Declaration dec = table.lookup(name);
		if (dec == null) {
			String msg = "Illigal token is" + expressionPixel.firstToken + "dec is null";
			throw new SemanticException(expressionPixel.firstToken, msg);	
		} 
		expressionPixel.dec = dec;
		Kind decType = dec.type;
		if (decType.equals(Kind.KW_image)) {
			expressionPixel.type = Type.INTEGER;
			pixelSelector.visit(this, arg);
			return Type.INTEGER;
		}
		String msg = "Illigal token is" + expressionPixel.firstToken + "dec type is not image";
		throw new SemanticException(expressionPixel.firstToken, msg);	
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = expressionIdent.name;
		Declaration dec = table.lookup(name);
		if (dec != null) {
			expressionIdent.dec = dec;
			Type decType = Types.getType(dec.type);
			expressionIdent.type = decType;
			return decType;
		} else {
			String msg = "Illigal token is" + expressionIdent.firstToken + "dec is null";
			throw new SemanticException(expressionIdent.firstToken, msg);	
		}
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = lhsSample.name;
		Declaration dec = table.lookup(name);
		lhsSample.pixelSelector.visit(this, arg);
		if (dec == null) {
			String msg = "Illigal token is" + lhsSample.firstToken + "dec is null";
			throw new SemanticException(lhsSample.firstToken, msg);	
		}
		lhsSample.dec = dec;
		Type decType = Types.getType(dec.type);
		if (decType.equals(Type.IMAGE)) {
			lhsSample.type = Type.INTEGER;
			return Type.INTEGER;
		}
		String msg = "Illigal token is" + lhsSample.firstToken + "dec type not image";
		throw new SemanticException(lhsSample.firstToken, msg);	
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = lhsPixel.name;
		Declaration dec = table.lookup(name);
		lhsPixel.pixelSelector.visit(this, arg);
		if (dec == null) {
			String msg = "Illigal token is" + lhsPixel.firstToken + "dec is null";
			throw new SemanticException(lhsPixel.firstToken, msg);	
		}
		lhsPixel.dec = dec;
		Type decType = Types.getType(dec.type);
		if (decType.equals(Type.IMAGE)) {
			lhsPixel.type = Type.INTEGER;
			return Type.INTEGER;
		}
		String msg = "Illigal token is" + lhsPixel.firstToken + "dec type not image";
		throw new SemanticException(lhsPixel.firstToken, msg);	
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String name = lhsIdent.name;
		Declaration dec = table.lookup(name);
		if (dec == null) {
			String msg = "Illigal token is" + lhsIdent.firstToken + "dec is null";
			throw new SemanticException(lhsIdent.firstToken, msg);	
		}
		lhsIdent.dec = dec;
		Type decType = Types.getType(dec.type);
		lhsIdent.type = decType;
		return decType;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e = statementIf.guard;
		Type eType = (Type)e.visit(this, arg);
		if (!eType.equals(Type.BOOLEAN)) {
			String msg = "Illigal token is" + statementIf.firstToken + " type is not boolean";
			throw new SemanticException(statementIf.firstToken, msg);	
		}
		statementIf.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws Exception {
		Expression e = statementWhile.guard;
		Type eType = (Type)e.visit(this, arg);
		if (!eType.equals(Type.BOOLEAN)) {
			throw new SemanticException(statementWhile.firstToken, "ILLIGAL TOKEN IS" + statementWhile.firstToken + " type is not boolean");
		}
		statementWhile.b.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg) throws Exception {
		Expression e = statementSleep.duration;
		Type eType = (Type)e.visit(this, arg);
		if (!eType.equals(Type.INTEGER)) {
			throw new SemanticException(statementSleep.firstToken, "ILLIGAL TOKEN IS" + statementSleep.firstToken + " duration is not integer");
		}
		return null;
	}


}

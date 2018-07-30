/**
 * Starter code for CodeGenerator.java used n the class project in COP5556 Programming Language Principles 
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import cop5556sp18.Types.Type;
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

import cop5556sp18.CodeGenUtils;
import cop5556sp18.Scanner.Kind;
import cop5556sp18.TypeChecker.SemanticException;

public class CodeGenerator implements ASTVisitor, Opcodes {

	/**
	 * All methods and variable static.
	 */

	static final int Z = 255;

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	Label dbegin;
	Label dend;
	final int defaultWidth;
	final int defaultHeight;
	int slot_number = 1;
	// final boolean itf = false;
	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 * @param defaultWidth
	 *            default width of images
	 * @param defaultHeight
	 *            default height of images
	 */
	public CodeGenerator(boolean DEVEL, boolean GRADE, String sourceFileName,
			int defaultWidth, int defaultHeight) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
	}
	
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		Label begin = new Label();
		
		// register begin label
//		mv.visitLabel(begin);
		Label end = new Label();
		for (ASTNode node : block.decsOrStatements) {
			// add Declaration
			if (node instanceof Declaration) {
				((Declaration)node).slot_number = slot_number;
				mv.visitLocalVariable(((Declaration)node).name, Types.getJVMType(Types.getType(((Declaration)node).type)), null, begin, end, slot_number);
				slot_number++;
			}
			node.visit(this, arg);
		}
		// register end label
//		dend = end;

		mv.visitLabel(end);
		return null;
	}

	@Override
	public Object visitBooleanLiteral(
			ExpressionBooleanLiteral expressionBooleanLiteral, Object arg)
			throws Exception {
		if (expressionBooleanLiteral.value){
			mv.visitInsn(ICONST_1); // TRUE
		}else{
		    mv.visitInsn(ICONST_0); // FALSE
		}
		return null;
	}

	@Override
	public Object visitDeclaration(Declaration declaration, Object arg)
			throws Exception {
		Kind type = declaration.type;
		
//		slot_number++;
		Expression e1 = declaration.width;
		Expression e2 = declaration.height;
		if (type.equals(Kind.KW_image) && e1 != null && e2 != null) {
			e1.visit(this, arg);
			e2.visit(this, arg);
		} else if (type.equals(Kind.KW_image) && e1 == null && e2 == null) {
			mv.visitLdcInsn(defaultWidth);
			mv.visitLdcInsn(defaultHeight);
//			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
		}
		if (type.equals(Kind.KW_image)) {
			mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeImage", RuntimeImageSupport.makeImageSig, false);
			mv.visitVarInsn(ASTORE, declaration.slot_number);
		} 
		return null;

	}

	@Override
	public Object visitExpressionBinary(ExpressionBinary expressionBinary,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0 = expressionBinary.leftExpression;
		Expression e1 = expressionBinary.rightExpression;
		Type e0Type = e0.getType();
		Type e1Type = e1.getType();
		Label falseLabel = new Label();
		Label trueLabel = new Label();
		Kind op = expressionBinary.op;


		if (e0Type.equals(Type.INTEGER) && e1Type.equals(Type.INTEGER)) {
			if (op.equals(Kind.OP_PLUS) || op.equals(Kind.OP_MINUS) || op.equals(Kind.OP_TIMES) || op.equals(Kind.OP_DIV) || op.equals(Kind.OP_MOD) || op.equals(Kind.OP_POWER) || op.equals(Kind.OP_AND) || op.equals(Kind.OP_OR)) {
				switch(op) {
					case OP_PLUS:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IADD);
						break;
					case OP_MINUS:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(ISUB);
						break;
					case OP_TIMES:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IMUL);
						break;
					case OP_DIV:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IDIV);
						break;
					case OP_MOD:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IREM);
						break;
					case OP_POWER:
						e0.visit(this, arg);
						mv.visitInsn(I2D);
						e1.visit(this,  arg);
						mv.visitInsn(I2D);
		                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
		                mv.visitInsn(D2I);
		                break;
					case OP_AND:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IAND);
						break;
					case OP_OR:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IOR);
						break;
					default:
						break;
				}
			} else if (op.equals(Kind.OP_EQ)||op.equals(Kind.OP_NEQ)||op.equals(Kind.OP_GT)||op.equals(Kind.OP_GE)||op.equals(Kind.OP_LT)||op.equals(Kind.OP_LE)) {
				switch(op) {
					case OP_EQ:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_NEQ:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPNE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_GT:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPGT, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_GE:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPGE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_LT:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPLT, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_LE:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPLE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					default:
						break;	
				}
			}
		} else if (e0Type.equals(Type.FLOAT) && e1Type.equals(Type.FLOAT)) {
			if (op.equals(Kind.OP_PLUS)||op.equals(Kind.OP_MINUS)||op.equals(Kind.OP_TIMES)||op.equals(Kind.OP_DIV)||op.equals(Kind.OP_POWER)) {
				switch(op) {
					case OP_PLUS:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FADD);
						break;
					case OP_MINUS:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FSUB);
						break;
					case OP_TIMES:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FMUL);
						break;
					case OP_DIV:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FDIV);
						break;
					case OP_POWER:
						e0.visit(this, arg);
						mv.visitInsn(F2D);
						e1.visit(this,  arg);
						mv.visitInsn(F2D);
		                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
		                mv.visitInsn(D2F);
						break;
					default: 
						break;
				}

			} else if (op.equals(Kind.OP_EQ)||op.equals(Kind.OP_NEQ)||op.equals(Kind.OP_GT)||op.equals(Kind.OP_GE)||op.equals(Kind.OP_LT)||op.equals(Kind.OP_LE)) {
				switch(op) {
					case OP_EQ:
						e0.visit(this, arg);
						e1.visit(this, arg);
//						mv.visitInsn(FSUB);
//						mv.visitInsn(F2);
						mv.visitInsn(FCMPG);
						mv.visitJumpInsn(IFEQ, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					
					case OP_NEQ:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FCMPG);
						mv.visitJumpInsn(IFNE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_GT:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FCMPG);
						mv.visitJumpInsn(IFGT, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_GE:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FCMPG);
						mv.visitJumpInsn(IFGE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_LT:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FCMPG);
						mv.visitJumpInsn(IFLT, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_LE:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(FCMPG);
						mv.visitJumpInsn(IFLE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					default:
						break;
				}
			} else {
				throw new Exception();		
			}
		} else if (e0Type.equals(Type.INTEGER) && e1Type.equals(Type.FLOAT)) {
			if (op.equals(Kind.OP_PLUS)||op.equals(Kind.OP_MINUS)||op.equals(Kind.OP_TIMES)||op.equals(Kind.OP_DIV)||op.equals(Kind.OP_POWER)) {
				switch(op) {
					case OP_PLUS:
						e0.visit(this, arg);
						mv.visitInsn(I2F);
						e1.visit(this, arg);
						mv.visitInsn(FADD);
						break;
					
					case OP_MINUS:
						e0.visit(this, arg);
						mv.visitInsn(I2F);
						e1.visit(this, arg);
						mv.visitInsn(FSUB);
						break;
					case OP_TIMES:
						e0.visit(this, arg);
						mv.visitInsn(I2F);
						e1.visit(this, arg);
						mv.visitInsn(FMUL);
						break;
					case OP_DIV:
						e0.visit(this, arg);
						mv.visitInsn(I2F);
						e1.visit(this, arg);
						mv.visitInsn(FDIV);
						break;
					case OP_POWER:
						e0.visit(this, arg);
						mv.visitInsn(I2D);
						e1.visit(this,  arg);
						mv.visitInsn(F2D);
		                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
		                mv.visitInsn(D2F);
						break;
					case OP_MOD:
						e0.visit(this, arg);
						mv.visitInsn(I2F);
						e1.visit(this, arg);
						mv.visitInsn(FREM);
						break;
					default:
						break;
				}
			} 
		} else if (e0Type.equals(Type.FLOAT) && e1Type.equals(Type.INTEGER)) {
			if (op.equals(Kind.OP_PLUS)||op.equals(Kind.OP_MINUS)||op.equals(Kind.OP_TIMES)||op.equals(Kind.OP_DIV)||op.equals(Kind.OP_POWER)) {
				switch(op) {
					case OP_PLUS:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(I2F);

						mv.visitInsn(FADD);
						break;
					
					case OP_MINUS:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(I2F);

						mv.visitInsn(FSUB);
						break;
					case OP_TIMES:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(I2F);

						mv.visitInsn(FMUL);
						break;
					case OP_DIV:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(I2F);

						mv.visitInsn(FDIV);
						break;
					case OP_POWER:
						e0.visit(this, arg);
						mv.visitInsn(F2D);
						e1.visit(this,  arg);
						mv.visitInsn(I2D);
		                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "pow", "(DD)D", false);
		                mv.visitInsn(D2F);
						break;
					default:
						break;
				}
			} 
		} else if (e0Type.equals(Type.BOOLEAN) && e1Type.equals(Type.BOOLEAN)) {
			if (op.equals(Kind.OP_EQ)||op.equals(Kind.OP_NEQ)||op.equals(Kind.OP_GT)||op.equals(Kind.OP_GE)||op.equals(Kind.OP_LT)||op.equals(Kind.OP_LE)||op.equals(Kind.OP_AND)||op.equals(Kind.OP_OR)) {
				switch(op) {
					case OP_EQ:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPEQ, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_NEQ:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPNE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_GT:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPGT, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_GE:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPGE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_LT:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPLT, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_LE:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitJumpInsn(IF_ICMPLE, trueLabel);
						mv.visitInsn(ICONST_0);
						mv.visitJumpInsn(GOTO, falseLabel);
						mv.visitLabel(trueLabel);
						mv.visitInsn(ICONST_1);
						mv.visitLabel(falseLabel);
						break;
					case OP_AND:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IAND);
					case OP_OR:
						e0.visit(this, arg);
						e1.visit(this, arg);
						mv.visitInsn(IOR);
					default:
						break;
				}
			} 
		} 
		return null;
	}

	@Override
	public Object visitExpressionConditional(
			ExpressionConditional expressionConditional, Object arg)
			throws Exception {
		Label falseLabel = new Label();
		Label trueLabel = new Label();
		expressionConditional.guard.visit(this, arg);
		mv.visitJumpInsn(IFNE, trueLabel); 
		expressionConditional.falseExpression.visit(this, arg);
		mv.visitJumpInsn(GOTO, falseLabel);
		mv.visitLabel(trueLabel);
		expressionConditional.trueExpression.visit(this, arg);
		mv.visitLabel(falseLabel);
		return null;
	}

	@Override
	public Object visitExpressionFloatLiteral(
			ExpressionFloatLiteral expressionFloatLiteral, Object arg)
			throws Exception {
		mv.visitLdcInsn(expressionFloatLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithExpressionArg(
			ExpressionFunctionAppWithExpressionArg expressionFunctionAppWithExpressionArg,
			Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind function = expressionFunctionAppWithExpressionArg.function;
		Expression e = expressionFunctionAppWithExpressionArg.e;
		e.visit(this, arg);
		Type type = e.type;
		if (type.equals(Type.INTEGER)) {
			if (function.equals(Kind.KW_abs) || function.equals(Kind.KW_red) || function.equals(Kind.KW_green) || function.equals(Kind.KW_blue) || function.equals(Kind.KW_alpha)) {
				switch(function) {
					case KW_abs:
						mv.visitMethodInsn(INVOKESTATIC, RuntimeWrapper.className, "integerabs", RuntimeWrapper.integerabsSig, false);
						break;
					case KW_red:
						mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getRed", "(I)I", false);
						break;
					case KW_green:
						mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getGreen", "(I)I", false);
						break;
					case KW_blue:
						mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getBlue", "(I)I", false);
						break;
					case KW_alpha:
						mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "getAlpha", "(I)I", false);
						break;
					default:
						break;
				}

			} else if (function.equals(Kind.KW_float)) {
				mv.visitInsn(I2F);
			} 
		} else if (type.equals(Type.FLOAT)) {
			System.out.println("testflow");
			if (function.equals(Kind.KW_abs) || function.equals(Kind.KW_sin) || function.equals(Kind.KW_cos) || function.equals(Kind.KW_atan) || function.equals(Kind.KW_log)) {
				switch(function) {
					case KW_abs:
						mv.visitMethodInsn(INVOKESTATIC, RuntimeWrapper.className, "floatabs", RuntimeWrapper.floatabsSig, false);
						break;
					case KW_sin:
						mv.visitMethodInsn(INVOKESTATIC, RuntimeWrapper.className, "floatsin", RuntimeWrapper.floatsinSig, false);
						break;
					case KW_cos:
						mv.visitMethodInsn(INVOKESTATIC, RuntimeWrapper.className, "floatcos", RuntimeWrapper.floatcosSig, false);
						break;
					case KW_atan:
						mv.visitMethodInsn(INVOKESTATIC, RuntimeWrapper.className, "floatatan", RuntimeWrapper.floatcosSig, false);
						break;
					case KW_log:
						mv.visitMethodInsn(INVOKESTATIC, RuntimeWrapper.className, "floatlog", RuntimeWrapper.floatlogSig, false);
						break;
					default:
						break;
				}
			} else if (function.equals(Kind.KW_float)) {
				// do nothing
			} else if (function.equals(Kind.KW_int)) {
				mv.visitInsn(F2I);	
			} 
		} else if (type.equals(Type.IMAGE)) {
			if (function.equals(Kind.KW_width)) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className, "getWidth", RuntimeImageSupport.getWidthSig, false);
			} else if (function.equals(Kind.KW_height)) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, RuntimeImageSupport.className, "getHeight", RuntimeImageSupport.getHeightSig, false);
			} 
		} 
		return null;
	}

	@Override
	public Object visitExpressionFunctionAppWithPixel(
			ExpressionFunctionAppWithPixel expressionFunctionAppWithPixel,
			Object arg) throws Exception {
		Kind name = expressionFunctionAppWithPixel.name;
		Expression e0 = expressionFunctionAppWithPixel.e0;
		Expression e1 = expressionFunctionAppWithPixel.e1;
		if (name.equals(Kind.KW_cart_x)) {
			e0.visit(this, arg);
			mv.visitInsn(F2D); 
			e1.visit(this, arg);
			mv.visitInsn(F2D); 
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(DMUL); 
			mv.visitInsn(D2I); 
		} else if(name.equals(Kind.KW_cart_y)) {
			e0.visit(this, arg);
			mv.visitInsn(F2D); 
			e1.visit(this, arg);
			mv.visitInsn(F2D); 
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(DMUL); 
			mv.visitInsn(D2I); 
		} else if(name.equals(Kind.KW_polar_a) ) {
			e1.visit(this, arg);
			mv.visitInsn(I2D); 
			e0.visit(this, arg);
			mv.visitInsn(I2D); 
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "atan2", "(DD)D", false);
			mv.visitInsn(D2F); 
		} else if (name.equals(Kind.KW_polar_r)) {
			e0.visit(this, arg);
			mv.visitInsn(I2D); 
			e1.visit(this, arg);
			mv.visitInsn(I2D); 
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "hypot", "(DD)D", false);
			mv.visitInsn(D2F); 
		}
		return null;
	}

	@Override
	public Object visitExpressionIdent(ExpressionIdent expressionIdent,
			Object arg) throws Exception {
		Declaration dec = expressionIdent.dec;
		switch(dec.type) {
			case KW_boolean:
				mv.visitVarInsn(ILOAD, dec.slot_number);  
				break;
			case KW_int:
				mv.visitVarInsn(ILOAD, dec.slot_number);  
				break;
			case KW_image:
				mv.visitVarInsn(ALOAD, dec.slot_number); 
				break;
			case KW_float:
				mv.visitVarInsn(FLOAD, dec.slot_number);
				break;
			case KW_filename:
				mv.visitVarInsn(ALOAD, dec.slot_number); 
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitExpressionIntegerLiteral(
			ExpressionIntegerLiteral expressionIntegerLiteral, Object arg)
			throws Exception {
		// This one is all done!
		mv.visitLdcInsn(expressionIntegerLiteral.value);
		return null;
	}

	@Override
	public Object visitExpressionPixel(ExpressionPixel expressionPixel,
			Object arg) throws Exception {
		mv.visitVarInsn(ALOAD, expressionPixel.dec.slot_number);
		PixelSelector ps = expressionPixel.pixelSelector;
		ps.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "getPixel", RuntimeImageSupport.getPixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPixelConstructor(
			ExpressionPixelConstructor expressionPixelConstructor, Object arg)
			throws Exception {
		Expression alpha = expressionPixelConstructor.alpha;
		Expression red = expressionPixelConstructor.red;
		Expression blue = expressionPixelConstructor.blue;
		Expression green = expressionPixelConstructor.green;
		alpha.visit(this, arg);
		red.visit(this, arg);
		green.visit(this, arg);
		blue.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimePixelOps.className, "makePixel", RuntimePixelOps.makePixelSig, false);
		return null;
	}

	@Override
	public Object visitExpressionPredefinedName(
			ExpressionPredefinedName expressionPredefinedName, Object arg)
			throws Exception {
		switch(expressionPredefinedName.name) {
			case KW_Z:   
				mv.visitLdcInsn(Z);
				break;
			case KW_default_height:
				mv.visitLdcInsn(defaultHeight);
				break;
			case KW_default_width:
				mv.visitLdcInsn(defaultWidth);
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitExpressionUnary(ExpressionUnary expressionUnary,
			Object arg) throws Exception {
		expressionUnary.expression.visit(this, arg);
		Kind op = expressionUnary.op;
		Type type = expressionUnary.expression.getType();
		switch(op) {
			case OP_PLUS:   
				// do nothing
				break;
			case OP_MINUS:
				if (type.equals(Type.FLOAT)) {
					mv.visitInsn(FNEG);
				} else {
					mv.visitInsn(INEG);
				}
				break;
			case OP_EXCLAMATION:
				if (type.equals(Type.BOOLEAN)) {
					Label falseLabel = new Label();
					Label trueLabel = new Label(); 
					mv.visitJumpInsn(IFEQ, trueLabel); 
					mv.visitInsn(ICONST_0);
					mv.visitJumpInsn(GOTO, falseLabel);
					mv.visitLabel(trueLabel);
					mv.visitInsn(ICONST_1);
					mv.visitLabel(falseLabel);
				} else {
					mv.visitLdcInsn(new Integer(-1));
					mv.visitInsn(IXOR);
				}
				break;
			default:
				break;
		}
		return null;
	}

	@Override
	public Object visitLHSIdent(LHSIdent lhsIdent, Object arg)
			throws Exception {
		Declaration dec = lhsIdent.dec;
		Kind type = dec.type;
		switch(type) {
			case KW_filename: 
				mv.visitVarInsn(ASTORE, dec.slot_number);
				break;
			case KW_boolean:
				mv.visitVarInsn(ISTORE, dec.slot_number);
				break;
			case KW_image:
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "deepCopy", RuntimeImageSupport.deepCopySig, false);
				mv.visitVarInsn(ASTORE, dec.slot_number);
				break;
			case KW_int:
				mv.visitVarInsn(ISTORE, dec.slot_number);
				break;
			case KW_float:
				mv.visitVarInsn(FSTORE, dec.slot_number);
				break;
			default:
				break;
		}	
		return null;
	}

	@Override
	public Object visitLHSPixel(LHSPixel lhsPixel, Object arg)
			throws Exception {
		//load x,y on the top of stack
		//lhs.index.visit(this, arg);
		PixelSelector ps = lhsPixel.pixelSelector;
		Declaration dec = lhsPixel.dec;
		mv.visitVarInsn(ALOAD, dec.slot_number);
		ps.visit(this, arg);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "setPixel", RuntimeImageSupport.setPixelSig, false);

		return null;
	}

	@Override
	public Object visitLHSSample(LHSSample lhsSample, Object arg)
			throws Exception {
		PixelSelector ps = lhsSample.pixelSelector;
		Declaration dec = lhsSample.dec;
		Kind color = lhsSample.color;
		mv.visitVarInsn(ALOAD, dec.slot_number);
		// load x, y
		ps.visit(this, arg);
		switch (color) {
			case KW_alpha:
				mv.visitInsn(ICONST_0);
				break;
			case KW_red:
				mv.visitInsn(ICONST_1);
				break;
			case KW_green:
				mv.visitInsn(ICONST_2);
				break;
			case KW_blue:
				mv.visitInsn(ICONST_3);
				break;
			default:
				break;
		}
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "updatePixelColor",RuntimeImageSupport.updatePixelColorSig, false);
		return null;
	}

	@Override
	public Object visitPixelSelector(PixelSelector pixelSelector, Object arg)
			throws Exception {
		Expression ex = pixelSelector.ex;
		Expression ey = pixelSelector.ey;
		ex.visit(this, arg);

		if (ex.type.equals(Type.FLOAT)) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "cos", "(D)D", false);
			mv.visitInsn(D2I);
		}
		ey.visit(this, arg);
		if (ey.type.equals(Type.FLOAT)) {
			mv.visitInsn(F2D);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "sin", "(D)D", false);
			mv.visitInsn(D2I);
		}
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO refactor and extend as necessary
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//		ClassWriter.COMPUTE_FRAMES
		// cw = new ClassWriter(0); //If the call to mv.visitMaxs(1, 1) crashes,
		// sometime helpful to
		// temporarily run it without COMPUTE_FRAMES. You probably
		// won't get a completely correct classfile, but
		// you will be able to see the code that was
		// generated.
		className = program.progName;
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null,
				"java/lang/Object", null);
		cw.visitSource(sourceFileName, null);

		// create main method
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main",
				"([Ljava/lang/String;)V", null, null);
		// initialize
		mv.visitCode();

		// add label before first instruction
		Label mainStart = new Label();
		mv.visitLabel(mainStart);

		CodeGenUtils.genLog(DEVEL, mv, "entering main");

		program.block.visit(this, arg);

		// generates code to add string to log
		CodeGenUtils.genLog(DEVEL, mv, "leaving main");

		// adds the required (by the JVM) return statement to main
		mv.visitInsn(RETURN);

		// adds label at end of code
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);

		// Because we use ClassWriter.COMPUTE_FRAMES as a parameter in the
		// constructor,
		// asm will calculate this itself and the parameters are ignored.
		// If you have trouble with failures in this routine, it may be useful
		// to temporarily change the parameter in the ClassWriter constructor
		// from COMPUTE_FRAMES to 0.
		// The generated classfile will not be correct, but you will at least be
		// able to see what is in it.

		mv.visitMaxs(0, 0);

		// terminate construction of main method
		mv.visitEnd();

		// terminate class construction
		cw.visitEnd();

		// generate classfile as byte array and return
		return cw.toByteArray();
	}

	@Override
	public Object visitStatementAssign(StatementAssign statementAssign,
			Object arg) throws Exception {
		statementAssign.e.visit(this, arg);
		statementAssign.lhs.visit(this, arg);
		return null;
	}

	@Override
	public Object visitStatementIf(StatementIf statementIf, Object arg)
			throws Exception {
		Expression guard = statementIf.guard;
		guard.visit(this, arg);
		Label falseLabel = new Label();
		mv.visitJumpInsn(IFEQ, falseLabel);
		statementIf.b.visit(this, arg);
		mv.visitLabel(falseLabel);
		return null;
	}

	@Override
	public Object visitStatementInput(StatementInput statementInput, Object arg)
			throws Exception {
		Expression e = statementInput.e;
		Declaration dec = statementInput.dec;
		Kind kind = dec.type;
		mv.visitVarInsn(ALOAD, 0);
		e.visit(this, arg);
		mv.visitInsn(AALOAD);
		switch (kind) {
			case KW_int: {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
				mv.visitVarInsn(ISTORE, statementInput.dec.slot_number);
			}break;
			case KW_boolean: {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
				mv.visitVarInsn(ISTORE, statementInput.dec.slot_number);
			}break;
			case KW_image: {
				if (dec.height == null && dec.width == null) {
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ACONST_NULL);
				}
				else {
					dec.width.visit(this, arg);	
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					dec.height.visit(this, arg);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
				}
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "readImage", RuntimeImageSupport.readImageSig, false);
				mv.visitVarInsn(ASTORE, statementInput.dec.slot_number);

			}break;
			case KW_filename: {
				mv.visitVarInsn(ASTORE, dec.slot_number);
			}break;
			case KW_float: {
				mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "parseFloat", "(Ljava/lang/String;)F", false);
				mv.visitVarInsn(FSTORE, statementInput.dec.slot_number);
			}break;
		default:
			break;
		}

		return null;
	}

	@Override
	public Object visitStatementShow(StatementShow statementShow, Object arg)
			throws Exception {
		/**
		 * TODO refactor and complete implementation.
		 * 
		 * For integers, booleans, and floats, generate code to print to
		 * console. For images, generate code to display in a frame.
		 * 
		 * In all cases, invoke CodeGenUtils.genLogTOS(GRADE, mv, type); before
		 * consuming top of stack.
		 */
		statementShow.e.visit(this, arg);
		Type type = statementShow.e.getType();

		switch (type) {
			case INTEGER : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
						"Ljava/io/PrintStream;");
				mv.visitInsn(Opcodes.SWAP);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
						"println", "(I)V", false);
			}
			break;
			case BOOLEAN : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
			}
			break;
			case FLOAT : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(F)V", false);
			}
			break; 
			case FILE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String)V", false);
			}
			break;
			case IMAGE : {
				CodeGenUtils.genLogTOS(GRADE, mv, type);
				//consume top of stack
				mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "makeFrame", RuntimeImageSupport.makeFrameSig, false);
				mv.visitInsn(POP);
			}
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitStatementSleep(StatementSleep statementSleep, Object arg)
			throws Exception {
		statementSleep.duration.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);		
		//assert false : "not yet implemented";
		return null;
	}

	@Override
	public Object visitStatementWhile(StatementWhile statementWhile, Object arg)
			throws Exception {
		Label start = new Label();
		Label end = new Label();
		mv.visitJumpInsn(GOTO, start);
		mv.visitLabel(end);
		statementWhile.b.visit(this, arg);
		mv.visitLabel(start);
		statementWhile.guard.visit(this, arg);
		mv.visitJumpInsn(IFNE, end);
		return null;		
	}

	@Override
	public Object visitStatementWrite(StatementWrite statementWrite, Object arg)
			throws Exception {
		mv.visitVarInsn(ALOAD, statementWrite.sourceDec.slot_number);
		mv.visitVarInsn(ALOAD, statementWrite.destDec.slot_number);
		mv.visitMethodInsn(INVOKESTATIC, RuntimeImageSupport.className, "write", RuntimeImageSupport.writeSig, false);
		return null;
	}

}

package cop5556sp18.AST;

import org.objectweb.asm.Label;

import cop5556sp18.Scanner.Kind;

/**
 * This code is for the class project in COP5556 Programming Language Principles 
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

public class Declaration extends ASTNode {
	
	public final Kind type;
	public final String name;
	public final Expression width;  //non null only for images declared with a size
	public final Expression height;  //non null only for images declared with a size
	public int slot_number;
	public Label begin;
	public Label end;

	public Declaration(Token firstToken, Token type, Token name, Expression width, Expression height) {
		super(firstToken);
		this.type = type.kind;
		this.name = name.getText();
		this.width = width;
		this.height = height;
		this.begin = null;
		this.end = null;
		this.slot_number = -1;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDeclaration(this, arg);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((height == null) ? 0 : height.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((width == null) ? 0 : width.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof Declaration))
			return false;
		Declaration other = (Declaration) obj;
		if (height == null) {
			if (other.height != null)
				return false;
		} else if (!height.equals(other.height))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		if (width == null) {
			if (other.width != null)
				return false;
		} else if (!width.equals(other.width))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Declaration [type=" + type + ", name=" + name + ", width="
				+ width + ", height=" + height + "]";
	}


	
	

}

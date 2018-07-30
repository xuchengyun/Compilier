package cop5556sp18;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import cop5556sp18.AST.*;
import cop5556sp18.Types.Type;


public class SymbolTable {
	int currentScope;
	int nextScope;
	Stack<Integer> scopeStack = new Stack<>();
	HashMap<String,HashMap<Integer,Declaration>> table = new HashMap<>();

	public SymbolTable() {
		this.nextScope = 1;
		this.currentScope = 0;
		scopeStack.push(currentScope);
	}
	
	public void enterScope(){
		currentScope = nextScope++;
		System.out.println("enter scope current stack: " + scopeStack);

		System.out.println("table enterscope:" + currentScope);
		scopeStack.push(currentScope);
		
	}
	
	
	public void leaveScope() {
		System.out.println("leave scope current stack: " + scopeStack);
		scopeStack.pop();
		currentScope = scopeStack.peek();
		System.out.println("table leavescope:" + currentScope);
	}
	
	public Declaration lookup(String identifier){
		if(!table.containsKey(identifier)){
			return null;
		} else {
			HashMap<Integer,Declaration> cur = table.get(identifier);
			int pos = 0;
			for(int i = scopeStack.size() - 1;i > 0;i--){
				int scope = scopeStack.get(i);
				if(cur.get(scope) != null){
					pos = i;
					break;
				}
			}
			return cur.get(pos);
		}
	}
	
	public boolean insert(String identifier, Declaration dec){
		HashMap<Integer, Declaration> cur = new HashMap<>();
		if(!table.containsKey(identifier)){
			cur.put(currentScope, dec);
			table.put(identifier, cur);
			return true;
		}else{
			cur = table.get(identifier);
			System.out.println(cur + ": this is tmp");
			if(cur.containsKey(currentScope)){
				return false;
			}else{
				cur.put(currentScope, dec);
				table.put(identifier, cur);
				return true;
			}
		}		
	}
	

}

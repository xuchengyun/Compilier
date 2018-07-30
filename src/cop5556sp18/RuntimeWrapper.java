package cop5556sp18;

public class RuntimeWrapper {
	public final static String className = "cop5556sp18/RuntimeWrapper";

	public static String integerabsSig = "(I)I";
	public static int integerabs(int arg0) {
		return (int)Math.abs(arg0);
	}
	
	public static String floatabsSig = "(F)F";
	public static float floatabs(float arg0) {
		return (float)Math.abs(arg0);
	}
	
	public static final String integerlogSig = "(I)I";
	public static int integerlog(int arg0) {
		double l =  Math.log(arg0);
		return (int) l;
	}
	
	public static final String floatlogSig = "(F)F";
	public static float floatlog(float arg0) {
		double l =  Math.log(arg0);
		return (float) l;
	}
	
	public static final String integeratanSig = "(I)I";
	public static int integeratan(int arg0) {
		double l =  Math.atan(arg0);
		return (int) l;
	}
	
	public static final String floatatanSig = "(F)F";
	public static float floatatan(float arg0) {
		double l =  Math.atan(arg0);
		return (float) l;
	}
	
	public static final String integercosSig = "(I)I";
	public static int integercos(int arg0) {
		double l =  Math.cos(arg0);
		return (int) l;
	}
	
	public static final String floatcosSig = "(F)F";
	public static float floatcos(float arg0) {
		double l =  Math.cos(arg0);
		return (float) l;
	}
	
	public static final String integersinSig = "(I)I";
	public static int integersin(int arg0) {
		double l =  Math.sin(arg0);
		return (int)l;
	}
	
	public static final String floatsinSig = "(F)F";
	public static float floatsin(float arg0) {
		double l =  Math.sin(arg0);
		return (float) l;
	}
	
}

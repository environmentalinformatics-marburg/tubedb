package tsdb.dsl.printformula;

public class PrintFormulaAddOp {
	
	public final boolean positive;
	public final PrintFormula a;
	
	public PrintFormulaAddOp(boolean positive, PrintFormula a) {
		this.positive = positive;
		this.a = a;
	}
	
	public static PrintFormulaAddOp[] concat(PrintFormulaAddOp a, PrintFormulaAddOp b) {
		return new PrintFormulaAddOp[] {a, b};
	}
	
	public static PrintFormulaAddOp[] concat(PrintFormulaAddOp a, PrintFormulaAddOp[] bs) {
		PrintFormulaAddOp[] rs = new PrintFormulaAddOp[1 + bs.length];
		rs[0] = a;
		for (int i = 0; i < bs.length; i++) {
			rs[1 + i] = bs[i];
		}
		return rs;
	}
	
	public static PrintFormulaAddOp[] concat(PrintFormulaAddOp[] as, PrintFormulaAddOp b) {
		PrintFormulaAddOp[] rs = new PrintFormulaAddOp[as.length + 1];
		for (int i = 0; i < as.length; i++) {
			rs[i] = as[i];
		}
		rs[as.length] = b;
		return rs;
	}	

	public static PrintFormulaAddOp[] concat(PrintFormulaAddOp[] as, PrintFormulaAddOp[] bs) {
		PrintFormulaAddOp[] rs = new PrintFormulaAddOp[as.length + bs.length];
		for (int i = 0; i < as.length; i++) {
			rs[i] = as[i];
		}
		for (int i = 0; i < bs.length; i++) {
			rs[as.length + i] = bs[i];
		}
		return rs;
	}
	
	public static PrintFormulaAddOp[] negate(PrintFormulaAddOp[] as) {
		PrintFormulaAddOp[] rs = new PrintFormulaAddOp[as.length];
		for (int i = 0; i < as.length; i++) {
			rs[i] = new PrintFormulaAddOp(!as[i].positive, as[i].a);
		}
		return rs;
	}
	
	public static int getDepth(PrintFormulaAddOp[] as) {
		int depth = 0;
		for(PrintFormulaAddOp a:as) {
			if(a.a.depth > depth) {
				depth = a.a.depth;
			}
		}
		return depth;
	}
}

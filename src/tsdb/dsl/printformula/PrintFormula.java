package tsdb.dsl.printformula;

public abstract class PrintFormula {
	
	public final int depth;
	
	public PrintFormula(int depth) {
		this.depth = depth;
	}
	
	public abstract <T> T accept(PrintFormulaVisitor<T> visitor);
	
	public static PrintFormulaAddOp[] negate(PrintFormulaAddOp[] as) {
		PrintFormulaAddOp[] rs = new PrintFormulaAddOp[as.length];
		for (int i = 0; i < as.length; i++) {
			rs[i] = new PrintFormulaAddOp(!as[i].positive, as[i].a);
		}
		return rs;
	}

	public static PrintFormula[] concat(PrintFormula a, PrintFormula b) {
		return new PrintFormula[] {a, b};
	}
	
	public static PrintFormula[] concat(PrintFormula a, PrintFormula[] bs) {
		PrintFormula[] rs = new PrintFormula[1 + bs.length];
		rs[0] = a;
		for (int i = 0; i < bs.length; i++) {
			rs[1 + i] = bs[i];
		}
		return rs;
	}
	
	public static PrintFormula[] concat(PrintFormula[] as, PrintFormula b) {
		PrintFormula[] rs = new PrintFormula[as.length + 1];
		for (int i = 0; i < as.length; i++) {
			rs[i] = as[i];
		}
		rs[as.length] = b;
		return rs;
	}	

	public static PrintFormula[] concat(PrintFormula[] as, PrintFormula[] bs) {
		PrintFormula[] rs = new PrintFormula[as.length + bs.length];
		for (int i = 0; i < as.length; i++) {
			rs[i] = as[i];
		}
		for (int i = 0; i < bs.length; i++) {
			rs[as.length + i] = bs[i];
		}
		return rs;
	}

	public static int getDepth(PrintFormula a, PrintFormula b) {
		return a.depth >= b.depth ? a.depth : b.depth;
	}
	
	public static int getDepth(PrintFormula[] as) {
		int depth = 0;
		for(PrintFormula a:as) {
			if(a.depth > depth) {
				depth = a.depth;
			}
		}
		return depth;
	}
}

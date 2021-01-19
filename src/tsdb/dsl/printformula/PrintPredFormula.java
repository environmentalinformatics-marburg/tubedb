package tsdb.dsl.printformula;

public abstract class PrintPredFormula {
	
	public final int depth;
	
	public PrintPredFormula(int depth) {
		this.depth = depth;
	}
	
	public abstract <T> T accept(PrintFormulaVisitor<T> visitor);
	
	public static PrintPredFormula[] concat(PrintPredFormula a, PrintPredFormula b) {
		return new PrintPredFormula[] {a, b};
	}
	
	public static PrintPredFormula[] concat(PrintPredFormula a, PrintPredFormula[] bs) {
		PrintPredFormula[] rs = new PrintPredFormula[1 + bs.length];
		rs[0] = a;
		for (int i = 0; i < bs.length; i++) {
			rs[1 + i] = bs[i];
		}
		return rs;
	}
	
	public static PrintPredFormula[] concat(PrintPredFormula[] as, PrintPredFormula b) {
		PrintPredFormula[] rs = new PrintPredFormula[as.length + 1];
		for (int i = 0; i < as.length; i++) {
			rs[i] = as[i];
		}
		rs[as.length] = b;
		return rs;
	}	

	public static PrintPredFormula[] concat(PrintPredFormula[] as, PrintPredFormula[] bs) {
		PrintPredFormula[] rs = new PrintPredFormula[as.length + bs.length];
		for (int i = 0; i < as.length; i++) {
			rs[i] = as[i];
		}
		for (int i = 0; i < bs.length; i++) {
			rs[as.length + i] = bs[i];
		}
		return rs;
	}
	
	public static int getDepth(PrintPredFormula a, PrintPredFormula b) {
		return a.depth >= b.depth ? a.depth : b.depth;
	}
	
	public static int getDepth(PrintPredFormula[] as) {
		int depth = 0;
		for(PrintPredFormula a:as) {
			if(a.depth > depth) {
				depth = a.depth;
			}
		}
		return depth;
	}
}

package tsdb.dsl;

import tsdb.dsl.computation.BooleanComputation;
import tsdb.dsl.computation.BooleanComputationAnd;
import tsdb.dsl.computation.BooleanComputationEqual;
import tsdb.dsl.computation.BooleanComputationEqualNum;
import tsdb.dsl.computation.BooleanComputationFalse;
import tsdb.dsl.computation.BooleanComputationLess;
import tsdb.dsl.computation.BooleanComputationLessEqual;
import tsdb.dsl.computation.BooleanComputationLessEqualNum1;
import tsdb.dsl.computation.BooleanComputationLessEqualNum2;
import tsdb.dsl.computation.BooleanComputationLessNum1;
import tsdb.dsl.computation.BooleanComputationLessNum2;
import tsdb.dsl.computation.BooleanComputationNotEqual;
import tsdb.dsl.computation.BooleanComputationNotEqualNum;
import tsdb.dsl.computation.BooleanComputationOr;
import tsdb.dsl.computation.BooleanComputationTrue;
import tsdb.dsl.computation.Computation;
import tsdb.dsl.computation.ComputationAdd;
import tsdb.dsl.computation.ComputationAddNum;
import tsdb.dsl.computation.ComputationConditional;
import tsdb.dsl.computation.ComputationCumsumByYear;
import tsdb.dsl.computation.ComputationCumsumByYearNeg;
import tsdb.dsl.computation.ComputationDiv;
import tsdb.dsl.computation.ComputationDivNum1;
import tsdb.dsl.computation.ComputationDivNum2;
import tsdb.dsl.computation.ComputationExp;
import tsdb.dsl.computation.ComputationExpNeg;
import tsdb.dsl.computation.ComputationLn;
import tsdb.dsl.computation.ComputationLnNeg;
import tsdb.dsl.computation.ComputationMul;
import tsdb.dsl.computation.ComputationMulNum;
import tsdb.dsl.computation.ComputationNum;
import tsdb.dsl.computation.ComputationPow;
import tsdb.dsl.computation.ComputationSub;
import tsdb.dsl.computation.ComputationSubNum;
import tsdb.dsl.computation.ComputationVar;
import tsdb.dsl.computation.ComputationVarNeg;
import tsdb.dsl.computation.ComputationVars;
import tsdb.dsl.computation.ComputationVarsNeg;

public class FormulaCompileVisitor implements FormulaVisitor1<Computation>, BooleanFormulaVisitor1<BooleanComputation> {

	private final Environment env;

	public FormulaCompileVisitor(Environment env) {
		this.env = env;
	}

	@Override
	public BooleanComputation visitAND(BooleanFormulaAND booleanFormulaAND) {
		return new BooleanComputationAnd(booleanFormulaAND.a.accept(this), booleanFormulaAND.b.accept(this));
	}

	@Override
	public BooleanComputation visitOR(BooleanFormulaOR booleanFormulaOR) {
		return new BooleanComputationOr(booleanFormulaOR.a.accept(this), booleanFormulaOR.b.accept(this));
	}

	@Override
	public BooleanComputation visitEqual(BooleanFormulaEqual booleanFormulaEqual) {
		if(booleanFormulaEqual.a instanceof FormulaNum) {
			return new BooleanComputationEqualNum(booleanFormulaEqual.b.accept(this), ((FormulaNum)booleanFormulaEqual.a).value );
		}
		if(booleanFormulaEqual.b instanceof FormulaNum) {
			return new BooleanComputationEqualNum(booleanFormulaEqual.a.accept(this), ((FormulaNum)booleanFormulaEqual.b).value );
		}
		return new BooleanComputationEqual(booleanFormulaEqual.a.accept(this), booleanFormulaEqual.b.accept(this));
	}

	@Override
	public BooleanComputation visitLess(BooleanFormulaLess booleanFormulaLess) {
		if(booleanFormulaLess.a instanceof FormulaNum) {
			return new BooleanComputationLessNum1(((FormulaNum)booleanFormulaLess.a).value, booleanFormulaLess.b.accept(this) );
		}
		if(booleanFormulaLess.b instanceof FormulaNum) {
			return new BooleanComputationLessNum2(booleanFormulaLess.a.accept(this), ((FormulaNum)booleanFormulaLess.b).value );
		}
		return new BooleanComputationLess(booleanFormulaLess.a.accept(this), booleanFormulaLess.b.accept(this));
	}

	@Override
	public BooleanComputation visitLessEqual(BooleanFormulaLessEqual booleanFormulaLessEqual) {
		if(booleanFormulaLessEqual.a instanceof FormulaNum) {
			return new BooleanComputationLessEqualNum1(((FormulaNum)booleanFormulaLessEqual.a).value, booleanFormulaLessEqual.b.accept(this) );
		}
		if(booleanFormulaLessEqual.b instanceof FormulaNum) {
			return new BooleanComputationLessEqualNum2(booleanFormulaLessEqual.a.accept(this), ((FormulaNum)booleanFormulaLessEqual.b).value );
		}
		return new BooleanComputationLessEqual(booleanFormulaLessEqual.a.accept(this), booleanFormulaLessEqual.b.accept(this));
	}

	@Override
	public BooleanComputation visitNotEqual(BooleanFormulaNotEqual booleanFormulaNotEqual) {
		if(booleanFormulaNotEqual.a instanceof FormulaNum) {
			return new BooleanComputationNotEqualNum(booleanFormulaNotEqual.b.accept(this), ((FormulaNum)booleanFormulaNotEqual.a).value );
		}
		if(booleanFormulaNotEqual.b instanceof FormulaNum) {
			return new BooleanComputationNotEqualNum(booleanFormulaNotEqual.a.accept(this), ((FormulaNum)booleanFormulaNotEqual.b).value );
		}		
		return new BooleanComputationNotEqual(booleanFormulaNotEqual.a.accept(this), booleanFormulaNotEqual.b.accept(this));
	}

	@Override
	public BooleanComputation visitTRUE(BooleanFormula booleanFormula) {
		return BooleanComputationTrue.DEFAULT;
	}

	@Override
	public BooleanComputation visitFALSE(BooleanFormula booleanFormula) {
		return BooleanComputationFalse.DEFAULT;
	}

	@Override
	public Computation visitAdd(FormulaAdd formulaAdd) {
		if(formulaAdd.a instanceof FormulaNum) {
			return new ComputationAddNum(formulaAdd.b.accept(this), ((FormulaNum)formulaAdd.a).value );
		}
		if(formulaAdd.b instanceof FormulaNum) {
			return new ComputationAddNum(formulaAdd.a.accept(this), ((FormulaNum)formulaAdd.b).value );
		}
		return new ComputationAdd(formulaAdd.a.accept(this), formulaAdd.b.accept(this));
	}

	@Override
	public Computation visitSub(FormulaSub formulaSub) {
		if(formulaSub.a instanceof FormulaNum) {
			return new ComputationSubNum(((FormulaNum)formulaSub.a).value, formulaSub.b.accept(this) );
		}
		if(formulaSub.b instanceof FormulaNum) {
			return new ComputationAddNum(formulaSub.a.accept(this), ((FormulaNum)formulaSub.b.negative()).value );
		}
		return new ComputationSub(formulaSub.a.accept(this), formulaSub.b.accept(this));
	}

	@Override
	public Computation visitMul(FormulaMul formulaMul) {
		if(formulaMul.a instanceof FormulaNum) {
			return new ComputationMulNum(formulaMul.b.accept(this), ((FormulaNum)formulaMul.a).value );
		}
		if(formulaMul.b instanceof FormulaNum) {
			return new ComputationMulNum(formulaMul.a.accept(this), ((FormulaNum)formulaMul.b).value );
		}
		return new ComputationMul(formulaMul.a.accept(this), formulaMul.b.accept(this));
	}

	@Override
	public Computation visitDiv(FormulaDiv formulaDiv) {
		if(formulaDiv.a instanceof FormulaNum) {
			return new ComputationDivNum1(((FormulaNum)formulaDiv.a).value, formulaDiv.b.accept(this) );
		}
		if(formulaDiv.b instanceof FormulaNum) {
			return new ComputationDivNum2(formulaDiv.a.accept(this), ((FormulaNum)formulaDiv.b).value );
		}
		return new ComputationDiv(formulaDiv.a.accept(this), formulaDiv.b.accept(this));
	}

	@Override
	public Computation visitPow(FormulaPow formulaPow) {
		return new ComputationPow(formulaPow.a.accept(this), formulaPow.b.accept(this));
	}

	@Override
	public Computation visitFunc(FormulaFunc formulaFunc) {
		Computation parameter = formulaFunc.parameter.accept(this);
		switch(formulaFunc.name) {
		case "exp":
			return formulaFunc.positive ? new ComputationExp(parameter) : new ComputationExpNeg(parameter);
		case "ln":
			return formulaFunc.positive ? new ComputationLn(parameter) : new ComputationLnNeg(parameter);
		case "cumsum_by_year":
			return formulaFunc.positive ? new ComputationCumsumByYear(parameter) : new ComputationCumsumByYearNeg(parameter);		
		default:
			throw new RuntimeException("function not found: "+formulaFunc.name);
		}		
	}

	@Override
	public Computation visitConditional(FormulaConditional formulaConditional) {
		return new ComputationConditional(formulaConditional.p.accept(this), formulaConditional.a.accept(this), formulaConditional.b.accept(this));
	}

	@Override
	public Computation visitVar(FormulaVar formulaVar) {		
		Computation computationOfTime = ComputationOfTime.compileVar(formulaVar.name, formulaVar.positive);
		if(computationOfTime != null) {
			return computationOfTime;
		}

		if(!env.containsSensor(formulaVar.name)) {
			throw new RuntimeException("sensor not found: "+formulaVar.name+"  in  "+env.sensorMap);
		}
		int pos = env.getSensorIndex(formulaVar.name);
		switch(pos) {
		case 0:			
			return formulaVar.positive ? ComputationVars.VAR0 : ComputationVarsNeg.VAR0;
		case 1:
			return formulaVar.positive ? ComputationVars.VAR1 : ComputationVarsNeg.VAR1;
		case 2:
			return formulaVar.positive ? ComputationVars.VAR2 : ComputationVarsNeg.VAR2;
		case 3:
			return formulaVar.positive ? ComputationVars.VAR3 : ComputationVarsNeg.VAR3;
		case 4:
			return formulaVar.positive ? ComputationVars.VAR4 : ComputationVarsNeg.VAR4;
		default:
			return formulaVar.positive ? new ComputationVar(pos) : new ComputationVarNeg(pos);
		}
	}

	@Override
	public Computation visitNum(FormulaNum formulaNum) {
		return new ComputationNum(formulaNum.value);
	}
}

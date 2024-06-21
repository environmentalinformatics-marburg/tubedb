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
import tsdb.dsl.computation.BooleanComputationLessEqualNumVar;
import tsdb.dsl.computation.BooleanComputationLessEqualVarNum;
import tsdb.dsl.computation.BooleanComputationLessNum1;
import tsdb.dsl.computation.BooleanComputationLessNum2;
import tsdb.dsl.computation.BooleanComputationLessNumVar;
import tsdb.dsl.computation.BooleanComputationLessVarNum;
import tsdb.dsl.computation.BooleanComputationNotEqual;
import tsdb.dsl.computation.BooleanComputationNotEqualNum;
import tsdb.dsl.computation.BooleanComputationOr;
import tsdb.dsl.computation.BooleanComputationTrue;
import tsdb.dsl.computation.ComputationAdd;
import tsdb.dsl.computation.ComputationAddNum;
import tsdb.dsl.computation.ComputationAddVar;
import tsdb.dsl.computation.ComputationAddVarNum;
import tsdb.dsl.computation.ComputationAddVarVar;
import tsdb.dsl.computation.ComputationArctan;
import tsdb.dsl.computation.ComputationArctanDeg;
import tsdb.dsl.computation.ComputationArctanDegNeg;
import tsdb.dsl.computation.ComputationArctanNeg;
import tsdb.dsl.computation.ComputationCbrt;
import tsdb.dsl.computation.ComputationCbrtVar;
import tsdb.dsl.computation.ComputationConditional;
import tsdb.dsl.computation.ComputationConditionalOneZero;
import tsdb.dsl.computation.ComputationCube;
import tsdb.dsl.computation.ComputationCubeVar;
import tsdb.dsl.computation.ComputationCumsumByYear;
import tsdb.dsl.computation.ComputationCumsumByYearNeg;
import tsdb.dsl.computation.ComputationDiv;
import tsdb.dsl.computation.ComputationDivNum1;
import tsdb.dsl.computation.ComputationDivNum2;
import tsdb.dsl.computation.ComputationDivNumVar;
import tsdb.dsl.computation.ComputationDivVar1;
import tsdb.dsl.computation.ComputationDivVar2;
import tsdb.dsl.computation.ComputationDivVarNum;
import tsdb.dsl.computation.ComputationExp;
import tsdb.dsl.computation.ComputationExpNeg;
import tsdb.dsl.computation.ComputationLn;
import tsdb.dsl.computation.ComputationLnNeg;
import tsdb.dsl.computation.ComputationMul;
import tsdb.dsl.computation.ComputationMulCubeNum;
import tsdb.dsl.computation.ComputationMulCubeVar0Num;
import tsdb.dsl.computation.ComputationMulCubeVar1Num;
import tsdb.dsl.computation.ComputationMulCubeVarNum;
import tsdb.dsl.computation.ComputationMulNum;
import tsdb.dsl.computation.ComputationMulPow4Num;
import tsdb.dsl.computation.ComputationMulPow4VarNum;
import tsdb.dsl.computation.ComputationMulPowVarNumNum;
import tsdb.dsl.computation.ComputationMulPowNumNum;
import tsdb.dsl.computation.ComputationMulPowNum_;
import tsdb.dsl.computation.ComputationMulSquareNum;
import tsdb.dsl.computation.ComputationMulSquareVar0Num;
import tsdb.dsl.computation.ComputationMulSquareVar1Num;
import tsdb.dsl.computation.ComputationMulSquareVarNum;
import tsdb.dsl.computation.ComputationMulVar;
import tsdb.dsl.computation.ComputationMulVar0Num;
import tsdb.dsl.computation.ComputationMulVar1Num;
import tsdb.dsl.computation.ComputationMulVarNum;
import tsdb.dsl.computation.ComputationNum;
import tsdb.dsl.computation.ComputationNum0;
import tsdb.dsl.computation.ComputationOfTime;
import tsdb.dsl.computation.ComputationPow;
import tsdb.dsl.computation.ComputationPow4;
import tsdb.dsl.computation.ComputationPow4Var;
import tsdb.dsl.computation.ComputationPow4rt;
import tsdb.dsl.computation.ComputationPow4rtVar;
import tsdb.dsl.computation.ComputationPowNum;
import tsdb.dsl.computation.ComputationRecoveringCumsumByYear;
import tsdb.dsl.computation.ComputationRecoveringCumsumByYearNeg;
import tsdb.dsl.computation.ComputationSqr;
import tsdb.dsl.computation.ComputationSqrNeg;
import tsdb.dsl.computation.ComputationSqrt;
import tsdb.dsl.computation.ComputationSqrtNeg;
import tsdb.dsl.computation.ComputationSqrtVar;
import tsdb.dsl.computation.ComputationSquareVar;
import tsdb.dsl.computation.ComputationSub;
import tsdb.dsl.computation.ComputationSubNum;
import tsdb.dsl.computation.ComputationSubNumVar;
import tsdb.dsl.computation.ComputationSubVar;
import tsdb.dsl.computation.ComputationSubVarVar;
import tsdb.dsl.computation.ComputationVar;
import tsdb.dsl.computation.ComputationVarNeg;
import tsdb.dsl.computation.ComputationVars;
import tsdb.dsl.computation.ComputationVarsNeg;
import tsdb.dsl.formula.BooleanFormula;
import tsdb.dsl.formula.BooleanFormulaAND;
import tsdb.dsl.formula.BooleanFormulaEqual;
import tsdb.dsl.formula.BooleanFormulaLess;
import tsdb.dsl.formula.BooleanFormulaLessEqual;
import tsdb.dsl.formula.BooleanFormulaNotEqual;
import tsdb.dsl.formula.BooleanFormulaOR;
import tsdb.dsl.formula.Formula;
import tsdb.dsl.formula.FormulaAdd;
import tsdb.dsl.formula.FormulaConditional;
import tsdb.dsl.formula.FormulaDiv;
import tsdb.dsl.formula.FormulaFunc;
import tsdb.dsl.formula.FormulaMul;
import tsdb.dsl.formula.FormulaNonDataVar;
import tsdb.dsl.formula.FormulaNum;
import tsdb.dsl.formula.FormulaPow;
import tsdb.dsl.formula.FormulaSub;
import tsdb.dsl.formula.FormulaVar;
import tsdb.util.Computation;

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
			if(booleanFormulaLess.b instanceof FormulaVar) {
				FormulaVar b = (FormulaVar)booleanFormulaLess.b;
				int bPos = env.getSensorIndex(b.name);
				if(b.positive) {
					return new BooleanComputationLessNumVar(((FormulaNum)booleanFormulaLess.a).value, bPos );
				} else {
					return new BooleanComputationLessVarNum(bPos, ((FormulaNum)booleanFormulaLess.a.negative()).value );
				}
			}
			return new BooleanComputationLessNum1(((FormulaNum)booleanFormulaLess.a).value, booleanFormulaLess.b.accept(this) );
		}
		if(booleanFormulaLess.b instanceof FormulaNum) {
			if(booleanFormulaLess.a instanceof FormulaVar) {
				FormulaVar a = (FormulaVar)booleanFormulaLess.a;
				int aPos = env.getSensorIndex(a.name);
				if(a.positive) {
					return new BooleanComputationLessVarNum(aPos, ((FormulaNum)booleanFormulaLess.b).value );
				} else {
					return new BooleanComputationLessNumVar(((FormulaNum)booleanFormulaLess.b.negative()).value, aPos );
				}
			}
			return new BooleanComputationLessNum2(booleanFormulaLess.a.accept(this), ((FormulaNum)booleanFormulaLess.b).value );
		}
		return new BooleanComputationLess(booleanFormulaLess.a.accept(this), booleanFormulaLess.b.accept(this));
	}

	@Override
	public BooleanComputation visitLessEqual(BooleanFormulaLessEqual booleanFormulaLessEqual) {
		if(booleanFormulaLessEqual.a instanceof FormulaNum) {
			FormulaNum a = (FormulaNum) booleanFormulaLessEqual.a;
			if(booleanFormulaLessEqual.b instanceof FormulaVar) {
				FormulaVar b = (FormulaVar) booleanFormulaLessEqual.b;
				int bPos = env.getSensorIndex(b.name);
				if(b.positive) {
					return new BooleanComputationLessEqualNumVar(a.value, bPos ); 
				} else {
					return new BooleanComputationLessEqualVarNum(bPos, a.negative().value );
				}
			}
			return new BooleanComputationLessEqualNum1(a.value, booleanFormulaLessEqual.b.accept(this) );
		}
		if(booleanFormulaLessEqual.b instanceof FormulaNum) {
			FormulaNum b = (FormulaNum) booleanFormulaLessEqual.b;
			if(booleanFormulaLessEqual.a instanceof FormulaVar) {
				FormulaVar a = (FormulaVar) booleanFormulaLessEqual.a;
				int aPos = env.getSensorIndex(a.name);
				if(a.positive) {
					return new BooleanComputationLessEqualVarNum(aPos, b.value ); 
				} else {
					return new BooleanComputationLessEqualNumVar(b.negative().value, aPos );
				}
			}
			return new BooleanComputationLessEqualNum2(booleanFormulaLessEqual.a.accept(this), b.value );
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
			FormulaNum a = (FormulaNum)formulaAdd.a;
			if(formulaAdd.b instanceof FormulaVar) {
				FormulaVar b = (FormulaVar) formulaAdd.b;
				int bPos = env.getSensorIndex(b.name);
				if(b.positive) {
					return new ComputationAddVarNum(bPos, a.value );
				} else {
					return new ComputationSubNumVar(a.value, bPos );
				}
			}
			return new ComputationAddNum(formulaAdd.b.accept(this), a.value );
		}
		if(formulaAdd.b instanceof FormulaNum) {
			FormulaNum b = (FormulaNum)formulaAdd.b;
			if(formulaAdd.a instanceof FormulaVar) {
				FormulaVar a = (FormulaVar) formulaAdd.a;
				int aPos = env.getSensorIndex(a.name);
				if(a.positive) {
					return new ComputationAddVarNum(aPos, b.value );
				} else {
					return new ComputationSubNumVar(b.value, aPos );
				}
			}
			return new ComputationAddNum(formulaAdd.a.accept(this), b.value );
		}
		if(formulaAdd.a instanceof FormulaVar && formulaAdd.b instanceof FormulaVar) {
			FormulaVar a = (FormulaVar) formulaAdd.a;
			FormulaVar b = (FormulaVar) formulaAdd.b;
			int aPos = env.getSensorIndex(a.name);
			int bPos = env.getSensorIndex(b.name);
			if(a.positive) {
				if(b.positive) {
					return new ComputationAddVarVar(aPos, bPos );
				} else {
					return new ComputationSubVarVar(aPos, bPos );
				}
			} else {
				if(b.positive) {
					return new ComputationSubVarVar(bPos, aPos );
				} else {
					// TODO
				}
			}
		}
		if(formulaAdd.a instanceof FormulaVar) {
			FormulaVar a = (FormulaVar) formulaAdd.a;
			int aPos = env.getSensorIndex(a.name);
			if(a.positive) {
				return new ComputationAddVar(formulaAdd.b.accept(this), aPos );
			} else {
				return new ComputationSubVar(formulaAdd.b.accept(this), aPos );
			}
		}
		if(formulaAdd.b instanceof FormulaVar) {
			FormulaVar b = (FormulaVar) formulaAdd.b;
			int bPos = env.getSensorIndex(b.name);
			if(b.positive) {
				return new ComputationAddVar(formulaAdd.a.accept(this), bPos );
			} else {
				return new ComputationSubVar(formulaAdd.a.accept(this), bPos );
			}
		}		
		return new ComputationAdd(formulaAdd.a.accept(this), formulaAdd.b.accept(this));
	}

	@Override
	public Computation visitSub(FormulaSub formulaSub) {
		if(formulaSub.a instanceof FormulaNum) {
			FormulaNum a = (FormulaNum) formulaSub.a;
			if(formulaSub.b instanceof FormulaVar) {
				FormulaVar b = (FormulaVar) formulaSub.b;
				int bPos = env.getSensorIndex(b.name);
				if(b.positive) {
					return new ComputationSubNumVar(a.value, bPos );
				} else {
					return new ComputationAddVarNum(bPos, a.value );
				}
			}
			return new ComputationSubNum(a.value, formulaSub.b.accept(this) );
		}
		if(formulaSub.b instanceof FormulaNum) {
			FormulaNum b = (FormulaNum) formulaSub.b;
			if(formulaSub.a instanceof FormulaVar) {
				FormulaVar a = (FormulaVar) formulaSub.a;
				int aPos = env.getSensorIndex(a.name);
				if(a.positive) {
					return new ComputationAddVarNum(aPos, b.negative().value );
				} else {
					return new ComputationSubNumVar(b.negative().value, aPos );
				}
			}
			return new ComputationAddNum(formulaSub.a.accept(this), b.negative().value );
		}
		if(formulaSub.a instanceof FormulaVar && formulaSub.b instanceof FormulaVar) {
			FormulaVar a = (FormulaVar) formulaSub.a;
			FormulaVar b = (FormulaVar) formulaSub.b;
			int aPos = env.getSensorIndex(a.name);
			int bPos = env.getSensorIndex(b.name);
			if(a.positive) {
				if(b.positive) {
					return new ComputationSubVarVar(aPos, bPos );
				} else {
					return new ComputationAddVarVar(aPos, bPos );
				}
			} else {
				if(b.positive) {
					// TODO
				} else {
					return new ComputationSubVarVar(bPos, aPos );
				}
			}
		}
		if(formulaSub.a instanceof FormulaVar) {
			FormulaVar a = (FormulaVar) formulaSub.a;
			int aPos = env.getSensorIndex(a.name);
			if(a.positive) {
				// TODO
			} else {
				// TODO
			}
		}
		if(formulaSub.b instanceof FormulaVar) {
			FormulaVar b = (FormulaVar) formulaSub.b;
			int bPos = env.getSensorIndex(b.name);
			if(b.positive) {
				return new ComputationSubVar(formulaSub.a.accept(this), bPos );
			} else {
				return new ComputationAddVar(formulaSub.a.accept(this), bPos );
			}
		}
		return new ComputationSub(formulaSub.a.accept(this), formulaSub.b.accept(this));
	}

	@Override
	public Computation visitMul(FormulaMul formulaMul) {
		if(formulaMul.a instanceof FormulaNum) {
			FormulaNum a = (FormulaNum) formulaMul.a;
			if(formulaMul.b instanceof FormulaVar) {
				FormulaVar b = (FormulaVar) formulaMul.b;
				int bPos = env.getSensorIndex(b.name);
				if(b.positive) {
					if(bPos == 0) {
						return new ComputationMulVar0Num(a.value);
					}
					if(bPos == 1) {
						return new ComputationMulVar1Num(a.value);
					}
					return new ComputationMulVarNum(bPos, a.value);
				} else {
					if(bPos == 0) {
						return new ComputationMulVar0Num(a.negative().value);
					}
					if(bPos == 1) {
						return new ComputationMulVar1Num(a.negative().value);
					}
					return new ComputationMulVarNum(bPos, a.negative().value);
				}
			}
			if(formulaMul.b instanceof FormulaPow) {
				FormulaPow b = (FormulaPow) formulaMul.b;
				if(b.b instanceof FormulaNum) {
					FormulaNum exponent = (FormulaNum) b.b;					
					if(b.a instanceof FormulaVar) {
						FormulaVar var = (FormulaVar) b.a;
						int varPos = env.getSensorIndex(var.name);
						if(var.positive) {
							if(exponent == FormulaNum.TWO) {
								if(varPos == 0) {
									return new ComputationMulSquareVar0Num(a.value);
								}
								if(varPos == 1) {
									return new ComputationMulSquareVar1Num(a.value);
								}
								return new ComputationMulSquareVarNum(varPos, a.value);
							}
							if(exponent == FormulaNum.THREE) {
								if(varPos == 0) {
									return new ComputationMulCubeVar0Num(a.value);
								}
								if(varPos == 1) {
									return new ComputationMulCubeVar1Num(a.value);
								}
								return new ComputationMulCubeVarNum(varPos, a.value);
							}
							if(exponent == FormulaNum.FOUR) {
								return new ComputationMulPow4VarNum(varPos, a.value);
							}
							return new ComputationMulPowVarNumNum(varPos, exponent.value, a.value);
						}						
					}					
					if(exponent == FormulaNum.TWO) {
						return new ComputationMulSquareNum(b.a.accept(this), a.value);
					}
					if(exponent == FormulaNum.THREE) {
						return new ComputationMulCubeNum(b.a.accept(this), a.value);
					}
					if(exponent == FormulaNum.FOUR) {
						return new ComputationMulPow4Num(b.a.accept(this), a.value);
					}
					return new ComputationMulPowNumNum(b.a.accept(this), exponent.value, a.value);
				}
				return new ComputationMulPowNum_(b.a.accept(this), b.b.accept(this), a.value);
			}
			return new ComputationMulNum(formulaMul.b.accept(this), a.value);
		}
		if(formulaMul.b instanceof FormulaNum) {
			FormulaNum b = (FormulaNum) formulaMul.b;
			if(formulaMul.a instanceof FormulaVar) {
				FormulaVar a = (FormulaVar) formulaMul.a;
				int aPos = env.getSensorIndex(a.name);
				if(a.positive) {
					return new ComputationMulVarNum(aPos, b.value );
				} else {
					return new ComputationMulVarNum(aPos, b.negative().value );
				}
			}
			return new ComputationMulNum(formulaMul.a.accept(this), b.value );
		}
		if(formulaMul.a instanceof FormulaVar) {
			FormulaVar a = (FormulaVar) formulaMul.a;
			int aPos = env.getSensorIndex(a.name);
			return new ComputationMulVar(aPos, formulaMul.b.accept(this));
		}
		if(formulaMul.b instanceof FormulaVar) {
			FormulaVar b = (FormulaVar) formulaMul.b;
			int bPos = env.getSensorIndex(b.name);
			return new ComputationMulVar(bPos, formulaMul.a.accept(this));
		}
		return new ComputationMul(formulaMul.a.accept(this), formulaMul.b.accept(this));
	}

	@Override
	public Computation visitDiv(FormulaDiv formulaDiv) {
		if(formulaDiv.a instanceof FormulaNum) {
			FormulaNum a = (FormulaNum) formulaDiv.a;
			if(formulaDiv.b instanceof FormulaVar) {
				FormulaVar b = (FormulaVar) formulaDiv.b;
				int bPos = env.getSensorIndex(b.name);
				if(b.positive) {
					return new ComputationDivNumVar(a.value, bPos );
				} else {
					return new ComputationDivNumVar(a.negative().value, bPos );
				}
			}
			return new ComputationDivNum1(a.value, formulaDiv.b.accept(this) );
		}
		if(formulaDiv.b instanceof FormulaNum) {
			FormulaNum b = (FormulaNum) formulaDiv.b;
			if(formulaDiv.a instanceof FormulaVar) {
				FormulaVar a = (FormulaVar) formulaDiv.a;
				int aPos = env.getSensorIndex(a.name);
				if(a.positive) {
					return new ComputationDivVarNum(aPos, b.value );
				} else {
					return new ComputationDivVarNum(aPos, b.negative().value );
				}
			}
			return new ComputationDivNum2(formulaDiv.a.accept(this), b.value );
		}
		if(formulaDiv.a instanceof FormulaVar) {
			FormulaVar a = (FormulaVar) formulaDiv.a;
			int aPos = env.getSensorIndex(a.name);
			if(a.positive) {
				return new ComputationDivVar1(aPos, formulaDiv.b.accept(this));
			} else {
				// TODO
			}
		}
		if(formulaDiv.b instanceof FormulaVar) {
			FormulaVar b = (FormulaVar) formulaDiv.b;
			int bPos = env.getSensorIndex(b.name);
			if(b.positive) {
				return new ComputationDivVar2(formulaDiv.a.accept(this), bPos);
			} else {
				// TODO
			}
		}
		return new ComputationDiv(formulaDiv.a.accept(this), formulaDiv.b.accept(this));
	}

	@Override
	public Computation visitPow(FormulaPow formulaPow) {
		if(formulaPow.b instanceof FormulaNum) {
			FormulaNum b = (FormulaNum) formulaPow.b;
			if(b == FormulaNum.TWO) {
				if(formulaPow.a instanceof FormulaVar) {
					int aPos = env.getSensorIndex(((FormulaVar)formulaPow.a).name);
					return new ComputationSquareVar(aPos);
				}
				return new ComputationSqr(formulaPow.a.accept(this));
			}
			if(b == FormulaNum.THREE) {
				if(formulaPow.a instanceof FormulaVar) {
					int aPos = env.getSensorIndex(((FormulaVar)formulaPow.a).name);
					return new ComputationCubeVar(aPos);
				}
				return new ComputationCube(formulaPow.a.accept(this));
			}
			if(b == FormulaNum.FOUR) {
				if(formulaPow.a instanceof FormulaVar) {
					int aPos = env.getSensorIndex(((FormulaVar)formulaPow.a).name);
					return new ComputationPow4Var(aPos);
				}
				return new ComputationPow4(formulaPow.a.accept(this));
			}			
			if(b == FormulaNum.ONE_HALF) {
				if(formulaPow.a instanceof FormulaVar) {
					int aPos = env.getSensorIndex(((FormulaVar)formulaPow.a).name);
					return new ComputationSqrtVar(aPos);
				}
				return new ComputationSqrt(formulaPow.a.accept(this));
			}
			if(b == FormulaNum.ONE_THIRD) {
				if(formulaPow.a instanceof FormulaVar) {
					int aPos = env.getSensorIndex(((FormulaVar)formulaPow.a).name);
					return new ComputationCbrtVar(aPos);
				}
				return new ComputationCbrt(formulaPow.a.accept(this));
			}
			if(b == FormulaNum.ONE_QUARTER) {
				if(formulaPow.a instanceof FormulaVar) {
					int aPos = env.getSensorIndex(((FormulaVar)formulaPow.a).name);
					return new ComputationPow4rtVar(aPos);
				}
				return new ComputationPow4rt(formulaPow.a.accept(this));
			}
			
			
			return new ComputationPowNum(formulaPow.a.accept(this), b.value);
		}
		return new ComputationPow(formulaPow.a.accept(this), formulaPow.b.accept(this));
	}

	@Override
	public Computation visitFunc(FormulaFunc formulaFunc) {
		Computation parameter = formulaFunc.parameter.accept(this);		
		switch(formulaFunc.name) {
		case "arctan":
			return formulaFunc.positive ? new ComputationArctan(parameter) : new ComputationArctanNeg(parameter);	
		case "arctan_deg":
			return formulaFunc.positive ? new ComputationArctanDeg(parameter) : new ComputationArctanDegNeg(parameter);					
		case "exp":
			return formulaFunc.positive ? new ComputationExp(parameter) : new ComputationExpNeg(parameter);
		case "ln":
			return formulaFunc.positive ? new ComputationLn(parameter) : new ComputationLnNeg(parameter);
		case "cumsum_by_year":
			return formulaFunc.positive ? new ComputationCumsumByYear(parameter) : new ComputationCumsumByYearNeg(parameter);
		case "recovering_cumsum_by_year":
			return formulaFunc.positive ? new ComputationRecoveringCumsumByYear(parameter) : new ComputationRecoveringCumsumByYearNeg(parameter);			
		case "sqr":
			return formulaFunc.positive ? new ComputationSqr(parameter) : new ComputationSqrNeg(parameter);
		case "sqrt":
			return formulaFunc.positive ? new ComputationSqrt(parameter) : new ComputationSqrtNeg(parameter);			
		default:
			throw new RuntimeException("function not found: "+formulaFunc.name);
		}		
	}

	@Override
	public Computation visitConditional(FormulaConditional formulaConditional) {
		if(formulaConditional.a == FormulaNum.ONE && formulaConditional.b == FormulaNum.ZERO) {
			return new ComputationConditionalOneZero(formulaConditional.p.accept(this));
		}
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
	public Computation visitNonDataVar(FormulaNonDataVar formulaNoDataVar) {
		Computation computationOfTime = ComputationOfTime.compileVar(formulaNoDataVar.name, formulaNoDataVar.positive);
		if(computationOfTime != null) {
			return computationOfTime;
		}
		throw new RuntimeException("not found: "+formulaNoDataVar.name);
	}

	@Override
	public Computation visitNum(FormulaNum formulaNum) {
		if(formulaNum == FormulaNum.ZERO) {
			return ComputationNum0.DEFUALT;
		}
		return new ComputationNum(formulaNum.value);
	}
}

package zen.lang;

import zen.deps.Constructor;
import zen.deps.Field;
import zen.deps.Var;

public final class ZenFuncType extends ZType {

	@Field public ZType[]		TypeParams;
	@Constructor public ZenFuncType(String ShortName, ZType[] UniqueTypeParams) {
		super(ZTypeFlag.UniqueType, ShortName, ZSystem.TopType);
		if(UniqueTypeParams == null) {
			this.TypeParams = new ZType[1];
			this.TypeParams[0] = ZSystem.VarType;
		}
		else {
			this.TypeParams = UniqueTypeParams;
		}
	}

	@Override public final boolean IsFuncType() {
		return true;
	}

	@Override public final boolean IsCompleteFunc(boolean IgnoreReturn) {
		@Var int i = 0;
		if(IgnoreReturn) {
			i = 1;
		}
		while(i < this.TypeParams.length) {
			if(this.TypeParams[i].IsVarType()) {
				return false;
			}
			i = i + 1;
		}
		return true;
	}

	@Override public boolean HasCallableSignature() {
		return !(this.GetRecvType().IsVarType());
	}

	@Override public String StringfySignature(String FuncName) {
		return ZFunc.StringfySignature(FuncName, this.GetFuncParamSize(), this.GetRecvType());
	}

	@Override public ZType GetBaseType() {
		return ZSystem.FuncType;
	}

	@Override public int GetParamSize() {
		return this.TypeParams.length;
	}

	@Override public ZType GetParamType(int Index) {
		return this.TypeParams[Index];
	}

	public final ZType GetReturnType() {
		return this.TypeParams[0];
	}

	public final int GetFuncParamSize() {
		return this.TypeParams.length - 1;
	}

	public final ZType GetRecvType() {
		if(this.TypeParams.length == 1) {
			return ZSystem.VoidType;
		}
		return this.TypeParams[1];
	}

	public final ZType GetFuncParamType(int Index) {
		return this.TypeParams[Index+1];
	}

	public boolean MatchFunc(ZenFuncType ContextFuncType) {
		@Var int i = 0;
		if(this.TypeParams[0].IsVarType() && !ContextFuncType.TypeParams[0].IsVarType()) {
			i = 1;
		}
		while(i < this.TypeParams.length) {
			@Var ZType ParamType =  ContextFuncType.TypeParams[i];
			if(this.TypeParams[i] != ParamType && !ParamType.IsVarType()) {
				return false;
			}
			i = i + 1;
		}
		return true;
	}


}

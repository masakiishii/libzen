// ***************************************************************************
// Copyright (c) 2013-2014, Konoha project authors. All rights reserved.
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// *  Redistributions of source code must retain the above copyright notice,
//    this list of conditions and the following disclaimer.
// *  Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// **************************************************************************

//ifdef JAVA

package zen.lang;

import java.util.ArrayList;

import zen.ast.ZenApplyNode;
import zen.ast.ZenBooleanNode;
import zen.ast.ZenCastNode;
import zen.ast.ZenErrorNode;
import zen.ast.ZenFloatNode;
import zen.ast.ZenIntNode;
import zen.ast.ZenMethodCallNode;
import zen.ast.ZenNode;
import zen.ast.ZenNullNode;
import zen.ast.ZenStupidCastNode;
import zen.ast.ZenSymbolNode;
import zen.deps.Field;
import zen.deps.LibNative;
import zen.deps.Var;
import zen.parser.ZLogger;
import zen.parser.ZNameSpace;
import zen.parser.ZToken;
import zen.parser.ZUtils;
import zen.parser.ZVisitor;

public abstract class ZenTypeChecker extends ZVisitor {

	protected void println(String string) {
		System.err.println("debug " + string);
	}

	public final static int DefaultTypeCheckPolicy			= 0;
	public final static int NoCheckPolicy                   = 1;
	public final static int EnforceCoercion                 = (1 << 1);
	//	public final static int CastPolicy                      = (1 << 1);
	//	public final static int IgnoreEmptyPolicy               = (1 << 2);
	//	public final static int AllowEmptyPolicy                = (1 << 3);
	//	public final static int AllowVoidPolicy                 = (1 << 4);
	//	public final static int AllowCoercionPolicy             = (1 << 5);
	//	public final static int OnlyConstPolicy                 = (1 << 6);
	//	public final static int NullablePolicy                  = (1 << 8);
	//	public final static int BlockPolicy                     = (1 << 7);

	@Field private ZNameSpace StackedNameSpace;
	@Field private ZType StackedContextType;
	@Field private ZenNode StackedTypedNode;

	@Field public ZLogger Logger;
	@Field private boolean StoppedVisitor;

	@Field public ZFuncContext FuncScope;

	public ZenTypeChecker(ZLogger Logger) {
		this.Logger = Logger;
		this.StackedNameSpace = null;
		this.StackedContextType = null;
		this.StackedTypedNode = null;
		this.StoppedVisitor = false;
		this.FuncScope = new ZFuncContext(null, Logger, null, null);
	}

	@Override public final void EnableVisitor() {
		this.StoppedVisitor = false;
	}

	@Override public final void StopVisitor() {
		this.StoppedVisitor = true;
	}

	@Override public final boolean IsVisitable() {
		return !this.StoppedVisitor;
	}

	public final ZNameSpace GetNameSpace() {
		return this.StackedNameSpace;
	}

	public final ZType GetContextType() {
		return this.StackedContextType;
	}

	public final void TypedNode(ZenNode Node, ZType Type) {
		Node.Type = Type;
		if(this.IsVisitable()) {
			this.StackedTypedNode = Node;
		}
	}

	public final void TypedCastNode(ZenNode Node, ZType ContextType, ZType NodeType) {
		if(NodeType.IsVarType() && !ContextType.IsVarType() && !(Node.ParentNode instanceof ZenCastNode)) {
			this.TypedNode(new ZenCastNode(ContextType, Node), ContextType);
		}
		else {
			this.TypedNode(Node, NodeType);
		}
	}

	public final void TypedNodeIf(ZenNode Node, ZType Type, ZenNode P1) {
		if(P1.Type.IsVarType()) {
			Node.Type = ZSystem.VarType;
		}
		else {
			Node.Type = Type;
		}
		if(this.IsVisitable()) {
			this.StackedTypedNode = Node;
		}
	}

	public final void TypedNodeIf2(ZenNode Node, ZType Type, ZenNode P1, ZenNode P2) {
		if(P1.Type.IsVarType() || P2.Type.IsVarType()) {
			Node.Type = ZSystem.VarType;
		}
		else {
			Node.Type = Type;
		}
		if(this.IsVisitable()) {
			this.StackedTypedNode = Node;
		}
	}

	public final void TypedNodeIf3(ZenNode Node, ZType Type, ZenNode P1, ZenNode P2, ZenNode P3) {
		if(P1.Type.IsVarType() || P2.Type.IsVarType() || P3.Type.IsVarType()) {
			Node.Type = ZSystem.VarType;
		}
		else {
			Node.Type = Type;
		}
		if(this.IsVisitable()) {
			this.StackedTypedNode = Node;
		}
	}

	public final void Todo(ZenNode Node) {
		this.Logger.ReportWarning(Node.SourceToken, "TODO: unimplemented type checker node: " + Node.getClass().getSimpleName());
		Node.Type = ZSystem.VarType;
		if(this.IsVisitable()) {
			this.StackedTypedNode = Node;
		}
	}

	public final void CheckErrorNode(ZenNode Node) {
		if(Node != null && Node.IsErrorNode()) {
			Node.Type = ZSystem.VoidType;
			this.StackedTypedNode = Node;
			this.StopVisitor();
		}
	}

	public final boolean TypeCheckNodeList(ZNameSpace NameSpace, ArrayList<ZenNode> ParamList) {
		if(this.IsVisitable()) {
			@Var boolean AllTyped = true;
			@Var int i = 0;
			while(i < ParamList.size()) {
				ZenNode SubNode = ParamList.get(i);
				SubNode = this.TypeCheck(SubNode, NameSpace, ZSystem.VarType, ZenTypeChecker.DefaultTypeCheckPolicy);
				ParamList.set(i, SubNode);
				if(SubNode.Type.IsVarType()) {
					AllTyped = false;
				}
				i = i + 1;
			}
			return AllTyped;
		}
		return false;
	}

	//	private void DumpFuncList(ArrayList<ZenFunc> FuncList, int StartIdx, int EndIdx) {
	//		@Var int i = StartIdx;
	//		while(i < EndIdx) {
	//			@Var ZenFunc Func = FuncList.get(i);
	//			System.err.println("["+i+"] " + Func);
	//			i = i + 1;
	//		}
	//		System.err.println("" + StartIdx + " ===> " + EndIdx);
	//	}
	//
	//	private boolean IsAcceptFunc(ZenFunc Func, ArrayList<ZenNode> ParamList) {
	//		@Var int i = 0;
	//		while(i < ParamList.size()) {
	//			@Var ZenType FuncType = Func.FuncType.GetFuncParamType(i);
	//			@Var ZenType ParamType = ParamList.get(i).Type;
	//			if(Func.FuncType.GetFuncParamType(i) != ParamList.get(i).Type) {
	//				if(FuncType != ParamType && !FuncType.Accept(ParamType)) {
	//					return false;
	//				}
	//			}
	//			i = i + 1;
	//		}
	//		return true;
	//	}
	//
	//	private void GuessFuncTypeAcceptedParam(ArrayList<ZenFunc> FuncList, ZenApplyNode Node, int BaseSize) {
	//		@Var int i = 0;
	//		while(i < BaseSize) {
	//			@Var ZenFunc Func = FuncList.get(i);
	//			if(this.IsAcceptFunc(Func, Node.ParamList)) {
	//				Node.ResolvedFunc = FuncList.get(0);
	//			}
	//			i = i + 1;
	//		}
	//	}

	protected ZFunc InferFuncType(ZNameSpace NameSpace, String FuncName, ZType FuncType, ZToken SourceToken) {
		if(FuncType.IsCompleteFunc(false)) {
			@Var ZNameSpace RootNameSpace = NameSpace.GetRootNameSpace();
			@Var String Signature = FuncType.StringfySignature(FuncName);
			LibNative.Assert(!RootNameSpace.HasSymbol(Signature));
			@Var ZFunc Func = new ZenSigFunc(0, FuncName, (ZenFuncType)FuncType, SourceToken);
			RootNameSpace.SetSymbol(Signature, Func, null);
			Func.Used();
			return Func;
		}
		return null;
	}

	protected ZType GuessFuncType(ZNameSpace NameSpace, String FuncName, ZenApplyNode Node, ZType ContextFuncType) {
		if(Node.ResolvedFunc == null) {
			@Var int FuncParamSize = Node.ParamList.size();
			@Var ZType RecvType = Node.GetRecvType();
			@Var String Signature = ZFunc.StringfySignature(FuncName, FuncParamSize, RecvType);
			@Var Object Func = NameSpace.GetSymbol(Signature);
			if(Func instanceof ZFunc) {
				Node.ResolvedFunc = ((ZFunc)Func);
				Node.ResolvedFunc.Used();
				return Node.ResolvedFunc.GetFuncType();
			}
			Node.ResolvedFunc = this.InferFuncType(NameSpace, FuncName, ContextFuncType, Node.SourceToken);
		}
		if(Node.ResolvedFunc == null) {
			this.println("unfound function call: " + FuncName + ", " + ContextFuncType);
			return ContextFuncType;
		}
		return Node.ResolvedFunc.FuncType;
	}

	protected ZType GuessMethodFuncType(ZNameSpace NameSpace, String FuncName, ZenMethodCallNode Node, ZType ContextType) {
		if(Node.ResolvedFunc == null) {
			Node.ParamList.add(0, Node.RecvNode);
			this.GuessFuncType(NameSpace, FuncName, Node, ContextType);
			Node.ParamList.remove(0);
		}
		if(Node.ResolvedFunc == null) {
			return ZSystem.VarType; // unspecified
		}
		return Node.ResolvedFunc.FuncType;
	}

	public final ZenNode TypeCheck(ZenNode Node, ZNameSpace NameSpace, ZType ContextType, int TypeCheckPolicy) {
		if(this.IsVisitable()) {
			if(Node.IsUntyped()) {
				ZenNode ParentNode = Node.ParentNode;
				ZNameSpace NameSpace_ = this.StackedNameSpace;
				ZType ContextType_ = this.StackedContextType;
				this.StackedNameSpace = NameSpace;
				this.StackedContextType = ContextType;
				Node.Accept(this);
				Node = this.TypeCheckImpl(this.StackedTypedNode, NameSpace, ContextType, TypeCheckPolicy);
				this.StackedTypedNode = Node;
				this.StackedNameSpace = NameSpace_;
				this.StackedContextType = ContextType_;
				if(ParentNode != Node.ParentNode && ParentNode != null) {
					ParentNode.SetChild(Node);
				}
			}
			else {
				Node = this.TypeCheckImpl(Node, NameSpace, ContextType, TypeCheckPolicy);
				this.StackedTypedNode = Node;
			}
		}
		this.CheckErrorNode(Node);
		return Node;
	}

	private final ZenNode InferType(ZType ContextType, ZenNode Node) {
		if(ContextType.IsInferrableType() && Node.Type instanceof ZenVarType) {
			((ZenVarType)Node.Type).Infer(ContextType, Node.SourceToken);
			Node.Type = ContextType;
			return Node;
		}
		if(ContextType instanceof ZenVarType && !Node.Type.IsVarType()) {
			((ZenVarType)ContextType).Infer(Node.Type, Node.SourceToken);
			return Node;
		}
		return Node;
	}

	private final ZenNode TypeCheckImpl(ZenNode Node, ZNameSpace NameSpace, ZType ContextType, int TypeCheckPolicy) {
		if(Node.IsErrorNode()) {
			return Node;
		}
		if(Node.Type.IsVarType()) {
			this.FuncScope.CountUnknownTypeNode(Node);
			return this.InferType(ContextType, Node);
		}
		if(Node.Type == ContextType || ContextType.IsVarType() || ContextType.Accept(Node.Type) || ZUtils.IsFlag(TypeCheckPolicy, NoCheckPolicy)) {
			return this.InferType(ContextType, Node);
		}
		if(ContextType.IsVoidType() && !Node.Type.IsVoidType()) {
			return new ZenCastNode(ZSystem.VoidType, Node);
		}
		@Var ZFunc CoercionFunc = NameSpace.GetCoercionFunc(Node.Type, ContextType);
		if(CoercionFunc != null) {

		}
		if(ContextType.IsFloatType() && Node.Type.IsIntType()) {
			return this.InferType(ContextType, new ZenCastNode(ContextType, Node));
		}
		if(ZUtils.IsFlag(TypeCheckPolicy, EnforceCoercion) && ContextType.IsStringType()) {
			return this.InferType(ContextType, new ZenCastNode(ContextType, Node));
		}
		//System.err.println("node="+ LibZen.GetClassName(Node) + "type error: requested = " + Type + ", given = " + Node.Type);
		return new ZenStupidCastNode(ContextType, Node);
	}

	protected ZType GetIndexType(ZNameSpace NameSpace, ZType RecvType) {
		if(RecvType.IsArrayType() || RecvType.IsStringType()) {
			return ZSystem.IntType;
		}
		if(RecvType.IsMapType()) {
			return ZSystem.StringType;
		}
		return ZSystem.VarType;
	}

	protected ZType GetElementType(ZNameSpace NameSpace, ZType RecvType) {
		if(RecvType.IsArrayType() || RecvType.IsMapType()) {
			return RecvType.GetParamType(0);
		}
		if(RecvType.IsStringType()) {
			return ZSystem.StringType;
		}
		return ZSystem.VarType;
	}

	private void SetClassField(ZNameSpace NameSpace, ZType ClassType, String FieldName, ZType FieldType, ZToken SourceToken) {
		ZenField Field = new ZenField(FieldName, FieldType, SourceToken);
		NameSpace.GetRootNameSpace().SetSymbol(ZNameSpace.StringfyClassSymbol(ClassType, FieldName), Field, SourceToken);
	}

	protected final ZenField GetField(ZNameSpace NameSpace, ZType ClassType, String FieldName) {
		Object Field = NameSpace.GetRootNameSpace().GetClassSymbol(ClassType, FieldName, true);
		if(Field instanceof ZenField) {
			return (ZenField)Field;
		}
		return null;
	}

	protected final ZType GetFieldType(ZNameSpace NameSpace, ZType ClassType, String FieldName) {
		if(ClassType instanceof ZenClassType) {
			return ((ZenClassType)ClassType).GetFieldType(FieldName, ZSystem.VoidType);
		}
		Object Field = NameSpace.GetRootNameSpace().GetClassSymbol(ClassType, FieldName, true);
		if(Field instanceof ZenField) {
			return ((ZenField)Field).FieldType;
		}
		return NameSpace.Generator.GetFieldType(ClassType, FieldName);
	}

	protected final ZType GetSetterType(ZNameSpace NameSpace, ZType ClassType, String FieldName) {
		if(ClassType instanceof ZenClassType) {
			return ((ZenClassType)ClassType).GetFieldType(FieldName, ZSystem.VoidType);
		}
		Object Field = NameSpace.GetRootNameSpace().GetClassSymbol(ClassType, FieldName, true);
		if(Field instanceof ZenField) {
			return ((ZenField)Field).FieldType;
		}
		return NameSpace.Generator.GetSetterType(ClassType, FieldName);
	}

	protected ZType InferFieldType(ZNameSpace NameSpace, ZType ClassType, String FieldName, ZType InferredType, ZToken SourceToken) {
		this.println("field inferrence " + ClassType + "." + FieldName + ": " + InferredType);
		if(ClassType.IsVarType() || !InferredType.IsInferrableType()) {
			return ZSystem.VarType;
		}
		if(ClassType.IsOpenType()) {
			this.Logger.ReportInfo(SourceToken, "field " + ClassType + "." + FieldName + ": " + InferredType);
			this.SetClassField(NameSpace, ClassType, FieldName, InferredType, SourceToken);
		}
		return InferredType;
	}

	protected ZenNode CreateDefaultValueNode(ZType Type, String FieldName) {
		if(FieldName != null && Type.IsFuncType()) {
			return new ZenSymbolNode(Type, null, FieldName, Type.StringfySignature(FieldName));
		}
		if(Type.IsIntType()) {
			return new ZenIntNode(null, 0);
		}
		else if(Type.IsBooleanType()) {
			return new ZenBooleanNode(null, false);
		}
		else if(Type.IsFloatType()) {
			return new ZenFloatNode(null, 0.0);
		}
		return new ZenNullNode(null);
	}


	protected ZenNode ReadOnlyName(ZenNode Node, ZType ClassType, String VarName) {
		return new ZenErrorNode(Node.SourceToken, "readonly: " + VarName);
	}

	protected ZenNode UndefinedName(ZenNode Node, String Name) {
		return new ZenErrorNode(Node.SourceToken, "undefined name: " + Name);
	}

	protected final ZType NewVarType(ZType VarType, String Name, ZToken SourceToken) {
		if(!(VarType instanceof ZenVarType) && VarType.IsVarType()) {
			int AlphaId = this.FuncScope.VarTypeList.size();
			ZenVarType VarType1 = new ZenVarType(Name, AlphaId, SourceToken);
			this.FuncScope.VarTypeList.add(VarType1);
			VarType = VarType1;
		}
		return VarType;
	}

	protected ZenVariable GetLocalVariable(ZNameSpace NameSpace, String VarName) {
		@Var Object VarInfo = NameSpace.GetSymbol(VarName);
		if(VarInfo instanceof ZenVariable) {
			return (ZenVariable)VarInfo;
		}
		return null;
	}

	protected void SetLocalVariable(ZNameSpace NameSpace, ZType VarType, String VarName, ZToken SourceToken) {
		@Var ZenVariable VarInfo = new ZenVariable(NameSpace.GetDefiningFunc(), 0, VarType, VarName, this.FuncScope.GetVarIndex(), SourceToken);
		NameSpace.SetSymbol(VarName, VarInfo, SourceToken);
	}

}
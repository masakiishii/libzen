// ***************************************************************************
// Copyright (c) 2013, JST/CREST DEOS project authors. All rights reserved.
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

package zen.codegen;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GETSTATIC;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.PUTSTATIC;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_6;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import zen.ast.ZenAndNode;
import zen.ast.ZenApplyNode;
import zen.ast.ZenBooleanNode;
import zen.ast.ZenConstPoolNode;
import zen.ast.ZenErrorNode;
import zen.ast.ZenFloatNode;
import zen.ast.ZenFunctionLiteralNode;
import zen.ast.ZenGetLocalNode;
import zen.ast.ZenGetterNode;
import zen.ast.ZenIfNode;
import zen.ast.ZenIntNode;
import zen.ast.ZenNode;
import zen.ast.ZenNullNode;
import zen.ast.ZenOrNode;
import zen.ast.ZenReturnNode;
import zen.ast.ZenSetLocalNode;
import zen.ast.ZenSetterNode;
import zen.ast.ZenStringNode;
import zen.ast.ZenTrinaryNode;
import zen.ast.ZenVarDeclNode;
import zen.ast2.ZenAllocateNode;
import zen.ast2.ZenApplyOverridedMethodNode;
import zen.ast2.ZenArrayLiteralNode;
import zen.ast2.ZenBreakNode;
import zen.ast2.ZenCatchNode;
import zen.ast2.ZenCommandNode;
import zen.ast2.ZenConstructorNode;
import zen.ast2.ZenContinueNode;
import zen.ast2.ZenDoWhileNode;
import zen.ast2.ZenForEachNode;
import zen.ast2.ZenForNode;
import zen.ast2.ZenGetIndexNode;
import zen.ast2.ZenInstanceOfNode;
import zen.ast2.ZenNewArrayNode;
import zen.ast2.ZenSetIndexNode;
import zen.ast2.ZenSwitchNode;
import zen.ast2.ZenThrowNode;
import zen.ast2.ZenTryNode;
import zen.ast2.ZenWhileNode;
import zen.deps.GreenTeaArray;
import zen.deps.LibZen;
import zen.deps.LibNative;
import zen.parser.ZenFunc;
import zen.parser.ZenGenerator;
import zen.parser.ZenNameSpace;
import zen.parser.ZenStaticTable;
import zen.parser.ZenType;

// GreenTea Generator should be written in each language.

final class SoftwareFaultException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SoftwareFaultException() {
	}
}

class JClassBuilder /*implements Opcodes */{
	final String SourceFile;
	final String ClassName;
	final String SuperClassName;
	final ArrayList<MethodNode> MethodList = new ArrayList<MethodNode>();
	final ArrayList<FieldNode> FieldList = new ArrayList<FieldNode>();
	final int ClassQualifer;

	JClassBuilder(int ClassQualifer, String SourceFile, String ClassName, String SuperClass) {
		this.ClassQualifer = ClassQualifer;
		this.SourceFile = SourceFile;
		this.ClassName = ClassName;
		this.SuperClassName = SuperClass;
	}
	
	void AddMethod(MethodNode m) {
		for(int i=0; i<MethodList.size(); i++) {
			MethodNode node = this.MethodList.get(i);
			if(node.name.equals(m.name) && node.desc.equals(m.desc)) {
				this.MethodList.set(i, m);
				return;
			}
		}
		this.MethodList.add(m);
	}

	void AddField(FieldNode m) {
		this.FieldList.add(m);
	}
	
	byte[] GenerateBytecode() {
		ClassWriter Visitor = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		Visitor.visit(V1_6, this.ClassQualifer, this.ClassName, null, this.SuperClassName, null);
		Visitor.visitSource(this.SourceFile, null);
		for(FieldNode f : this.FieldList) {
			f.accept(Visitor);
		}

		for(MethodNode m : this.MethodList) {
			m.accept(Visitor);
		}
		Visitor.visitEnd();
		return Visitor.toByteArray();
	}

	void OutputClassFile(String className, String dir) {
		byte[] ba = this.GenerateBytecode();
		File file = new File(dir, this.ClassName + ".class");
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(ba);
			fos.close();
		}
		catch(IOException e) {
			LibZen.VerboseException(e);
		} 
	}

}

class GreenTeaClassLoader extends ClassLoader {
	final ZenNameSpace NameSpace;
	final HashMap<String,JClassBuilder> ByteCodeMap;
	final String GlobalStaticClassName;
	final String ContextFieldName;
	final String GontextDescripter;
	
	public GreenTeaClassLoader(ZenNameSpace NameSpace) {
		this.NameSpace = NameSpace;
		this.ByteCodeMap = new HashMap<String,JClassBuilder>();
		
		this.GlobalStaticClassName = "Global$" + 0/*Context.ParserId*/;
		JClassBuilder GlobalClass = new JClassBuilder(ACC_PUBLIC|ACC_FINAL, null, this.GlobalStaticClassName, "java/lang/Object");
		FieldNode fn = new FieldNode(ACC_STATIC, "ParserContext", Type.getDescriptor(ZenNameSpace.class), null, null);
		this.ContextFieldName = fn.name;
		this.GontextDescripter = fn.desc;
		GlobalClass.AddField(fn);
		
		// static init
		MethodNode mn = new MethodNode(ACC_PUBLIC | ACC_STATIC, "<clinit>", "()V", null, null);
		JMethodBuilder MethodBuilder = new JMethodBuilder(NameSpace.Generator, this, mn);
		MethodBuilder.LoadConst(NameSpace);
		MethodBuilder.AsmVisitor.visitFieldInsn(PUTSTATIC, this.GlobalStaticClassName, this.ContextFieldName, this.GontextDescripter);
		MethodBuilder.AsmVisitor.visitInsn(RETURN);
		GlobalClass.AddMethod(mn);
		byte[] b = GlobalClass.GenerateBytecode();
		this.defineClass(this.GlobalStaticClassName, b, 0, b.length);
	}

	private void AddClassBuilder(JClassBuilder ClassBuilder) {
		this.ByteCodeMap.put(ClassBuilder.ClassName, ClassBuilder);
	}

	JClassBuilder NewBuilder(String SourceFile, String ClassName, String SuperClassName) {
		JClassBuilder cb = new JClassBuilder(ACC_PUBLIC, SourceFile, ClassName, SuperClassName);
		this.AddClassBuilder(cb);
		return cb;
	}
	
	JClassBuilder GenerateMethodHolderClass(String SourceFile, String FuncName, MethodNode AsmMethodNode) {
		JClassBuilder HolderClass = new JClassBuilder(ACC_PUBLIC|ACC_FINAL, SourceFile, JLib.GetHolderClassName(NameSpace, FuncName), "java/lang/Object");
		this.AddClassBuilder(HolderClass);
		HolderClass.AddMethod(AsmMethodNode);
		return HolderClass;
	}
	
	@Override protected Class<?> findClass(String name) {
		JClassBuilder cb = this.ByteCodeMap.get(name);
		if(cb != null) {
			byte[] b = cb.GenerateBytecode();
			this.ByteCodeMap.remove(name);
			return this.defineClass(name, b, 0, b.length);
		}
		return null;
	}

}


class JLib {
	static HashMap<String, Type> TypeMap = new HashMap<String, Type>();
	static Method GetConstPool;
	static Method GetTypeById;
	static Method GetFuncById;
	static Method GetNameSpaceById;
	static Method DynamicGetter;
	static Method DynamicSetter;
	static Method BoxBooleanValue;
	static Method BoxIntValue;
	static Method BoxFloatValue;
	static Method UnboxBooleanValue;
	static Method UnboxIntValue;
	static Method UnboxFloatValue;
	static Method GreenCastOperator;
	static Method GreenInstanceOfOperator;
	static Method NewNewArray;
	static Method NewArray;
	static Method InvokeFunc;
	static Method InvokeOverridedFunc;
	static Method InvokeDynamicFunc;
	static Method InvokeDynamicMethod;
	
	static Method ExecCommandVoid;
	static Method ExecCommandBool;
	static Method ExecCommandString;
	static Method ExecCommandTask;
	
	static {
		TypeMap.put("void", Type.VOID_TYPE);
		TypeMap.put("boolean", Type.BOOLEAN_TYPE);
		TypeMap.put("int", Type.LONG_TYPE);
		TypeMap.put("float", Type.DOUBLE_TYPE);
		TypeMap.put("any", Type.getType(Object.class));
		TypeMap.put("String", Type.getType(String.class));
		TypeMap.put("Array", Type.getType(GreenTeaArray.class));
		TypeMap.put("Func", Type.getType(ZenFunc.class));

		try {
			GetConstPool = ZenStaticTable.class.getMethod("GetConstPool", int.class);
			GetTypeById = ZenStaticTable.class.getMethod("GetTypeById", int.class);
			GetFuncById = ZenStaticTable.class.getMethod("GetFuncById", int.class);
			DynamicGetter = LibZen.class.getMethod("DynamicGetter", Object.class, String.class);
			DynamicSetter = LibZen.class.getMethod("DynamicSetter", Object.class, String.class, Object.class);
			InvokeFunc = LibZen.class.getMethod("InvokeFunc", ZenFunc.class, Object[].class);
			InvokeOverridedFunc = LibZen.class.getMethod("InvokeOverridedMethod", long.class, ZenNameSpace.class, ZenFunc.class, Object[].class);
			InvokeDynamicFunc = LibZen.class.getMethod("InvokeDynamicFunc", long.class, ZenType.class, ZenNameSpace.class, String.class, Object[].class);
			InvokeDynamicMethod = LibZen.class.getMethod("InvokeDynamicMethod", long.class, ZenType.class, ZenNameSpace.class, String.class, Object[].class);
			
			BoxBooleanValue = Boolean.class.getMethod("valueOf", boolean.class);
			BoxIntValue = Long.class.getMethod("valueOf", long.class);
			BoxFloatValue = Double.class.getMethod("valueOf", double.class);
			UnboxBooleanValue = Boolean.class.getMethod("booleanValue");
			UnboxIntValue = Long.class.getMethod("longValue");
			UnboxFloatValue = Double.class.getMethod("doubleValue");

			GreenCastOperator = LibZen.class.getMethod("DynamicCast", ZenType.class, Object.class);
			GreenInstanceOfOperator = LibZen.class.getMethod("DynamicInstanceOf", Object.class, ZenType.class);
			NewNewArray = LibZen.class.getMethod("NewNewArray", ZenType.class, Object[].class);
			NewArray = LibZen.class.getMethod("NewArray", ZenType.class, Object[].class);
//			ExecCommandVoid = DShellProcess.class.getMethod("ExecCommandVoid", String[][].class);
//			ExecCommandBool = DShellProcess.class.getMethod("ExecCommandBool", String[][].class);
//			ExecCommandString = DShellProcess.class.getMethod("ExecCommandString", String[][].class);
//			ExecCommandTask = DShellProcess.class.getMethod("ExecCommandTask", String[][].class);
		}
		catch(Exception e) {
			e.printStackTrace();
			LibNative.Exit(1, "load error");
		}
	}
	
	public static String GetHolderClassName(ZenNameSpace NameSpace, String FuncName) {
		return "FuncHolder" + FuncName + "$" + 0; //Context.ParserId;
	}
	
	static Type GetAsmType(ZenType GreenType) {
		Type type = TypeMap.get(GreenType.ShortName);
		if(type != null) {
			return type;
		}
		if(GreenType.TypeBody != null && GreenType.TypeBody instanceof Class<?>) {
			return Type.getType((Class<?>) GreenType.TypeBody);
		}
		if(GreenType.IsTypeVariable()) {
			return Type.getType(Object.class);
		}
		if(GreenType.IsGenericType()) {
			return GetAsmType(GreenType.BaseType);
		}
		return Type.getType("L" + GreenType.GetNativeName() + ";");
	}

	static String GetMethodDescriptor(ZenFunc Func) {
		Type ReturnType = GetAsmType(Func.GetReturnType());
		Type[] argTypes = new Type[Func.GetFuncParamSize()];
		for(int i = 0; i < argTypes.length; i++) {
			ZenType ParamType = Func.GetFuncParamType(i);
			argTypes[i] = GetAsmType(ParamType);
		}
		return Type.getMethodDescriptor(ReturnType, argTypes);
	}
}

final class JLocalVarStack {
	public final String Name;
	public final Type   TypeInfo;
	public final int    Index;

	public JLocalVarStack(int Index, Type TypeInfo, String Name) {
		this.Index = Index;
		this.TypeInfo = TypeInfo;
		this.Name = Name;
	}
}

class JMethodBuilder {
	final GreenTeaClassLoader           LocalClassLoader;
	final MethodVisitor                 AsmVisitor;
	final ZenGenerator                   Generator;
	ArrayList<JLocalVarStack>     LocalVals;
	int                           LocalSize;
	Stack<Label>                  BreakLabelStack;
	Stack<Label>                  ContinueLabelStack;
	int PreviousLine;

	public JMethodBuilder(ZenGenerator Generator, GreenTeaClassLoader ClassLoader, MethodVisitor AsmVisitor) {
		this.Generator = Generator;
		this.LocalClassLoader = ClassLoader;
		this.AsmVisitor = AsmVisitor;
		this.LocalVals = new ArrayList<JLocalVarStack>();
		this.LocalSize = 0;
		this.BreakLabelStack = new Stack<Label>();
		this.ContinueLabelStack = new Stack<Label>();
		this.PreviousLine = 0;
	}

	void SetLineNumber(long FileLine) {
		if(FileLine != 0) {
			int Line = ZenStaticTable.GetFileLineNumber(FileLine);
			if(Line != this.PreviousLine) {
				Label LineLabel = new Label();
				this.AsmVisitor.visitLineNumber(Line, LineLabel);
				this.PreviousLine = Line;
			}
		}
	}

	void SetLineNumber(ZenNode Node) {
		this.SetLineNumber(Node.Token.FileLine);
	}
	
	void LoadLocal(JLocalVarStack local) {
		Type type = local.TypeInfo;
		this.AsmVisitor.visitVarInsn(type.getOpcode(ILOAD), local.Index);
	}

	void StoreLocal(JLocalVarStack local) {
		Type type = local.TypeInfo;
		this.AsmVisitor.visitVarInsn(type.getOpcode(ISTORE), local.Index);
	}

	public JLocalVarStack FindLocalVariable(String Name) {
		for(int i = 0; i < this.LocalVals.size(); i++) {
			JLocalVarStack l = this.LocalVals.get(i);
			if(l.Name.equals(Name)) {
				return l;
			}
		}
		return null;
	}

	public JLocalVarStack AddLocal(ZenType GreenType, String Name) {
		Type LocalType =  JLib.GetAsmType(GreenType);
		JLocalVarStack local = new JLocalVarStack(this.LocalSize, LocalType, Name);
		this.LocalVals.add(local);
		this.LocalSize += LocalType.getSize();
		return local;
	}
	
	void LoadConst(Object Value) {
		if(Value instanceof Boolean || Value instanceof Long || Value instanceof Double || Value instanceof String) {
			this.AsmVisitor.visitLdcInsn(Value);
			return;
		}
		if(Value instanceof ZenNameSpace) {
			this.AsmVisitor.visitFieldInsn(GETSTATIC, this.LocalClassLoader.GlobalStaticClassName, this.LocalClassLoader.ContextFieldName, this.LocalClassLoader.GontextDescripter);
			return;
		}
		if(Value instanceof ZenType) {
			int id = ((ZenType)Value).TypeId;
			this.AsmVisitor.visitLdcInsn(id);
			this.InvokeMethodCall(ZenType.class, JLib.GetTypeById);
			return;
		}
		else if(Value instanceof ZenFunc) {
			int id = ((ZenFunc)Value).FuncId;
			this.AsmVisitor.visitLdcInsn(id);
			this.InvokeMethodCall(ZenFunc.class, JLib.GetFuncById);
			return;
		}
		int id = ZenStaticTable.AddConstPool(Value);
		this.AsmVisitor.visitLdcInsn(id);
		this.InvokeMethodCall(Value.getClass(), JLib.GetConstPool);
	}

	void LoadNewArray(ZenGenerator Visitor, int StartIdx, ArrayList<ZenNode> NodeList) {
		this.AsmVisitor.visitLdcInsn(NodeList.size() - StartIdx);
		this.AsmVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(Object.class));
		//System.err.println("** arraysize = " + (NodeList.size() - StartIdx));
		for(int i = StartIdx; i < NodeList.size(); i++) {
			this.AsmVisitor.visitInsn(DUP);
			this.AsmVisitor.visitLdcInsn(i);
			NodeList.get(i).Accept(Visitor);
//			System.out.println("i="+i+" type="+NodeList.get(i).Type);
			this.CheckCast(Object.class, NodeList.get(i).Type);
			this.AsmVisitor.visitInsn(AASTORE);
		}
	}
	
	void CheckCast(Class<?> RequiredType, Class<?> GivenType) {
		//System.err.println("casting .. giventype = " + GivenType + ", requested = " + RequiredType);
		if(RequiredType == void.class || RequiredType == GivenType ) {
			return;
		}
		if(RequiredType == long.class) {
			if(GivenType == Object.class) {
				this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
				this.InvokeMethodCall(long.class, JLib.UnboxIntValue);
				return;
			}
		}
		if(RequiredType == double.class) {
			if(GivenType == Object.class) {
				this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Double.class));
				this.InvokeMethodCall(double.class, JLib.UnboxFloatValue);
				return;
			}
		}
		if(RequiredType == boolean.class) {
			if(GivenType == Object.class) {
				this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(Long.class));
				this.InvokeMethodCall(boolean.class, JLib.UnboxBooleanValue);
				return;
			}
		}
		if(GivenType == long.class) {
			if(RequiredType == Object.class) {
				this.InvokeMethodCall(Long.class, JLib.BoxIntValue);
				return;
			}
		}
		if(GivenType == double.class) {
			if(RequiredType == Object.class) {
				this.InvokeMethodCall(Double.class, JLib.BoxFloatValue);
				return;
			}
		}
		if(GivenType == boolean.class) {
			if(RequiredType == Object.class) {
				this.InvokeMethodCall(Boolean.class, JLib.BoxBooleanValue);
				return;
			}
		}
		if(GivenType.isArray()) {
			return;//FIXME
		}
		if(GivenType.isPrimitive() && RequiredType.isPrimitive()) {
			return;//FIXME
		}
		//System.err.println("CHECKCAST (" + RequiredType + ") " + GivenType);
		this.AsmVisitor.visitTypeInsn(CHECKCAST, Type.getInternalName(RequiredType));
	}

	void CheckCast(Class<?> RequiredType, ZenType GivenType) {
		if(GivenType != null) {
			this.CheckCast(RequiredType, GivenType.GetNativeType(false));
		}
//		else {
//			System.err.println("cannot check cast given = " + GivenType + " RequiredType="+RequiredType);
//		}
	}

	void CheckCast(ZenType RequiredType, ZenType GivenType) {
		this.CheckCast(RequiredType.GetNativeType(false), GivenType);
	}

	void Call(Constructor<?> method) {
		String owner = Type.getInternalName(method.getDeclaringClass());
		this.AsmVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", Type.getConstructorDescriptor(method));
	}

	void InvokeMethodCall(Method method) {
//		System.err.println("giventype = " + method);
		InvokeMethodCall(void.class, method);
	}

	void InvokeMethodCall(ZenType RequiredType, Method method) {
		Class<?> RequiredNativeType = Object.class;
		if(RequiredType != null) {
			RequiredNativeType = RequiredType.GetNativeType(false);
		}
		InvokeMethodCall(RequiredNativeType, method);
	}

	void InvokeMethodCall(Class<?> RequiredType, Method method) {
		int inst;
		if(Modifier.isStatic(method.getModifiers())) {
			inst = INVOKESTATIC;
		}
		else if(Modifier.isInterface(method.getModifiers())) {
			inst = INVOKEINTERFACE;
		}
		else {
			inst = INVOKEVIRTUAL;
		}
		String owner = Type.getInternalName(method.getDeclaringClass());
		this.AsmVisitor.visitMethodInsn(inst, owner, method.getName(), Type.getMethodDescriptor(method));
		//System.err.println("\tRequiredType="+RequiredType+", " + method);
		this.CheckCast(RequiredType, method.getReturnType());
	}

	public void PopValue(boolean IsLong) {
		if(IsLong) {
			this.AsmVisitor.visitInsn(POP2);
		}
		else {
			this.AsmVisitor.visitInsn(POP);
		}
	}

	public void PushEvaluatedNode(ZenType RequestedType, ZenNode ParamNode) {
		//System.err.println("requested=" + RequestedType + ", given="+ParamNode.Type);
		ParamNode.Accept(this.Generator);
		this.CheckCast(RequestedType, ParamNode.Type);
	}
	
}

public class JavaByteCodeGenerator extends ZenGenerator {
	GreenTeaClassLoader ClassGenerator;
	JMethodBuilder CurrentVisitor;
	ArrayList<ZenType> StackFrame;

	public JavaByteCodeGenerator(String TargetCode, String OutputFile, int GeneratorFlag) {
		super("java", OutputFile, GeneratorFlag);
		this.StackFrame = new ArrayList<ZenType>();
	}

	private void PushStack(ZenType Type) {
		if(!Type.IsVoidType()) {
			StackFrame.add(Type);
		}
		//System.out.println(StackFrame.size());
	}

	private void PopStack() {
		assert(StackFrame.size() > 0);
		ZenType LastType = StackFrame.remove(StackFrame.size() - 1);
		CurrentVisitor.PopValue(LastType.IsIntType());
		//System.out.println(StackFrame.size());
	}

	private void RemoveStack(int VariableSize) {
		assert(StackFrame.size() - VariableSize >= 0);
		for (int i = 0; i < VariableSize; i++) {
			StackFrame.remove(StackFrame.size() - 1);
		}
		//System.out.println(StackFrame.size());
	}

	@Override
	public void VisitBlock(ZenNode Node) {
		int CurrentStackIndex = this.StackFrame.size();
		/*local*/ZenNode CurrentNode = Node;
		while(CurrentNode != null) {
			CurrentNode.Accept(this);
			CurrentNode = CurrentNode.NextNode;
		}
		while(CurrentStackIndex != this.StackFrame.size()) {
			this.PopStack();
		}
	}

	@Override public void InitContext(ZenNameSpace Context) {
		super.InitContext(Context);
		this.ClassGenerator = new GreenTeaClassLoader(Context);
	}

	@Override public void GenerateFunc(ZenFunc Func, ArrayList<String> NameList, ZenNode Body) {
		//this.Context.ShowReportedErrors();
		String MethodName = Func.GetNativeFuncName();
		String MethodDesc = JLib.GetMethodDescriptor(Func);
		MethodNode AsmMethodNode = new MethodNode(ACC_PUBLIC | ACC_STATIC, MethodName, MethodDesc, null, null);
		JClassBuilder ClassHolder = this.ClassGenerator.GenerateMethodHolderClass(ZenStaticTable.GetSourceFileName(Body.Token.FileLine), MethodName, AsmMethodNode);

		JMethodBuilder LocalBuilder = new JMethodBuilder(this, this.ClassGenerator, AsmMethodNode);
		JMethodBuilder PushedBuilder = this.CurrentVisitor;

		for(int i = 0; i < NameList.size(); i++) {
			String Name = NameList.get(i);
			LocalBuilder.AddLocal(Func.GetFuncParamType(i), Name);
		}
		this.CurrentVisitor = LocalBuilder;
		this.VisitBlock(Body);
		this.CurrentVisitor = PushedBuilder;
		if(Func.GetReturnType().IsVoidType()) {
			// JVM always needs return;
			LocalBuilder.AsmVisitor.visitInsn(RETURN);
		}
		try {
			if(LibZen.DebugMode) {
				ClassHolder.OutputClassFile(ClassHolder.ClassName, ".");
			}
			Class<?> DefinedClass = this.ClassGenerator.loadClass(ClassHolder.ClassName);
			Method[] DefinedMethods = DefinedClass.getMethods();
			for(Method m : DefinedMethods) {
				if(m.getName().equals(Func.GetNativeFuncName())) {
					Func.SetNativeMethod(0, m);
					break;
				}
			}
		} catch(Exception e) {
			LibZen.VerboseException(e);
		}
	}

//	@Override public void OpenClassField(ZenSyntaxTree ParsedTree, ZenType ClassType, ZenClassField ClassField) {
//		String ClassName = ClassType.GetNativeName();
//		String superClassName = ClassType.SuperType.GetNativeName();
//		//System.err.println("class name = " + ClassName + " extends " + superClassName);
//		JClassBuilder ClassBuilder = this.ClassGenerator.NewBuilder(ZenStaticTable.GetSourceFileName(ParsedTree.KeyToken.FileLine), ClassName, superClassName);
//		// generate field
//		for(ZenFieldInfo field : ClassField.FieldList) {
//			if(field.FieldIndex >= ClassField.ThisClassIndex) {
//				String fieldName = field.NativeName;
//				Type fieldType = JLib.GetAsmType(field.Type);
//				FieldNode node = new FieldNode(ACC_PUBLIC, fieldName, fieldType.getDescriptor(), null, null);
//				ClassBuilder.AddField(node);
//			}
//		}
//		// generate default constructor (for jvm)
//		MethodNode constructor = new MethodNode(ACC_PUBLIC, "<init>", "(Lorg/GreenTeaScript/ZenType;)V", null, null);
//		constructor.visitVarInsn(ALOAD, 0);
//		constructor.visitVarInsn(ALOAD, 1);
//		constructor.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "(Lorg/GreenTeaScript/ZenType;)V");
//		for(ZenFieldInfo field : ClassField.FieldList) {
//			if(field.FieldIndex >= ClassField.ThisClassIndex && field.InitValue != null) {
//				String name = field.NativeName;
//				String desc = JLib.GetAsmType(field.Type).getDescriptor();
//				constructor.visitVarInsn(ALOAD, 0);
//				constructor.visitLdcInsn(field.InitValue);
//				constructor.visitFieldInsn(PUTFIELD, ClassName, name, desc);
//			}
//		}
//		constructor.visitInsn(RETURN);
//		ClassBuilder.AddMethod(constructor);
//		try {
//			ClassType.TypeBody = this.ClassGenerator.loadClass(ClassName);
//			LibNative.Assert(ClassType.TypeBody != null);
//		}
//		catch (Exception e) {
//			LibZen.VerboseException(e);
//		}
//	}

	//-----------------------------------------------------
	@Override public void VisitNullNode(ZenNullNode Node) {
		this.CurrentVisitor.AsmVisitor.visitInsn(ACONST_NULL);
		this.PushStack(Node.Type);
	}

	@Override public void VisitBooleanNode(ZenBooleanNode Node) {
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(Node.Value);
		this.PushStack(Node.Type);
	}

	@Override public void VisitIntNode(ZenIntNode Node) {
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(Node.Value);
		this.PushStack(Node.Type);
	}

	@Override public void VisitFloatNode(ZenFloatNode Node) {
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(Node.Value);
		this.PushStack(Node.Type);
	}

	@Override public void VisitStringNode(ZenStringNode Node) {
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(Node.Value);
		this.PushStack(Node.Type);
	}

	//FIXME Need to Implement
//	@Override public void VisitRegexNode(ZenRegexNode Node) {
//		this.VisitingBuilder.Append("");
//	}

	@Override public void VisitConstPoolNode(ZenConstPoolNode Node) {
		Object constValue = Node.ConstValue;
		LibNative.Assert(Node.ConstValue != null);
		this.CurrentVisitor.LoadConst(constValue);
		this.PushStack(Node.Type);
	}

	@Override public void VisitAllocateNode(ZenAllocateNode Node) {
		Type type = JLib.GetAsmType(Node.Type);
		String owner = type.getInternalName();
		this.CurrentVisitor.AsmVisitor.visitTypeInsn(NEW, owner);
		this.CurrentVisitor.AsmVisitor.visitInsn(DUP);
		if(!Node.Type.IsNativeType()) {
			this.CurrentVisitor.LoadConst(Node.Type);
			this.CurrentVisitor.AsmVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "(Lorg/GreenTeaScript/ZenType;)V");
		} else {
			this.CurrentVisitor.AsmVisitor.visitMethodInsn(INVOKESPECIAL, owner, "<init>", "()V");
		}
		this.PushStack(Node.Type);
	}

	@Override public void VisitGetLocalNode(ZenGetLocalNode Node) {
		JLocalVarStack local = this.CurrentVisitor.FindLocalVariable(Node.NativeName);
		this.CurrentVisitor.LoadLocal(local);
		this.PushStack(Node.Type);
	}

	@Override public void VisitSetLocalNode(ZenSetLocalNode Node) {
		JLocalVarStack local = this.CurrentVisitor.FindLocalVariable(Node.NativeName);
		this.CurrentVisitor.PushEvaluatedNode(Node.ValueNode.Type, Node.ValueNode);
		this.CurrentVisitor.StoreLocal(local);
	}

	@Override public void VisitConstructorNode(ZenConstructorNode Node) {
		if(Node.Type.TypeBody instanceof Class<?>) {
			// native class
			Class<?> klass = (Class<?>) Node.Type.TypeBody;
//			Type type = Type.getType(klass);
			this.CurrentVisitor.AsmVisitor.visitTypeInsn(NEW, Type.getInternalName(klass));
			this.CurrentVisitor.AsmVisitor.visitInsn(DUP);
			for(int i = 0; i<Node.ParamList.size(); i++) {
				ZenNode ParamNode = Node.ParamList.get(i);
				this.CurrentVisitor.PushEvaluatedNode(Node.Func.GetFuncParamType(i), ParamNode);
//				ParamNode.Accept(this);
//				this.VisitingBuilder.CheckCast(Node.Func.GetFuncParamType(i), ParamNode.Type);
			}
			this.RemoveStack(Node.ParamList.size());
			this.CurrentVisitor.Call((Constructor<?>) Node.Func.FuncBody);
		} else {
			LibZen.TODO("TypeBody is not Class<?>");
		}
		this.PushStack(Node.Type);
	}

	@Override public void VisitGetterNode(ZenGetterNode Node) {
		String name = Node.ResolvedFunc.FuncName;
		Type fieldType = JLib.GetAsmType(Node.ResolvedFunc.GetReturnType());
		Type ownerType = JLib.GetAsmType(Node.ResolvedFunc.GetFuncParamType(0));
		Node.RecvNode.Accept(this);
		this.CurrentVisitor.AsmVisitor.visitFieldInsn(GETFIELD, ownerType.getInternalName(), name, fieldType.getDescriptor());
		this.RemoveStack(1);
		this.PushStack(Node.Type);
	}

	@Override public void VisitSetterNode(ZenSetterNode Node) {
		String name = Node.NativeName;
		Type fieldType = JLib.GetAsmType(Node.ResolvedFunc.GetFuncParamType(1));
		Type ownerType = JLib.GetAsmType(Node.ResolvedFunc.GetFuncParamType(0));
		Node.RecvNode.Accept(this);
		this.CurrentVisitor.PushEvaluatedNode(Node.ResolvedFunc.GetFuncParamType(1), Node.ValueNode);
		this.CurrentVisitor.AsmVisitor.visitFieldInsn(PUTFIELD, ownerType.getInternalName(), name, fieldType.getDescriptor());
		this.RemoveStack(2);
	}

//	@Override public void VisitMethodCallNode(ZenMethodCall Node) {
//		ZenFunc Func = Node.ResolvedFunc;
//		this.VisitingBuilder.SetLineNumber(Node);
//		for(int i = 0; i < Node.ParamList.size(); i++) {
//			ZenNode ParamNode = Node.ParamList.get(i);
//			this.VisitingBuilder.PushEvaluatedNode(Func.GetFuncParamType(i), ParamNode);
////			ParamNode.Accept(this);
////			this.VisitingBuilder.CheckCast(Func.GetFuncParamType(i), ParamNode.Type);
//		}
//		if(Func.FuncBody instanceof Method) {
//			this.VisitingBuilder.InvokeMethodCall(Node.Type, (Method) Func.FuncBody);
//		}
//		else {
//			String MethodName = Func.GetNativeFuncName(); 
//			String Owner = JLib.GetHolderClassName(this.Context, MethodName);
//			String MethodDescriptor = JLib.GetMethodDescriptor(Func);
//			this.VisitingBuilder.AsmVisitor.visitMethodInsn(INVOKESTATIC, Owner, MethodName, MethodDescriptor);
//		}
//		this.RemoveStack(Node.ParamList.size());
//		this.PushStack(Node.Type);
//	}

	@Override public void VisitApplyNode(ZenApplyNode Node) {
		Node.FuncNode.Accept(this);
		this.CurrentVisitor.LoadNewArray(this, 0, Node.ParamList);
		this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.InvokeFunc);
		this.RemoveStack(Node.ParamList.size());
		PushStack(Node.Type);
	}

	@Override public void VisitApplyOverridedMethodNode(ZenApplyOverridedMethodNode Node) {
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(Node.Token.FileLine);
		this.CurrentVisitor.LoadConst(Node.NameSpace);
		this.CurrentVisitor.LoadConst(Node.Func);
		this.CurrentVisitor.LoadNewArray(this, 0, Node.ParamList);
		this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.InvokeOverridedFunc);		
		this.RemoveStack(Node.ParamList.size());
		PushStack(Node.Type);
	}
	
//	@Override public void VisitApplyDynamicFuncNode(ZenApplyDynamicFuncNode Node) {
//		this.VisitingBuilder.AsmVisitor.visitLdcInsn((long)Node.Token.FileLine);
//		this.VisitingBuilder.LoadConst(Node.Type);
//		this.VisitingBuilder.LoadConst(Node.NameSpace);
//		this.VisitingBuilder.LoadConst(Node.FuncName);		
//		this.VisitingBuilder.LoadNewArray(this, 0, Node.ParamList);
//		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.InvokeDynamicFunc);				
//		this.VisitingBuilder.CheckReturn(Node.Type, ZenStaticTable.AnyType);
//	}
//
//	@Override public void VisitApplyDynamicMethodNode(ZenApplyDynamicMethodNode Node) {
//		this.VisitingBuilder.AsmVisitor.visitLdcInsn((long)Node.Token.FileLine);
//		this.VisitingBuilder.LoadConst(Node.Type);
//		this.VisitingBuilder.LoadConst(Node.NameSpace);
//		this.VisitingBuilder.LoadConst(Node.FuncName);		
//		this.VisitingBuilder.LoadNewArray(this, 0, Node.ParamList);
//		this.VisitingBuilder.InvokeMethodCall(Node.Type, JLib.InvokeDynamicMethod);				
//		this.VisitingBuilder.CheckReturn(Node.Type, ZenStaticTable.AnyType);
//	}
	
//	@Override public void VisitUnaryNode(ZenUnaryNode Node) {
//		LibNative.Assert(Node.ResolvedFunc.FuncBody instanceof Method);
//		this.VisitingBuilder.PushEvaluatedNode(Node.ResolvedFunc.GetFuncParamType(0), Node.RecvNode);
//		this.VisitingBuilder.InvokeMethodCall(Node.Type, (Method)Node.ResolvedFunc.FuncBody);
//		this.RemoveStack(1);
//		PushStack(Node.Type);
//	}
//
//	@Override public void VisitBinaryNode(ZenBinaryNode Node) {
//		LibNative.Assert(Node.ResolvedFunc.FuncBody instanceof Method);
//		this.VisitingBuilder.PushEvaluatedNode(Node.ResolvedFunc.GetFuncParamType(0), Node.LeftNode);
//		this.VisitingBuilder.PushEvaluatedNode(Node.ResolvedFunc.GetFuncParamType(1), Node.RightNode);
//		this.VisitingBuilder.InvokeMethodCall(Node.Type, (Method)Node.ResolvedFunc.FuncBody);
//		this.RemoveStack(2);
//		PushStack(Node.Type);
//	}

	@Override public void VisitGetIndexNode(ZenGetIndexNode Node) {
		Node.RecvNode.Accept(this);
		this.CurrentVisitor.PushEvaluatedNode(Node.ResolvedFunc.GetFuncParamType(1), Node.IndexNode);
		this.CurrentVisitor.InvokeMethodCall(Node.Type, (Method) Node.ResolvedFunc.FuncBody);
		this.RemoveStack(2);
		PushStack(Node.Type);
	}

	@Override public void VisitSetIndexNode(ZenSetIndexNode Node) {
		Node.RecvNode.Accept(this);
		this.CurrentVisitor.PushEvaluatedNode(Node.ResolvedFunc.GetFuncParamType(1), Node.IndexNode);
		this.CurrentVisitor.PushEvaluatedNode(Node.ResolvedFunc.GetFuncParamType(2), Node.ValueNode);
		this.CurrentVisitor.InvokeMethodCall(Node.Type, (Method) Node.ResolvedFunc.FuncBody);
		this.RemoveStack(3);
	}

	@Override public void VisitArrayLiteralNode(ZenArrayLiteralNode Node) {
		this.CurrentVisitor.LoadConst(Node.Type);
		this.CurrentVisitor.LoadNewArray(this, 0, Node.NodeList);
		this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.NewNewArray);
		this.RemoveStack(Node.NodeList.size());
		PushStack(Node.Type);
		PushStack(Node.Type);
	}

	@Override
	public void VisitNewArrayNode(ZenNewArrayNode Node) {
		this.CurrentVisitor.LoadConst(Node.Type);
		this.CurrentVisitor.LoadNewArray(this, 0, Node.NodeList);
		this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.NewArray);
		this.RemoveStack(Node.NodeList.size());
		PushStack(Node.Type);
		PushStack(Node.Type);
	}

	@Override public void VisitAndNode(ZenAndNode Node) {
		Label elseLabel = new Label();
		Label mergeLabel = new Label();
		this.CurrentVisitor.PushEvaluatedNode(ZenStaticTable.BooleanType, Node.LeftNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFEQ, elseLabel);

		this.CurrentVisitor.PushEvaluatedNode(ZenStaticTable.BooleanType, Node.RightNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFEQ, elseLabel);

		this.CurrentVisitor.AsmVisitor.visitLdcInsn(true);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);

		this.CurrentVisitor.AsmVisitor.visitLabel(elseLabel);
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(false);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);

		this.CurrentVisitor.AsmVisitor.visitLabel(mergeLabel);
	}

	@Override public void VisitOrNode(ZenOrNode Node) {
		Label thenLabel = new Label();
		Label mergeLabel = new Label();
		this.CurrentVisitor.PushEvaluatedNode(ZenStaticTable.BooleanType, Node.LeftNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFNE, thenLabel);

		this.CurrentVisitor.PushEvaluatedNode(ZenStaticTable.BooleanType, Node.RightNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFNE, thenLabel);

		this.CurrentVisitor.AsmVisitor.visitLdcInsn(false);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);

		this.CurrentVisitor.AsmVisitor.visitLabel(thenLabel);
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(true);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, mergeLabel);

		this.CurrentVisitor.AsmVisitor.visitLabel(mergeLabel);
	}

//	@Override public void VisitSelfAssignNode(ZenSelfAssignNode Node) {
//		if(Node.LeftNode instanceof ZenGetLocalNode) {
//			ZenGetLocalNode Left = (ZenGetLocalNode)Node.LeftNode;
//			JLocalVarStack local = this.VisitingBuilder.FindLocalVariable(Left.NativeName);
//			Node.LeftNode.Accept(this);
//			Node.RightNode.Accept(this);
//			this.VisitingBuilder.InvokeMethodCall((Method)Node.Func.FuncBody);
//			this.VisitingBuilder.StoreLocal(local);
//		}
//		else {
//			LibZen.TODO("selfAssign");
//		}
//	}

	@Override public void VisitVarDeclNode(ZenVarDeclNode Node) {
		JLocalVarStack local = this.CurrentVisitor.AddLocal(Node.Type, Node.NativeName);
		this.CurrentVisitor.PushEvaluatedNode(Node.DeclType, Node.InitNode);
		this.CurrentVisitor.StoreLocal(local);
		this.VisitBlock(Node.BlockNode);
		this.PopStack();
	}

	@Override public void VisitIfNode(ZenIfNode Node) {
		Label ElseLabel = new Label();
		Label EndLabel = new Label();
		this.CurrentVisitor.PushEvaluatedNode(ZenStaticTable.BooleanType, Node.CondNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFEQ, ElseLabel);
		this.RemoveStack(1);
		// Then
		this.VisitBlock(Node.ThenNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
		// Else
		this.CurrentVisitor.AsmVisitor.visitLabel(ElseLabel);
		if(Node.ElseNode != null) {
			this.VisitBlock(Node.ElseNode);
			this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
		}
		// End
		this.CurrentVisitor.AsmVisitor.visitLabel(EndLabel);
	}

	@Override public void VisitTrinaryNode(ZenTrinaryNode Node) {
		Label ElseLabel = new Label();
		Label EndLabel = new Label();
		this.CurrentVisitor.PushEvaluatedNode(ZenStaticTable.BooleanType, Node.CondNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFEQ, ElseLabel);
		// Then
		this.VisitBlock(Node.ThenNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
		// Else
		this.CurrentVisitor.AsmVisitor.visitLabel(ElseLabel);
		this.VisitBlock(Node.ElseNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, EndLabel);
		// End
		this.CurrentVisitor.AsmVisitor.visitLabel(EndLabel);
		this.PushStack(Node.Type);
	}

	@Override public void VisitSwitchNode(ZenSwitchNode Node) {
		int cases = Node.CaseList.size() / 2;
		int[] keys = new int[cases];
		Label[] caseLabels = new Label[cases];
		Label defaultLabel = new Label();
		Label breakLabel = new Label();
		for(int i=0; i<cases; i++) {
			keys[i] = ((Number)((ZenConstPoolNode)Node.CaseList.get(i*2)).ConstValue).intValue();
			caseLabels[i] = new Label();
		}
		Node.MatchNode.Accept(this);
		this.CurrentVisitor.AsmVisitor.visitInsn(L2I);
		this.CurrentVisitor.AsmVisitor.visitLookupSwitchInsn(defaultLabel, keys, caseLabels);
		for(int i=0; i<cases; i++) {
			this.CurrentVisitor.BreakLabelStack.push(breakLabel);
			this.CurrentVisitor.AsmVisitor.visitLabel(caseLabels[i]);
			this.VisitBlock(Node.CaseList.get(i*2+1));
			this.CurrentVisitor.BreakLabelStack.pop();
		}
		this.CurrentVisitor.AsmVisitor.visitLabel(defaultLabel);
		this.VisitBlock(Node.DefaultBlock);
		this.CurrentVisitor.AsmVisitor.visitLabel(breakLabel);
	}

	@Override public void VisitWhileNode(ZenWhileNode Node) {
		Label continueLabel = new Label();
		Label breakLabel = new Label();
		this.CurrentVisitor.BreakLabelStack.push(breakLabel);
		this.CurrentVisitor.ContinueLabelStack.push(continueLabel);

		this.CurrentVisitor.AsmVisitor.visitLabel(continueLabel);
		this.CurrentVisitor.PushEvaluatedNode(ZenStaticTable.BooleanType, Node.CondNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
		this.VisitBlock(Node.BodyNode);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, continueLabel);
		this.CurrentVisitor.AsmVisitor.visitLabel(breakLabel);

		this.CurrentVisitor.BreakLabelStack.pop();
		this.CurrentVisitor.ContinueLabelStack.pop();
	}

	@Override public void VisitDoWhileNode(ZenDoWhileNode Node) {
		Label headLabel = new Label();
		Label continueLabel = new Label();
		Label breakLabel = new Label();
		this.CurrentVisitor.BreakLabelStack.push(breakLabel);
		this.CurrentVisitor.ContinueLabelStack.push(continueLabel);

		this.CurrentVisitor.AsmVisitor.visitLabel(headLabel);
		this.VisitBlock(Node.BodyNode);
		this.CurrentVisitor.AsmVisitor.visitLabel(continueLabel);
		Node.CondNode.Accept(this);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, headLabel);
		this.CurrentVisitor.AsmVisitor.visitLabel(breakLabel);

		this.CurrentVisitor.BreakLabelStack.pop();
		this.CurrentVisitor.ContinueLabelStack.pop();
	}

	@Override public void VisitForNode(ZenForNode Node) {
		Label headLabel = new Label();
		Label continueLabel = new Label();
		Label breakLabel = new Label();
		this.CurrentVisitor.BreakLabelStack.push(breakLabel);
		this.CurrentVisitor.ContinueLabelStack.push(continueLabel);

		this.CurrentVisitor.AsmVisitor.visitLabel(headLabel);
		Node.CondNode.Accept(this);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(IFEQ, breakLabel); // condition
		this.VisitBlock(Node.BodyNode);
		this.CurrentVisitor.AsmVisitor.visitLabel(continueLabel);
		Node.IterNode.Accept(this);
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, headLabel);
		this.CurrentVisitor.AsmVisitor.visitLabel(breakLabel);

		this.CurrentVisitor.BreakLabelStack.pop();
		this.CurrentVisitor.ContinueLabelStack.pop();
	}

	@Override public void VisitForEachNode(ZenForEachNode Node) {
		LibZen.TODO("ForEach");
	}

	@Override public void VisitReturnNode(ZenReturnNode Node) {
		if(Node.ValueNode != null) {
			Node.ValueNode.Accept(this);
			Type type = JLib.GetAsmType(Node.ValueNode.Type);
			this.CurrentVisitor.AsmVisitor.visitInsn(type.getOpcode(IRETURN));
		}
		else {
			this.CurrentVisitor.AsmVisitor.visitInsn(RETURN);
		}
	}

	@Override public void VisitBreakNode(ZenBreakNode Node) {
		Label l = this.CurrentVisitor.BreakLabelStack.peek();
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, l);
	}

	@Override public void VisitContinueNode(ZenContinueNode Node) {
		Label l = this.CurrentVisitor.ContinueLabelStack.peek();
		this.CurrentVisitor.AsmVisitor.visitJumpInsn(GOTO, l);
	}

	@Override public void VisitTryNode(ZenTryNode Node) {
		int catchSize = LibZen.ListSize(Node.CatchList);
		MethodVisitor mv = this.CurrentVisitor.AsmVisitor;
		Label beginTryLabel = new Label();
		Label endTryLabel = new Label();
		Label finallyLabel = new Label();
		Label catchLabel[] = new Label[catchSize];

		// try block
		mv.visitLabel(beginTryLabel);
		this.VisitBlock(Node.TryNode);
		mv.visitLabel(endTryLabel);
		mv.visitJumpInsn(GOTO, finallyLabel);

		// prepare
		for(int i = 0; i < catchSize; i++) { //TODO: add exception class name
			catchLabel[i] = new Label();
			ZenCatchNode Catch = (/*cast*/ZenCatchNode) Node.CatchList.get(i);
			String throwType = JLib.GetAsmType(Catch.ExceptionType).getInternalName();
			mv.visitTryCatchBlock(beginTryLabel, endTryLabel, catchLabel[i], throwType);
		}

		// catch block
		for(int i = 0; i < catchSize; i++) { //TODO: add exception class name
			ZenCatchNode Catch = (/*cast*/ZenCatchNode) Node.CatchList.get(i);
			JLocalVarStack local = this.CurrentVisitor.AddLocal(Catch.ExceptionType, Catch.ExceptionName);
			mv.visitLabel(catchLabel[i]);
			this.CurrentVisitor.StoreLocal(local);
			this.VisitBlock(Catch.BodyNode);
			mv.visitJumpInsn(GOTO, finallyLabel);
			//FIXME: remove local
		}

		// finally block
		mv.visitLabel(finallyLabel);
		this.VisitBlock(Node.FinallyNode);
	}

	@Override public void VisitThrowNode(ZenThrowNode Node) {
		// use wrapper
		//String name = Type.getInternalName(ZenThrowableWrapper.class);
		//this.VisitingBuilder.MethodVisitor.visitTypeInsn(NEW, name);
		//this.VisitingBuilder.MethodVisitor.visitInsn(DUP);
		//Node.Expr.Accept(this);
		//this.box();
		//this.VisitingBuilder.typeStack.pop();
		//this.VisitingBuilder.MethodVisitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ljava/lang/Object;)V");
		//this.VisitingBuilder.MethodVisitor.visitInsn(ATHROW);
	}

	@Override public void VisitInstanceOfNode(ZenInstanceOfNode Node) {
		if(Node.TypeInfo.IsGenericType() || Node.TypeInfo.IsVirtualType()) {
			Node.ExprNode.Accept(this);
			this.CurrentVisitor.LoadConst(Node.TypeInfo);
			this.CurrentVisitor.InvokeMethodCall(boolean.class, JLib.GreenInstanceOfOperator);
		}
		else {
			Node.ExprNode.Accept(this);
			this.CurrentVisitor.CheckCast(Object.class, Node.ExprNode.Type);
			Class<?> NativeType = Node.TypeInfo.GetNativeType(true);
			this.CurrentVisitor.AsmVisitor.visitTypeInsn(INSTANCEOF, Type.getInternalName(NativeType));
		}
		this.PushStack(Node.Type);
	}

//	@Override public void VisitCastNode(ZenCastNode Node) {
//		this.VisitingBuilder.LoadConst(Node.CastType);
//		Node.Expr.Accept(this);
//		this.VisitingBuilder.CheckCast(Object.class, Node.Expr.Type);
//		this.VisitingBuilder.InvokeMethodCall(Node.CastType, JLib.GreenCastOperator);
//		this.PushStack(Node.CastType);
//	}

	@Override public void VisitFunctionLiteralNode(ZenFunctionLiteralNode Node) {
		LibZen.TODO("FunctionNode");
	}

	@Override public void VisitErrorNode(ZenErrorNode Node) {
		String name = Type.getInternalName(SoftwareFaultException.class);
		this.CurrentVisitor.SetLineNumber(Node);
		this.CurrentVisitor.AsmVisitor.visitTypeInsn(NEW, name);
		this.CurrentVisitor.AsmVisitor.visitInsn(DUP);
		this.CurrentVisitor.LoadConst(Node.Token.GetErrorMessage());
		this.CurrentVisitor.AsmVisitor.visitMethodInsn(INVOKESPECIAL, name, "<init>", "(Ljava/lang/Object;)V");
		this.CurrentVisitor.AsmVisitor.visitInsn(ATHROW);
	}

	@Override public void VisitCommandNode(ZenCommandNode Node) {
		ArrayList<ArrayList<ZenNode>> Args = new ArrayList<ArrayList<ZenNode>>();
		ZenCommandNode node = Node;
		while(node != null) {
			Args.add(node.ArgumentList);
			node = (ZenCommandNode) node.PipedNextNode;
		}
		// new String[][n]
		this.CurrentVisitor.AsmVisitor.visitLdcInsn(Args.size());
		this.CurrentVisitor.AsmVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(String[].class));
		for(int i=0; i<Args.size(); i++) {
			// new String[m];
			ArrayList<ZenNode> Arg = Args.get(i);
			this.CurrentVisitor.AsmVisitor.visitInsn(DUP);
			this.CurrentVisitor.AsmVisitor.visitLdcInsn(i);
			this.CurrentVisitor.AsmVisitor.visitLdcInsn(Arg.size());
			this.CurrentVisitor.AsmVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(String.class));
			for(int j=0; j<Arg.size(); j++) {
				this.CurrentVisitor.AsmVisitor.visitInsn(DUP);
				this.CurrentVisitor.AsmVisitor.visitLdcInsn(j);
				Arg.get(j).Accept(this);
				this.CurrentVisitor.AsmVisitor.visitInsn(AASTORE);
			}
			this.CurrentVisitor.AsmVisitor.visitInsn(AASTORE);
		}
		if(Node.Type.IsBooleanType()) {
			this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.ExecCommandBool);
		}
		else if(Node.Type.IsStringType()) {
			this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.ExecCommandString);
		}
		else if(LibZen.EqualsString(Node.Type.toString(), "Task")) {
			this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.ExecCommandTask);
		}
		else {
			this.CurrentVisitor.InvokeMethodCall(Node.Type, JLib.ExecCommandVoid);
		}
		this.PushStack(Node.Type);
	}

	@Override public void InvokeMainFunc(String MainFuncName) {
		try {
			Class<?> MainClass = Class.forName(JLib.GetHolderClassName(this.RootNameSpace, MainFuncName), false, this.ClassGenerator);
			Method m = MainClass.getMethod(MainFuncName);
			if(m != null) {
				m.invoke(null);
			}
		} catch(ClassNotFoundException e) {
			LibZen.VerboseException(e);
		} catch(InvocationTargetException e) {
			LibZen.VerboseException(e);
		} catch(IllegalAccessException e) {
			LibZen.VerboseException(e);
		} catch(NoSuchMethodException e) {
			LibZen.VerboseException(e);
		}
	}
}

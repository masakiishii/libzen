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
package zen.parser;

import zen.ast.ZenNode;
import zen.deps.Field;
import zen.lang.ZSystem;
import zen.lang.ZType;
import zen.lang.ZenTypeChecker;
import zen.lang.ZenTypeInfer;
//endif VAJA

public abstract class ZGenerator extends ZVisitor {
	@Field private String            GrammarInfo;
	@Field public final String       TargetCode;
	@Field public final String       TargetVersion;

	@Field public final ZNameSpace  RootNameSpace;
	@Field public String             OutputFile;
	@Field public ZLogger          Logger;

	@Field public ZenTypeChecker TypeChecker;
	@Field private boolean StoppedVisitor;

	protected ZGenerator(String TargetCode, String TargetVersion) {
		super();
		this.RootNameSpace = new ZNameSpace(this, null);
		this.GrammarInfo = "";
		this.TargetCode = TargetCode;
		this.TargetVersion = TargetVersion;

		this.OutputFile = null;
		this.Logger = new ZLogger();
		this.TypeChecker = new ZenTypeInfer(this.Logger);
		this.StoppedVisitor = false;

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

	public String GetGrammarInfo() {
		return this.GrammarInfo.trim();
	}

	public void SetGrammarInfo(String GrammarInfo) {
		this.GrammarInfo = this.GrammarInfo + GrammarInfo + " ";
	}

	public String GetTargetLangInfo() {
		return this.TargetCode + this.TargetVersion;
	}


	public final String ReportError(int Level, ZToken Token, String Message) {
		return this.Logger.Report(Level, Token, Message);
	}

	public void DoCodeGeneration(ZNameSpace NameSpace, ZenNode Node) {
		Node.Accept(this);
	}

	public Object EvalTopLevelNode(ZenNode TopLevelNode) {
		return null;
	}

	public ZType GetFieldType(ZType BaseType, String Name) {
		return ZSystem.VarType;     // undefined
	}

	public ZType GetSetterType(ZType BaseType, String Name) {
		return ZSystem.VarType;     // undefined
		//return ZenSystem.VoidType;   // readonly
	}



}

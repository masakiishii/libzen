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

package zen.ast;

import zen.parser.ZVisitor;
import zen.type.ZClassType;
import zen.type.ZType;
import zen.util.Field;
import zen.util.Var;

public final class ZClassNode extends ZListNode {
	public static final int _NameInfo = 0;
	public static final int _TypeInfo = 1;

	@Field public String  GivenName = null;
	@Field public ZClassType ClassType = null;
	@Field public boolean IsExport = false;

	public ZClassNode(ZNode ParentNode) {
		super(ParentNode, null, 2);
	}

	public final String ClassName() {
		if(this.GivenName == null) {
			this.GivenName = this.AST[ZParamNode._NameInfo].SourceToken.GetTextAsName();
		}
		return this.GivenName;
	}

	public final ZType SuperType() {
		if(this.AST[ZParamNode._TypeInfo] != null) {
			return this.AST[ZParamNode._TypeInfo].Type;
		}
		else {
			return ZClassType._ObjectType;
		}
	}

	public final ZFieldNode GetFieldNode(int Index) {
		@Var ZNode Node = this.GetListAt(Index);
		if(Node instanceof ZFieldNode) {
			return (ZFieldNode)Node;
		}
		return null;
	}

	@Override public void Accept(ZVisitor Visitor) {
		Visitor.VisitClassNode(this);
	}
}
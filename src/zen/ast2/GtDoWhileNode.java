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

package zen.ast2;

<<<<<<< HEAD:src/zen/ast2/GtDoWhileNode.java
import zen.ast.GtNode;
import zen.parser.GtGenerator;
import zen.parser.GtToken;
import zen.parser.GtType;
=======
import parser.GtNodeVisitor;
import parser.GtToken;
import parser.GtType;
import parser.ast.GtNode;
>>>>>>> e755b72769721359763b8610626c7340818b7aa2:src/parser/ast2/GtDoWhileNode.java

final public class GtDoWhileNode extends GtNode {
	/*field*/public GtNode	CondNode;
	/*field*/public GtNode	BodyNode;
	public GtDoWhileNode/*constructor*/(GtType Type, GtToken Token, GtNode CondNode, GtNode BodyNode) {
		super(Type, Token);
		this.CondNode = CondNode;
		this.BodyNode = BodyNode;
		this.SetChild2(CondNode, BodyNode);
	}
	@Override public void Accept(GtNodeVisitor Visitor) {
		Visitor.VisitDoWhileNode(this);
	}
	public GtNode ToWhileNode() {
		/**
		while(true) {
			$BodyNode;
			break;
		}
		while($CondNode) {
			$BodyNode;
		}
		**/
		return null;
	}
}
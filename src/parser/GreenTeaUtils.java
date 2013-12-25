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

package parser;

import java.util.ArrayList;

import parser.ast.GtNode;
import parser.deps.LibGreenTea;
import parser.deps.LibNative;

public class GreenTeaUtils implements GreenTeaConsts {
//endif VAJA
/*GreenTeaUtils Begin*/
	public final static boolean IsFlag(int flag, int flag2) {
		return ((flag & flag2) == flag2);
	}

	public final static int UnsetFlag(int flag, int flag2) {
		return (flag & (~flag2));
	}

	public final static String JoinStrings(String Unit, int Times) {
		/*local*/String s = "";
		/*local*/int i = 0;
		while(i < Times) {
			s = s + Unit;
			i = i + 1;
		}
		return s;
	}

	public final static int AsciiToTokenMatrixIndex(char c) {
		if(c < 128) {
			return CharMatrix[c];
		}
		return UnicodeChar;
	}

	private final static String n2s(int n) {
		if(n < (27)) {
			return LibGreenTea.CharToString((/*cast*/char)(65 + (n - 0)));
		}
		else if(n < (27 + 10)) {
			return LibGreenTea.CharToString((/*cast*/char)(48 + (n - 27)));
		}
		else {
			return LibGreenTea.CharToString((/*cast*/char)(97 + (n - 37)));
		}
	}

	public final static String NumberToAscii(int number) {
		if(number >= 3600) {
			return n2s(number / 3600) + NumberToAscii(number % 3600);
		}
		return n2s((number / 60)) + n2s((number % 60));
	}

	public final static String NativeVariableName(String Name, int Index) {
		return Name + NativeNameSuffix + Index;
	}

	public final static String MangleGenericType(GtType BaseType, int BaseIdx, ArrayList<GtType> TypeList) {
		/*local*/String s = BaseType.ShortName + NativeNameSuffix;
		/*local*/int i = BaseIdx;
		while(i < LibGreenTea.ListSize(TypeList)) {
			/*local*/GtType Type = TypeList.get(i);
			if(Type.IsTypeVariable()) {
				s = s + Type.ShortName;
			}
			else {
				s = s + NumberToAscii(TypeList.get(i).TypeId);
			}
			i = i + 1;
		}
		return s;
	}

	public final static int ApplyTokenFunc(GtTokenFunc TokenFunc, GtTokenContext TokenContext, String ScriptSource, int Pos) {
		while(TokenFunc != null) {
			/*local*/int NextIdx = (/*cast*/int)LibNative.ApplyTokenFunc(TokenFunc.Func, TokenContext, ScriptSource, Pos);
			if(NextIdx > Pos) return NextIdx;
			TokenFunc = TokenFunc.ParentFunc;
		}
		return MismatchedPosition;
	}

	public final static GtSyntaxPattern MergeSyntaxPattern(GtSyntaxPattern Pattern, GtSyntaxPattern Parent) {
		if(Parent == null) return Pattern;
		/*local*/GtSyntaxPattern MergedPattern = new GtSyntaxPattern(Pattern.PackageNameSpace, Pattern.PatternName, Pattern.MatchFunc);
		MergedPattern.ParentPattern = Parent;
		return MergedPattern;
	}

//	public final static boolean IsMismatchedOrError(GtSyntaxTree Tree) {
//		return (Tree == null || Tree.IsMismatchedOrError());
//	}
//
//	public final static boolean IsValidSyntax(GtSyntaxTree Tree) {
//		return !(GreenTeaUtils.IsMismatchedOrError(Tree));
//	}
//
//	public final static GtSyntaxTree TreeHead(GtSyntaxTree Tree) {
//		if(Tree != null) {
//			while(Tree.PrevTree != null) {
//				Tree = Tree.PrevTree;
//			}
//		}
//		return Tree;
//	}
//
//	public final static GtSyntaxTree TreeTail(GtSyntaxTree Tree) {
//		if(Tree != null) {
//			while(Tree.NextTree != null) {
//				Tree = Tree.NextTree;
//			}
//		}
//		return Tree;
//	}
//
//	public final static GtSyntaxTree LinkTree(GtSyntaxTree LastNode, GtSyntaxTree Node) {
//		Node.PrevTree = LastNode;
//		if(LastNode != null) {
//			LastNode.NextTree = Node;
//		}
//		return GreenTeaUtils.TreeTail(Node);
//	}

	public final static GtSyntaxTree ApplySyntaxPattern_OLD(GtNameSpace NameSpace, GtTokenContext TokenContext, GtSyntaxTree LeftTree, GtSyntaxPattern Pattern) {
		/*local*/int Pos = TokenContext.GetPosition(0);
		/*local*/int ParseFlag = TokenContext.ParseFlag;
		/*local*/GtSyntaxPattern CurrentPattern = Pattern;
		while(CurrentPattern != null) {
			/*local*/GtFunc delegate = CurrentPattern.MatchFunc;
			TokenContext.RollbackPosition(Pos, 0);
			if(CurrentPattern.ParentPattern != null) {   // This means it has next patterns
				TokenContext.ParseFlag = ParseFlag | BackTrackParseFlag;
			}
			//LibGreenTea.DebugP("B :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + ", next=" + CurrentPattern.ParentPattern);
			TokenContext.IndentLevel += 1;
			/*local*/GtSyntaxTree ParsedTree = LibNative.ApplyParseFunc(delegate, NameSpace, TokenContext, LeftTree, CurrentPattern);
			TokenContext.IndentLevel -= 1;
			TokenContext.ParseFlag = ParseFlag;
			if(ParsedTree != null && ParsedTree.IsMismatched()) {
				ParsedTree = null;
			}
			//LibGreenTea.DebugP("E :" + JoinStrings("  ", TokenContext.IndentLevel) + CurrentPattern + " => " + ParsedTree);
			if(ParsedTree != null) {
				return ParsedTree;
			}
			CurrentPattern = CurrentPattern.ParentPattern;
		}
		if(TokenContext.IsAllowedBackTrack()) {
			TokenContext.RollbackPosition(Pos, 0);
		}
		else {
			TokenContext.SkipErrorStatement();
		}
		if(Pattern == null) {
			LibGreenTea.VerboseLog(VerboseUndefined, "undefined syntax pattern: " + Pattern);
		}
		return TokenContext.ReportExpectedPattern_OLD(Pattern);
	}


	// typing
	public final static GtNode ApplyTypeFunc(GtFunc TypeFunc, GtTypeEnv Gamma, GtSyntaxTree ParsedTree, GtType Type) {
		if(TypeFunc != null) {
			Gamma.NameSpace = ParsedTree.NameSpace;
			return LibNative.ApplyTypeFunc(TypeFunc, Gamma, ParsedTree, Type);
		}
		return Gamma.Generator.CreateEmptyNode(GtStaticTable.VoidType);
	}

	public final static GtNode LinkNode(GtNode LastNode, GtNode Node) {
		Node.PrevNode = LastNode;
		if(LastNode != null) {
			LastNode.NextNode = Node;
		}
		return Node;
	}

/*GreenTeaUtils End*/
//ifdef JAVA
	
}
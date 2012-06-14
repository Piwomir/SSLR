/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.sonar.sslr.symboltable;

import com.google.common.base.Predicates;
import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.symboltable.LocalScope;
import com.sonar.sslr.api.symboltable.Scope;
import com.sonar.sslr.api.symboltable.Symbol;
import com.sonar.sslr.api.symboltable.SymbolTableBuilder;
import com.sonar.sslr.api.symboltable.SymbolTableBuilderContext;
import com.sonar.sslr.api.symboltable.SymbolTableBuilderVisitor;
import com.sonar.sslr.test.miniC.MiniCGrammar;

public class MiniCSymbolTableBuilder {

  private final SymbolTableBuilder builder = new SymbolTableBuilder();

  public MiniCSymbolTableBuilder(MiniCGrammar grammar) {
    builder.addToFirstPhase(new SymbolTableBuilderVisitor(grammar.compilationUnit) {
      @Override
      public void visitNode(SymbolTableBuilderContext symbolTableBuilderContext, AstNode astNode) {
        Scope currentScope = symbolTableBuilderContext.getEnclosingScope(astNode);

        Scope newCurrentScope = new LocalScope(currentScope);
        symbolTableBuilderContext.define(astNode, newCurrentScope);
      }
    });
    builder.addToFirstPhase(new SymbolTableBuilderVisitor(grammar.compoundStatement) {
      @Override
      public void visitNode(SymbolTableBuilderContext symbolTableBuilderContext, AstNode astNode) {
        Scope currentScope = symbolTableBuilderContext.getEnclosingScope(astNode);

        Scope newCurrentScope = new LocalScope(currentScope);
        symbolTableBuilderContext.define(astNode, newCurrentScope);
      }
    });
    builder.addToFirstPhase(new SymbolTableBuilderVisitor(grammar.structDefinition) {
      @Override
      public void visitNode(SymbolTableBuilderContext symbolTableBuilderContext, AstNode astNode) {
        String name = astNode.getChild(1).getTokenValue();
        Scope currentScope = symbolTableBuilderContext.getEnclosingScope(astNode);

        StructSymbol structSymbol = new StructSymbol(currentScope, name, astNode);
        symbolTableBuilderContext.define(structSymbol);

        Scope newCurrentScope = new LocalScope(currentScope);
        symbolTableBuilderContext.define(astNode, newCurrentScope);
      }
    });
    builder.addToFirstPhase(new SymbolTableBuilderVisitor(grammar.functionDefinition) {
      @Override
      public void visitNode(SymbolTableBuilderContext symbolTableBuilderContext, AstNode astNode) {
        String name = astNode.getChild(1).getTokenValue();
        Scope currentScope = symbolTableBuilderContext.getEnclosingScope(astNode);

        FunctionSymbol functionSymbol = new FunctionSymbol(currentScope, name, astNode);
        symbolTableBuilderContext.define(functionSymbol);

        Scope newCurrentScope = new LocalScope(currentScope);
        symbolTableBuilderContext.define(astNode, newCurrentScope);
      }
    });
    builder.addToFirstPhase(new SymbolTableBuilderVisitor(grammar.variableDefinition, grammar.parameterDeclaration, grammar.structMember) {
      @Override
      public void visitNode(SymbolTableBuilderContext symbolTableBuilderContext, AstNode astNode) {
        String name = astNode.getChild(1).getTokenValue();
        Scope currentScope = symbolTableBuilderContext.getEnclosingScope(astNode);

        VariableSymbol variableSymbol = new VariableSymbol(currentScope, name, astNode);
        symbolTableBuilderContext.define(variableSymbol);
      }
    });

    builder.addToSecondPhase(new SymbolTableBuilderVisitor(grammar.binVariableReference) {
      @Override
      public void visitNode(SymbolTableBuilderContext symbolTableBuilderContext, AstNode astNode) {
        Scope enclosingScope = symbolTableBuilderContext.getEnclosingScope(astNode);
        String referencedName = astNode.getTokenValue();
        Symbol referencedSymbol = enclosingScope.resolve(referencedName, Predicates.instanceOf(VariableSymbol.class));
        symbolTableBuilderContext.addReference(astNode, referencedSymbol);
      }
    });
  }

  public SymbolTableBuilderContext buildSymbolTable(AstNode astNode) {
    return builder.buildSymbolTable(astNode);
  }

}
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
package org.sonar.sslr.internal.vm.lexerful;

import com.google.common.collect.ImmutableSet;
import com.sonar.sslr.api.TokenType;
import org.sonar.sslr.internal.matchers.Matcher;
import org.sonar.sslr.internal.vm.Machine;
import org.sonar.sslr.internal.vm.NativeExpression;

import java.util.Set;

/**
 * TODO Replacement for {@link com.sonar.sslr.impl.matcher.TokenTypesMatcher}
 */
public class TokenTypesExpression extends NativeExpression implements Matcher {

  private final Set<TokenType> types;

  public TokenTypesExpression(TokenType... types) {
    this.types = ImmutableSet.copyOf(types);
  }

  @Override
  public void execute(Machine machine) {
    if (machine.length() == 0 || !types.contains(machine.tokenAt(0).getType())) {
      machine.backtrack();
      return;
    }
    machine.createLeafNode(this, 1);
    machine.jump(1);
  }

  @Override
  public String toString() {
    return "TokenTypes " + types;
  }

}

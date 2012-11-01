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
package org.sonar.sslr.matchers;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.impl.Parser;
import com.sonar.sslr.impl.ParsingState;
import com.sonar.sslr.impl.matcher.RuleDefinition;
import org.apache.commons.io.IOUtils;
import org.sonar.sslr.internal.matchers.AstCreator;
import org.sonar.sslr.internal.matchers.InputBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Adapts {@link ParseRunner} to be used as {@link Parser}.
 *
 * <p>This class is not intended to be sub-classed by clients.</p>
 *
 * @since 2.0
 */
public class ParserAdapter<G extends LexerlessGrammar> extends Parser<G> {

  private final Charset charset;
  private final ParseRunner parseRunner;

  public ParserAdapter(Charset charset, G grammar) {
    super(grammar);
    this.charset = charset;
    this.parseRunner = new ParseRunner(grammar.getRootRule());
  }

  /**
   * @return constructed AST
   * @throws RecognitionException if unable to parse
   */
  @Override
  public AstNode parse(String source) {
    URI uri;
    try {
      uri = new URI("tests://unittest");
    } catch (URISyntaxException e) {
      // Can't happen
      throw new IllegalStateException(e);
    }
    return parse(uri, source.toCharArray());
  }

  /**
   * @return constructed AST
   * @throws RecognitionException if unable to parse
   */
  @Override
  public AstNode parse(File file) {
    return parse(file.toURI(), fileToCharArray(file, charset));
  }

  private static char[] fileToCharArray(File file, Charset charset) {
    FileInputStream is = null;
    try {
      is = new FileInputStream(file);
      return IOUtils.toCharArray(is, charset.name());
    } catch (IOException e) {
      throw new RecognitionException(0, e.getMessage(), e);
    } finally {
      IOUtils.closeQuietly(is);
    }
  }

  private AstNode parse(URI uri, char[] input) {
    ParsingResult result = parseRunner.parse(input);
    if (result.isMatched()) {
      return AstCreator.create(uri, input, result.getParseTreeRoot());
    } else {
      ParseError parseError = result.getParseError();
      InputBuffer inputBuffer = parseError.getInputBuffer();
      int line = inputBuffer.getPosition(parseError.getErrorIndex()).getLine();
      String message = new ParseErrorFormatter().format(parseError);
      throw new RecognitionException(line, message);
    }
  }

  @Override
  public AstNode parse(List<Token> tokens) {
    throw new UnsupportedOperationException();
  }

  @Override
  public RuleDefinition getRootRule() {
    throw new UnsupportedOperationException();
  }

  @Override
  public ParsingState getParsingState() {
    throw new UnsupportedOperationException();
  }

}
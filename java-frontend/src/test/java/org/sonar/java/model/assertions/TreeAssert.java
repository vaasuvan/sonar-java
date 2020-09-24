/*
 * SonarQube Java
 * Copyright (C) 2012-2020 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.model.assertions;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.Assertions;
import org.sonar.java.model.JavaTree;
import org.sonar.plugins.java.api.tree.ListTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.SyntaxToken;
import org.sonar.plugins.java.api.tree.Tree;

public class TreeAssert extends AbstractAssert<TreeAssert, Tree> {

  private TreeAssert(Tree actual) {
    super(actual, TreeAssert.class);
  }

  public static TreeAssert assertThat(Tree actual) {
    return new TreeAssert(actual);
  }

  public static LiteralTreeAssert assertThat(LiteralTree actual) {
    return new LiteralTreeAssert(actual);
  }

  public static ListTreeAssert assertThat(ListTree<?> actual) {
    return new ListTreeAssert(actual);
  }

  public static SyntaxTokenAssert assertThat(SyntaxToken actual) {
    return new SyntaxTokenAssert(actual);
  }

  public TreeAssert is(Tree.Kind kind) {
    Assertions.assertThat(actual.is(kind)).isTrue();
    return this;
  }

  public TreeAssert isNot(Tree.Kind kind) {
    Assertions.assertThat(actual.is(kind)).isFalse();
    return this;
  }

  public TreeAssert hasChildrenSize(int size) {
    Assertions.assertThat(((JavaTree) actual).getChildren()).hasSize(size);
    return this;
  }

  public static class LiteralTreeAssert extends AbstractAssert<LiteralTreeAssert, LiteralTree> {
    private LiteralTreeAssert(LiteralTree actual) {
      super(actual, LiteralTreeAssert.class);
    }

    public static LiteralTreeAssert assertThat(LiteralTree actual) {
      return new LiteralTreeAssert(actual);
    }

    public LiteralTreeAssert hasValue(String value) {
      Assertions.assertThat(actual.value()).isEqualTo(value);
      return this;
    }

    public LiteralTreeAssert is(Tree.Kind kind) {
      TreeAssert.assertThat((Tree) actual).is(kind);
      return this;
    }

    public LiteralTreeAssert hasChildrenSize(int size) {
      TreeAssert.assertThat((Tree) actual).hasChildrenSize(size);
      return this;
    }
  }

  public static class ListTreeAssert extends AbstractListAssert<ListTreeAssert, ListTree<?>, Tree, TreeAssert> {

    private ListTreeAssert(ListTree<?> actual) {
      super(actual, ListTreeAssert.class);
    }

    public ListTreeAssert is(Tree.Kind kind) {
      Assertions.assertThat(actual.is(kind)).isTrue();
      return this;
    }

    public ListTreeAssert hasSeparatorsSize(int size) {
      Assertions.assertThat(actual.separators()).hasSize(size);
      return this;
    }

    public ListTreeAssert hasChildrenSize(int size) {
      TreeAssert.assertThat((Tree) actual).hasChildrenSize(size);
      return this;
    }

    @Override
    protected TreeAssert toAssert(Tree value, String description) {
      return TreeAssert.assertThat(value);
    }

    @Override
    protected ListTreeAssert newAbstractIterableAssert(Iterable<? extends Tree> iterable) {
      throw new IllegalStateException("Not implemented");
    }
  }

  public static class SyntaxTokenAssert extends AbstractAssert<SyntaxTokenAssert, SyntaxToken> {

    private SyntaxTokenAssert(SyntaxToken actual) {
      super(actual, SyntaxTokenAssert.class);
    }

    public SyntaxTokenAssert is(String expected) {
      Assertions.assertThat(actual.text()).isEqualTo(expected);
      return this;
    }

    public SyntaxTokenAssert isAtLine(int line) {
      Assertions.assertThat(actual.line()).isEqualTo(line);
      return this;
    }

    public SyntaxTokenAssert startsAtColumn(int column) {
      Assertions.assertThat(actual.column()).isEqualTo(column);
      return this;
    }
  }

}

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
package org.sonar.java.model;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sonar.java.ast.parser.TypeParameterListTreeImpl;
import org.sonar.plugins.java.api.tree.AnnotationTree;
import org.sonar.plugins.java.api.tree.ArrayAccessExpressionTree;
import org.sonar.plugins.java.api.tree.ArrayDimensionTree;
import org.sonar.plugins.java.api.tree.ArrayTypeTree;
import org.sonar.plugins.java.api.tree.AssertStatementTree;
import org.sonar.plugins.java.api.tree.AssignmentExpressionTree;
import org.sonar.plugins.java.api.tree.BinaryExpressionTree;
import org.sonar.plugins.java.api.tree.BlockTree;
import org.sonar.plugins.java.api.tree.BreakStatementTree;
import org.sonar.plugins.java.api.tree.CaseGroupTree;
import org.sonar.plugins.java.api.tree.CaseLabelTree;
import org.sonar.plugins.java.api.tree.CatchTree;
import org.sonar.plugins.java.api.tree.ClassTree;
import org.sonar.plugins.java.api.tree.CompilationUnitTree;
import org.sonar.plugins.java.api.tree.ConditionalExpressionTree;
import org.sonar.plugins.java.api.tree.ContinueStatementTree;
import org.sonar.plugins.java.api.tree.DoWhileStatementTree;
import org.sonar.plugins.java.api.tree.EmptyStatementTree;
import org.sonar.plugins.java.api.tree.EnumConstantTree;
import org.sonar.plugins.java.api.tree.ExpressionStatementTree;
import org.sonar.plugins.java.api.tree.ExpressionTree;
import org.sonar.plugins.java.api.tree.ForEachStatement;
import org.sonar.plugins.java.api.tree.ForStatementTree;
import org.sonar.plugins.java.api.tree.IdentifierTree;
import org.sonar.plugins.java.api.tree.IfStatementTree;
import org.sonar.plugins.java.api.tree.ImportClauseTree;
import org.sonar.plugins.java.api.tree.ImportTree;
import org.sonar.plugins.java.api.tree.InstanceOfTree;
import org.sonar.plugins.java.api.tree.LabeledStatementTree;
import org.sonar.plugins.java.api.tree.LambdaExpressionTree;
import org.sonar.plugins.java.api.tree.LiteralTree;
import org.sonar.plugins.java.api.tree.MemberSelectExpressionTree;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.MethodReferenceTree;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Modifier;
import org.sonar.plugins.java.api.tree.ModifierKeywordTree;
import org.sonar.plugins.java.api.tree.NewArrayTree;
import org.sonar.plugins.java.api.tree.NewClassTree;
import org.sonar.plugins.java.api.tree.PackageDeclarationTree;
import org.sonar.plugins.java.api.tree.ParameterizedTypeTree;
import org.sonar.plugins.java.api.tree.ParenthesizedTree;
import org.sonar.plugins.java.api.tree.PrimitiveTypeTree;
import org.sonar.plugins.java.api.tree.ReturnStatementTree;
import org.sonar.plugins.java.api.tree.StatementTree;
import org.sonar.plugins.java.api.tree.StaticInitializerTree;
import org.sonar.plugins.java.api.tree.SwitchExpressionTree;
import org.sonar.plugins.java.api.tree.SwitchStatementTree;
import org.sonar.plugins.java.api.tree.SynchronizedStatementTree;
import org.sonar.plugins.java.api.tree.ThrowStatementTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.TryStatementTree;
import org.sonar.plugins.java.api.tree.TypeArguments;
import org.sonar.plugins.java.api.tree.TypeCastTree;
import org.sonar.plugins.java.api.tree.TypeParameterTree;
import org.sonar.plugins.java.api.tree.TypeParameters;
import org.sonar.plugins.java.api.tree.TypeTree;
import org.sonar.plugins.java.api.tree.UnaryExpressionTree;
import org.sonar.plugins.java.api.tree.UnionTypeTree;
import org.sonar.plugins.java.api.tree.VariableTree;
import org.sonar.plugins.java.api.tree.WhileStatementTree;
import org.sonar.plugins.java.api.tree.WildcardTree;
import org.sonar.plugins.java.api.tree.YieldStatementTree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.java.model.assertions.TreeAssert.assertThat;

class JavaTreeModelTest {

  @Test
  void line_of_tree() {
    CompilationUnitTree empty = compilationUnit("");
    assertThat(((JavaTree) empty).getLine()).isEqualTo(1);
    ClassTree classTree = firstType("class A {}");
    assertThat(((JavaTree) classTree).getLine()).isEqualTo(1);
    assertThat(((JavaTree) classTree.modifiers()).getLine()).isEqualTo(-1);
  }

  @Test
  void basic_type() {
    PrimitiveTypeTree tree = (PrimitiveTypeTree) ((MethodTree) firstTypeMember("class T { int m() { return null; } }")).returnType();
    assertThat(tree.keyword()).is("int");
    assertThat(tree).hasChildrenSize(1);

    tree = (PrimitiveTypeTree) ((MethodTree) firstTypeMember("class T { void m() { return null; } }")).returnType();
    assertThat(tree.keyword()).is("void");
    assertThat(tree).hasChildrenSize(1);
  }

  @Test
  void type() {
    ArrayTypeTree tree = (ArrayTypeTree) ((MethodTree) firstTypeMember("class T { int[] m() { return null; } }")).returnType();
    assertThat(tree.type()).isInstanceOf(PrimitiveTypeTree.class);
  }

  @Nested
  class Literals {

    @Test
    void int_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { int m() { return 1; } }");
      assertThat(tree)
        .is(Tree.Kind.INT_LITERAL)
        .hasChildrenSize(1)
        .hasValue("1");
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(27);
    }

    @Test
    void long_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { long m() { return 1L; } }");
      assertThat(tree)
        .is(Tree.Kind.LONG_LITERAL)
        .hasChildrenSize(1)
        .hasValue("1L");
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(28);
    }

    @Test
    void float_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { float m() { return 1F; } }");
      assertThat(tree)
        .is(Tree.Kind.FLOAT_LITERAL)
        .hasValue("1F")
        .hasChildrenSize(1);
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(29);
    }

    @Test
    void double_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { double m() { return 1d; } }");
      assertThat(tree)
        .is(Tree.Kind.DOUBLE_LITERAL)
        .hasValue("1d")
        .hasChildrenSize(1);
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(30);
    }

    @Test
    void boolean_true_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { boolean m() { return true; } }");
      assertThat(tree)
        .is(Tree.Kind.BOOLEAN_LITERAL)
        .hasValue("true")
        .hasChildrenSize(1);
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(31);
    }

    @Test
    void boolean_false_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { boolean m() { return false; } }");
      assertThat(tree)
        .is(Tree.Kind.BOOLEAN_LITERAL)
        .hasValue("false")
        .hasChildrenSize(1);
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(31);
    }

    @Test
    void char_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { char m() { return 'c'; } }");
      assertThat(tree)
        .is(Tree.Kind.CHAR_LITERAL)
        .hasValue("'c'")
        .hasChildrenSize(1);
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(28);
    }

    @Test
    void string_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { String m() { return \"s\"; } }");
      assertThat(tree)
        .is(Tree.Kind.STRING_LITERAL)
        .hasValue("\"s\"")
        .hasChildrenSize(1);
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(30);
    }

    @Test
    void null_literal() {
      LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { Object m() { return null; } }");
      assertThat(tree)
        .is(Tree.Kind.NULL_LITERAL)
        .hasValue("null")
        .hasChildrenSize(1);
      assertThat(tree.token())
        .isAtLine(1)
        .startsAtColumn(30);
    }
  }

  /**
   * Text Blocks (Preview) http://openjdk.java.net/jeps/355
   *
   * @see org.eclipse.jdt.core.dom.TextBlock
   */
  @Test
  void expression_text_block() {
    LiteralTree tree = (LiteralTree) expressionOfReturnStatement("class T { Object m() { return \"\"\"\ntext block\"\"\"; } }");
    assertThat(tree)
      .is(Tree.Kind.STRING_LITERAL)
      .hasValue("\"\"\"\ntext block\"\"\"");
    assertThat(tree.token())
      .isAtLine(1)
      .startsAtColumn(30);
  }

  /**
   * Switch Expressions http://openjdk.java.net/jeps/361
   *
   * @see org.eclipse.jdt.core.dom.SwitchExpression
   * @see org.eclipse.jdt.core.dom.YieldStatement
   */
  @Test
  void expression_switch() {
    SwitchExpressionTree tree = (SwitchExpressionTree) expressionOfReturnStatement("class T { Object m() { return switch (0) { default -> 0; case 0 -> 0; }; } }");
    assertThat(tree).is(Tree.Kind.SWITCH_EXPRESSION);
    assertThat(tree.cases()).hasSize(2);
    YieldStatementTree statement = (YieldStatementTree) tree.cases().get(0).body().get(0);
    assertThat(statement.yieldKeyword()).as("implicit yield-statement").isNull();
    assertThat(statement.expression()).is(Tree.Kind.INT_LITERAL);

    tree = (SwitchExpressionTree) expressionOfReturnStatement("class T { Object m() { return switch (0) { default: yield 0; case 0: yield 0; }; } }");
    assertThat(tree).is(Tree.Kind.SWITCH_EXPRESSION);
    assertThat(tree.cases()).hasSize(2);
    statement = (YieldStatementTree) tree.cases().get(0).body().get(0);
    assertThat(statement.yieldKeyword()).is("yield");
    assertThat(statement.expression()).is(Tree.Kind.INT_LITERAL);
  }

  @Test
  void compilation_unit() {
    CompilationUnitTree tree = compilationUnit("import foo; import bar; class Foo {} class Bar {}");
    assertThat(tree)
      .is(Tree.Kind.COMPILATION_UNIT)
      .hasChildrenSize(5);
    assertThat(tree.packageDeclaration()).isNull();
    assertThat(tree.imports()).hasSize(2);
    assertThat(tree.types()).hasSize(2);

    tree = compilationUnit("package pkg; import foo; import bar; class Foo {} class Bar {}");
    assertThat(tree)
      .is(Tree.Kind.COMPILATION_UNIT)
      .hasChildrenSize(6);
    assertThat(tree.packageDeclaration()).isNotNull();
    assertThat(tree.imports()).hasSize(2);
    assertThat(tree.types()).hasSize(2);

    tree = compilationUnit("import foo; import bar; ; class Foo {} class Bar {}");
    assertThat(tree)
      .is(Tree.Kind.COMPILATION_UNIT)
      .hasChildrenSize(6);
    assertThat(tree.packageDeclaration()).isNull();
    assertThat(tree.imports()).hasSize(3);
    assertThat(tree.imports().get(2)).is(Tree.Kind.EMPTY_STATEMENT);
    assertThat(tree.types()).hasSize(2);
  }

  @Test
  void package_declaration() {
    PackageDeclarationTree tree = JParserTestUtils.parsePackage("package myPackage;").packageDeclaration();
    assertThat(tree)
      .is(Tree.Kind.PACKAGE)
      .hasChildrenSize(3);
    assertThat(tree.annotations()).isEmpty();
    assertThat(tree.packageKeyword()).is("package");
    assertThat(tree.packageName()).is(Tree.Kind.IDENTIFIER);
    assertThat(tree.semicolonToken()).is(";");

    tree = JParserTestUtils.parsePackage("@Foo @Bar package org.myPackage;").packageDeclaration();
    assertThat(tree)
      .is(Tree.Kind.PACKAGE)
      .hasChildrenSize(5);
    assertThat(tree.annotations()).hasSize(2);
    assertThat(tree.packageKeyword()).is("package");
    assertThat(tree.packageName()).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.semicolonToken()).is(";");
  }

  @Test
  void import_declaration() {
    ImportClauseTree tree = compilationUnit(";").imports().get(0);
    assertThat(tree)
      .is(Tree.Kind.EMPTY_STATEMENT)
      .isNot(Tree.Kind.IMPORT);

    ImportTree importTree = (ImportTree) compilationUnit("import foo.Bar;").imports().get(0);
    assertThat(importTree)
      .is(Tree.Kind.IMPORT)
      .hasChildrenSize(3);
    assertThat(importTree.isStatic()).isFalse();
    assertThat(importTree.qualifiedIdentifier()).isNotNull();

    importTree = (ImportTree) compilationUnit("import foo.bar.*;").imports().get(0);
    assertThat(importTree)
      .is(Tree.Kind.IMPORT)
      .hasChildrenSize(3);
    assertThat(importTree.isStatic()).isFalse();
    assertThat(importTree.qualifiedIdentifier()).isNotNull();

    importTree = (ImportTree) compilationUnit("import static foo.Bar.method;").imports().get(0);
    assertThat(importTree)
      .is(Tree.Kind.IMPORT)
      .hasChildrenSize(4);
    assertThat(importTree.isStatic()).isTrue();
    assertThat(importTree.qualifiedIdentifier()).isNotNull();

    importTree = (ImportTree) compilationUnit("import static foo.Bar.*;").imports().get(0);
    assertThat(importTree)
      .is(Tree.Kind.IMPORT)
      .hasChildrenSize(4);
    assertThat(importTree.isStatic()).isTrue();
    assertThat(importTree.qualifiedIdentifier()).isNotNull();
  }

  /**
   * 4.5.1. Type Arguments and Wildcards
   */
  @Nested
  class TypeArgumentsAndWildcards {

    final VariableTree variableTree = (VariableTree) firstMethodFirstStatement("public class T { void m() { ClassType<? extends A, ? super B, ?, C> var; } }");
    final ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree) variableTree.type();
    final TypeArguments typeArguments = parameterizedTypeTree.typeArguments();

    @Test
    void type_arguments() {
      assertThat(variableTree)
        .hasChildrenSize(4);
      assertThat(parameterizedTypeTree)
        .hasChildrenSize(2);
      assertThat(typeArguments)
        .hasSize(4)
        .hasSeparatorsSize(3)
        .hasChildrenSize(9);
      assertThat(typeArguments.get(3))
        .isInstanceOf(IdentifierTree.class);
    }

    @Test
    void extends_wildcard() {
      WildcardTree wildcard = (WildcardTree) typeArguments.get(0);

      assertThat(wildcard)
        .is(Tree.Kind.EXTENDS_WILDCARD)
        .hasChildrenSize(3);
      assertThat(wildcard.bound()).isInstanceOf(IdentifierTree.class);
      assertThat(wildcard.queryToken()).is("?");
      assertThat(wildcard.extendsOrSuperToken()).is("extends");
    }

    @Test
    void super_wildcard() {
      WildcardTree wildcard = (WildcardTree) typeArguments.get(1);

      assertThat(wildcard)
        .is(Tree.Kind.SUPER_WILDCARD)
        .hasChildrenSize(3);
      assertThat(wildcard.bound()).isInstanceOf(IdentifierTree.class);
      assertThat(wildcard.queryToken()).is("?");
      assertThat(wildcard.extendsOrSuperToken()).is("super");
    }

    @Test
    void unbounded_wildcard() {
      WildcardTree wildcard = (WildcardTree) typeArguments.get(2);

      assertThat(wildcard)
        .is(Tree.Kind.UNBOUNDED_WILDCARD)
        .hasChildrenSize(1);
      assertThat(wildcard.bound()).isNull();
      assertThat(wildcard.queryToken()).is("?");
      assertThat(wildcard.extendsOrSuperToken()).isNull();
    }

    @Test
    void annotated_extends_wildcard() {
      VariableTree variableTree = (VariableTree) firstMethodFirstStatement("public class T { void m() { ClassType<@Foo ? extends A> var; } }");
      ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree) variableTree.type();
      assertThat(parameterizedTypeTree).hasChildrenSize(2);

      TypeArguments typeArguments = parameterizedTypeTree.typeArguments();
      assertThat(typeArguments).hasChildrenSize(3);

      WildcardTree wildcard = (WildcardTree) typeArguments.get(0);
      assertThat(wildcard)
        .is(Tree.Kind.EXTENDS_WILDCARD)
        .hasChildrenSize(4);
      assertThat(wildcard.bound()).isInstanceOf(IdentifierTree.class);
      assertThat(wildcard.queryToken()).is("?");
      assertThat(wildcard.annotations()).hasSize(1);
      assertThat(wildcard.extendsOrSuperToken()).is("extends");

    }

    @Test
    void annotated_extends_wildcard_bound() {
      VariableTree variableTree = (VariableTree) firstMethodFirstStatement("public class T { void m() { ClassType<? extends @Foo @Bar A> var; } }");
      ParameterizedTypeTree parameterizedTypeTree = (ParameterizedTypeTree) variableTree.type();
      assertThat(parameterizedTypeTree).hasChildrenSize(2);

      TypeArguments typeArguments = parameterizedTypeTree.typeArguments();
      assertThat(typeArguments).hasChildrenSize(3);

      WildcardTree wildcard = (WildcardTree) typeArguments.get(0);
      assertThat(wildcard)
        .is(Tree.Kind.EXTENDS_WILDCARD)
        .hasChildrenSize(3);
      assertThat(wildcard.bound()).isInstanceOf(IdentifierTree.class);
      assertThat(wildcard.annotations()).isEmpty();
      assertThat(wildcard.queryToken()).is("?");
      assertThat(wildcard.extendsOrSuperToken()).is("extends");
    }
  }

  /*
   * 8. Classes
   */
  @Nested
  class Classes {
    @Test
    void extended_class() {
      ClassTree tree = firstType("public class T<U> extends C implements I1, I2 { }");
      assertThat(tree).is(Tree.Kind.CLASS);
      List<ModifierKeywordTree> modifiers = tree.modifiers().modifiers();
      assertThat(modifiers).hasSize(1);
      assertThat(modifiers.get(0).modifier()).isEqualTo(Modifier.PUBLIC);
      assertThat(modifiers.get(0).keyword()).is("public");
      assertThat(tree.simpleName().name()).isEqualTo("T");
      TypeParameters typeParameters = tree.typeParameters();
      assertThat(typeParameters)
        .isNotEmpty()
        .hasEmptySeparators()
        .hasChildrenSize(3);
      assertThat(tree.openBraceToken()).is("{");
      assertThat(tree.superClass()).isNotNull();
      assertThat(tree.superInterfaces())
        .hasSize(2)
        .hasSeparatorsSize(1);
      assertThat(tree.superInterfaces().separators().get(0)).is(",");
      assertThat(tree.closeBraceToken()).is("}");
      assertThat(tree.declarationKeyword()).is("class");
    }

    @Test
    void simple_class() {
      ClassTree tree = firstType("public class T { }");
      List<ModifierKeywordTree> modifiers = tree.modifiers().modifiers();
      assertThat(modifiers).hasSize(1);
      assertThat(modifiers.get(0).modifier()).isEqualTo(Modifier.PUBLIC);
      assertThat(modifiers.get(0).keyword()).is("public");
      assertThat(tree.simpleName().name()).isEqualTo("T");
      assertThat(tree.typeParameters()).isEmpty();
      assertThat(tree.superClass()).isNull();
      assertThat(tree.superInterfaces()).isEmpty();
      assertThat(tree.declarationKeyword()).is("class");
    }

    @Test
    void parametrized_class() {
      ClassTree tree = firstType("class T<U,V> { }");
      assertThat(tree.modifiers()).isEmpty();
      assertThat(tree.simpleName().name()).isEqualTo("T");
      TypeParameters typeParameters = tree.typeParameters();
      assertThat(typeParameters)
        .hasSize(2)
        .hasSeparatorsSize(1)
        .hasChildrenSize(5);
      assertThat(tree.superClass()).isNull();
      assertThat(tree.superInterfaces()).isEmpty();
      assertThat(tree.declarationKeyword()).is("class");
    }

    @Test
    void annotated_class() {
      ClassTree tree = firstType("@Deprecated class T { }");
      assertThat(tree).is(Tree.Kind.CLASS);
      assertThat(tree.modifiers().annotations()).hasSize(1);
      assertThat(tree.declarationKeyword()).is("class");
    }
  }

  @Test
  void annotations() {
    ClassTree tree = firstType("@SuppressWarnings(\"unchecked\") class T { }");
    List<AnnotationTree> annotations = tree.modifiers().annotations();
    assertThat(annotations).hasSize(1);
    AnnotationTree annotation = annotations.get(0);
    assertThat(annotation.annotationType()).is(Tree.Kind.IDENTIFIER);
    assertThat(annotation.arguments().openParenToken()).isNotNull();
    assertThat(annotation.arguments().closeParenToken()).isNotNull();
    assertThat(annotation.arguments())
      .hasEmptySeparators()
      .hasSize(1);
    assertThat(annotation.arguments().get(0)).is(Tree.Kind.STRING_LITERAL);
    assertThat(annotation.atToken()).isNotNull();
    assertThat(annotation).hasChildrenSize(3);

    tree = firstType("@Target( ) class U {}");
    annotations = tree.modifiers().annotations();
    assertThat(annotations).hasSize(1);
    annotation = annotations.get(0);
    assertThat(annotation.arguments().openParenToken()).isNotNull();
    assertThat(annotation.arguments()).isEmpty();
    assertThat(annotation.arguments().separators()).isEmpty();
    assertThat(annotation.arguments().closeParenToken()).isNotNull();
    assertThat(annotation.atToken()).isNotNull();
    assertThat(annotation).hasChildrenSize(3);

    tree = firstType("@Target({ElementType.METHOD}) class U {}");
    annotations = tree.modifiers().annotations();
    assertThat(annotations).hasSize(1);
    annotation = annotations.get(0);
    assertThat(annotation.arguments().openParenToken()).isNotNull();
    assertThat(annotation.arguments()).hasSize(1);
    assertThat(annotation.arguments().get(0)).is(Tree.Kind.NEW_ARRAY);
    assertThat(annotation.arguments().separators()).isEmpty();
    assertThat(annotation.arguments().closeParenToken()).isNotNull();
    assertThat(annotation.atToken()).isNotNull();
    assertThat(annotation).hasChildrenSize(3);

    tree = firstType("@SuppressWarnings({\"hello\",}) class U {}");
    annotations = tree.modifiers().annotations();
    assertThat(annotations).hasSize(1);
    annotation = annotations.get(0);
    assertThat(annotation.arguments().openParenToken()).isNotNull();
    assertThat(annotation.arguments()).hasSize(1);
    assertThat(annotation.arguments().get(0)).is(Tree.Kind.NEW_ARRAY);
    NewArrayTree arg = (NewArrayTree) annotation.arguments().get(0);
    assertThat(arg.initializers()).hasSize(1);
    assertThat(arg.initializers().get(0)).is(Tree.Kind.STRING_LITERAL);
    assertThat(arg.initializers()).hasSeparatorsSize(1);
    assertThat(annotation.arguments().closeParenToken()).isNotNull();
    assertThat(annotation.atToken()).isNotNull();
    assertThat(annotation).hasChildrenSize(3);

    tree = firstType("@Target(value={ElementType.METHOD}, value2=\"toto\") class T { }");
    annotations = tree.modifiers().annotations();
    assertThat(annotations).hasSize(1);
    annotation = annotations.get(0);
    assertThat(annotation.annotationType()).is(Tree.Kind.IDENTIFIER);
    assertThat(annotation.arguments().openParenToken()).isNotNull();
    assertThat(annotation.arguments()).hasSize(2);
    assertThat(annotation.arguments()).hasSeparatorsSize(1);
    assertThat(annotation.arguments().get(0)).is(Tree.Kind.ASSIGNMENT);
    assertThat(annotation.arguments().closeParenToken()).isNotNull();
    assertThat(annotation.atToken()).isNotNull();
    assertThat(annotation).hasChildrenSize(3);

    VariableTree variable = (VariableTree) firstMethodFirstStatement("class T { private void meth() { @NonNullable String str;}}");
    assertThat(variable).hasChildrenSize(4);
    annotations = variable.modifiers().annotations();
    assertThat(annotations).hasSize(1);
    annotation = annotations.get(0);
    assertThat(annotation.annotationType()).is(Tree.Kind.IDENTIFIER);
    assertThat(annotation.atToken()).isNotNull();
    assertThat(annotation.arguments()).isEmpty();
    assertThat(annotation).hasChildrenSize(3);

    annotations = JParserTestUtils.parsePackage("@PackageLevelAnnotation package blammy;").packageDeclaration().annotations();
    assertThat(annotations).hasSize(1);
    assertThat(annotations.get(0).atToken()).isNotNull();
    assertThat(annotation.arguments()).isEmpty();
    assertThat(annotation).hasChildrenSize(3);

    variable = (VariableTree) firstMethodFirstStatement("class T { private void m() { @Foo Integer foo; } }");
    assertThat(variable.modifiers().annotations()).hasSize(1);
    assertThat(variable.type()).is(Tree.Kind.IDENTIFIER);
    assertThat(variable.type().annotations()).isEmpty();
    assertThat(variable).hasChildrenSize(4);

    variable = (VariableTree) firstMethodFirstStatement("class T { private void m() { @Foo java.lang.Integer foo; } }");
    assertThat(variable.modifiers().annotations()).hasSize(1);
    assertThat(variable.type()).is(Tree.Kind.MEMBER_SELECT);
    assertThat(variable.type().annotations()).isEmpty();
    assertThat(variable).hasChildrenSize(4);

    variable = (VariableTree) firstMethodFirstStatement("class T { private void m() { java.lang.@Foo Integer foo; } }");
    assertThat(variable.modifiers()).isEmpty();
    assertThat(variable.type()).is(Tree.Kind.MEMBER_SELECT);
    assertThat(variable.type().annotations()).isEmpty();
    assertThat(variable).hasChildrenSize(4);
    assertThat(((MemberSelectExpressionTree) variable.type()).identifier().annotations()).hasSize(1);

    variable = (VariableTree) firstMethodFirstStatement("class T { private void m() { a.B.C foo = a.B.new @Foo C(); } }");
    assertThat(variable.modifiers()).isEmpty();
    assertThat(variable).hasChildrenSize(6);
    TypeTree type = ((NewClassTree) variable.initializer()).identifier();
    assertThat(type).is(Tree.Kind.IDENTIFIER);
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    variable = (VariableTree) firstMethodFirstStatement("class T { private void m() { int[] foo = new @Foo int[42]; } }");
    assertThat(variable.modifiers()).isEmpty();
    assertThat(variable).hasChildrenSize(6);
    type = ((NewArrayTree) variable.initializer()).type();
    assertThat(type).is(Tree.Kind.PRIMITIVE_TYPE);
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    variable = ((TryStatementTree) firstMethodFirstStatement("class T { private void m() { try{ } catch (@Foo E1 | E2 e) {}; } }")).catches().get(0).parameter();
    assertThat(variable.modifiers()).hasSize(1);
    assertThat(variable).hasChildrenSize(3);
    type = variable.type();
    assertThat(type).is(Tree.Kind.UNION_TYPE);
    assertThat(type.annotations()).isEmpty();
    assertThat(((UnionTypeTree) type).typeAlternatives()).hasSeparatorsSize(1);
    assertThat(type).hasChildrenSize(1);

    variable = (VariableTree) firstMethodFirstStatement("class T { private void m() { a.b.C<Integer> foo = a.B.new @Foo C<Integer>(); }}");
    assertThat(variable.modifiers()).isEmpty();
    assertThat(variable).hasChildrenSize(6);
    type = ((NewClassTree) variable.initializer()).identifier();
    assertThat(type).is(Tree.Kind.PARAMETERIZED_TYPE);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(2);
    type = ((ParameterizedTypeTree) type).type();
    assertThat(type).is(Tree.Kind.IDENTIFIER);
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    variable = (VariableTree) firstMethodFirstStatement("package a; class B { private void m() { a.B.C foo = new a.B. @Foo C(); } class C {}}");
    assertThat(variable.modifiers()).isEmpty();
    assertThat(variable).hasChildrenSize(6);
    type = ((NewClassTree) variable.initializer()).identifier();
    assertThat(type).is(Tree.Kind.MEMBER_SELECT);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(3);
    type = ((MemberSelectExpressionTree) type).identifier();
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    variable = (VariableTree) firstMethodFirstStatement("package a.b; class C<T> { private void m() { a.b.C<Integer> foo = new a.b. @Foo C<Integer>(); } }");
    assertThat(variable.modifiers()).isEmpty();
    assertThat(variable).hasChildrenSize(6);
    type = ((NewClassTree) variable.initializer()).identifier();
    assertThat(type).is(Tree.Kind.PARAMETERIZED_TYPE);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(2);
    type = ((ParameterizedTypeTree) type).type();
    assertThat(type).is(Tree.Kind.MEMBER_SELECT);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(3);
    type = ((MemberSelectExpressionTree) type).identifier();
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    ClassTree classTree = firstType("class T extends a.b.@Foo C {}");
    assertThat(classTree.modifiers()).isEmpty();
    assertThat(classTree).hasChildrenSize(9);
    type = classTree.superClass();
    assertThat(type).is(Tree.Kind.MEMBER_SELECT);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(3);
    type = ((MemberSelectExpressionTree) type).identifier();
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    classTree = firstType("class T extends a.b. @Foo C<Integer> {}");
    assertThat(classTree.modifiers()).isEmpty();
    assertThat(classTree).hasChildrenSize(9);
    type = classTree.superClass();
    assertThat(type).is(Tree.Kind.PARAMETERIZED_TYPE);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(2);
    type = ((ParameterizedTypeTree) type).type();
    assertThat(type).is(Tree.Kind.MEMBER_SELECT);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(3);
    type = ((MemberSelectExpressionTree) type).identifier();
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    classTree = (ClassTree) firstMethodFirstStatement("class MyClass<A, B, C> { void foo() { class MyOtherClass extends @Foo MyClass<A, B, C>.MyInnerClass {} } class MyInnerClass {}}");
    assertThat(classTree.modifiers()).isEmpty();
    assertThat(classTree).hasChildrenSize(9);
    type = classTree.superClass();
    assertThat(type).is(Tree.Kind.MEMBER_SELECT);
    assertThat(type.annotations()).isEmpty();
    type = (ParameterizedTypeTree) ((MemberSelectExpressionTree) type).expression();
    assertThat(type).is(Tree.Kind.PARAMETERIZED_TYPE);
    assertThat(type.annotations()).isEmpty();
    assertThat(type).hasChildrenSize(2);
    type = ((ParameterizedTypeTree) type).type();
    assertThat(type).is(Tree.Kind.IDENTIFIER);
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);

    TypeCastTree typeCast = (TypeCastTree) ((ReturnStatementTree) firstMethodFirstStatement("class T { private long m(int a) { return (@Foo long) a; } }")).expression();
    assertThat(typeCast.type()).isNotNull();
    assertThat(typeCast).hasChildrenSize(5);
    type = typeCast.type();
    assertThat(type).is(Tree.Kind.PRIMITIVE_TYPE);
    assertThat(type.annotations()).hasSize(1);
    assertThat(type).hasChildrenSize(2);
  }

  @Test
  void annotations_in_for_each_statements() {
    ForEachStatement tree = (ForEachStatement) firstMethodFirstStatement("class C { void foo(Object[] values) { for(@Nullable Object value : values) { } } }");
    assertThat(tree.variable().modifiers().annotations()).hasSize(1);
  }

  @Test
  void class_init_declaration() {
    BlockTree tree = (BlockTree) firstTypeMember("class T { { ; ; } }");
    assertThat(tree).is(Tree.Kind.INITIALIZER);
    assertThat(tree.body()).hasSize(2);
    assertThat(tree.openBraceToken()).is("{");
    assertThat(tree.closeBraceToken()).is("}");
    assertThat(tree).hasChildrenSize(4);

    tree = (BlockTree) firstTypeMember("class T { static { ; ; } }");
    assertThat(tree).is(Tree.Kind.STATIC_INITIALIZER);
    StaticInitializerTree staticInitializerTree = (StaticInitializerTree) tree;
    assertThat(staticInitializerTree.body()).hasSize(2);
    assertThat(staticInitializerTree.staticKeyword()).is("static");
    assertThat(staticInitializerTree.openBraceToken()).is("{");
    assertThat(staticInitializerTree.closeBraceToken()).is("}");
    assertThat(tree).hasChildrenSize(5);
  }

  @Test
  void class_constructor() {
    MethodTree tree = (MethodTree) firstTypeMember("class T { T(int p1, int... p2) throws Exception1, Exception2 {} }");
    assertThat(tree).is(Tree.Kind.CONSTRUCTOR);
    assertThat(tree.returnType()).isNull();
    assertThat(tree.simpleName().name()).isEqualTo("T");
    assertThat(tree.parameters()).hasSize(2);
    assertThat(tree.parameters().get(0).type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.parameters().get(1).type()).isInstanceOf(ArrayTypeTree.class);
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(10);
  }

  @Test
  void class_field() {
    List<Tree> declarations = firstType("class T { public int f1 = 42, f2[]; }").members();
    assertThat(declarations).hasSize(2);

    VariableTree tree = (VariableTree) declarations.get(0);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.simpleName().name()).isEqualTo("f1");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree.endToken()).isNotNull();
    assertThat(tree.endToken()).is(",");
    assertThat(tree).hasChildrenSize(6);

    tree = (VariableTree) declarations.get(1);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.type()).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets((ArrayTypeTree) tree.type());
    assertThat(tree.simpleName().name()).isEqualTo("f2");
    assertThat(tree.initializer()).isNull();
    assertThat(tree.endToken()).isNotNull();
    assertThat(tree.endToken()).is(";");
    assertThat(tree).hasChildrenSize(4);
  }

  @Test
  void class_method() {
    MethodTree tree = (MethodTree) firstTypeMember("class T { public int m(int p[][]){} }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.parameters()).hasSize(1);
    assertThat(tree.parameters().get(0).type()).isInstanceOf(ArrayTypeTree.class);
    assertThat(tree).hasChildrenSize(8);
    assertThat(((ArrayTypeTree) tree.parameters().get(0).type()).type()).isInstanceOf(ArrayTypeTree.class);

    tree = (MethodTree) firstTypeMember("class T { public <T> int m(@Annotate int p1, int... p2) throws Exception1, Exception2 {} }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.typeParameters()).isNotEmpty();
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).hasSize(2);
    assertThat(tree.parameters().get(0).type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.parameters().get(0).modifiers().annotations()).hasSize(1);
    assertThat(tree.parameters().get(1).type()).isInstanceOf(ArrayTypeTree.class);
    assertThat(tree.parameters().get(1).endToken()).isNull();
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(11);

    // void method
    tree = (MethodTree) firstTypeMember("class T { public void m(int p) throws Exception1, Exception2 {} }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.typeParameters()).isEmpty();
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).hasSize(1);
    assertThat(tree.parameters().get(0).endToken()).isNull();
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(10);

    tree = (MethodTree) firstTypeMember("class T { public int[] m(int p1, int... p2)[] throws Exception1, Exception2 {} }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.returnType()).is(Tree.Kind.ARRAY_TYPE);
    assertThat(((ArrayTypeTree) tree.returnType()).type()).is(Tree.Kind.ARRAY_TYPE);
    assertThat(((ArrayTypeTree) ((ArrayTypeTree) tree.returnType()).type()).type()).is(Tree.Kind.PRIMITIVE_TYPE);
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).hasSize(2);
    assertThat(tree.parameters().get(0).type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.parameters().get(1).type()).isInstanceOf(ArrayTypeTree.class);
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(11);

    tree = (MethodTree) firstTypeMember("class T { public int m()[] { return null; } }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.parameters()).isEmpty();
    assertThat(tree.returnType()).is(Tree.Kind.ARRAY_TYPE);
    assertThat(tree.returnType()).isNot(Tree.Kind.PRIMITIVE_TYPE);
    assertThat(tree).hasChildrenSize(7);
  }

  /*
   * 8.9. Enums
   */

  @Test
  void enum_declaration() {
    ClassTree tree = firstType("public enum T implements I1, I2 { }");
    assertThat(tree).is(Tree.Kind.ENUM);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.simpleName().name()).isEqualTo("T");
    assertThat(tree.superClass()).isNull();
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.declarationKeyword()).is("enum");
    assertThat(tree).hasChildrenSize(8);

    tree = firstType("public enum T { }");
    assertThat(tree).is(Tree.Kind.ENUM);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.simpleName().name()).isEqualTo("T");
    assertThat(tree.superClass()).isNull();
    assertThat(tree.openBraceToken()).is("{");
    assertThat(tree.closeBraceToken()).is("}");
    assertThat(tree.superInterfaces()).isEmpty();
    assertThat(tree.declarationKeyword()).is("enum");
    assertThat(tree).hasChildrenSize(7);
  }

  @Test
  void enum_constant() {
    List<Tree> declarations = firstType("enum T { C1, C2(2) { }; }").members();
    assertThat(declarations).hasSize(2);

    EnumConstantTree tree = (EnumConstantTree) declarations.get(0);
    assertThat(tree).is(Tree.Kind.ENUM_CONSTANT);
    assertThat(tree.simpleName().name()).isEqualTo("C1");
    assertThat(tree.separatorToken()).is(",");
    assertThat(tree).hasChildrenSize(3);
    NewClassTree newClassTree = tree.initializer();
    assertThat(newClassTree.arguments()).isEmpty();
    assertThat(newClassTree.classBody()).isNull();
    assertThat(newClassTree.newKeyword()).isNull();
    assertThat(newClassTree).hasChildrenSize(2);

    tree = (EnumConstantTree) declarations.get(1);
    assertThat(tree).is(Tree.Kind.ENUM_CONSTANT);
    assertThat(tree.simpleName().name()).isEqualTo("C2");
    assertThat(tree.separatorToken()).is(";");
    assertThat(tree).hasChildrenSize(3);
    newClassTree = tree.initializer();
    assertThat(newClassTree.arguments().openParenToken()).isNotNull();
    assertThat(newClassTree.arguments()).hasSize(1);
    assertThat(newClassTree.arguments().closeParenToken()).isNotNull();
    assertThat(newClassTree.classBody()).isNotNull();
    assertThat(newClassTree.classBody().openBraceToken()).is("{");
    assertThat(newClassTree.newKeyword()).isNull();
    assertThat(newClassTree).hasChildrenSize(3);
  }

  @Test
  void enum_field() {
    List<Tree> declarations = firstType("enum T { ; public int f1 = 42, f2[]; }").members();
    assertThat(declarations).hasSize(3);

    assertThat(declarations.get(0)).is(Tree.Kind.EMPTY_STATEMENT);

    VariableTree tree = (VariableTree) declarations.get(1);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.simpleName().name()).isEqualTo("f1");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree).hasChildrenSize(6);

    tree = (VariableTree) declarations.get(2);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThatArrayTypeHasBrackets((ArrayTypeTree) tree.type());
    assertThat(tree.simpleName().name()).isEqualTo("f2");
    assertThat(tree.initializer()).isNull();
    assertThat(tree).hasChildrenSize(4);
  }

  @Test
  void enum_constructor() {
    MethodTree tree = (MethodTree) firstType("enum T { ; T(int p1, int... p2) throws Exception1, Exception2 {} }").members().get(1);
    assertThat(tree).is(Tree.Kind.CONSTRUCTOR);
    assertThat(tree.returnType()).isNull();
    assertThat(tree.simpleName().name()).isEqualTo("T");
    assertThat(tree.parameters()).hasSize(2);
    assertThat(tree.parameters().get(0).type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasEllipsis((ArrayTypeTree) tree.parameters().get(1).type());
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(10);
  }

  @Test
  void enum_method() {
    MethodTree tree = (MethodTree) firstType("enum T { ; int m(int p1, int... p2) throws Exception1, Exception2 {} }").members().get(1);
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).hasSize(2);
    assertThat(tree.parameters().get(0).type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasEllipsis((ArrayTypeTree) tree.parameters().get(1).type());
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(11);

    // void method
    tree = (MethodTree) firstType("enum T { ; void m(int p) throws Exception1, Exception2; }").members().get(1);
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).hasSize(1);
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(10);
  }

  /*
   * 9. Interfaces
   */

  @Test
  void interface_declaration() {
    ClassTree tree = firstType("public interface T<U> extends I1, I2 { }");
    assertThat(tree).is(Tree.Kind.INTERFACE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.simpleName().name()).isEqualTo("T");
    TypeParameters typeParameters = tree.typeParameters();
    assertThat(typeParameters).hasChildrenSize(3);
    assertThat(typeParameters).isNotEmpty();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.superInterfaces()).hasSize(2);
    assertThat(tree.superInterfaces()).hasSeparatorsSize(1);
    assertThat(tree.declarationKeyword()).is("interface");
    assertThat(tree).hasChildrenSize(8);

    tree = firstType("public interface T { }");
    assertThat(tree).is(Tree.Kind.INTERFACE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.simpleName().name()).isEqualTo("T");
    assertThat(tree.typeParameters()).isEmpty();
    assertThat(tree.superClass()).isNull();
    assertThat(tree.openBraceToken()).is("{");
    assertThat(tree.closeBraceToken()).is("}");
    assertThat(tree.superInterfaces()).isEmpty();
    assertThat(tree.declarationKeyword()).is("interface");
    assertThat(tree).hasChildrenSize(7);
  }

  @Test
  void interface_field() {
    List<Tree> declarations = firstType("interface T { public int f1 = 42, f2[] = { 13 }; }").members();
    assertThat(declarations).hasSize(2);

    VariableTree tree = (VariableTree) declarations.get(0);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.simpleName().name()).isEqualTo("f1");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree).hasChildrenSize(6);

    tree = (VariableTree) declarations.get(1);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.PUBLIC);
    assertThat(tree.type()).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets((ArrayTypeTree) tree.type());
    assertThat(tree.simpleName().name()).isEqualTo("f2");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree).hasChildrenSize(6);
  }

  @Test
  void interface_method() {
    MethodTree tree = (MethodTree) firstTypeMember("interface T { <T> int m(int p1, int... p2) throws Exception1, Exception2; }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.typeParameters()).isNotEmpty();
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).hasSize(2);
    assertThat(tree.parameters().get(0).type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.parameters().get(1).type()).isInstanceOf(ArrayTypeTree.class);
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(11);

    // void method
    tree = (MethodTree) firstTypeMember("interface T { void m(int p) throws Exception1, Exception2; }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.typeParameters()).isEmpty();
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).hasSize(1);
    assertThat(tree.throwsClauses()).hasSize(2);
    assertThat(tree.throwsClauses()).hasSeparatorsSize(1);
    assertThat(tree.block()).isNull();
    assertThat(tree.defaultValue()).isNull();
    assertThat(tree).hasChildrenSize(10);
  }

  /*
   * 9.6. Annotation Types
   */

  @Test
  void annotation_declaration() {
    ClassTree tree = firstType("public @interface T { }");
    assertThat(tree).is(Tree.Kind.ANNOTATION_TYPE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).keyword()).is("public");
    assertThat(tree.simpleName().name()).isEqualTo("T");
    assertThat(tree.superClass()).isNull();
    assertThat(tree.openBraceToken()).is("{");
    assertThat(tree.closeBraceToken()).is("}");
    assertThat(tree.superInterfaces()).isEmpty();
    assertThat(tree.declarationKeyword()).is("interface");
    assertThat(tree).hasChildrenSize(8);
  }

  @Test
  void annotation_method() {
    MethodTree tree = (MethodTree) firstTypeMember("@interface T { int m() default 0; }");
    assertThat(tree).is(Tree.Kind.METHOD);
    assertThat(tree.returnType()).isNotNull();
    assertThat(tree.simpleName().name()).isEqualTo("m");
    assertThat(tree.parameters()).isEmpty();
    assertThat(tree.throwsClauses()).isEmpty();
    assertThat(tree.block()).isNull();
    assertThat(tree.defaultValue()).isNotNull();
    assertThat(tree).hasChildrenSize(9);

    tree = (MethodTree) firstTypeMember("@interface plop{ public String method(); }");
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).keyword()).is("public");
    assertThat(tree).hasChildrenSize(7);

  }

  @Test
  void annotation_constant() {
    List<Tree> members = firstType("@interface T { int c1 = 1, c2[] = { 2 }; }").members();
    assertThat(members).hasSize(2);

    VariableTree tree = (VariableTree) members.get(0);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.simpleName().name()).isEqualTo("c1");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree).hasChildrenSize(6); // 1+5, as empty modifiers are always included

    tree = (VariableTree) members.get(1);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.type()).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets((ArrayTypeTree) tree.type());
    assertThat(tree.simpleName().name()).isEqualTo("c2");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree).hasChildrenSize(6);
  }

  /*
   * 14. Blocks and Statements
   */

  /**
   * 14.2. Blocks
   */
  @Test
  void blocks() {
    BlockTree tree = ((MethodTree) firstTypeMember("class T { void m() { ; ; } }")).block();
    assertThat(tree).is(Tree.Kind.BLOCK);
    assertThat(tree.openBraceToken()).is("{");
    assertThat(tree.body()).hasSize(2);
    assertThat(tree.closeBraceToken()).is("}");
    assertThat(tree).hasChildrenSize(4);
  }

  /**
   * 14.3. Local Class Declarations
   */
  @Test
  void local_class_declaration() {
    BlockTree block = ((MethodTree) firstTypeMember("class T { void m() { abstract class Local { } } }")).block();
    ClassTree tree = (ClassTree) block.body().get(0);
    assertThat(tree).is(Tree.Kind.CLASS);
    assertThat(tree.simpleName().identifierToken()).is("Local");
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.ABSTRACT);
    assertThat(tree).isNotNull();
    assertThat(tree).hasChildrenSize(7);
  }

  /**
   * 14.4. Local Variable Declaration Statements
   */
  @Test
  void local_variable_declaration() {
    BlockTree block = ((MethodTree) firstTypeMember("class T { void m() { int a = 42, b[]; final @Nullable int c = 42; } }")).block();
    List<StatementTree> declarations = block.body();
    assertThat(declarations).hasSize(3);

    VariableTree tree = (VariableTree) declarations.get(0);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).isEmpty();
    assertThat(tree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.simpleName().name()).isEqualTo("a");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree.endToken()).isNotNull();
    assertThat(tree.endToken()).is(",");
    assertThat(tree).hasChildrenSize(6);

    tree = (VariableTree) declarations.get(1);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).isEmpty();
    assertThat(tree.type()).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets((ArrayTypeTree) tree.type());
    assertThat(tree.simpleName().name()).isEqualTo("b");
    assertThat(tree.initializer()).isNull();
    assertThat(tree.endToken()).isNotNull();
    assertThat(tree.endToken()).is(";");
    assertThat(tree).hasChildrenSize(4);

    // TODO Test annotation

    tree = (VariableTree) declarations.get(2);
    assertThat(tree).is(Tree.Kind.VARIABLE);
    assertThat(tree.modifiers().modifiers()).hasSize(1);
    assertThat(tree.modifiers().modifiers().get(0).modifier()).isEqualTo(Modifier.FINAL);
    assertThat(tree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.simpleName().name()).isEqualTo("c");
    assertThat(tree.initializer()).isNotNull();
    assertThat(tree.endToken()).isNotNull();
    assertThat(tree.endToken()).is(";");
    assertThat(tree).hasChildrenSize(6);
  }

  /**
   * 14.6. The Empty Statement
   */
  @Test
  void empty_statement() {
    EmptyStatementTree tree = (EmptyStatementTree) firstMethodFirstStatement("class T { void m() { ; } }");
    assertThat(tree).is(Tree.Kind.EMPTY_STATEMENT);
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(1);
  }

  /**
   * 14.7. Labeled Statements
   */
  @Test
  void labeled_statement() {
    LabeledStatementTree tree = (LabeledStatementTree) firstMethodFirstStatement("class T { void m() { label: ; } }");
    assertThat(tree).is(Tree.Kind.LABELED_STATEMENT);
    assertThat(tree.label().name()).isEqualTo("label");
    assertThat(tree.statement()).isNotNull();
    assertThat(tree.colonToken()).is(":");
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 14.8. Expression Statements
   */
  @Test
  void expression_statement() {
    ExpressionStatementTree tree = (ExpressionStatementTree) firstMethodFirstStatement("class T { void m() { i++; } }");
    assertThat(tree).is(Tree.Kind.EXPRESSION_STATEMENT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(2);
  }

  /**
   * 14.9. The if Statement
   */
  @Test
  void if_statement() {
    IfStatementTree tree = (IfStatementTree) firstMethodFirstStatement("class T { void m() { if (true) { } } }");
    assertThat(tree).is(Tree.Kind.IF_STATEMENT);
    assertThat(tree.ifKeyword()).is("if");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.thenStatement()).isNotNull();
    assertThat(tree.elseKeyword()).isNull();
    assertThat(tree.elseStatement()).isNull();
    assertThat(tree).hasChildrenSize(5);

    tree = (IfStatementTree) firstMethodFirstStatement("class T { void m() { if (true) { } else { } } }");
    assertThat(tree).is(Tree.Kind.IF_STATEMENT);
    assertThat(tree.ifKeyword()).is("if");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.thenStatement()).isNotNull();
    assertThat(tree.elseKeyword()).is("else");
    assertThat(tree.elseStatement()).isNotNull();
    assertThat(tree).hasChildrenSize(7);
  }

  /**
   * 14.10. The assert Statement
   */
  @Test
  void assert_statement() {
    AssertStatementTree tree = (AssertStatementTree) firstMethodFirstStatement("class T { void m() { assert true; } }");
    assertThat(tree).is(Tree.Kind.ASSERT_STATEMENT);
    assertThat(tree.assertKeyword()).is("assert");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.colonToken()).isNull();
    assertThat(tree.detail()).isNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(3);

    tree = (AssertStatementTree) firstMethodFirstStatement("class T { void m() { assert true : \"detail\"; } }");
    assertThat(tree).is(Tree.Kind.ASSERT_STATEMENT);
    assertThat(tree.assertKeyword()).is("assert");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.colonToken()).is(":");
    assertThat(tree.detail()).isNotNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(5);
  }

  /**
   * 14.11. The switch Statement
   */
  @Test
  void switch_statement_and_expression() {
    SwitchStatementTree tree = (SwitchStatementTree) firstMethodFirstStatement("class T { void m(int e) { switch (e) { case 1: case 2, 3: ; default: ; } } }");
    assertThat(tree).is(Tree.Kind.SWITCH_STATEMENT);
    assertThat(tree.switchKeyword()).is("switch");
    assertThat(tree.openBraceToken()).is("{");
    assertThat(tree.closeBraceToken()).is("}");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.cases()).hasSize(2);

    SwitchExpressionTree switchExpression = tree.asSwitchExpression();
    assertThat(switchExpression).is(Tree.Kind.SWITCH_EXPRESSION);
    assertThat(tree.switchKeyword()).isEqualTo(switchExpression.switchKeyword());
    assertThat(tree.openBraceToken()).isEqualTo(switchExpression.openBraceToken());
    assertThat(tree.closeBraceToken()).isEqualTo(switchExpression.closeBraceToken());
    assertThat(tree.openParenToken()).isEqualTo(switchExpression.openParenToken());
    assertThat(tree.expression()).isEqualTo(switchExpression.expression());
    assertThat(tree.closeParenToken()).isEqualTo(switchExpression.closeParenToken());
    assertThat(tree.cases()).isEqualTo(switchExpression.cases());
    assertThat(switchExpression).hasChildrenSize(8);

    CaseGroupTree c = tree.cases().get(0);
    assertThat(c).is(Tree.Kind.CASE_GROUP);
    assertThat(c.labels()).hasSize(2);
    CaseLabelTree caseLabelTree = c.labels().get(0);
    assertThat(caseLabelTree.isFallThrough()).isTrue();
    assertThat(caseLabelTree.caseOrDefaultKeyword()).is("case");
    assertThat(caseLabelTree.expression()).isNotNull();
    assertThat(caseLabelTree.expressions()).hasSize(1);
    assertThat(((LiteralTree) caseLabelTree.expression())).hasValue("1");
    assertThat(((LiteralTree) caseLabelTree.expressions().get(0))).hasValue("1");
    assertThat(caseLabelTree.colonToken()).is(":");
    assertThat(caseLabelTree.colonOrArrowToken()).is(":");
    assertThat(caseLabelTree).hasChildrenSize(3);

    caseLabelTree = c.labels().get(1);
    assertThat(caseLabelTree.isFallThrough()).isTrue();
    assertThat(caseLabelTree.caseOrDefaultKeyword()).is("case");
    assertThat(caseLabelTree.expression()).isNotNull();
    assertThat(caseLabelTree.expressions()).hasSize(2);
    assertThat(((LiteralTree) caseLabelTree.expression())).hasValue("2");
    assertThat(((LiteralTree) caseLabelTree.expressions().get(0))).hasValue("2");
    assertThat(((LiteralTree) caseLabelTree.expressions().get(1))).hasValue("3");
    assertThat(caseLabelTree.colonToken()).is(":");
    assertThat(caseLabelTree.colonOrArrowToken()).is(":");
    assertThat(caseLabelTree).hasChildrenSize(4);
    assertThat(c.body()).hasSize(1);

    c = tree.cases().get(1);
    assertThat(c).is(Tree.Kind.CASE_GROUP);
    assertThat(c.labels()).hasSize(1);
    caseLabelTree = c.labels().get(0);
    assertThat(caseLabelTree.isFallThrough()).isTrue();
    assertThat(caseLabelTree.caseOrDefaultKeyword()).is("default");
    assertThat(caseLabelTree.expression()).isNull();
    assertThat(caseLabelTree.colonOrArrowToken()).is(":");
    assertThat(caseLabelTree).hasChildrenSize(2);
    assertThat(c.body()).hasSize(1);

    // FIXME ECJ broken for switch expression?
    // ReturnStatementTree rst = (ReturnStatementTree) firstMethodFirstStatement("class T {\n" +
    // " int m(int e) {\n" +
    // " return switch (e) {\n" +
    // " case 1 -> 0;\n" +
    // " case 2, 3 -> 1;\n" +
    // " default -> 42;\n" +
    // " };\n" +
    // " }\n" +
    // "}");
    // switchExpression = (SwitchExpressionTree) rst.expression();
    // assertThat(switchExpression.cases()).hasSize(3);
    // c = switchExpression.cases().get(1);
    // assertThat(c.labels()).hasSize(2);
    // caseLabelTree = c.labels().get(1);
    // assertThat(caseLabelTree.isFallThrough()).isFalse();
    // assertThat(caseLabelTree.caseOrDefaultKeyword()).is("case");
    // assertThat(caseLabelTree.expression()).isNotNull();
    // assertThat(caseLabelTree.colonOrArrowToken()).is("->");
    // assertThat(c.body()).hasSize(1);

    tree = (SwitchStatementTree) firstMethodFirstStatement("class T { void m() { switch (e) { default: } } }");
    assertThat(tree.cases()).hasSize(1);
    assertThat(tree.cases().get(0).body()).isEmpty();
    assertThat(tree).hasChildrenSize(1);
  }

  /**
   * 14.12. The while Statement
   */
  @Test
  void while_statement() {
    WhileStatementTree tree = (WhileStatementTree) firstMethodFirstStatement("class T { void m() { while (true) ; } }");
    assertThat(tree).is(Tree.Kind.WHILE_STATEMENT);
    assertThat(tree.whileKeyword()).is("while");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.statement()).isNotNull();
    assertThat(tree).hasChildrenSize(5);
  }

  /**
   * 14.13. The do Statement
   */
  @Test
  void do_statement() {
    DoWhileStatementTree tree = (DoWhileStatementTree) firstMethodFirstStatement("class T { void m() { do ; while (true); } }");
    assertThat(tree).is(Tree.Kind.DO_STATEMENT);
    assertThat(tree.doKeyword()).is("do");
    assertThat(tree.statement()).isNotNull();
    assertThat(tree.whileKeyword()).is("while");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(7);
  }

  /**
   * 14.14. The for Statement
   */
  @Test
  void for_statement() {
    ForStatementTree tree = (ForStatementTree) firstMethodFirstStatement("class T { void m() { for (int i = 0; i < 42; i ++) ; } }");
    assertThat(tree).is(Tree.Kind.FOR_STATEMENT);
    assertThat(tree.forKeyword()).is("for");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.initializer()).hasSize(1);
    assertThat(tree.initializer().get(0)).isInstanceOf(VariableTree.class);
    assertThat(tree.initializer().get(0)).hasChildrenSize(5);
    assertThat(tree.firstSemicolonToken()).is(";");
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.secondSemicolonToken()).is(";");
    assertThat(tree.update()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.statement()).isNotNull();
    assertThat(tree).hasChildrenSize(9);

    tree = (ForStatementTree) firstMethodFirstStatement("class T { void m() { for (i = 0; i < 42; i ++) ; } }");
    assertThat(tree).is(Tree.Kind.FOR_STATEMENT);
    assertThat(tree.initializer()).hasSize(1);
    assertThat(tree.initializer().get(0)).isInstanceOf(ExpressionStatementTree.class);
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.update()).isNotNull();
    assertThat(tree.statement()).isNotNull();
    assertThat(tree).hasChildrenSize(9);

    tree = (ForStatementTree) firstMethodFirstStatement("class T { void m() { for ( ; ; ) ; } }");
    assertThat(tree).is(Tree.Kind.FOR_STATEMENT);
    assertThat(tree.initializer()).isEmpty();
    assertThat(tree.condition()).isNull();
    assertThat(tree.update()).isEmpty();
    assertThat(tree.statement()).isNotNull();
    assertThat(tree).hasChildrenSize(8);

    tree = (ForStatementTree) firstMethodFirstStatement("class T { void m() { for (i = 0, j = 1; i < 42; i++, j--) ; } }");
    assertThat(tree).is(Tree.Kind.FOR_STATEMENT);
    assertThat(tree.initializer()).hasSize(2);
    assertThat(tree.initializer()).hasSeparatorsSize(1);
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.update()).hasSize(2);
    assertThat(tree.update()).hasSeparatorsSize(1);
    assertThat(tree.statement()).isNotNull();
    assertThat(tree).hasChildrenSize(9);

    tree = (ForStatementTree) firstMethodFirstStatement("class T { void m() { for (int i = 0, j = 1; i < 42; i++, j--) ; } }");
    assertThat(tree).is(Tree.Kind.FOR_STATEMENT);
    assertThat(tree.initializer()).hasSize(2);
    assertThat(tree.initializer().separators()).isEmpty();
    assertThat(tree.condition()).isNotNull();
    assertThat(tree.update()).hasSize(2);
    assertThat(tree.update()).hasSeparatorsSize(1);
    assertThat(tree.statement()).isNotNull();
    assertThat(tree).hasChildrenSize(9);
  }

  @Test
  void enhanced_for_statement() {
    ForEachStatement tree = (ForEachStatement) firstMethodFirstStatement("class T { void m() { for (Object o : objects) ; } }");
    assertThat(tree).is(Tree.Kind.FOR_EACH_STATEMENT);
    assertThat(tree.forKeyword()).is("for");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.variable()).isNotNull();
    assertThat(tree.colonToken()).is(":");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.statement()).isNotNull();
    assertThat(tree).hasChildrenSize(7);
  }

  /**
   * 14.15. The break Statement
   */
  @Test
  void break_statement() {
    BreakStatementTree tree = (BreakStatementTree) firstMethodFirstStatement("class T { void m() { break ; } }");
    assertThat(tree).is(Tree.Kind.BREAK_STATEMENT);
    assertThat(tree.breakKeyword()).is("break");
    assertThat(tree.label()).isNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(2);

    tree = (BreakStatementTree) firstMethodFirstStatement("class T { void m() { break label ; } }");
    assertThat(tree).is(Tree.Kind.BREAK_STATEMENT);
    assertThat(tree.breakKeyword()).is("break");
    assertThat(tree.label().name()).isEqualTo("label");
    assertThat(((IdentifierTree)tree.value()).name()).isEqualTo("label");
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 14.16. The continue Statement
   */
  @Test
  void continue_statement() {
    ContinueStatementTree tree = (ContinueStatementTree) firstMethodFirstStatement("class T { void m() { continue ; } }");
    assertThat(tree).is(Tree.Kind.CONTINUE_STATEMENT);
    assertThat(tree.continueKeyword()).is("continue");
    assertThat(tree.label()).isNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(2);

    tree = (ContinueStatementTree) firstMethodFirstStatement("class T { void m() { continue label ; } }");
    assertThat(tree).is(Tree.Kind.CONTINUE_STATEMENT);
    assertThat(tree.continueKeyword()).is("continue");
    assertThat(tree.label().name()).isEqualTo("label");
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 14.17. The return Statement
   */
  @Test
  void return_statement() {
    ReturnStatementTree tree = (ReturnStatementTree) firstMethodFirstStatement("class T { boolean m() { return ; } }");
    assertThat(tree).is(Tree.Kind.RETURN_STATEMENT);
    assertThat(tree.returnKeyword()).is("return");
    assertThat(tree.expression()).isNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(2);

    tree = (ReturnStatementTree) firstMethodFirstStatement("class T { boolean m() { return true; } }");
    assertThat(tree).is(Tree.Kind.RETURN_STATEMENT);
    assertThat(tree.returnKeyword()).is("return");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 14.18. The throw Statement
   */
  @Test
  void throw_statement() {
    ThrowStatementTree tree = (ThrowStatementTree) firstMethodFirstStatement("class T { void m() { throw e; } }");
    assertThat(tree).is(Tree.Kind.THROW_STATEMENT);
    assertThat(tree.throwKeyword()).is("throw");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.semicolonToken()).is(";");
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 14.19. The synchronized Statement
   */
  @Test
  void synchronized_statement() {
    SynchronizedStatementTree tree = (SynchronizedStatementTree) firstMethodFirstStatement("class T { void m() { synchronized(e) { } } }");
    assertThat(tree).is(Tree.Kind.SYNCHRONIZED_STATEMENT);
    assertThat(tree.synchronizedKeyword()).is("synchronized");
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.block()).isNotNull();
    assertThat(tree).hasChildrenSize(5);
  }

  /**
   * 14.20. The try statement
   */
  @Test
  void try_statement() {
    TryStatementTree tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try { } finally { } } }");
    assertThat(tree).is(Tree.Kind.TRY_STATEMENT);
    assertThat(tree.resourceList()).isEmpty();
    assertThat(tree.block()).isNotNull();
    assertThat(tree.catches()).isEmpty();
    assertThat(tree.finallyKeyword()).is("finally");
    assertThat(tree.finallyBlock()).isNotNull();
    assertThat(tree).hasChildrenSize(4);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try { } catch (RuntimeException e1) { } catch (Exception e2) { } } }");
    assertThat(tree).is(Tree.Kind.TRY_STATEMENT);
    assertThat(tree.tryKeyword()).is("try");
    assertThat(tree.openParenToken()).isNull();
    assertThat(tree.resourceList()).isEmpty();
    assertThat(tree.closeParenToken()).isNull();
    assertThat(tree.block()).isNotNull();
    assertThat(tree.finallyKeyword()).isNull();
    assertThat(tree.finallyBlock()).isNull();
    assertThat(tree.catches()).hasSize(2);
    assertThat(tree).hasChildrenSize(4);
    CatchTree catchTree = tree.catches().get(0);
    assertThat(catchTree.catchKeyword()).is("catch");
    assertThat(catchTree.block()).isNotNull();
    assertThat(catchTree.openParenToken()).is("(");
    assertThat(catchTree.closeParenToken()).is(")");
    assertThat(catchTree).hasChildrenSize(5);
    VariableTree parameterTree = catchTree.parameter();
    assertThat(parameterTree.type()).isNotNull();
    assertThat(parameterTree.simpleName().name()).isEqualTo("e1");
    assertThat(parameterTree.initializer()).isNull();
    assertThat(parameterTree).hasChildrenSize(3);
    catchTree = tree.catches().get(1);
    assertThat(catchTree).hasChildrenSize(5);
    parameterTree = catchTree.parameter();
    assertThat(parameterTree.type()).isNotNull();
    assertThat(parameterTree.simpleName().name()).isEqualTo("e2");
    assertThat(parameterTree.initializer()).isNull();
    assertThat(parameterTree).hasChildrenSize(3);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try { } catch (Exception e) { } finally { } } }");
    assertThat(tree).is(Tree.Kind.TRY_STATEMENT);
    assertThat(tree.resourceList()).isEmpty();
    assertThat(tree.block()).isNotNull();
    assertThat(tree.catches()).hasSize(1);
    assertThat(tree.finallyKeyword()).is("finally");
    assertThat(tree.catches().get(0)).hasChildrenSize(5);
    assertThat(tree.finallyBlock()).isNotNull();
    assertThat(tree).hasChildrenSize(5);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try { } catch (final @Foo Exception e) { } } }");
    assertThat(tree).is(Tree.Kind.TRY_STATEMENT);
    assertThat(tree.resourceList()).isEmpty();
    assertThat(tree.block()).isNotNull();
    assertThat(tree.catches()).hasSize(1);
    assertThat(tree.finallyKeyword()).isNull();
    assertThat(tree.finallyBlock()).isNull();
    assertThat(tree).hasChildrenSize(3);
    catchTree = tree.catches().get(0);
    assertThat(catchTree).hasChildrenSize(5);
    parameterTree = catchTree.parameter();
    assertThat(parameterTree.modifiers()).hasSize(2);
    assertThat(parameterTree.simpleName().identifierToken()).is("e");
    assertThat(parameterTree.type()).is(Tree.Kind.IDENTIFIER);
    assertThat(parameterTree.endToken()).isNull();
    assertThat(parameterTree.initializer()).isNull();
    assertThat(parameterTree).hasChildrenSize(3);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try (final @Foo Resource r = open()) { } } }");
    assertThat(tree).is(Tree.Kind.TRY_STATEMENT);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.catches()).isEmpty();
    assertThat(tree.finallyKeyword()).isNull();
    assertThat(tree.finallyBlock()).isNull();
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.resourceList()).hasSize(1);
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree).hasChildrenSize(5);
    VariableTree resource = (VariableTree) tree.resourceList().get(0);
    assertThat(resource.simpleName().name()).isEqualTo("r");
    assertThat(resource.initializer()).isNotNull();
    assertThat(resource.modifiers()).hasSize(2);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try (Resource r1 = open(); Resource r2 = open()) { } catch (Exception e) { } finally { } } }");
    assertThat(tree).is(Tree.Kind.TRY_STATEMENT);
    assertThat(tree.block()).isNotNull();
    assertThat(tree.catches()).hasSize(1);
    assertThat(tree.finallyKeyword()).is("finally");
    assertThat(tree.catches().get(0)).hasChildrenSize(5);
    assertThat(tree.finallyBlock()).isNotNull();
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.resourceList()).hasSize(2);
    assertThat(tree.resourceList()).hasSeparatorsSize(1);
    assertThat(tree.closeParenToken()).is(")");
    resource = (VariableTree) tree.resourceList().get(0);
    assertThat(resource.simpleName().name()).isEqualTo("r1");
    assertThat(resource.initializer()).isNotNull();
    resource = (VariableTree) tree.resourceList().get(1);
    assertThat(resource.simpleName().name()).isEqualTo("r2");
    assertThat(resource.initializer()).isNotNull();
    assertThat(tree).hasChildrenSize(8);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try { } catch (Exception1 | Exception2 e) { } } }");
    parameterTree = tree.catches().get(0).parameter();
    assertThat(parameterTree).hasChildrenSize(3);
    UnionTypeTree type = (UnionTypeTree) parameterTree.type();
    assertThat(type).hasChildrenSize(1);
    assertThat(type.typeAlternatives()).hasSize(2);
    assertThat(type.typeAlternatives()).hasSeparatorsSize(1);
    assertThat(tree).hasChildrenSize(3);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try (r1) { } } }");
    assertThat(tree.resourceList()).hasSize(1);
    assertThat(tree.resourceList().separators()).isEmpty();

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try (r1; super.field; new A().f) { } } }");
    assertThat(tree.resourceList()).hasSize(3);
    assertThat(tree.resourceList()).hasSeparatorsSize(2);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try (r1; Resource r2 = open();) { } } }");
    assertThat(tree.resourceList()).hasSize(2);
    assertThat(tree.resourceList()).hasSeparatorsSize(2);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try (r1; Resource r2 = open()) { } } }");
    assertThat(tree.resourceList()).hasSize(2);
    assertThat(tree.resourceList()).hasSeparatorsSize(1);

    tree = (TryStatementTree) firstMethodFirstStatement("class T { void m() { try (Resource r2 = open(); r1;) { } } }");
    assertThat(tree.resourceList()).hasSize(2);
    assertThat(tree.resourceList()).hasSeparatorsSize(2);
  }

  /*
   * 15. Expressions
   */

  /*
   * 15.8. Primary Expressions
   */

  /**
   * 15.8.2. Class Literals
   */
  @Test
  void class_literal() {
    MemberSelectExpressionTree tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { m() { return void.class; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.identifier().identifierToken()).is("class");
    assertThat(tree.identifier().name()).isEqualTo("class");
    assertThat(tree.operatorToken()).isNotNull();
    assertThat(tree).hasChildrenSize(3);

    tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { m() { return int.class; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isInstanceOf(PrimitiveTypeTree.class);
    assertThat(tree.identifier().identifierToken()).is("class");
    assertThat(tree.identifier().name()).isEqualTo("class");
    assertThat(tree.operatorToken()).isNotNull();
    assertThat(tree).hasChildrenSize(3);

    tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { m() { return int[].class; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets((ArrayTypeTree) tree.expression());
    assertThat(tree.identifier().identifierToken()).is("class");
    assertThat(tree.identifier().name()).isEqualTo("class");
    assertThat(tree.operatorToken()).isNotNull();
    assertThat(tree).hasChildrenSize(3);

    tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { m() { return T.class; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.identifier().identifierToken()).is("class");
    assertThat(tree.identifier().name()).isEqualTo("class");
    assertThat(tree.operatorToken()).isNotNull();
    assertThat(tree).hasChildrenSize(3);

    tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { m() { return T[].class; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets((ArrayTypeTree) tree.expression());
    assertThat(tree.identifier().identifierToken()).is("class");
    assertThat(tree.identifier().name()).isEqualTo("class");
    assertThat(tree.operatorToken()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.8.3. this
   */
  @Test
  void this_expression() {
    IdentifierTree tree = (IdentifierTree) expressionOfReturnStatement("class T { Object m() { return this; } }");
    assertThat(tree).is(Tree.Kind.IDENTIFIER);
    assertThat(tree).isNotNull();
    assertThat(tree.identifierToken()).is("this");
    assertThat(tree.name()).isEqualTo("this");
    assertThat(tree).hasChildrenSize(1);
  }

  /**
   * 15.8.4. Qualified this
   */
  @Test
  void qualified_this() {
    MemberSelectExpressionTree tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { Object m() { return ClassName.this; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.identifier().identifierToken()).is("this");
    assertThat(tree.identifier().name()).isEqualTo("this");
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.8.5. Parenthesized Expressions
   */
  @Test
  void parenthesized_expression() {
    ParenthesizedTree tree = (ParenthesizedTree) expressionOfReturnStatement("class T { boolean m() { return (true); } }");
    assertThat(tree).is(Tree.Kind.PARENTHESIZED_EXPRESSION);
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.9. Class Instance Creation Expressions
   */
  @Test
  void class_instance_creation_expression() {
    NewClassTree tree = (NewClassTree) expressionOfReturnStatement("class T { T m() { return new T(true, false) {}; } }");
    assertThat(tree).is(Tree.Kind.NEW_CLASS);
    assertThat(tree.enclosingExpression()).isNull();
    assertThat(tree.dotToken()).isNull();
    assertThat(tree.arguments().openParenToken()).isNotNull();
    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().closeParenToken()).isNotNull();
    assertThat(tree.identifier()).isNotNull();
    assertThat(tree.classBody()).isNotNull();
    assertThat(tree.newKeyword()).isNotNull();
    assertThat(tree).hasChildrenSize(4);
    // assertThat(tree.typeArguments()).isEmpty();

    tree = (NewClassTree) expressionOfReturnStatement("class T { T m() { return Enclosing.new T(true, false) {}; } }");
    assertThat(tree).is(Tree.Kind.NEW_CLASS);
    assertThat(tree.enclosingExpression()).isNotNull();
    assertThat(tree.dotToken()).isNotNull();
    assertThat(tree.identifier()).isNotNull();
    assertThat(tree.arguments().openParenToken()).isNotNull();
    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().closeParenToken()).isNotNull();
    assertThat(tree.classBody()).isNotNull();
    assertThat(tree.newKeyword()).isNotNull();
    assertThat(tree).hasChildrenSize(6);
    // assertThat(tree.typeArguments()).isEmpty();

    tree = (NewClassTree) expressionOfReturnStatement("class T { T m() { return this.new T(true, false) {}; } }");
    assertThat(tree.enclosingExpression()).isNotNull();
    assertThat(tree.dotToken()).isNotNull();
    assertThat(tree.identifier()).isNotNull();
    assertThat(tree.arguments().openParenToken()).isNotNull();
    assertThat(tree.arguments()).hasSize(2);
    assertThat(tree.arguments().closeParenToken()).isNotNull();
    assertThat(tree.classBody()).isNotNull();
    assertThat(tree.newKeyword()).isNotNull();
    assertThat(tree).hasChildrenSize(6);
    // assertThat(tree.typeArguments()).isEmpty();

    tree = (NewClassTree) ((VariableTree) firstMethodFirstStatement("class T { void m() { Foo myInt = new<Integer>Foo(42); } }")).initializer();
    assertThat(tree.enclosingExpression()).isNull();
    assertThat(tree.dotToken()).isNull();
    assertThat(tree.identifier()).isNotNull();
    assertThat(tree.typeArguments()).isNotNull();
    assertThat(tree.typeArguments()).hasSize(1);
    assertThat(tree).hasChildrenSize(4);
  }

  /**
   * 15.10. Array Creation Expressions
   */
  @Test
  void array_creation_expression() {
    {
      NewArrayTree tree = (NewArrayTree) expressionOfReturnStatement("class T { int[][] m() { return new int[][]{{1}, {2, 3}}; } }");
      assertThat(tree).is(Tree.Kind.NEW_ARRAY);
      assertThat(tree.type()).isNotNull();
      assertThat(tree.dimensions()).hasSize(2);
      assertThat(tree.initializers()).hasSize(2);
      assertThat(tree.initializers()).hasSeparatorsSize(1);
      assertThat(tree.newKeyword()).isNotNull();
      assertThat(tree).hasChildrenSize(7);

      ArrayDimensionTree dimension = tree.dimensions().get(0);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).isEmpty();
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).isNull();
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(2);
      dimension = tree.dimensions().get(1);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).isEmpty();
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).isNull();
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(2);

      NewArrayTree firstDim = (NewArrayTree) tree.initializers().get(0);
      assertThat(firstDim.initializers()).hasSize(1);
      assertThat(firstDim.initializers().separators()).isEmpty();
      assertThat(firstDim).hasChildrenSize(3);
      NewArrayTree secondDim = (NewArrayTree) tree.initializers().get(1);
      assertThat(secondDim.initializers()).hasSize(2);
      assertThat(secondDim.initializers()).hasSeparatorsSize(1);
      assertThat(secondDim).hasChildrenSize(3);
    }
    {
      NewArrayTree tree = (NewArrayTree) expressionOfReturnStatement("class T { int[][] m() { return new int[2][2]; } }");
      assertThat(tree).is(Tree.Kind.NEW_ARRAY);
      assertThat(tree.type()).isNotNull();
      assertThat(tree.dimensions()).hasSize(2);
      assertThat(tree.initializers()).isEmpty();
      assertThat(tree.newKeyword()).isNotNull();
      assertThat(tree).hasChildrenSize(5);
      ArrayDimensionTree dimension = tree.dimensions().get(0);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).isEmpty();
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).is(Tree.Kind.INT_LITERAL);
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(3);
    }
    {
      NewArrayTree tree = (NewArrayTree) expressionOfReturnStatement("class T { int[][] m() { return new int[] @Bar [] {{}, {}}; } }");
      assertThat(tree).is(Tree.Kind.NEW_ARRAY);
      assertThat(tree.type()).isNotNull();
      assertThat(tree.dimensions()).hasSize(2);
      assertThat(tree.initializers()).hasSize(2);
      assertThat(tree.newKeyword()).isNotNull();
      assertThat(tree).hasChildrenSize(7);
      ArrayDimensionTree dimension = tree.dimensions().get(0);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).isEmpty();
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).isNull();
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(2);
      dimension = tree.dimensions().get(1);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).hasSize(1);
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).isNull();
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(3);
    }
    {
      NewArrayTree tree = (NewArrayTree) expressionOfReturnStatement("class T { int[][] m() { return new int[2] @Bar [3]; } }");
      assertThat(tree).is(Tree.Kind.NEW_ARRAY);
      assertThat(tree.type()).isNotNull();
      assertThat(tree.dimensions()).hasSize(2);
      assertThat(tree.initializers()).isEmpty();
      assertThat(tree.newKeyword()).isNotNull();
      assertThat(tree).hasChildrenSize(5);
      ArrayDimensionTree dimension = tree.dimensions().get(0);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).isEmpty();
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).is(Tree.Kind.INT_LITERAL);
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(3);
      dimension = tree.dimensions().get(1);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).hasSize(1);
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).is(Tree.Kind.INT_LITERAL);
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(4);
    }
    {
      NewArrayTree tree = (NewArrayTree) expressionOfReturnStatement("class T { int[] m() { return new int @Foo [2]; } }");
      assertThat(tree).is(Tree.Kind.NEW_ARRAY);
      assertThat(tree.type()).isNotNull();
      assertThat(tree.dimensions()).hasSize(1);
      assertThat(tree.initializers()).isEmpty();
      assertThat(tree.newKeyword()).isNotNull();
      assertThat(tree).hasChildrenSize(4);
      ArrayDimensionTree dimension = tree.dimensions().get(0);
      assertThat(dimension).is(Tree.Kind.ARRAY_DIMENSION);
      assertThat(dimension.annotations()).hasSize(1);
      assertThat(dimension.openBracketToken()).is("[");
      assertThat(dimension.expression()).is(Tree.Kind.INT_LITERAL);
      assertThat(dimension.closeBracketToken()).is("]");
      assertThat(dimension).hasChildrenSize(4);
    }
    {
      NewArrayTree tree = (NewArrayTree) ((VariableTree) firstMethodFirstStatement("class T { void m() { int[] a = {,}; }}")).initializer();
      assertThat(tree).is(Tree.Kind.NEW_ARRAY);
      assertThat(tree.type()).isNull();
      assertThat(tree.dimensions()).isEmpty();
      assertThat(tree.initializers()).isEmpty();
      // FIXME missing separator
      assertThat(tree.initializers().separators()).isEmpty();
    }
  }

  /**
   * 15.11. Field Access Expressions
   */
  @Test
  void field_access_expression() {
    MemberSelectExpressionTree tree;

    tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { int m() { return super.identifier; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.identifier()).isNotNull();
    assertThat(tree.operatorToken()).isNotNull();
    assertThat(tree).hasChildrenSize(3);

    tree = (MemberSelectExpressionTree) expressionOfReturnStatement("class T { int m() { return ClassName.super.identifier; } }");
    assertThat(tree).is(Tree.Kind.MEMBER_SELECT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.identifier()).isNotNull();
    assertThat(tree.operatorToken()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.12. Method Invocation Expressions
   */
  @Test
  void method_invocation_expression() {
    // TODO test NonWildTypeArguments
    {
      MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { void m() { identifier(true, false); } }");
      assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
      assertThat(((IdentifierTree) tree.methodSelect()).name()).isEqualTo("identifier");
      assertThat(tree.arguments().openParenToken()).isNotNull();
      assertThat(tree.arguments()).hasSize(2);
      assertThat(tree.arguments()).hasSeparatorsSize(1);
      assertThat(tree.arguments().closeParenToken()).isNotNull();
      assertThat(tree).hasChildrenSize(2);
    }
    {
      MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { void m() { this.<T>identifier(true, false); } }");
      assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
      MemberSelectExpressionTree memberSelectExpression = (MemberSelectExpressionTree) tree.methodSelect();
      assertThat(memberSelectExpression.identifier().name()).isEqualTo("identifier");
      assertThat(tree.arguments()).hasSize(2);
      assertThat(tree).hasChildrenSize(3);
    }
    {
      MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { super.identifier(true, false); } }");
      assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
      MemberSelectExpressionTree memberSelectExpression = (MemberSelectExpressionTree) tree.methodSelect();
      assertThat(memberSelectExpression).hasChildrenSize(3);
      assertThat(memberSelectExpression.identifier().name()).isEqualTo("identifier");
      assertThat(memberSelectExpression.operatorToken()).isNotNull();
      assertThat(((IdentifierTree) memberSelectExpression.expression()).name()).isEqualTo("super");
      assertThat(tree.arguments()).hasSize(2);
      assertThat(tree).hasChildrenSize(2);
    }
    {
      MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { TypeName.super.identifier(true, false); } }");
      assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
      MemberSelectExpressionTree memberSelectExpression = (MemberSelectExpressionTree) tree.methodSelect();
      assertThat(memberSelectExpression.identifier().name()).isEqualTo("identifier");
      assertThat(memberSelectExpression.operatorToken()).isNotNull();
      memberSelectExpression = (MemberSelectExpressionTree) memberSelectExpression.expression();
      assertThat(memberSelectExpression).hasChildrenSize(3);
      assertThat(memberSelectExpression.identifier().name()).isEqualTo("super");
      assertThat(memberSelectExpression.operatorToken()).isNotNull();
      assertThat(((IdentifierTree) memberSelectExpression.expression()).name()).isEqualTo("TypeName");
      assertThat(tree.arguments()).hasSize(2);
      assertThat(tree).hasChildrenSize(2);
    }
    {
      MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { TypeName.identifier(true, false); } }");
      assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
      MemberSelectExpressionTree memberSelectExpression = (MemberSelectExpressionTree) tree.methodSelect();
      assertThat(memberSelectExpression).hasChildrenSize(3);
      assertThat(memberSelectExpression.identifier().name()).isEqualTo("identifier");
      assertThat(memberSelectExpression.operatorToken()).isNotNull();
      assertThat(((IdentifierTree) memberSelectExpression.expression()).name()).isEqualTo("TypeName");
      assertThat(tree.arguments()).hasSize(2);
      assertThat(tree).hasChildrenSize(2);
    }
    {
      MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { TypeName.<T>identifier(true, false); } }");
      assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
      MemberSelectExpressionTree memberSelectExpression = (MemberSelectExpressionTree) tree.methodSelect();
      assertThat(memberSelectExpression).hasChildrenSize(3);
      assertThat(memberSelectExpression.identifier().name()).isEqualTo("identifier");
      assertThat(memberSelectExpression.operatorToken()).isNotNull();
      assertThat(((IdentifierTree) memberSelectExpression.expression()).name()).isEqualTo("TypeName");
      assertThat(tree.arguments()).hasSize(2);
      assertThat(tree).hasChildrenSize(3);
    }
    {
      MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { primary().<T>identifier(true, false); } }");
      assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
      MemberSelectExpressionTree memberSelectExpression = (MemberSelectExpressionTree) tree.methodSelect();
      assertThat(memberSelectExpression).hasChildrenSize(3);
      assertThat(memberSelectExpression.identifier().name()).isEqualTo("identifier");
      assertThat(memberSelectExpression.expression()).isInstanceOf(MethodInvocationTree.class);
      assertThat(memberSelectExpression.operatorToken()).isNotNull();
      assertThat(tree.arguments()).hasSize(2);
      assertThat(tree).hasChildrenSize(3);
    }
  }

  /**
   * 8.8.7.1. Explicit Constructor Invocations
   */
  @Test
  void explicit_constructor_invocation() {
    // TODO test NonWildTypeArguments

    MethodInvocationTree tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { this(true, false); } }");
    assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
    assertThat(((IdentifierTree) tree.methodSelect()).name()).isEqualTo("this");
    assertThat(tree.arguments()).hasSize(2);

    tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { <T>this(true, false); } }");
    assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
    assertThat(((IdentifierTree) tree.methodSelect()).name()).isEqualTo("this");
    assertThat(tree.arguments()).hasSize(2);

    tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { super(true, false); } }");
    assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
    assertThat(((IdentifierTree) tree.methodSelect()).name()).isEqualTo("super");
    assertThat(tree.arguments()).hasSize(2);

    tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { <T>super(true, false); } }");
    assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
    assertThat(((IdentifierTree) tree.methodSelect()).name()).isEqualTo("super");
    assertThat(tree.arguments()).hasSize(2);

    tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { ClassName.super(true, false); } }");
    assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
    MemberSelectExpressionTree methodSelect = (MemberSelectExpressionTree) tree.methodSelect();
    assertThat(methodSelect).hasChildrenSize(3);
    assertThat(methodSelect.identifier().name()).isEqualTo("super");
    assertThat(methodSelect.operatorToken()).isNotNull();
    assertThat(((IdentifierTree) methodSelect.expression()).name()).isEqualTo("ClassName");
    assertThat(tree.arguments()).hasSize(2);

    tree = (MethodInvocationTree) expressionOfFirstStatement("class T { T() { ClassName.<T>super(true, false); } }");
    assertThat(tree).is(Tree.Kind.METHOD_INVOCATION);
    methodSelect = (MemberSelectExpressionTree) tree.methodSelect();
    assertThat(methodSelect).hasChildrenSize(3);
    assertThat(methodSelect.identifier().name()).isEqualTo("super");
    assertThat(methodSelect.operatorToken()).isNotNull();
    assertThat(((IdentifierTree) methodSelect.expression()).name()).isEqualTo("ClassName");
    assertThat(tree.arguments()).hasSize(2);
  }

  @Test
  void array_field() {
    VariableTree field;
    ArrayTypeTree arrayTypeTree, childArrayTypeTree;

    field = (VariableTree) firstTypeMember("class T { int[] a; }");
    assertThat(field).hasChildrenSize(4);
    assertThat(field.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) field.type();
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    field = (VariableTree) firstTypeMember("class T { int[][] a; }");
    assertThat(field).hasChildrenSize(4);
    assertThat(field.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) field.type();
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);
    assertThat(arrayTypeTree.type()).isInstanceOf(ArrayTypeTree.class);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    field = (VariableTree) firstTypeMember("class T { int @Foo [] a; }");
    assertThat(field).hasChildrenSize(4);
    assertThat(field.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) field.type();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);

    field = (VariableTree) firstTypeMember("class T { int @Foo @bar [] a; }");
    assertThat(field).hasChildrenSize(4);
    assertThat(field.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) field.type();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 2);
    assertThat(arrayTypeTree).hasChildrenSize(5);

    field = (VariableTree) firstTypeMember("class T { int[] @Foo [] a; }");
    assertThat(field).hasChildrenSize(4);
    assertThat(field.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) field.type();
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    field = (VariableTree) firstTypeMember("class T { int[] a[]; }");
    assertThat(field).hasChildrenSize(4);
    assertThat(field.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) field.type();
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    field = (VariableTree) firstTypeMember("class T { int[] a @Foo []; }");
    assertThat(field).hasChildrenSize(4);
    assertThat(field.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) field.type();
    assertThat(arrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
  }

  @Test
  void array_method_return_type() {
    MethodTree method;
    ArrayTypeTree arrayTypeTree, childArrayTypeTree;

    method = (MethodTree) firstTypeMember("class T { int[] m(); }");
    assertThat(method.returnType()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) method.returnType();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);

    method = (MethodTree) firstTypeMember("class T { int @Foo [] m(); }");
    assertThat(method.returnType()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) method.returnType();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);

    method = (MethodTree) firstTypeMember("class T { int @Foo @bar [] m(); }");
    assertThat(method.returnType()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) method.returnType();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 2);
    assertThat(arrayTypeTree).hasChildrenSize(5);

    method = (MethodTree) firstTypeMember("class T { int[] @Foo [] m(); }");
    assertThat(method.returnType()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) method.returnType();
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    method = (MethodTree) firstTypeMember("class T { int[] m()[]; }");
    assertThat(method.returnType()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) method.returnType();
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    method = (MethodTree) firstTypeMember("class T { int[] m() @Foo []; }");
    assertThat(method.returnType()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) method.returnType();
    assertThat(arrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
  }

  @Test
  void array_formal_parameter() {
    MethodTree method;
    VariableTree variable;
    ArrayTypeTree arrayTypeTree, childArrayTypeTree;

    method = (MethodTree) firstTypeMember("interface T { void m(int[] a); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3); // 1+2, as empty modifiers are always included
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);

    method = (MethodTree) firstTypeMember("interface T { void m(int... a); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasEllipsis(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(2);

    method = (MethodTree) firstTypeMember("interface T { void m(int @Foo ... a); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasEllipsisAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(3);

    method = (MethodTree) firstTypeMember("interface T { void m(int[] @Foo ... a); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    childArrayTypeTree = (ArrayTypeTree) variable.type();
    arrayTypeTree = (ArrayTypeTree) childArrayTypeTree.type();
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasEllipsisAndAnnotations(childArrayTypeTree, 1);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.ellipsisToken().column() > arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    method = (MethodTree) firstTypeMember("interface T { void m(int @Foo [] a); }");
    assertThat(variable).hasChildrenSize(3);
    variable = method.parameters().get(0);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);

    method = (MethodTree) firstTypeMember("interface T { void m(int @Foo @bar [] a); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThat(arrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 2);
    assertThat(arrayTypeTree).hasChildrenSize(5);

    method = (MethodTree) firstTypeMember("interface T { void m(int[] @Foo [] a); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    method = (MethodTree) firstTypeMember("interface T { void m(int[] a[]); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThatArrayTypeHasBrackets(arrayTypeTree);
    assertThat(arrayTypeTree).hasChildrenSize(3);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);

    method = (MethodTree) firstTypeMember("interface T { void m(int[] a @Foo []); }");
    variable = method.parameters().get(0);
    assertThat(variable).hasChildrenSize(3);
    assertThat(variable.type()).isInstanceOf(ArrayTypeTree.class);
    arrayTypeTree = (ArrayTypeTree) variable.type();
    assertThat(arrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBracketsAndAnnotations(arrayTypeTree, 1);
    assertThat(arrayTypeTree).hasChildrenSize(4);
    childArrayTypeTree = (ArrayTypeTree) arrayTypeTree.type();
    assertThat(childArrayTypeTree).isInstanceOf(ArrayTypeTree.class);
    assertThatArrayTypeHasBrackets(childArrayTypeTree);
    assertThat(childArrayTypeTree).hasChildrenSize(3);
    assertThat(childArrayTypeTree.openBracketToken().column() < arrayTypeTree.openBracketToken().column()).isTrue();
    assertThat(childArrayTypeTree.type()).isInstanceOf(PrimitiveTypeTree.class);
  }

  /**
   * 15.13. Array Access Expressions
   */
  @Test
  void array_access_expression() {
    String code = "class T { T() { return a[42]; } }";
    ArrayAccessExpressionTree tree = (ArrayAccessExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.ARRAY_ACCESS_EXPRESSION);
    assertThat(tree).hasChildrenSize(2);
    assertThat(tree.expression()).isNotNull();
    ArrayDimensionTree dimension = tree.dimension();
    assertThat(dimension).isNotNull();
    assertThat(dimension.openBracketToken()).is("[");
    assertThat(dimension.expression()).is(Tree.Kind.INT_LITERAL);
    assertThat(dimension.closeBracketToken()).is("]");
    assertThat(dimension).hasChildrenSize(3);
  }

  /**
   * 15.14. Postfix Expressions
   */
  @Test
  void postfix_expression() {
    UnaryExpressionTree tree;
    tree = (UnaryExpressionTree) ((ExpressionStatementTree) firstMethodFirstStatement(("class T { void m() { i++; } }"))).expression();
    assertThat(tree).is(Tree.Kind.POSTFIX_INCREMENT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.operatorToken()).is("++");
    assertThat(tree).hasChildrenSize(2);

    tree = (UnaryExpressionTree) ((ExpressionStatementTree) firstMethodFirstStatement(("class T { void m() { i--; } }"))).expression();
    assertThat(tree).is(Tree.Kind.POSTFIX_DECREMENT);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.operatorToken()).is("--");
    assertThat(tree).hasChildrenSize(2);
  }

  /**
   * 15.15. Unary Operators
   */
  @Test
  void unary_operators() {
    UnaryExpressionTree tree;
    tree = (UnaryExpressionTree) ((ExpressionStatementTree) firstMethodFirstStatement(("class T { void m() { ++i; } }"))).expression();
    assertThat(tree).is(Tree.Kind.PREFIX_INCREMENT);
    assertThat(tree.operatorToken()).is("++");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree).hasChildrenSize(2);

    tree = (UnaryExpressionTree) ((ExpressionStatementTree) firstMethodFirstStatement(("class T { void m() { --i; } }"))).expression();
    assertThat(tree).is(Tree.Kind.PREFIX_DECREMENT);
    assertThat(tree.operatorToken()).is("--");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree).hasChildrenSize(2);
  }

  /**
   * 15.16. Cast Expressions
   */
  @Test
  void type_cast() {
    TypeCastTree tree = (TypeCastTree) expressionOfReturnStatement("class T { boolean m() { return (Boolean) true; } }");
    assertThat(tree).is(Tree.Kind.TYPE_CAST);
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.type()).isNotNull();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree).hasChildrenSize(5);

    tree = (TypeCastTree) expressionOfReturnStatement("class T { boolean m() { return (Foo<T> & Bar) true; } }");
    assertThat(tree).is(Tree.Kind.TYPE_CAST);
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.type()).isNotNull();
    assertThat(tree.andToken()).isNotNull();
    assertThat(tree.bounds()).hasSize(1);
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree).hasChildrenSize(6);

    tree = (TypeCastTree) expressionOfReturnStatement("class T { boolean m() { return (Foo<T> & @Gul Bar) true; } }");
    assertThat(tree).is(Tree.Kind.TYPE_CAST);
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.type()).isNotNull();
    assertThat(tree.andToken()).isNotNull();
    assertThat(tree.bounds()).hasSize(1);
    assertThat(tree.bounds().separators()).isEmpty();
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree).hasChildrenSize(6);

    tree = (TypeCastTree) expressionOfReturnStatement("class T { boolean m() { return (Foo<T> & @Gul Bar & Qix & Plop) true; } }");
    assertThat(tree).is(Tree.Kind.TYPE_CAST);
    assertThat(tree.openParenToken()).is("(");
    assertThat(tree.type()).isNotNull();
    assertThat(tree.andToken()).isNotNull();
    assertThat(tree.bounds()).hasSize(3);
    assertThat(tree.bounds()).hasSeparatorsSize(2);
    assertThat(tree.closeParenToken()).is(")");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree).hasChildrenSize(6);
  }

  /**
   * 15.17. Multiplicative Operators
   */
  @Test
  void multiplicative_expression() {
    String code = "class T { int m() { return 1 * 2 / 3 % 4; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.REMAINDER);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("%");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.DIVIDE);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("/");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.MULTIPLY);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("*");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.18. Additive Operators
   */
  @Test
  void additive_expression() {
    String code = "class T { int m() { return 1 + 2 - 3; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.MINUS);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("-");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree.rightOperand()).is(Tree.Kind.INT_LITERAL);
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.PLUS);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("+");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.19. Shift Operators
   */
  @Test
  void shift_expression() {
    String code = "class T { int m() { return 1 >> 2 << 3 >>> 4; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.UNSIGNED_RIGHT_SHIFT);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is(">>>");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.LEFT_SHIFT);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("<<");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.RIGHT_SHIFT);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is(">>");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.20. Relational Operators
   */
  @Test
  void relational_expression() {
    String code = "class T { boolean m() { return 1 < 2 > 3; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.GREATER_THAN);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is(">");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.LESS_THAN);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("<");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.20.2. Type Comparison Operator instanceof
   */
  @Test
  void instanceof_expression() {
    InstanceOfTree tree = (InstanceOfTree) expressionOfReturnStatement("class T { boolean m() { return null instanceof Object; } }");
    assertThat(tree).is(Tree.Kind.INSTANCE_OF);
    assertThat(tree.expression()).isNotNull();
    assertThat(tree.instanceofKeyword()).is("instanceof");
    assertThat(tree.type()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.21. Equality Operators
   */
  @Test
  void equality_expression() {
    String code = "class T { boolean m() { return false == false != true; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.NOT_EQUAL_TO);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("!=");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.EQUAL_TO);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("==");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.22. Bitwise and Logical Operators
   */
  @Test
  void bitwise_and_logical_operators() {
    String code = "class T { int m() { return 1 & 2 & 3; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.AND);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("&");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.AND);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("&");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);

    code = "class T { int m() { return 1 ^ 2 ^ 3; } }";
    tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.XOR);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("^");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.XOR);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("^");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);

    code = "class T { int m() { return 1 | 2 | 3; } }";
    tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.OR);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("|");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.OR);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("|");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.23. Conditional-And Operator &&
   */
  @Test
  void conditional_and_expression() {
    String code = "class T { boolean m() { return false && false && true; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.CONDITIONAL_AND);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("&&");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.CONDITIONAL_AND);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("&&");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.24. Conditional-Or Operator ||
   */
  @Test
  void conditional_or_expression() {
    String code = "class T { boolean m() { return false || false || true; } }";
    BinaryExpressionTree tree = (BinaryExpressionTree) expressionOfReturnStatement(code);
    assertThat(tree).is(Tree.Kind.CONDITIONAL_OR);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("||");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
    tree = (BinaryExpressionTree) tree.leftOperand();
    assertThat(tree).is(Tree.Kind.CONDITIONAL_OR);
    assertThat(tree.leftOperand()).isNotNull();
    assertThat(tree.operatorToken()).is("||");
    assertThat(tree.rightOperand()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  /**
   * 15.25. Conditional Operator ? :
   */
  @Test
  void conditional_expression() {
    ConditionalExpressionTree tree;
    tree = (ConditionalExpressionTree) expressionOfReturnStatement("class T { boolean m() { return true ? true : false; } }");
    assertThat(tree).is(Tree.Kind.CONDITIONAL_EXPRESSION);
    assertThat(tree.condition()).isInstanceOf(LiteralTree.class);
    assertThat(tree.questionToken()).is("?");
    assertThat(tree.trueExpression()).isInstanceOf(LiteralTree.class);
    assertThat(tree.colonToken()).is(":");
    assertThat(tree.falseExpression()).isInstanceOf(LiteralTree.class);
    assertThat(tree).hasChildrenSize(5);

    tree = (ConditionalExpressionTree) expressionOfReturnStatement("class T { boolean m() { return true ? true : false ? true : false; } }");
    assertThat(tree).is(Tree.Kind.CONDITIONAL_EXPRESSION);
    assertThat(tree.condition()).isInstanceOf(LiteralTree.class);
    assertThat(tree.trueExpression()).isInstanceOf(LiteralTree.class);
    assertThat(tree.falseExpression()).isInstanceOf(ConditionalExpressionTree.class);
    assertThat(tree).hasChildrenSize(5);
    tree = (ConditionalExpressionTree) tree.falseExpression();
    assertThat(tree).is(Tree.Kind.CONDITIONAL_EXPRESSION);
    assertThat(tree.condition()).isInstanceOf(LiteralTree.class);
    assertThat(tree.trueExpression()).isInstanceOf(LiteralTree.class);
    assertThat(tree.falseExpression()).isInstanceOf(LiteralTree.class);
    assertThat(tree).hasChildrenSize(5);
  }

  /**
   * 15.26. Assignment Operators
   */
  @Test
  void assignment_expression() {
    String code = "class T { void m() { a += 42; } }";
    AssignmentExpressionTree tree = (AssignmentExpressionTree) ((ExpressionStatementTree) firstMethodFirstStatement(code)).expression();
    assertThat(tree).is(Tree.Kind.PLUS_ASSIGNMENT);
    assertThat(tree.variable()).isNotNull();
    assertThat(tree.operatorToken()).is("+=");
    assertThat(tree.expression()).isNotNull();
    assertThat(tree).hasChildrenSize(3);
  }

  @Test
  void method_reference_expression_should_not_break_AST() throws Exception {
    String code = "class T { public void meth(){IntStream.range(1,12).map(new MethodReferences()::<String>square).map(super::myMethod).map(int[]::new).forEach(System.out::println);}}";
    MethodInvocationTree mit = (MethodInvocationTree) ((ExpressionStatementTree) firstMethodFirstStatement(code)).expression();

    MethodReferenceTree mrt = (MethodReferenceTree) mit.arguments().get(0);
    assertThat(mrt.expression()).is(Tree.Kind.MEMBER_SELECT);
    assertThat(mrt.doubleColon()).isNotNull();
    assertThat(mrt).hasChildrenSize(3);

    mit = (MethodInvocationTree) ((MemberSelectExpressionTree) mit.methodSelect()).expression();
    mrt = (MethodReferenceTree) mit.arguments().get(0);
    assertThat(mrt.expression()).is(Tree.Kind.ARRAY_TYPE);
    assertThat(mrt.doubleColon()).isNotNull();
    assertThat(mrt.method().name()).isEqualTo("new");
    assertThat(mrt).hasChildrenSize(3);

    mit = (MethodInvocationTree) ((MemberSelectExpressionTree) mit.methodSelect()).expression();
    mrt = (MethodReferenceTree) mit.arguments().get(0);
    assertThat(mrt.expression()).is(Tree.Kind.IDENTIFIER);
    assertThat(((IdentifierTree) mrt.expression()).name()).isEqualTo("super");
    assertThat(mrt.doubleColon()).isNotNull();
    assertThat(mrt).hasChildrenSize(3);

    mit = (MethodInvocationTree) ((MemberSelectExpressionTree) mit.methodSelect()).expression();
    mrt = (MethodReferenceTree) mit.arguments().get(0);
    assertThat(mrt.expression()).is(Tree.Kind.NEW_CLASS);
    assertThat(mrt.doubleColon()).isNotNull();
    assertThat(mrt.typeArguments()).isNotNull();
    assertThat(mrt.method().name()).isEqualTo("square");
    assertThat(mrt).hasChildrenSize(4);
  }

  @Test
  void lambda_expressions() {
    String code = "class T { public void meth(){IntStream.range(1,12).map(x->x*x).map((int a)-> {return a*a;});}}";
    ExpressionTree expressionTree = ((ExpressionStatementTree) firstMethodFirstStatement(code)).expression();

    // parsing not broken by lambda
    assertThat(expressionTree).isNotNull();

    MethodInvocationTree mit = (MethodInvocationTree) expressionTree;
    LambdaExpressionTree tree = (LambdaExpressionTree) mit.arguments().get(0);
    assertThat(tree.openParenToken()).isNotNull();
    assertThat(tree.parameters()).hasSize(1);
    assertThat(tree.parameters().get(0)).is(Tree.Kind.VARIABLE);
    assertThat(tree.closeParenToken()).isNotNull();
    assertThat(tree.arrowToken()).isNotNull();
    assertThat(tree.body()).is(Tree.Kind.BLOCK);

    tree = (LambdaExpressionTree) ((MethodInvocationTree) ((MemberSelectExpressionTree) mit.methodSelect()).expression()).arguments().get(0);
    assertThat(tree.openParenToken()).isNull();
    assertThat(tree.parameters()).hasSize(1);
    assertThat(tree.parameters().get(0)).is(Tree.Kind.VARIABLE);
    assertThat(tree.closeParenToken()).isNull();
    assertThat(tree.arrowToken()).isNotNull();
    assertThat(tree.body()).is(Tree.Kind.MULTIPLY);
  }

  @Test
  void type_parameters_tokens() {
    ParameterizedTypeTree tree = (ParameterizedTypeTree) firstType("class Foo<E> extends List<E> {}").superClass();
    assertThat(tree).isNotNull();
    assertThat(tree).hasChildrenSize(2);
    TypeArguments typeArguments = tree.typeArguments();
    assertThat(typeArguments)
      .isNotNull()
      .hasSize(1);
    assertThat(typeArguments.separators()).isEmpty();
    assertThat(typeArguments.openBracketToken()).isNotNull();
    assertThat(typeArguments.closeBracketToken()).isNotNull();
    assertThat(typeArguments).hasChildrenSize(3);

    tree = (ParameterizedTypeTree) firstType("class Mop<K,V> implements Map<K,V> {}").superInterfaces().get(0);
    assertThat(tree).isNotNull();
    assertThat(tree).hasChildrenSize(2);
    typeArguments = tree.typeArguments();
    assertThat(typeArguments)
      .isNotNull()
      .hasSize(2);
    assertThat(typeArguments).hasSeparatorsSize(1);
    assertThat(typeArguments.openBracketToken()).isNotNull();
    assertThat(typeArguments.closeBracketToken()).isNotNull();
    assertThat(typeArguments).hasChildrenSize(5);
  }

  @Test
  void type_parameters_and_bounds() {
    TypeParameterListTreeImpl tree = (TypeParameterListTreeImpl) firstType("class Foo<T, U extends Object & Number> {}").typeParameters();
    assertThat(tree.openBracketToken()).is("<");
    assertThat(tree.closeBracketToken()).is(">");
    assertThat(tree).hasSize(2);
    assertThat(tree).hasSeparatorsSize(1);
    assertThat(tree).hasChildrenSize(5);

    TypeParameterTree param = tree.get(0);
    assertThat(param.identifier().name()).isEqualTo("T");
    assertThat(param.bounds()).isEmpty();
    assertThat(param.bounds().separators()).isEmpty();
    assertThat(param).hasChildrenSize(1);

    param = tree.get(1);
    assertThat(param.identifier().name()).isEqualTo("U");
    assertThat(param.bounds()).hasSize(2);
    assertThat(param.bounds()).hasSeparatorsSize(1);
    assertThat(((IdentifierTree) param.bounds().get(0)).name()).isEqualTo("Object");
    assertThat(((IdentifierTree) param.bounds().get(1)).name()).isEqualTo("Number");
    assertThat(param).hasChildrenSize(3);
  }

  private static ExpressionTree expressionOfReturnStatement(String code) {
    return ((ReturnStatementTree) firstMethodFirstStatement(code)).expression();
  }

  private static ExpressionTree expressionOfFirstStatement(String code) {
    return ((ExpressionStatementTree) firstMethodFirstStatement(code)).expression();
  }

  private static StatementTree firstMethodFirstStatement(String code) {
    return ((MethodTree) firstTypeMember(code)).block().body().get(0);
  }

  private static Tree firstTypeMember(String code) {
    return firstType(code).members().get(0);
  }

  private static ClassTree firstType(String code) {
    return (ClassTree) compilationUnit(code).types().get(0);
  }

  private static CompilationUnitTree compilationUnit(String code) {
    return JParserTestUtils.parse(code);
  }

  private static void assertThatArrayTypeHasBrackets(ArrayTypeTree tree) {
    assertThatArrayTypeHasBrackets(tree, 0, false);
  }

  private static void assertThatArrayTypeHasBracketsAndAnnotations(ArrayTypeTree tree, int numberAnnotations) {
    assertThatArrayTypeHasBrackets(tree, numberAnnotations, false);
  }

  private static void assertThatArrayTypeHasEllipsisAndAnnotations(ArrayTypeTree tree, int numberAnnotations) {
    assertThatArrayTypeHasBrackets(tree, numberAnnotations, true);
  }

  private static void assertThatArrayTypeHasEllipsis(ArrayTypeTree tree) {
    assertThatArrayTypeHasBrackets(tree, 0, true);
  }

  private static void assertThatArrayTypeHasBrackets(ArrayTypeTree tree, int numberAnnotations, boolean shouldHaveEllipsis) {
    assertThat(tree.annotations()).hasSize(numberAnnotations);

    if (shouldHaveEllipsis) {
      assertThat(tree.ellipsisToken()).is("...");
      assertThat(tree.openBracketToken()).isNull();
      assertThat(tree.closeBracketToken()).isNull();

    } else {
      assertThat(tree.ellipsisToken()).isNull();
      assertThat(tree.openBracketToken()).is("[");
      assertThat(tree.closeBracketToken()).is("]");
    }
  }
}

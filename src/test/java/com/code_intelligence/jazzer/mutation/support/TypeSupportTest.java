/*
 * Copyright 2024 Code Intelligence GmbH
 *
 * By downloading, you agree to the Code Intelligence Jazzer Terms and Conditions.
 *
 * The Code Intelligence Jazzer Terms and Conditions are provided in LICENSE-JAZZER.txt
 * located in the root directory of the project.
 */

package com.code_intelligence.jazzer.mutation.support;

import static com.code_intelligence.jazzer.mutation.support.TypeSupport.annotatedTypeEquals;
import static com.code_intelligence.jazzer.mutation.support.TypeSupport.asAnnotatedType;
import static com.code_intelligence.jazzer.mutation.support.TypeSupport.asSubclassOrEmpty;
import static com.code_intelligence.jazzer.mutation.support.TypeSupport.containedInDirectedCycle;
import static com.code_intelligence.jazzer.mutation.support.TypeSupport.visitAnnotatedType;
import static com.code_intelligence.jazzer.mutation.support.TypeSupport.withExtraAnnotations;
import static com.code_intelligence.jazzer.mutation.support.TypeSupport.withTypeArguments;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.code_intelligence.jazzer.mutation.annotation.NotNull;
import com.code_intelligence.jazzer.mutation.annotation.WithSize;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

class TypeSupportTest {
  @Test
  void testFillTypeVariablesRawType_oneVariable() {
    AnnotatedParameterizedType actual =
        withTypeArguments(
            new TypeHolder<@NotNull List>() {}.annotatedType(),
            new TypeHolder<@NotNull String>() {}.annotatedType());
    AnnotatedParameterizedType expected =
        (AnnotatedParameterizedType)
            new TypeHolder<@NotNull List<@NotNull String>>() {}.annotatedType();

    // Test both equals implementations as we implement them ourselves.
    assertThat(actual.getType()).isEqualTo(expected.getType());
    assertThat(expected.getType()).isEqualTo(actual.getType());

    assertThat(actual.getAnnotations()).isEqualTo(expected.getAnnotations());
    assertThat(expected.getAnnotations()).isEqualTo(actual.getAnnotations());

    assertThat(((ParameterizedType) actual.getType()).getActualTypeArguments())
        .isEqualTo(((ParameterizedType) expected.getType()).getActualTypeArguments());
    assertThat(((ParameterizedType) expected.getType()).getActualTypeArguments())
        .isEqualTo(((ParameterizedType) actual.getType()).getActualTypeArguments());
  }

  @Test
  // Java <= 11 does not implement AnnotatedType#equals.
  // https://github.com/openjdk/jdk/commit/ab0128ca51de59aaaa674654ca8d4e16b3b79965
  @EnabledForJreRange(min = JRE.JAVA_12)
  void testFillTypeVariablesAnnotatedType_oneVariable() {
    AnnotatedParameterizedType actual =
        withTypeArguments(
            new TypeHolder<@NotNull List>() {}.annotatedType(),
            new TypeHolder<@NotNull String>() {}.annotatedType());
    AnnotatedParameterizedType expected =
        (AnnotatedParameterizedType)
            new TypeHolder<@NotNull List<@NotNull String>>() {}.annotatedType();

    // Test both equals implementations as we implement them ourselves.
    assertThat(actual).isEqualTo(expected);
    assertThat(expected).isEqualTo(actual);

    assertThat(actual.getType()).isEqualTo(expected.getType());
    assertThat(expected.getType()).isEqualTo(actual.getType());

    assertThat(actual.getAnnotations()).isEqualTo(expected.getAnnotations());
    assertThat(expected.getAnnotations()).isEqualTo(actual.getAnnotations());

    assertThat(actual.getAnnotatedActualTypeArguments())
        .isEqualTo(expected.getAnnotatedActualTypeArguments());
    assertThat(expected.getAnnotatedActualTypeArguments())
        .isEqualTo(actual.getAnnotatedActualTypeArguments());
  }

  @Test
  void testFillTypeVariablesRawType_oneVariable_differentType() {
    AnnotatedParameterizedType actual =
        withTypeArguments(
            new TypeHolder<@NotNull List>() {}.annotatedType(),
            new TypeHolder<@NotNull String>() {}.annotatedType());
    AnnotatedParameterizedType differentParameterAnnotation =
        (AnnotatedParameterizedType)
            new TypeHolder<@NotNull List<@NotNull Boolean>>() {}.annotatedType();

    // Test both equals implementations as we implement them ourselves.
    assertThat(actual.getType()).isNotEqualTo(differentParameterAnnotation.getType());
    assertThat(differentParameterAnnotation.getType()).isNotEqualTo(actual.getType());

    assertThat(actual.getAnnotations()).isEqualTo(differentParameterAnnotation.getAnnotations());
    assertThat(differentParameterAnnotation.getAnnotations()).isEqualTo(actual.getAnnotations());

    assertThat(((ParameterizedType) actual.getType()).getActualTypeArguments())
        .isNotEqualTo(
            ((ParameterizedType) differentParameterAnnotation.getType()).getActualTypeArguments());
    assertThat(
            ((ParameterizedType) differentParameterAnnotation.getType()).getActualTypeArguments())
        .isNotEqualTo(((ParameterizedType) actual.getType()).getActualTypeArguments());
  }

  @Test
  // Java <= 11 does not implement AnnotatedType#equals.
  // https://github.com/openjdk/jdk/commit/ab0128ca51de59aaaa674654ca8d4e16b3b79965
  @EnabledForJreRange(min = JRE.JAVA_12)
  void testFillTypeVariablesAnnotatedType_oneVariable_differentAnnotations() {
    AnnotatedParameterizedType actual =
        withTypeArguments(
            new TypeHolder<@NotNull List>() {}.annotatedType(),
            new TypeHolder<@NotNull String>() {}.annotatedType());
    AnnotatedParameterizedType differentParameterAnnotation =
        (AnnotatedParameterizedType) new TypeHolder<@NotNull List<String>>() {}.annotatedType();

    // Test both equals implementations as we implement them ourselves.
    assertThat(actual).isNotEqualTo(differentParameterAnnotation);
    assertThat(differentParameterAnnotation).isNotEqualTo(actual);

    assertThat(actual.getType()).isEqualTo(differentParameterAnnotation.getType());
    assertThat(differentParameterAnnotation.getType()).isEqualTo(actual.getType());

    assertThat(actual.getAnnotations()).isEqualTo(differentParameterAnnotation.getAnnotations());
    assertThat(differentParameterAnnotation.getAnnotations()).isEqualTo(actual.getAnnotations());

    assertThat(actual.getAnnotatedActualTypeArguments())
        .isNotEqualTo(differentParameterAnnotation.getAnnotatedActualTypeArguments());
    assertThat(differentParameterAnnotation.getAnnotatedActualTypeArguments())
        .isNotEqualTo(actual.getAnnotatedActualTypeArguments());
  }

  @Test
  void testFillTypeVariablesRawType_twoVariables() {
    AnnotatedParameterizedType actual =
        withTypeArguments(
            new TypeHolder<@NotNull Map>() {}.annotatedType(),
            new TypeHolder<@NotNull String>() {}.annotatedType(),
            new TypeHolder<byte[]>() {}.annotatedType());
    AnnotatedParameterizedType expected =
        (AnnotatedParameterizedType)
            new TypeHolder<@NotNull Map<@NotNull String, byte[]>>() {}.annotatedType();

    // Test both equals implementations as we implement them ourselves.
    assertThat(actual.getType()).isEqualTo(expected.getType());
    assertThat(expected.getType()).isEqualTo(actual.getType());

    assertThat(actual.getAnnotations()).isEqualTo(expected.getAnnotations());
    assertThat(expected.getAnnotations()).isEqualTo(actual.getAnnotations());

    assertThat(((ParameterizedType) actual.getType()).getActualTypeArguments())
        .isEqualTo(((ParameterizedType) expected.getType()).getActualTypeArguments());
    assertThat(((ParameterizedType) expected.getType()).getActualTypeArguments())
        .isEqualTo(((ParameterizedType) actual.getType()).getActualTypeArguments());
  }

  @Test
  // Java <= 11 does not implement AnnotatedType#equals.
  // https://github.com/openjdk/jdk/commit/ab0128ca51de59aaaa674654ca8d4e16b3b79965
  @EnabledForJreRange(min = JRE.JAVA_12)
  void testFillTypeVariablesAnnotatedType_twoVariables() {
    AnnotatedParameterizedType actual =
        withTypeArguments(
            new TypeHolder<@NotNull Map>() {}.annotatedType(),
            new TypeHolder<@NotNull String>() {}.annotatedType(),
            new TypeHolder<byte[]>() {}.annotatedType());
    AnnotatedParameterizedType expected =
        (AnnotatedParameterizedType)
            new TypeHolder<@NotNull Map<@NotNull String, byte[]>>() {}.annotatedType();

    // Test both equals implementations as we implement them ourselves.
    assertThat(actual).isEqualTo(expected);
    assertThat(expected).isEqualTo(actual);

    assertThat(actual.getType()).isEqualTo(expected.getType());
    assertThat(expected.getType()).isEqualTo(actual.getType());

    assertThat(actual.getAnnotations()).isEqualTo(expected.getAnnotations());
    assertThat(expected.getAnnotations()).isEqualTo(actual.getAnnotations());

    assertThat(actual.getAnnotatedActualTypeArguments())
        .isEqualTo(expected.getAnnotatedActualTypeArguments());
    assertThat(expected.getAnnotatedActualTypeArguments())
        .isEqualTo(actual.getAnnotatedActualTypeArguments());
  }

  @Test
  void testFillTypeVariables_failures() {
    assertThrows(
        IllegalArgumentException.class,
        () -> withTypeArguments(new TypeHolder<List>() {}.annotatedType()));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            withTypeArguments(
                new TypeHolder<List<?>>() {}.annotatedType(), asAnnotatedType(String.class)));
  }

  @Test
  void testAsSubclassOrEmpty() {
    assertThat(asSubclassOrEmpty(asAnnotatedType(String.class), String.class))
        .hasValue(String.class);
    assertThat(asSubclassOrEmpty(asAnnotatedType(String.class), CharSequence.class))
        .hasValue(String.class);
    assertThat(asSubclassOrEmpty(asAnnotatedType(CharSequence.class), String.class)).isEmpty();
    assertThat(asSubclassOrEmpty(new TypeHolder<List<String>>() {}.annotatedType(), List.class))
        .isEmpty();
  }

  @Target(ElementType.TYPE_USE)
  @Retention(RetentionPolicy.RUNTIME)
  private @interface A {
    int value();
  }

  @Test
  void testVisitAnnotatedType() {
    Map<Integer, Class<?>> visited = new LinkedHashMap<>();
    AnnotatedType type =
        new TypeHolder<
            @A(1) List<
                @A(2) Map<@A(3) byte @A(4) [] @A(5) [], @A(6) Byte> @A(7) [] @A(8)
                    []>>() {}.annotatedType();

    visitAnnotatedType(
        type,
        (clazz, annotations) ->
            stream(annotations)
                .map(annotation -> ((A) annotation).value())
                .forEach(value -> visited.put(value, clazz)));

    assertThat(visited)
        .containsExactly(
            1,
            List.class,
            7,
            Map[][].class,
            8,
            Map[].class,
            2,
            Map.class,
            4,
            byte[][].class,
            5,
            byte[].class,
            3,
            byte.class,
            6,
            Byte.class)
        .inOrder();
  }

  @Test
  void testContainedInDirectedCycle() {
    Function<Integer, Stream<Integer>> successors =
        integer -> {
          switch (integer) {
            case 1:
              return Stream.of(2);
            case 2:
              return Stream.of(3);
            case 3:
              return Stream.of(4, 5);
            case 4:
              return Stream.of(2);
            case 5:
              return Stream.empty();
            default:
              throw new IllegalStateException();
          }
        };

    assertThat(containedInDirectedCycle(1, successors)).isFalse();
    assertThat(containedInDirectedCycle(2, successors)).isTrue();
    assertThat(containedInDirectedCycle(3, successors)).isTrue();
    assertThat(containedInDirectedCycle(4, successors)).isTrue();
    assertThat(containedInDirectedCycle(5, successors)).isFalse();
  }

  void doTestWithExtraAnnotationsWithCustomEquality(
      BiPredicate<AnnotatedType, AnnotatedType> equality) {
    Annotation NOT_NULL =
        new TypeHolder<@NotNull String>() {}.annotatedType().getAnnotation(NotNull.class);
    Annotation WITH_SIZE =
        new TypeHolder<@WithSize(min = 42) String>() {}.annotatedType()
            .getAnnotation(WithSize.class);
    assertThat(
            equality.test(
                withExtraAnnotations(new TypeHolder<Boolean>() {}.annotatedType(), NOT_NULL),
                new TypeHolder<@NotNull Boolean>() {}.annotatedType()))
        .isTrue();
    assertThat(
            equality.test(
                withExtraAnnotations(new TypeHolder<Boolean>() {}.annotatedType()),
                new TypeHolder<@NotNull Boolean>() {}.annotatedType()))
        .isFalse();
    assertThat(
            equality.test(
                withExtraAnnotations(
                    new TypeHolder<@NotNull Map<String, @NotNull String>>() {}.annotatedType(),
                    WITH_SIZE),
                new TypeHolder<
                    @NotNull @WithSize(min = 42) Map<
                        String, @NotNull String>>() {}.annotatedType()))
        .isTrue();
    assertThat(
            equality.test(
                withExtraAnnotations(
                    new TypeHolder<Map<String, @NotNull String>>() {}.annotatedType(),
                    NOT_NULL,
                    WITH_SIZE),
                new TypeHolder<
                    @NotNull @WithSize(min = 42) Map<
                        String, @NotNull String>>() {}.annotatedType()))
        .isTrue();
    assertThat(
            equality.test(
                withExtraAnnotations(
                    new TypeHolder<@NotNull Map<String, @NotNull String>>() {}.annotatedType(),
                    WITH_SIZE),
                new TypeHolder<
                    @WithSize(min = 42) @NotNull Map<
                        String, @NotNull String>>() {}.annotatedType()))
        .isFalse();
    assertThat(
            equality.test(
                withExtraAnnotations(
                    new TypeHolder<Map<String, @NotNull String>>() {}.annotatedType(), NOT_NULL),
                new TypeHolder<@NotNull Map>() {}.annotatedType()))
        .isFalse();
    assertThat(
            equality.test(
                withExtraAnnotations(
                    new TypeHolder<@NotNull Byte[]>() {}.annotatedType(), NOT_NULL),
                new TypeHolder<@NotNull Byte @NotNull []>() {}.annotatedType()))
        .isTrue();
    assertThat(
            equality.test(
                withExtraAnnotations(
                    new TypeHolder<@NotNull Byte[]>() {}.annotatedType(), NOT_NULL),
                new TypeHolder<@NotNull Byte @NotNull []>() {}.annotatedType()))
        .isTrue();
  }

  @Test
  void testWithExtraAnnotationsWithBackportedEquality() {
    doTestWithExtraAnnotationsWithCustomEquality(TypeSupport::annotatedTypeEquals);
  }

  @Test
  void testWithExtraAnnotationsWithBackportedEquality_flipped() {
    doTestWithExtraAnnotationsWithCustomEquality((a, b) -> annotatedTypeEquals(b, a));
  }

  // The tests below verify that the equals implementation of the AnnotatedType implementations
  // shipped with Java 12+ are compatible with our annotatedTypeEquals. While we do not use equals
  // in our code, this is additional assurance that our custom implementation is correct and also
  // allows us to migrate to the standard equals in the (far) future.

  // Java <= 11 does not implement AnnotatedType#equals.
  // https://github.com/openjdk/jdk/commit/ab0128ca51de59aaaa674654ca8d4e16b3b79965
  @Test
  @EnabledForJreRange(min = JRE.JAVA_12)
  void testWithExtraAnnotationsWithEquals() {
    doTestWithExtraAnnotationsWithCustomEquality(Objects::equals);
  }

  // Java <= 11 does not implement AnnotatedType#equals.
  // https://github.com/openjdk/jdk/commit/ab0128ca51de59aaaa674654ca8d4e16b3b79965
  @Test
  @EnabledForJreRange(min = JRE.JAVA_12)
  void testWithExtraAnnotationsWithEquals_flipped() {
    doTestWithExtraAnnotationsWithCustomEquality((a, b) -> Objects.equals(b, a));
  }
}

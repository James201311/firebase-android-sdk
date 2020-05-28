// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.firebase.decoders;

import androidx.annotation.NonNull;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

//TODO: implement hashCode(), equals(), and toString().

/**
 *  {@link TypeToken} is used to represent types supported by the library in a type-safe manner.
 *
 * <p> {@link TypeToken} supports the following types:
 * <ol>
 *   <li>Primitive types</li>
 *   <li>Array Types: primitive arrays(i.e. {@code int[]}) and object arrays(i.e. {@code Foo[]})</li>
 *   <li>Plain class types. i.e. {@code Foo}</li>
 *   <li>Generic types. i.e. {@code Foo<String, Double>}</li>
 *   <li>Wildcard types: only support {@code Foo<? extend Bar>} by downgrading it to {@code Foo<Bar>},
 *   and throw exception when stumbled upon {@code Foo<? super Bar>}.
 *   </li>
 * </ol>
 */
public abstract class TypeToken<T> {

  /**
   * Return an {@link TypeToken} to represent generic type {@code T}.
   *
   * <p>For example:
   * <p>Create an {@code TypeToken<List<String>} of generic type {@code List<String>}:
   * <p>{@code TypeToken<Link<String>> token = TypeToken.of(new Safe<Link<String>>() {});}
   * */
  @NonNull
  public static <T> TypeToken<T> of(@NonNull Safe<T> token) {
    Type superclass = token.getClass().getGenericSuperclass();
    if (superclass instanceof Class) {
      throw new RuntimeException("Missing type parameters");
    }
    Type type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    return of(type);
  }

  @NonNull
  private static <T> TypeToken<T> of(@NonNull Type type) {
    if(type instanceof WildcardType) {
      if(((WildcardType) type).getLowerBounds().length == 0) {
        return of(((WildcardType) type).getUpperBounds()[0]);
      }
      throw new RuntimeException("<? super T> is not supported");
    }

    if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return new ArrayToken<T>(TypeToken.of(componentType));
    }

    //Regular Class Type || Primitive Type || Non-Generic Array Type
    if (type instanceof Class<?>) {
      Class<T> typeToken = (Class<T>) type;
      return of(typeToken);
    }

    ParameterizedType parameterizedType = (ParameterizedType) type;
    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
    Class<T> rawType = (Class<T>) (parameterizedType.getRawType());

    TypeTokenContainer container = new TypeTokenContainer() {
      @NonNull
      @Override
      public <T> TypeToken<T> at(int index) {
        return TypeToken.of(actualTypeArguments[index]);
      }
    };
    return new ClassToken<T>(rawType, container);
  }

  /**
   * Return an {@link TypeToken} to represent plain class type {@code T}.
   *
   * <p>For example:
   * <p>Create an {@code TypeToken<Foo>} of type {@code Foo}:
   * <p>{@code TypeToken<Foo> token = TypeToken.of(Foo.class);}
   * */  @NonNull
  public static <T> TypeToken<T> of(@NonNull Class<T> typeToken) {
    if (typeToken.isArray()) {
      Class<?> componentTypeToken = typeToken.getComponentType();
      return new ArrayToken<T>(TypeToken.of(componentTypeToken));
    }
    return new ClassToken<T>((Class<T>) typeToken);
  }


  /**
   *  {@link ClassToken} is used to represent types in a type-safe manner, including Primitive types, Plain class types,
   *  Generic types, and Wildcard types.
   */
  public static class ClassToken<T> extends TypeToken<T> {
    private final Class<T> rawType;
    private final TypeTokenContainer typeArguments;

    private ClassToken(Class<T> token) {
      this.rawType = token;
      typeArguments = null;
    }

    private ClassToken(Class<T> rawType, TypeTokenContainer typeArguments) {
      this.rawType = rawType;
      this.typeArguments = typeArguments;
    }

    @NonNull
    public Class<T> getRawType() {
      return rawType;
    }

    @NonNull
    public TypeTokenContainer getTypeArguments() {
      return typeArguments;
    }
  }

  /**
   *  {@link ArrayToken} is used to represent Array types in a type-safe manner.
   *  such as: primitive arrays(i.e. {@code int[]}) and object arrays(i.e. {@code Foo[]})</li>
   */
  public static class ArrayToken<T> extends TypeToken<T> {
    private final TypeToken<?> componentType;

    private ArrayToken(TypeToken<?> componentType) {
      this.componentType = componentType;
    }

    @NonNull
    public TypeToken<?> getComponentType() {
      return componentType;
    }
  }
}

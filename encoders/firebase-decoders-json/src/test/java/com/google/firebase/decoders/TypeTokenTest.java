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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class TypeTokenTest {
  static class Foo {}

  //Primitive type
  @Test
  public void primitiveType_typeIsCorrectlyCaptured(){
    ClassToken<?> intToken = (ClassToken<?>) TypeToken.of(int.class);
    ClassToken<?> doubleToken = (ClassToken<?>) TypeToken.of(double.class);
    ClassToken<?> floatToken = (ClassToken<?>) TypeToken.of(float.class);
    ClassToken<?> longToken = (ClassToken<?>) TypeToken.of(long.class);
    ClassToken<?> byteToken = (ClassToken<?>) TypeToken.of(byte.class);
    ClassToken<?> charToken = (ClassToken<?>) TypeToken.of(char.class);
    ClassToken<?> shortToken = (ClassToken<?>) TypeToken.of(short.class);
    ClassToken<?> booleanToken = (ClassToken<?>) TypeToken.of(boolean.class);

    assertEquals(intToken.getRawType(), int.class);
    assertEquals(doubleToken.getRawType(), double.class);
    assertEquals(floatToken.getRawType(), float.class);
    assertEquals(longToken.getRawType(), long.class);
    assertEquals(byteToken.getRawType(), byte.class);
    assertEquals(charToken.getRawType(), char.class);
    assertEquals(shortToken.getRawType(), short.class);
    assertEquals(booleanToken.getRawType(), boolean.class);
  }

  //Array type
  @Test
  public void generalArrayTypeWithSafe_componentTypeIsCorrectlyCaptured() {
    ClassToken<?> fooComponentType = (ClassToken<?>) ((ArrayToken<?>)TypeToken.of(new Safe<Foo[]>() {})).getComponentType();
    ClassToken<?> intComponentType = (ClassToken<?>) ((ArrayToken<?>)TypeToken.of(new Safe<int[]>() {})).getComponentType();

    assertEquals(Foo.class, fooComponentType.getRawType());
    assertEquals(int.class, intComponentType.getRawType());
  }

  @Test
  public void generalArrayTypeWithoutSafe_componentTypeIsCorrectlyCaptured() {
    ClassToken<?> fooComponentType = (ClassToken<?>) ((ArrayToken<?>) TypeToken.of(Foo[].class)).getComponentType();
    ClassToken<?> intComponentType = (ClassToken<?>) ((ArrayToken<?>) TypeToken.of(int[].class)).getComponentType();

    assertEquals(Foo.class, fooComponentType.getRawType());
    assertEquals(int.class, intComponentType.getRawType());
  }

  @Test
  public void genericArrayType_rawTypeIsCorrectlyCaptured() {
    ClassToken<?> componentType = (ClassToken<?>) ((ArrayToken<?>) TypeToken.of(new Safe<List<String>[]>() {})).getComponentType();
    assertEquals(List.class, componentType.getRawType());
  }

  //Plain Class Type
  public void plainClassType_rawTypeIsCorrectlyCaptured() {
    assertEquals(Foo.class, ((ClassToken<?>) TypeToken.of(new Safe<Foo>() {})).getRawType());
  }

  //Generic Type
  @Test
  public void genericType_actualTypeParametersAreCorrectlyCaptured(){
    TypeTokenContainer typeTokenContainer = ((ClassToken<?>) TypeToken.of(new Safe<Map<String, Foo>>() {})).getTypeArguments();

    assertEquals(String.class, ((ClassToken<?>) typeTokenContainer.at(0)).getRawType());
    assertEquals(Foo.class, ((ClassToken<?>) typeTokenContainer.at(1)).getRawType());
  }

  @Test
  public void nestedGenericType_actualTypeParametersAreCorrectlyCaptured(){
    TypeTokenContainer typeTokenContainer = ((ClassToken<?>) TypeToken.of(new Safe<Map<String, List<String>>>() {})).getTypeArguments();
    assertEquals(String.class, ((ClassToken<?>) typeTokenContainer.at(0)).getRawType());
    ClassToken<?> listOfStringTypeToken = ((ClassToken<?>) typeTokenContainer.at(1));
    ClassToken<?> stringTypeToken = (ClassToken<?>) listOfStringTypeToken.getTypeArguments().at(0);

    assertEquals(List.class, ((ClassToken<?>) typeTokenContainer.at(1)).getRawType());
    assertEquals(String.class, stringTypeToken.getRawType());
  }

  //Wildcard Type
  @Test
  public void ar(){
    ClassToken<?> classToken = (ClassToken<?>) ((ClassToken<?>) TypeToken.of(new Safe<List<? extends Number>>(){})).getTypeArguments().at(0);
    assertEquals(Number.class, classToken.getRawType());
  }
}

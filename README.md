[![JCenter](https://maven-badges.herokuapp.com/maven-central/xyz.truenight.latte/latte/badge.svg)](https://maven-badges.herokuapp.com/maven-central/xyz.truenight.latte/latte)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/xyz.truenight.latte/latte/badge.svg)](https://maven-badges.herokuapp.com/maven-central/xyz.truenight.latte/latte)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/xyz.truenight.latte/latte/badge.svg)](http://www.javadoc.io/doc/xyz.truenight.latte/latte)
[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

# Overview

Latte is a deep equality comparison and object cloning library.

Do you want your data to be `final`? Do you want to safely broadcast to
different parts of the program?

This usually leads to cloning, which requires writing a copy constructor for
every model class, and overriding `equals()` for deep equality.

Latte provides:

  * Generic cloning that works on every type
  * Generic deep equality (value equality) that works on every type

# TOC

* [Overview](#overview)
* [Installation](#installation)
* [Usage](#usage)
  * [Static Methods](#static-methods)
  * [Annotations](#annotations)

# Installation

Add `Latte` dependency to your `build.gradle` file:

```groovy
dependencies {
    compile 'xyz.truenight.latte:latte:1.0'
}
```

or to your `pom.xml` if you're using Maven:

```xml
<dependency>
  <groupId>xyz.truenight.latte</groupId>
  <artifactId>latte</artifactId>
  <version>1.0</version>
  <type>pom</type>
</dependency>
```

# Usage

## Static Methods

### `Latte.clone(object)`

Efficient deep cloning that works on objects of any type.

```java
SomeObject object = new SomeObject(param1, param2, param3, param4);

SomeObject clone = Latte.clone(object);

object == clone;
// false
```

### `Latte.equal(one, other)`

Efficient deep equality that works on objects of any type.

```java
SomeObject clone = Latte.clone(object);

Latte.equal(object, clone);
// true
```

## Annotations

Use `@IgnoreField, @UseAdapter(TypeAdapter.class), @UnorderedCollection` annotations
to customise Latte's behaviour for individual classes:

```java
public class SomeObject {
  @IgnoreField(ignoreClone = false) // field will be cloned but won't be compared
  private String param1;
  
  private Object param2; // for non-primitive fields, reflective adapter is used
  
  @UseAdapter(CustomAdapter.class) // to use custom adapter that extends TypeAdapter or TypeAdapterFactory
  private Object param3;
  
  @UnorderedCollection // element order will be ignored during comparison 
  // (Same effect as @UseAdapter(UnorderedCollectionTypeAdapter.class))
  private Collection<Object> param4;
  
  private SomeObject() {
  
  }
  
  public SomeObject(String param1, Object param2, Object param3, Collection<Object> param4) {
    this.param1 = param1;
    this.param2 = param2;
    this.param3 = param3;
    this.param4 = param4;
  }
}
```

package com.okaphone.yajp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * The output of the parser.
 * Any primitive or composite value that is possible in JSON can be represented bij an instance of this class (i.e. one of the six subclasses).
 * It is a true value class, so all instances are immutable and have identity based on their embedded value.
 *
 * @throws UnsupportedOperationException when used in ways that the type (i.e. subclass) of the value does not support.
 *
 * © Copyright J.R. Marks 2023
 */
public abstract class Value<TYPE> {
   private static final NullValue NULL=new NullValue();
   private static final BooleanValue TRUE=new BooleanValue(true);
   private static final BooleanValue FALSE=new BooleanValue(false);
   private static final NumberValue ZERO=new NumberValue(0.0);
   private static final StringValue STRING=new StringValue("");
   private static final ArrayValue ARRAY=new ArrayValue();
   private static final ObjectValue OBJECT=new ObjectValue(Collections.emptyMap());
   private final TYPE value;

   private Value(final TYPE value) {
      this.value=value;
   }

   static NullValue of() {
      return NULL;
   }

   static BooleanValue of(final boolean value) {
      return value?TRUE:FALSE;
   }

   static NumberValue of(final double value) {
      return value==0.0?ZERO:new NumberValue(value);
   }

   static StringValue of(final String value) {
      return value.isEmpty()?STRING:new StringValue(value);
   }

   static ArrayValue of(final Value<?>... value) {
      return value.length==0?ARRAY:new ArrayValue(value);
   }

   static ObjectValue of(final Map<String,Value<?>> value) {
      return value.isEmpty()?OBJECT:new ObjectValue(value);
   }

   public boolean isNull() {
      return false;
   }

   public boolean isZero() {
      return false;
   }

   public boolean isEmpty() {
      return false;
   }

   public boolean bool() {
      throw error();
   }

   public double number() {
      throw error();
   }

   public long integer() {
      throw error();
   }

   public String string() {
      throw error();
   }

   public Value<?>[] array() {
      throw error();
   }

   public int length() {
      throw error();
   }

   public Value<?> get(final int i) {
      throw error();
   }

   public Value<?> get(final int... i) {
      throw error();
   }

   public List<Value<?>> list() {
      throw error();
   }

   public Stream<Value<?>> stream() {
      throw error();
   }

   public Map<String,Value<?>> object() {
      throw error();
   }

   public Set<String> keys() {
      throw error();
   }

   public Value<?> get(final String key) {
      throw error();
   }

   public Value<?> get(final String... key) {
      throw error();
   }

   public final TYPE value() {
      return value;
   }

   @Override
   public String toString() {
      return value.toString();
   }

   @Override
   public boolean equals(final Object other) {
      if(this==other) {
         return true;
      }
      if(other!=null&&other.getClass()==getClass()) {
         return value.equals(((Value)other).value());
      }
      return false;
   }

   @Override
   public int hashCode() {
      return getClass().hashCode()*31+value.hashCode();
   }

   private RuntimeException error() {
      return new UnsupportedOperationException("wrong type: "+getClass());
   }

   public static final class NullValue
         extends Value<Void> {
      private NullValue() {
         super(null);
      }

      @Override
      public final boolean isNull() {
         return true;
      }
   }

   public static final class BooleanValue
         extends Value<Boolean> {
      private BooleanValue(final boolean value) {
         super(value);
      }

      @Override
      public final boolean bool() {
         return value();
      }
   }

   public static final class NumberValue
         extends Value<Double> {
      private NumberValue(final double value) {
         super(value);
      }

      @Override
      public final boolean isZero() {
         return value()==0.0;
      }

      @Override
      public final double number() {
         return value();
      }

      @Override
      public final long integer() {
         return Math.round(value());
      }
   }

   public static final class StringValue
         extends Value<String> {
      private StringValue(final String value) {
         super(value);
      }

      @Override
      public final boolean isEmpty() {
         return value().isEmpty();
      }

      @Override
      public final String string() {
         return value();
      }

      @Override
      public final int length() {
         return value().length();
      }
   }

   public static final class ArrayValue
         extends Value<Value<?>[]> {
      private ArrayValue(final Value<?>... value) {
         super(value.clone());
      }

      @Override
      public boolean isEmpty() {
         return value().length==0;
      }

      @Override
      public Value<?>[] array() {
         return value().clone();
      }

      @Override
      public int length() {
         return value().length;
      }

      @Override
      public Value<?> get(final int i) {
         return value()[i];
      }

      @Override
      public final Value<?> get(final int... i) {
         final Value<?> value=get(i[0]);
         return i.length==1?value:value.get(Arrays.copyOfRange(i,1,i.length));
      }

      @Override
      public List<Value<?>> list() {
         final List<Value<?>> list=new ArrayList<>();
         for(final Value<?> item:value()) {
            list.add(item);
         }
         return list;
      }

      @Override
      public Stream<Value<?>> stream() {
         return Arrays.stream(value());
      }
   }

   public static final class ObjectValue
         extends Value<Map<String,Value<?>>> {
      private ObjectValue(final Map<String,Value<?>> value) {
         super(new HashMap<>(value));
      }

      @Override
      public boolean isEmpty() {
         return value().isEmpty();
      }

      @Override
      public Map<String,Value<?>> object() {
         return new HashMap<>(value());
      }

      @Override
      public Set<String> keys() {
         return new HashSet<>(value().keySet());
      }

      @Override
      public Value<?> get(final String key) {
         return value().get(key);
      }

      @Override
      public Value<?> get(final String... key) {
         final Value<?> value=get(key[0]);
         return key.length==1?value:value==null?null:value.get(Arrays.copyOfRange(key,1,key.length));
      }
   }
}

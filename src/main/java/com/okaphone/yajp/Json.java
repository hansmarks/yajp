package com.okaphone.yajp;

import static com.okaphone.yajp.Utils.replace;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Json { // ECMA-404
   private static final Pattern NUMERIC=Pattern.compile("-?\\d.*");
   private static final Pattern NUMBER=Pattern.compile("([-\\d.eE+]+).*");
   private static final Pattern ENCODED=Pattern.compile("\\\\([\\\\\"/bfnrt]|(u[\\da-fA-F]{4}))");
   private static final Pattern TAIL=Pattern.compile("\\s*([,}\\]].*)?");

   private Json() {
   }

   // The parser
   public static enum Type {
      NULL,
      BOOLEAN,
      NUMBER,
      STRING,
      ARRAY,
      OBJECT
   }

   public static final class Value {
      private static final Value NULL=new Value(Type.NULL,null);
      private static final Value TRUE=new Value(Type.BOOLEAN,true);
      private static final Value FALSE=new Value(Type.BOOLEAN,false);
      private static final Value ZERO=new Value(Type.NUMBER,0.0);
      private static final Value STRING=new Value(Type.STRING,"");
      private static final Value ARRAY=new Value(Type.ARRAY,new Value[0]);
      private static final Value OBJECT=new Value(Type.OBJECT,Collections.emptyMap());
      private final Type type;
      private final Object value;

      private Value(final Type type,final Object value) {
         this.type=type;
         this.value=value;
      }

      private static Value of() {
         return NULL;
      }

      private static Value of(final boolean value) {
         return value?TRUE:FALSE;
      }

      private static Value of(final double value) {
         return value==0.0?ZERO:new Value(Type.NUMBER,value);
      }

      private static Value of(final String value) {
         return value.isEmpty()?STRING:new Value(Type.STRING,value);
      }

      private static Value of(final Value... value) {
         return value.length==0?ARRAY:new Value(Type.ARRAY,value.clone());
      }

      private static Value of(final Map<String,Value> value) {
         return value.isEmpty()?OBJECT:new Value(Type.OBJECT,new HashMap<>(value));
      }

      public final Type type() {
         return type;
      }

      public final Object value() {
         return value;
      }

      public final boolean isNull() {
         switch(type) {
            case NULL:
               return true;
            default:
               return false;
         }
      }

      public final boolean bool() {
         switch(type) {
            case BOOLEAN:
               return (boolean)value;
            default:
               throw error();
         }
      }

      public final boolean isZero() {
         switch(type) {
            case NUMBER:
               return (double)value==0.0;
            default:
               return false;
         }
      }

      public final double number() {
         switch(type) {
            case NUMBER:
               return (double)value;
            default:
               throw error();
         }
      }

      public final long integer() {
         return Math.round(number());
      }

      @SuppressWarnings("unchecked")
      public final boolean isEmpty() {
         switch(type) {
            case STRING:
               return ((String)value).isEmpty();
            case ARRAY:
               return ((Value[])value).length==0;
            case OBJECT:
               return ((Map<String,Value>)value).isEmpty();
            default:
               return false;
         }
      }

      public final String string() {
         switch(type) {
            case STRING:
               return (String)value;
            default:
               throw error();
         }
      }

      public final Value[] array() {
         switch(type) {
            case ARRAY:
               return ((Value[])value).clone();
            default:
               throw error();
         }
      }

      public final int length() {
         switch(type) {
            case STRING:
               return ((String)value).length();
            case ARRAY:
               return ((Value[])value).length;
            default:
               throw error();
         }
      }

      public final Value get(final int i) {
         switch(type) {
            case ARRAY:
               return ((Value[])value)[i];
            default:
               throw error();
         }
      }

      public final Value get(final int... i) {
         final Value item=Value.this.get(i[0]);
         return i.length==1?item:item.get(Arrays.copyOfRange(i,1,i.length));
      }

      public final List<Value> list() {
         switch(type) {
            case ARRAY:
               final List<Value> list=new ArrayList<>();
               for(final Value item:(Value[])value) {
                  list.add(item);
               }
               return list;
            default:
               throw error();
         }
      }

      public final Stream<Value> stream() {
         switch(type) {
            case ARRAY:
               return Arrays.stream((Value[])value);
            default:
               throw error();
         }
      }

      @SuppressWarnings("unchecked")
      public final Map<String,Value> object() {
         switch(type) {
            case OBJECT:
               return new HashMap<>((Map<String,Value>)value);
            default:
               throw error();
         }
      }

      @SuppressWarnings("unchecked")
      public final Set<String> keys() {
         switch(type) {
            case OBJECT:
               return new HashSet<>(((Map<String,Value>)value).keySet());
            default:
               throw error();
         }
      }

      @SuppressWarnings("unchecked")
      public final Value get(final String key) {
         switch(type) {
            case OBJECT:
               return ((Map<String,Value>)value).get(key);
            default:
               throw error();
         }
      }

      public final Value get(final String... key) {
         final Value member=Value.this.get(key[0]);
         return key.length==1?member:member==null?null:member.get(Arrays.copyOfRange(key,1,key.length));
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
         if(other instanceof Value) {
            final Value val=(Value)other;
            return type!=val.type()&&value.equals(val.value());
         }
         return false;
      }

      @Override
      public int hashCode() {
         return type.hashCode()*31+value.hashCode();
      }

      private RuntimeException error() {
         return new IllegalStateException("wrong type: "+type);
      }
   }

   private static final class Parsed {
      private final Value value;
      private final String tail;

      private Parsed(final Value value,final String tail) {
         this.value=value;
         this.tail=tail;
      }

      private Value value() {
         return value;
      }

      private String tail() {
         return tail;
      }
   }

   private static String numeric(final String json) {
      return NUMBER.matcher(json).replaceFirst("$1");
   }

   private static String quoted(final String json) {
      int i=0;
      do {
         i=json.indexOf('"',i+1);
      } while(0<i&&json.charAt(i-1)=='\\');
      return i<0?null:json.substring(0,i+1);
   }

   private static String unquote(final String quoted) {
      final String value=quoted.substring(1,quoted.length()-1);
      return value.indexOf('\\')<0
             ?value
             :replace(value,
                      ENCODED,
                      (matcher)->{
                         final String found=matcher.group(1);
                         switch(found) {
                            case "\\":
                            case "\"":
                            case "/":
                               return found;
                            case "b":
                               return "\b";
                            case "f":
                               return "\f";
                            case "n":
                               return "\n";
                            case "r":
                               return "\r";
                            case "t":
                               return "\t";
                            default: // u
                               return new String(Character.toChars(Integer.parseInt(found.substring(1),16)));
                         }
                      }
            );
   }

   private static Parsed key(final String json) {
      final String src=json.trim();
      if(src.charAt(0)=='"') {
         final String key=quoted(src);
         if(key!=null) {
            return key(Value.of(unquote(key)),src.substring(key.length()));
         }
      }
      throw syntax(src);
   }

   private static Parsed value(final String json) {
      final String src=json.trim();
      if(src.isEmpty()) {
         return null;
      }
      if(src.startsWith("null")) {
         return value(Value.of(),src.substring(4));
      }
      if(src.startsWith("true")) {
         return value(Value.of(true),src.substring(4));
      }
      if(src.startsWith("false")) {
         return value(Value.of(false),src.substring(5));
      }
      if(NUMERIC.matcher(src).matches()) {
         final String value=numeric(src);
         return value(Value.of(Double.parseDouble(value)),src.substring(value.length()));
      }
      if(src.charAt(0)=='"') {
         final String value=quoted(src);
         if(value!=null) {
            return value(Value.of(unquote(value)),src.substring(value.length()));
         }
      }
      if(src.charAt(0)=='[') {
         final List<Value> items=new ArrayList<>();
         String tail=src.substring(1);
         while(!tail.isEmpty()) {
            if(lookahead(tail,']')) {
               return value(Value.of(items.toArray(new Value[0])),skip(tail,']'));
            }
            final Parsed item=value(tail);
            items.add(item.value());
            tail=next(item.tail());
         }
      }
      if(src.charAt(0)=='{') {
         final Map<String,Value> members=new HashMap<>();
         String tail=src.substring(1);
         while(!tail.isEmpty()) {
            if(lookahead(tail,'}')) {
               return value(Value.of(members),skip(tail,'}'));
            }
            final Parsed key=key(tail);
            final Parsed value=value(skip(key.tail(),':'));
            members.put(key.value().string(),value.value());
            tail=next(value.tail());
         }
      }
      throw syntax(src);
   }

   private static String next(final String tail) {
      if(lookahead(tail,',')) {
         return skip(tail,',');
      }
      return tail;
   }

   private static Parsed key(final Value value,final String tail) {
      if(lookahead(tail,':')) {
         return new Parsed(value,tail);
      }
      throw syntax(tail);
   }

   private static Parsed value(final Value value,final String tail) {
      if(TAIL.matcher(tail).matches()) {
         return new Parsed(value,tail);
      }
      throw syntax(tail);
   }

   private static boolean lookahead(final String tail,final char next) {
      int i=0;
      while(i<tail.length()&&Character.isWhitespace(tail.charAt(i))) {
         i++;
      }
      return i<tail.length()&&tail.charAt(i)==next;
   }

   private static String skip(final String tail,final char next) {
      return tail.substring(tail.indexOf(next)+1);
   }

   private static RuntimeException syntax(final String json) {
      return new IllegalStateException("syntax error: "+json);
   }

   public static Value parse(final String json) {
      final Parsed parsed=value(json);
      if(!parsed.tail().isEmpty()) {
         throw syntax(parsed.tail());
      }
      return parsed.value();
   }

   // Convenience methods
   public static <TYPE> TYPE value0(final Value value,final Function<Value,TYPE> map) {
      return value==null||value.isNull()?null:map.apply(value);
   }

   public static <TYPE> Optional<TYPE> value(final Value value,final Function<Value,TYPE> map) {
      return Optional.ofNullable(value0(value,map));
   }

   public static Boolean bool0(final Value value) {
      return value0(value,Value::bool);
   }

   public static Optional<Boolean> bool(final Value value) {
      return Optional.ofNullable(bool0(value));
   }

   public static Double number0(final Value value) {
      return value0(value,Value::number);
   }

   public static Optional<Double> number(final Value value) {
      return Optional.ofNullable(number0(value));
   }

   public static Long integer0(final Value value) {
      return value0(value,Value::integer);
   }

   public static Optional<Long> integer(final Value value) {
      return Optional.ofNullable(integer0(value));
   }

   public static String string0(final Value value) {
      return value0(value,Value::string);
   }

   public static Optional<String> string(final Value value) {
      return Optional.ofNullable(string0(value));
   }

   // extra's for JSON construction
   private static String encode(final String raw) {
      return replace(raw,(c)->{
                  switch(c) {
                     case '\\':
                        return "\\\\";
                     case '"':
                        return "\\\"";
                     case '\b':
                        return "\\b";
                     case '\f':
                        return "\\f";
                     case '\n':
                        return "\\n";
                     case '\r':
                        return "\\r";
                     case '\t':
                        return "\\t";
                     default:
                        if(c<' ') {
                           final String hex="000"+Integer.toHexString(c);
                           return "\\u"+hex.substring(hex.length()-4);
                        }
                        return null;
                  }
               });
   }

   private static String quote(final String value) {
      return value.isEmpty()?"\"\"":'"'+encode(value)+'"';
   }

   private static String value(final Object value) {
      if(value==null) {
         return "null";
      }
      if(value instanceof List) {
         return array((List)value);
      }
      if(value instanceof Builder) {
         return object((Builder)value);
      }
      if(value instanceof Boolean||value instanceof Integer||value instanceof Long||value instanceof Float||value instanceof Double) {
         return value.toString();
      }
      return quote(value.toString());
   }

   private static String array(final List<?> values) {
      return values.isEmpty()?"[]":values.stream().map(Json::value).collect(Collectors.joining(",","[","]"));
   }

   private static String member(final Map.Entry<String,?> member) {
      return quote(member.getKey())+':'+value(member.getValue());
   }

   /** (use the Builder for objects and Lists for arrays)
    *
    * @param members
    * @return
    */
   public static String object(final Builder members) {
      return members.isEmpty()?"{}":members.entrySet().stream().map(Json::member).collect(Collectors.joining(",","{","}"));
   }

   public static final class Builder
         extends HashMap<String,Object> {
   }
}

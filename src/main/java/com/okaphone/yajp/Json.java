package com.okaphone.yajp;

import static com.okaphone.yajp.Utils.replace;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The parser.
 * And some convenience methods for null safe usage.
 *
 * Utility class, no need to create an instance.
 *
 * © Copyright J.R. Marks 2023
 */
public class Json {
   private static final Pattern NUMERIC=Pattern.compile("-?\\d.*");
   private static final Pattern NUMBER=Pattern.compile("([-\\d.eE+]+).*");
   private static final Pattern ENCODED=Pattern.compile("\\\\([\\\\\"/bfnrt]|(u[\\da-fA-F]{4}))");
   private static final Pattern TAIL=Pattern.compile("[\\x20\\r\\n\\t]*([,}\\]].*)?");
   private static final String WHITESPACE=" \r\n\t";

   private Json() {
   }

   private static final class Parsed {
      private final Value<?> value;
      private final String tail;

      private Parsed(final Value<?> value,final String tail) {
         this.value=value;
         this.tail=tail;
      }

      private Value<?> value() {
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
         final List<Value<?>> items=new ArrayList<>();
         String tail=src.substring(1);
         while(!tail.isEmpty()) {
            if(lookahead(tail,']')) {
               return value(Value.of(items.toArray(new Value<?>[0])),skip(tail,']'));
            }
            final Parsed item=value(tail);
            items.add(item.value());
            tail=next(item.tail());
         }
      }
      if(src.charAt(0)=='{') {
         final Map<String,Value<?>> members=new HashMap<>();
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

   private static Parsed key(final Value<?> value,final String tail) {
      if(lookahead(tail,':')) {
         return new Parsed(value,tail);
      }
      throw syntax(tail);
   }

   private static Parsed value(final Value<?> value,final String tail) {
      if(TAIL.matcher(tail).matches()) {
         return new Parsed(value,tail);
      }
      throw syntax(tail);
   }

   private static boolean lookahead(final String tail,final char next) {
      int i=0;
      while(i<tail.length()&&0<=WHITESPACE.indexOf(tail.charAt(i))) {
         i++;
      }
      return i<tail.length()&&tail.charAt(i)==next;
   }

   private static String skip(final String tail,final char next) {
      return tail.substring(tail.indexOf(next)+1);
   }

   private static RuntimeException syntax(final String json) {
      return new IllegalArgumentException("syntax error: "+json);
   }

   /**
    * Parses a valid ECMA-404 JSON string.
    *
    * @param message a JSON string (normally an anonymous object or array, but a primitive value also works)
    * @return a {@link Value} object that represents the parsed string
    *
    * @throws IllegalArgumentException on syntax errors
    */
   public static Value<?> parse(final String message) {
      final Parsed parsed=value(message);
      if(!parsed.tail().isEmpty()) {
         throw syntax(parsed.tail());
      }
      return parsed.value();
   }

   public static <TYPE> TYPE value0(final Value<?> value,final Function<Value<?>,TYPE> map) {
      return value==null||value.isNull()?null:map.apply(value);
   }

   public static <TYPE> Optional<TYPE> value(final Value<?> value,final Function<Value<?>,TYPE> map) {
      return Optional.ofNullable(value0(value,map));
   }

   public static Boolean bool0(final Value<?> value) {
      return value0(value,Value::bool);
   }

   public static Optional<Boolean> bool(final Value<?> value) {
      return Optional.ofNullable(bool0(value));
   }

   public static Double number0(final Value<?> value) {
      return value0(value,Value::number);
   }

   public static Optional<Double> number(final Value<?> value) {
      return Optional.ofNullable(number0(value));
   }

   public static Long integer0(final Value<?> value) {
      return value0(value,Value::integer);
   }

   public static Optional<Long> integer(final Value<?> value) {
      return Optional.ofNullable(integer0(value));
   }

   public static String string0(final Value<?> value) {
      return value0(value,Value::string);
   }

   public static Optional<String> string(final Value<?> value) {
      return Optional.ofNullable(string0(value));
   }
}

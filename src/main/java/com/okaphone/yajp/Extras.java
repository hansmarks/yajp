package com.okaphone.yajp;

import static com.okaphone.yajp.Utils.replace;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Extra's for JSON message construction.
 *
 * Utility class, no need to create an instance.
 *
 * © Copyright J.R. Marks 2023
 */
public class Extras {
   private static final Set<Class<?>> PRIMITIVE; // Only the numeric ones that is (i.e. not Character)

   static {
      final Set<Class<?>> primitive=new HashSet<>();
      primitive.add(Boolean.class);
      primitive.add(Byte.class);
      primitive.add(Short.class);
      primitive.add(Integer.class);
      primitive.add(Long.class);
      primitive.add(Float.class);
      primitive.add(Double.class);
      PRIMITIVE=Collections.unmodifiableSet(primitive);
   }

   private Extras() {
   }

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
      if(value instanceof ObjectBuilder) {
         return object((ObjectBuilder)value);
      }
      if(value instanceof ArrayBuilder) {
         return array((ArrayBuilder)value);
      }
      if(PRIMITIVE.contains(value.getClass())) {
         return value.toString();
      }
      return quote(value.toString());
   }

   private static String member(final Map.Entry<String,Object> member) {
      return quote(member.getKey())+':'+value(member.getValue());
   }

   private static String object(final ObjectBuilder members) {
      return members.isEmpty()?"{}":members.entrySet().stream().map(Extras::member).collect(Collectors.joining(",","{","}"));
   }

   private static String array(final ArrayBuilder items) {
      return items.isEmpty()?"[]":items.stream().map(Extras::value).collect(Collectors.joining(",","[","]"));
   }

   /**
    * This builder can be used to create a JSON message (anonymous object).
    * All entries in the map will be converted to JSON.
    * Use {@link ObjectBuilder}s and {@link ArrayBuilder}s for entries that represent JSON objects and -arrays.
    * Any members that have no direct representation in JSON will be converted to strings.
    */
   public static class ObjectBuilder
         extends HashMap<String,Object> {
      public ObjectBuilder() {
      }

      public ObjectBuilder(final Map<? extends Object,? extends Object> values) {
         values.entrySet().forEach(entry->put(entry.getKey().toString(),entry.getValue()));
      }

      /**
       * Builds an anonymous JSON object.
       *
       * @return a valid ECMA-404 JSON string.
       */
      public String build() {
         return object(this);
      }
   }

   /**
    * This builder can be used to create a JSON message (anonymous array).
    * All items (of mixed type) will be converted to JSON.
    * Use {@link ObjectBuilder}s and {@link ArrayBuilder}s for items that represent JSON objects and -arrays.
    * Any items that have no direct representation in JSON will be converted to strings.
    */
   public static class ArrayBuilder
         extends ArrayList<Object> {
      public ArrayBuilder() {
      }

      public ArrayBuilder(final Stream<? extends Object> values) {
         values.forEach(this::add);
      }

      public ArrayBuilder(final Collection<? extends Object> values) {
         values.forEach(this::add);
      }

      public ArrayBuilder(final Object... values) {
         for(final Object value:values) {
            add(value);
         }
      }

      /**
       * Builds an anonymous JSON array.
       *
       * @return a valid ECMA-404 JSON string.
       */
      public String build() {
         return array(this);
      }
   }
}

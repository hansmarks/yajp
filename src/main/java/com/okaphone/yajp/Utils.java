package com.okaphone.yajp;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A few handy string functions.
 *
 * Utility class, no need to create an instance.
 *
 * © Copyright J.R. Marks 2023
 */
public class Utils {
   private Utils() {
   }

   public static final String replace(final String value,final Function<Character,String> map) {
      StringBuilder builder=null;
      int j=0;
      for(int i=0;i<value.length();i++) {
         final String target=map.apply(value.charAt(i));
         if(target!=null) {
            if(builder==null) {
               builder=new StringBuilder();
            }
            if(j<i) {
               builder.append(value.substring(j,i));
            }
            j=i+1;
            if(!target.isEmpty()) {
               builder.append(target);
            }
         }
      }
      if(builder==null) {
         return value;
      }
      if(j<value.length()) {
         builder.append(value.substring(j));
      }
      return builder.toString();
   }

   public static final String replace(final String value,final Pattern source,final Function<Matcher,String> target) {
      final StringBuffer result=new StringBuffer();
      final Matcher matcher=source.matcher(value);
      while(matcher.find()) {
         matcher.appendReplacement(result,Matcher.quoteReplacement(target.apply(matcher)));
      }
      matcher.appendTail(result);
      return result.toString();
   }
}

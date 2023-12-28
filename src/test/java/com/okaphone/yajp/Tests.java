package com.okaphone.yajp;

import com.okaphone.yajp.Extras.ArrayBuilder;
import com.okaphone.yajp.Extras.ObjectBuilder;
import java.util.Collections;
import junit.framework.TestCase;

public class Tests
      extends TestCase {
   public Tests() {
   }

   public void testNullParser() {
      assertTrue(Json.parse("null").isNull());
      assertNull(Json.parse("null").value());
      assertNull(Json.parse("null").value());
      assertNull(Json.parse(" null").value());
      assertNull(Json.parse("null ").value());
      assertNull(Json.parse(" null ").value());
   }

   public void testBoolParser() {
      assertFalse(Json.parse("true").isNull());
      assertEquals(Boolean.TRUE,Json.parse("true").value());
      assertTrue(Json.parse("true").bool());
      assertTrue(Json.parse(" true").bool());
      assertTrue(Json.parse("true ").bool());
      assertTrue(Json.parse(" true ").bool());
      assertFalse(Json.parse("false").isNull());
      assertFalse(Json.parse("false").bool());
      assertFalse(Json.parse("false").bool());
      assertFalse(Json.parse(" false").bool());
      assertFalse(Json.parse("false ").bool());
      assertFalse(Json.parse(" false ").bool());
   }

   public void testNumberParser() {
      assertFalse(Json.parse("1").isNull());
      assertEquals(1.0,Json.parse("1").number(),0.0);
      assertEquals(1.0,Json.parse("1").number(),0.0);
      assertEquals(1.23,Json.parse("1.23").number(),0.0);
      assertEquals(1.23,Json.parse(" 1.23").number(),0.0);
      assertEquals(1.23,Json.parse("1.23 ").number(),0.0);
      assertEquals(1.23,Json.parse(" 1.23 ").number(),0.0);
      assertEquals(-1.23,Json.parse("-1.23").number(),0.0);
   }

   public void testStringParser() {
      assertFalse(Json.parse("\"\"").isNull());
      assertTrue(Json.parse("\"\"").isEmpty());
      assertEquals("",Json.parse("\"\"").string());
      assertFalse(Json.parse("\"aap\"").isEmpty());
      assertEquals("aap",Json.parse("\"aap\"").string());
      assertEquals("aap",Json.parse(" \"aap\"").string());
      assertEquals("aap",Json.parse("\"aap\" ").string());
      assertEquals("aap",Json.parse(" \"aap\" ").string());
      assertEquals("aap\"noot",Json.parse("\"aap\\\"noot\"").string());
      assertEquals("aap\"noot\"mies",Json.parse("\"aap\\\"noot\\\"mies\"").string());
      assertEquals("aap\\noot",Json.parse("\"aap\\\\noot\"").string());
      assertEquals("aap/noot",Json.parse("\"aap\\/noot\"").string());
      assertEquals("aap\bnoot",Json.parse("\"aap\\bnoot\"").string());
      assertEquals("aap\fnoot",Json.parse("\"aap\\fnoot\"").string());
      assertEquals("aap\nnoot",Json.parse("\"aap\\nnoot\"").string());
      assertEquals("aap\rnoot",Json.parse("\"aap\\rnoot\"").string());
      assertEquals("aap\tnoot",Json.parse("\"aap\\tnoot\"").string());
      assertEquals("aap&noot",Json.parse("\"aap\\u0026noot\"").string());
      assertEquals("aap\u0003noot",Json.parse("\"aap\\u0003noot\"").string());
   }

   public void testArrayParser() {
      assertFalse(Json.parse("[]").isNull());
      assertTrue(Json.parse("[]").isEmpty());
      assertEquals(0,Json.parse("[]").array().length);
      final Value<?>[] test=Json.parse("[1,[2,[{\"aap\":4},\"5\",[6,7]]],3]").array();
      assertEquals(1.0,test[0].number(),0.0);
      assertEquals(2.0,test[1].get(0).number(),0.0);
      assertEquals(3,test[2].integer(),0.0);
      assertEquals(4.0,test[1].get(1,0).get("aap").number(),0.0);
      assertEquals("5",test[1].get(1,1).string());
      assertEquals(6.0,test[1].get(1,2,0).number(),0.0);
   }

   public void testObjectParser() {
      assertFalse(Json.parse("{}").isNull());
      assertTrue(Json.parse("{}").isEmpty());
      assertEquals(0,Json.parse("{}").object().size());
      final Value<?> test=Json.parse("{\"aap\":1,\"noot\":{\"wim\":2,\"gijs\":{\"does\":[4,6],\"hok\":\"5\"}},\"mies\":3}");
      assertEquals(1.0,test.get("aap").number(),0.0);
      assertEquals(2.0,test.get("noot","wim").number(),0.0);
      assertEquals(3.0,test.get("mies").number(),0.0);
      assertEquals(4,test.get("noot","gijs","does").array()[0].integer());
      assertEquals("5",test.get("noot","gijs","hok").string());
      assertEquals(6.0,test.get("noot","gijs","does").get(1).number());
   }

   public void testArrayBuilder() {
      assertEquals("[]",new ArrayBuilder().build());
      assertEquals("[]",new ArrayBuilder(Collections.emptyList()).build());
      assertEquals("[1,2,3]",new ArrayBuilder(1,2,3).build());
      assertEquals("[\"1\",\"2\",\"3\"]",new ArrayBuilder("1","2","3").build());
      final ObjectBuilder object=new ObjectBuilder();
      object.put("aap",2);
      assertEquals("[1,[{\"aap\":2},null],\"3\",true]",new ArrayBuilder(1,new ArrayBuilder(object,null),"3",true).build());
   }

   public void testObjectBuilder() {
      assertEquals("{}",new ObjectBuilder().build());
      assertEquals("{}",new ObjectBuilder(Collections.emptyMap()).build());
      final ObjectBuilder array=new ObjectBuilder();
      array.put("aap",1);
      array.put("noot",2);
      array.put("mies",3);
      final String json=array.build();
      assertTrue(json.contains("\"aap\":1"));
      assertTrue(json.contains("\"noot\":2"));
      assertTrue(json.contains("\"mies\":3"));
      array.clear();
      array.put("aap","1");
      array.put("noot","2");
      array.put("mies","3");
      final String json2=array.build();
      assertTrue(json2.contains("\"aap\":\"1\""));
      assertTrue(json2.contains("\"noot\":\"2\""));
      assertTrue(json2.contains("\"mies\":\"3\""));
      array.clear();
      array.put("aap",1);
      final ObjectBuilder array2=new ObjectBuilder();
      array2.put("wim",new ArrayBuilder(2));
      array2.put("zus",null);
      array.put("noot",array2);
      array.put("gijs","3");
      array.put("mies",true);
      final String json3=array.build();
      assertTrue(json3.contains("\"aap\":1"));
      assertTrue(json3.contains("\"noot\":{"));
      assertTrue(json3.contains("\"wim\":[2]"));
      assertTrue(json3.contains("\"zus\":null"));
      assertTrue(json3.contains("\"gijs\":\"3\""));
      assertTrue(json3.contains("\"mies\":true"));
   }
}

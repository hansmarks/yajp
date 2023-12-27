package com.okaphone.yajp;

import com.okaphone.yajp.Extras.ArrayBuilder;
import com.okaphone.yajp.Json.Value;
import java.util.ArrayList;
import java.util.HashMap;
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
      Value[] test=Json.parse("[1,[2],3]").array();
      assertEquals(1.0,test[0].number(),0.0);
      assertEquals(2.0,test[1].get(0).number(),0.0);
      assertEquals(3.0,test[2].number(),0.0);
   }

   public void testObjectParser() {
      assertFalse(Json.parse("{}").isNull());
      assertTrue(Json.parse("{}").isEmpty());
      assertEquals(new HashMap<String,Value>(),Json.parse("{}").object());
      Value test=Json.parse("{\"aap\":1,\"noot\":{\"wim\":2},\"mies\":3}");
      assertEquals(1.0,test.get("aap").number(),0.0);
      assertEquals(2.0,test.get("noot","wim").number(),0.0);
      assertEquals(3.0,test.get("mies").number(),0.0);
   }

   public void testArrayBuilder() {
      assertEquals("[]",new ArrayBuilder().build());
      assertEquals("[]",new ArrayBuilder(new ArrayList<String>()).build());
      assertEquals("[1,2,3]",new ArrayBuilder(1,2,3).build());
      assertEquals("[\"1\",\"2\",\"3\"]",new ArrayBuilder("1","2","3").build());
   }
}

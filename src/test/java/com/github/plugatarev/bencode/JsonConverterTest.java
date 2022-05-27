package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.parser.Element;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class JsonConverterTest {
    JsonConverter converter = new JsonConverter();

    private void test(Element input, String expected){
        System.out.println(converter.json(input));
        Assert.assertEquals(converter.json(input), expected);
    }

    @Test
    public void string(){
        String expected = "\"hello\"";
        Element input = new Element.BString("hello");
        test(input, expected);
    }

    @Test
    public void number(){
        Element input = new Element.BInteger(1232);
        String expected = "1232";
        test(input, expected);
    }

    @Test
    public void list(){
        List<Element> elements = Arrays.asList(new Element.BInteger(123), new Element.BString("key"), new Element.BString("he||0"));
        Element input = new Element.BList(elements);
        String expected = "[123, \"key\", \"he||0\"]";
        test(input, expected);
    }

    @Test
    public void dictionary(){
        Map<Element.BString, Element> dict = new HashMap<>();
        dict.put(new Element.BString("bar"), new Element.BString("spam"));
        dict.put(new Element.BString("foo"), new Element.BInteger(42));
        Element input = new Element.BDictionary(dict);
        String expected = """
                {
                  "bar": "spam"
                  "foo": 42
                }""";
        test(input, expected);
    }

    @Test
    public void listWithinList(){
        List<Element> nested = Arrays.asList(new Element.BInteger(123), new Element.BString("key"), new Element.BString("he||0"));
        Element element = new Element.BList(nested);
        List<Element> elements = Arrays.asList(element, new Element.BInteger(0));
        Element input = new Element.BList(elements);
        String expected = "[[123, \"key\", \"he||0\"], 0]";
        test(input, expected);
    }

    @Test
    public void allTypesWithinDictionary(){
        List<Element> nested = Arrays.asList(new Element.BInteger(123), new Element.BString("key"), new Element.BString("he||0"));
        Element list = new Element.BList(nested);
        Element string = new Element.BString("dfg");
        Element number = new Element.BInteger(43);
        Map<Element.BString, Element> dict = new HashMap<>();
        Element dict1 = new Element.BDictionary(dict);
        dict.put(new Element.BString("bar"), new Element.BString("spam"));
        dict.put(new Element.BString("foo"), new Element.BInteger(42));

        Map<Element.BString, Element> map = new HashMap<>();
        map.put(new Element.BString("a"), list);
        map.put(new Element.BString("b"), string);
        map.put(new Element.BString("c"), number);
        map.put(new Element.BString("d"), dict1);

        Element input = new Element.BDictionary(map);
        String expected = """
                {
                  "a": [123, "key", "he||0"]
                  "b": "dfg"
                  "c": 43
                  "d":\s
                  {
                    "bar": "spam"
                    "foo": 42
                  }
                }""";
        test(input, expected);
    }
}

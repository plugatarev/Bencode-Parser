package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.parser.Element;

//public class JsonConverter {
//
//    int nestingLevel;
//    private static int SHIFT = 2;
//
//    public String json(Element element) {
//        return switch (element) {
//            case Element.JInteger i -> shifted(i.value());
//            case Element.JDictionary jDictionary -> {
//                nestingLevel++;
//                // for loop
//                nestingLevel--;
//            }
//            case Element.JArray jArray -> {
//            }
//            case Element.JString jString -> {
//            }
//        };
//    }
//
//    private String shifted(Object value) {
//        return " ".repeat(nestingLevel * SHIFT) + value;
//    }
//}

package com.github.plugatarev.bencode.parser;

import java.util.List;
import java.util.Map;

public sealed interface Element {

    record BInteger(int value) implements Element {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record BList(List<Element> member) implements Element{}

    record BDictionary(Map<Element.BString, Element> dict) implements Element{}

    record BString(String str) implements  Element{
        @Override
        public String toString(){
            return "\"" + str + "\"";
        }
    }
}


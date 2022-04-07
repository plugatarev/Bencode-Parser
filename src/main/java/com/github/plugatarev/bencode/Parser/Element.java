package com.github.plugatarev.bencode.Parser;

import java.util.List;
import java.util.Map;

public sealed interface Element {

    record JInteger(int value) implements Element {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record JArray(List<Element> member) implements Element{
        @Override
        public String toString(){
            return member.toString();
        }
    }

    record JDictionary(Map<Element, Element> dict, int nestingLevel) implements Element{
        @Override
        public String toString(){
            StringBuilder str = new StringBuilder("\n%s{\n".formatted("\t".repeat(nestingLevel - 1)));
            for (Map.Entry<Element, Element> entry : dict.entrySet()) {
                str.append("%s%s: %s\n".formatted("\t".repeat(nestingLevel), entry.getKey().toString(), entry.getValue().toString()));
            }
            str.append("%s}\n".formatted("\t".repeat(nestingLevel - 1)));
            return str.toString();

        }
    }

    record JString(String str) implements  Element{
        @Override
        public String toString(){
            return "\"" + str + "\"";
        }
    }

}


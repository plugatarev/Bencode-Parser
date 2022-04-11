package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.parser.Element;

import java.util.Map;

public class JsonConverter {

    int nestingLevel;
    private static final int SHIFT = 2;

    public String json(Element element) {
        return switch (element) {
            case Element.BInteger jInteger -> shifted(jInteger.value());
            case Element.BDictionary jDictionary -> {
                StringBuilder str = new StringBuilder();
                if (nestingLevel == 0) appendStrings(str, shifted("{"), "\n");
                else appendStrings(str, "\n", shifted("{"), "\n");

                nestingLevel++;
                for (Map.Entry<Element, Element> entry : jDictionary.dict().entrySet()) {
                    appendStrings(str, shifted(entry.getKey().toString()), ": ", json(entry.getValue()), "\n");
                }
                nestingLevel--;
                str.append(shifted("}"));
                yield str.toString();
            }
            case Element.BArray jArray -> jArray.member().toString();
            case Element.BString jString -> jString.toString();
        };
    }

    private void appendStrings(StringBuilder string, String... part){
        for (String p : part){
            string.append(p);
        }
    }

    private String shifted(Object value) {
        return " ".repeat(nestingLevel * SHIFT) + value;
    }
}

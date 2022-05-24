package com.github.plugatarev.bencode;

import com.github.plugatarev.bencode.parser.Element;


public class JsonConverter {

    int nestingLevel;
    private static final int SHIFT = 2;

    public String json(Element element) {
        return switch (element) {
            case Element.BInteger bInteger -> String.valueOf(bInteger.value());
            case Element.BDictionary bDictionary -> {
                StringBuilder sb = new StringBuilder();
                if (nestingLevel == 0) appendStrings(sb, "{", "\n");
                else appendStrings(sb, "\n", shifted("{"), "\n");

                nestingLevel++;
                bDictionary.dict().forEach((k, v) -> appendStrings(sb, shifted(k), ": ", json(v), "\n"));
                nestingLevel--;

                sb.append(shifted("}"));
                yield sb.toString();
            }
            case Element.BList bList -> bList.list().toString();
            case Element.BString bString -> bString.toString();
        };
    }

    private void appendStrings(StringBuilder sb, String... part){
        for (String p : part){
            sb.append(p);
        }
    }

    private String shifted(Object value) {
        return " ".repeat(nestingLevel * SHIFT) + value;
    }
}

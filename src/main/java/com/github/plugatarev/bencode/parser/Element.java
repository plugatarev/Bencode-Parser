package com.github.plugatarev.bencode.parser;

import java.util.List;
import java.util.Map;

public sealed interface Element {

    record BInteger(int value) implements Element {}

    record BArray(List<Element> member) implements Element{}

    record BDictionary(Map<Element, Element> dict, int nestingLevel) implements Element{}

    record BString(String str) implements  Element{}

}


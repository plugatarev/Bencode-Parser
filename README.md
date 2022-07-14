# Bencode-Parser
The parser accepts a file(input) in bencode format, translates it into a json representation and prints it to a file(output).
___
###Example:
input: d3:bar4:spam3:fooi42ee  
output:  
{  
&emsp;"bar": "spam"  
&emsp;"foo": 42  
}

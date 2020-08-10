The hook will send the warning and error messages of the checks to the
SVN-client that submitted the commit. For this, the following XML format is
used:

```XML
<submitResults>
    <message tool="check-name" type="type" file="file" line="line-number" message="message">
        <example position="column-number"/>
    </message>
</submitResults>
```

* For `check-name` see README.md
* `type` is either `error` or `warning`
* `message` is a human-readable message describing the error or warning
* `file` is the path of the file that caused this message realtive to the
  submission directory (i.e. without the *exercise* or *group* directory names)
* `line-number` is the line number inside the `file` that caused this message
* `column-number` is the character inisde the line that caused this message

`file`, `line-number`, and `column-number` are optional, the other attributes
are always present. If the `column-number` is not specified, the `<example>` tag
is left out so that the `<message>` tag has no children.

Example of a minimal message:
```XML
<submitResults>
    <message tool="file-size" type="error" message="Submission size is too large"/>
</submitResults>
```

Example with multiple messages with all fields set.
```XML
<submitResults>
    <message tool="javac" type="error" message="';' expected" file="Main.java" line="20">
        <example position="42"/>
    </message>
    <message tool="javac" type="error" message="not a statement" file="Main.java" line="20">
        <example position="8"/>
    </message>
</submitResults>
```

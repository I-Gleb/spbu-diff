# diff utility
## Project made for programming basics course at Department of Mathematics and Computer Science, St. Petersburg State University

[Task definition](./TASK.md)

### Documentation

The diff utility (short for difference) compares two text files and determines the changes that can be performed in order to get second file from the first one.

#### Input data

This utility works in console mode and supports two types of information input: through launch options or through interactive mode.

##### Run with parameters

Interface:

     Usage: diff [OPTIONS] FILEFIRST FILESECOND
    
     options:
     -f, -u, --full, --unified output format
     -c, --color colored output
     -h, --help Show help message and exit
    
     Arguments:
     FILEFIRST path to the first file
     FILESECOND path to the second file

The *-f* and *--full* options correspond to the Full format, while *-u* and *--unified* correspond to the Unified format.
If multiple *output format* options are given, the last one takes precedence.
By default, the comparison is displayed in Full format without color lines.


Examples of using:

     $ diff -c a.txt b.txt
     $ diff --color --unified a.txt b.txt

##### Interactive mode

Interface:

Interactive mode is activated when you run diff with no options.
It sequentially asks the user for the paths to the compared files,
then prompts you to select the output format (possible values: "full" and "unified")
and chromaticity (possible values: "y" and "n").

Usage example:

     $ diff
     Enter path to the first file:
     a.txt
     Enter path to second file:
     b.txt
     Full or unified format? (full/unified)
     unified
     Make colorful lines? (y/n)
     n

#### Conclusion

If the input data is incorrect, an error message is displayed and the program ends with a return code of 1.

With correct input, the result of the utility's operation is a text file that is output to the normal output stream.
At the very beginning of the output file, there is a header with the names of the compared files and the times they were last modified.
There are two supported formats below:
* Full - all deleted, added and unchanged lines are displayed. "+" is displayed before the added ones, and "-" before the removed ones.
* Unified - standard [unified format for diff](https://www.gnu.org/software/diffutils/manual/html_node/Unified-Format.html).
Changes are grouped in blocks, and unmodified parts are ignored. Before and after each block, the "context" is displayed - 3 unchanged lines.
Also, before each block, information about it is displayed in a standard Unified format.

Colored output is supported. It prints added lines in green and deleted lines in red.

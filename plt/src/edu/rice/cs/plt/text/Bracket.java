/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2010 JavaPLT group at Rice University
All rights reserved.

Developed by:   Java Programming Languages Team
                Rice University
                http://www.cs.rice.edu/~javaplt/

Redistribution and use in source and binary forms, with or without modification, are permitted 
provided that the following conditions are met:

    - Redistributions of source code must retain the above copyright notice, this list of conditions 
      and the following disclaimer.
    - Redistributions in binary form must reproduce the above copyright notice, this list of 
      conditions and the following disclaimer in the documentation and/or other materials provided 
      with the distribution.
    - Neither the name of the JavaPLT group, Rice University, nor the names of the library's 
      contributors may be used to endorse or promote products derived from this software without 
      specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND 
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*END_COPYRIGHT_BLOCK*/

package edu.rice.cs.plt.text;

import java.util.regex.Pattern;

public class Bracket {
  private final Pattern _left;
  private final Pattern _right;
  private final boolean _nests;
  
  public Bracket(String leftRegex, String rightRegex, boolean nests) {
    _left = Pattern.compile(leftRegex); _right = Pattern.compile(rightRegex); _nests = nests;
  }
  
  public Pattern left() { return _left; }
  public Pattern right() { return _right; }
  public boolean nests() { return _nests; }
  
  public static Bracket literal(String leftLiteral, String rightLiteral, boolean nests) {
    return new Bracket(Pattern.quote(leftLiteral), Pattern.quote(rightLiteral), nests);
  }
  
  public static final Bracket PARENTHESES = literal("(", ")", true);
  public static final Bracket SQUARE_BRACKETS = literal("[", "]", true);
  public static final Bracket BRACES = literal("{", "}", true);
  public static final Bracket ANGLE_BRACKETS = literal("<", ">", true);
  public static final Bracket QUOTES = literal("\"", "\"", false);
  public static final Bracket APOSTROPHES = literal("'", "'", false);
  public static final Bracket C_LINE_COMMENT = new Bracket("//", TextUtil.NEWLINE_PATTERN, false);
  public static final Bracket PERL_LINE_COMMENT = new Bracket("#", TextUtil.NEWLINE_PATTERN, false);
  public static final Bracket C_BLOCK_COMMENT = literal("/*", "*/", false);
  public static final Bracket ML_BLOCK_COMMENT = literal("(*", "*)", true);
}

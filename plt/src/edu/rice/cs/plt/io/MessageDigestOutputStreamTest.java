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

package edu.rice.cs.plt.io;

import junit.framework.TestCase;
import edu.rice.cs.plt.text.TextUtil;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import edu.rice.cs.plt.debug.Stopwatch;

public class MessageDigestOutputStreamTest extends TestCase {
  
  public void testMD5() throws IOException {
    // expected results come from OS X md5 command
    MessageDigestOutputStream out;
    
    out = MessageDigestOutputStream.makeMD5();
    assertEquals("d4 1d 8c d9 8f 00 b2 04 e9 80 09 98 ec f8 42 7e",
                 TextUtil.toHexString(out.digest()));
    
    out = MessageDigestOutputStream.makeMD5();
    out.writeAll(new FileInputStream("testFiles/hashfile.txt"));
    assertEquals("5d 35 ed 4a 2a 02 63 bf 48 c4 bc 27 36 09 a6 48",
                 TextUtil.toHexString(out.digest()));
  }
  
  public void testSHA1() throws IOException {
    // expected results come from OS X "openssl dgst -sha1" command
    MessageDigestOutputStream out;
    
    out = MessageDigestOutputStream.makeSHA1();
    assertEquals("da 39 a3 ee 5e 6b 4b 0d 32 55 bf ef 95 60 18 90 af d8 07 09",
                 TextUtil.toHexString(out.digest()));
    
    out = MessageDigestOutputStream.makeSHA1();
    out.writeAll(new FileInputStream("testFiles/hashfile.txt"));
    assertEquals("03 f6 9f fb c3 4b ff fc 16 31 c6 5f 28 ad 2a 91 09 19 60 3f",
                 TextUtil.toHexString(out.digest()));
  }
  
  public void testSHA256() throws IOException {
    // expected results come from running this code (no command-line tool is handy...)
    MessageDigestOutputStream out;
    
    out = MessageDigestOutputStream.makeSHA256();
    assertEquals("e3 b0 c4 42 98 fc 1c 14 9a fb f4 c8 99 6f b9 24 27 ae 41 e4 64 9b 93 4c a4 95 99 1b 78 52 b8 55",
                 TextUtil.toHexString(out.digest()));
    
    out = MessageDigestOutputStream.makeSHA256();
    out.writeAll(new FileInputStream("testFiles/hashfile.txt"));
    assertEquals("53 5e c1 74 99 d0 23 56 9a 11 78 4e cc 07 59 71 a2 03 ec 69 50 0c 9f cd 01 5a a2 88 eb 3a 2c 37",
                 TextUtil.toHexString(out.digest()));
  }
  
  
  /** Run to compare performance.  args[0] is a file name. */
  public static void main(String... args) throws IOException {
    File f = new File(args[0]);
    Stopwatch s = new Stopwatch();
    int iterations = 20;
    long time;

    s.start();
    for (int i = 0; i < iterations; i++) { IOUtil.adler32Hash(f); }
    time = s.stop();
    System.out.println("Time for " + iterations + " runs of Adler-32: " + time);
    
    s.start();
    for (int i = 0; i < iterations; i++) { IOUtil.crc32Hash(f); }
    time = s.stop();
    System.out.println("Time for " + iterations + " runs of CRC-32: " + time);
    
    s.start();
    for (int i = 0; i < iterations; i++) { IOUtil.md5Hash(f); }
    time = s.stop();
    System.out.println("Time for " + iterations + " runs of MD5: " + time);
    
    s.start();
    for (int i = 0; i < iterations; i++) { IOUtil.sha1Hash(f); }
    time = s.stop();
    System.out.println("Time for " + iterations + " runs of SHA-1: " + time);
    
    s.start();
    for (int i = 0; i < iterations; i++) { IOUtil.sha256Hash(f); }
    time = s.stop();
    System.out.println("Time for " + iterations + " runs of SHA-256: " + time);
  }
  
}

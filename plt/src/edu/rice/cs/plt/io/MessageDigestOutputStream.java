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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** A stream that accumulates its bytes in a {@link MessageDigest} object. */
public class MessageDigestOutputStream extends DirectOutputStream {
  private final MessageDigest _messageDigest;
  
  /**
   * Instantiate with the given MessageDigest.  {@code messageDigest} will not be reset, and
   * may contain a partially-computed digest.
   */
  public MessageDigestOutputStream(MessageDigest messageDigest) { _messageDigest = messageDigest; }
  
  /** Return {@code messageDigest.digest()}. */
  public byte[] digest() { return _messageDigest.digest(); }
  
  @Override public void close() {}
  @Override public void flush() {}
  @Override public void write(byte[] bbuf) { _messageDigest.update(bbuf); }
  @Override public void write(byte[] bbuf, int offset, int len) { _messageDigest.update(bbuf, offset, len); }
  @Override public void write(int b) { _messageDigest.update((byte) b); }
  
  /**
   * Create a stream for computing MD5 hashes.  Throws a {@code RuntimeException} with a
   * {@link NoSuchAlgorithmException} cause if the MD5 algorithm implementation cannot be located.
   */
  public static MessageDigestOutputStream makeMD5() {
    try { return new MessageDigestOutputStream(MessageDigest.getInstance("MD5")); }
    catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
  }
  
  /**
   * Create a stream for computing SHA-1 hashes.  Throws a {@code RuntimeException} with a
   * {@link NoSuchAlgorithmException} cause if the SHA-1 algorithm implementation cannot be located.
   */
  public static MessageDigestOutputStream makeSHA1() {
    try { return new MessageDigestOutputStream(MessageDigest.getInstance("SHA-1")); }
    catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
  }
  
  /**
   * Create a stream for computing SHA-256 hashes.  Throws a {@code RuntimeException} with a
   * {@link NoSuchAlgorithmException} cause if the SHA-256 algorithm implementation cannot be located.
   */
  public static MessageDigestOutputStream makeSHA256() {
    try { return new MessageDigestOutputStream(MessageDigest.getInstance("SHA-256")); }
    catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
  }
  
}

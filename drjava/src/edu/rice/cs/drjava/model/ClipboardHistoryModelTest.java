package edu.rice.cs.drjava.model;

import junit.framework.TestCase;
import junit.framework.Assert;

import java.util.List;

public class ClipboardHistoryModelTest extends TestCase {
  public void testPutGetAndResize() {
    ClipboardHistoryModel clip = ClipboardHistoryModel.singleton();
    
    Assert.assertEquals("An empty clipboard does not return null", null, clip.getMostRecent());
    clip.put("abcd");
    Assert.assertEquals("Clipboard does not return expected string", "abcd", clip.getMostRecent());
    
    for(int i = 0; i < 10; i++){
      clip.put("" + (char)(i+26));
    }
    
    List<String> strings = clip.getStrings();
    for(int i = 0; i < 10; i++){
      Assert.assertEquals("Clipboard does not return expected string in list", "" + (char)(i+26), strings.get(i));
    }
    
    clip.resize(5);
    
    Assert.assertEquals("Clipboard size did not change after resize", 5, clip.getStrings().size());
  }
  
}

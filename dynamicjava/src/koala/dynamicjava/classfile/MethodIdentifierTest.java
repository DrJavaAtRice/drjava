
package koala.dynamicjava.classfile;

import junit.framework.*;

public class MethodIdentifierTest extends TestCase {
  
  private MethodIdentifier _methodIDf;
  private String _declaringClassStr;
  private String _nameOfClassStr;
  private String _returnTypeStr;
  private String[] _parametersTypesStrs = {"type1","type2","type3"};
  
  /**
   * Create a new instance of this TestCase.
   * @param     String name
   */
  public MethodIdentifierTest(String name) {
    super(name);
  }
  protected void setUp() {
    _declaringClassStr = new String("declaringClassString");
    _nameOfClassStr = new String("nameOfClassString");
    _returnTypeStr = new String("returnTypeString");
    
    _methodIDf =  new MethodIdentifier(_declaringClassStr,_nameOfClassStr,_returnTypeStr,_parametersTypesStrs);
  }
  public void testConstructor(){
    assertEquals(_declaringClassStr,_methodIDf.getDeclaringClass());
    assertEquals(_nameOfClassStr,_methodIDf.getName());
    assertEquals(_returnTypeStr,_methodIDf.getType());
    String[] tmpStr = _methodIDf.getParameters();
    assertTrue("The parameters should be of the same length",tmpStr.length==_parametersTypesStrs.length);
    for(int i=0;i<_parametersTypesStrs.length;i++){
      assertTrue("Parameter: "+tmpStr[i]+" should have been equal to parameter: "+_parametersTypesStrs[i], tmpStr[i].equals(_parametersTypesStrs[i]));
    }
  }
}
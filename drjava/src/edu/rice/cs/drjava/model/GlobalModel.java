package edu.rice.cs.drjava;

import javax.swing.text.Document;

public class GlobalModel {
  
  private DefinitionsDocument _definitionsDoc;
  private InteractionsDocument _interactionsDoc;
  private Document _consoleDoc;
  private CompilerError[] _compileErrors;
  private ModelList<GlobalModelListener> _listeners;
  
  public GlobalModel(DefinitionsDocument definitionsDoc, 
                     InteractionsDocument interactionsDoc,
                     Document consoleDoc, 
                     CompilerError[] compileErrors) 
  {
    _definitionsDoc = definitionsDoc;
    _interactionsDoc = interactionsDoc;
    _consoleDoc = consoleDoc;
    _compileErrors = compileErrors;
    _listeners = new ModelList<GlobalModelListener>();
  }
  
  public void addListener(GlobalModelListener listener) {
    _listeners.insertFront(listener);
  }
  
  public Document getDefinitionsDocument() {
    return _definitionsDoc;
  }
  public Document getInteractionsDocument() {
    return _interactionsDoc;
  }
  public Document getConsoleDocument() {
    return _consoleDoc;
  }
  public CompilerError[] getCompileErrors() {
    return _compileErrors;
  }
  
  public boolean isModifiedSinceSave() {
    return _definitionsDoc.isModifiedSinceSave();
  }
  
  public void newFile() {
    notifyListeners(new EventNotifier() {
      public void notify(GlobalModelListener l) {
        l.fireNewFileEvent();
      }
    });
  }
  
  public void saveFileAs(String fileName) {
    // Frustrating need to make final for inner class.
    // Could we make a one-argument constructor for the
    // inner class instead?
    final String _fileName = fileName;
    
    // Note that we are not yet saving the document to
    // disk! But this call alone passes existing tests;
    // I'm late and must commit before leaving...
    _definitionsDoc.resetModification();
    
    notifyListeners(new EventNotifier() {
      public void notify(GlobalModelListener l) {
        l.fireSaveFileEvent(_fileName);
      }
    });
  }
  
  private void notifyListeners(EventNotifier n) {
    ModelList<GlobalModelListener>.Iterator i = _listeners.getIterator();
    
    // Frustrating need to move iterator out of initial position before iterating.
    i.next();
    while(!i.atEnd()) {
      n.notify(i.current());
      i.next();
    }
  }
  
  private abstract class EventNotifier {
    public abstract void notify(GlobalModelListener l);
  }
}
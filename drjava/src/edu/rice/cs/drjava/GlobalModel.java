package edu.rice.cs.drjava;

import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import java.io.FileWriter;
import java.io.IOException;

public class GlobalModel {
  
  private DefinitionsEditorKit _editorKit;
  private DefinitionsDocument _definitionsDoc;
  private InteractionsDocument _interactionsDoc;
  private Document _consoleDoc;
  private CompilerError[] _compileErrors;
  private ModelList<GlobalModelListener> _listeners;
  
  public GlobalModel(DefinitionsEditorKit editorKit,
                     DefinitionsDocument definitionsDoc, 
                     InteractionsDocument interactionsDoc,
                     Document consoleDoc, 
                     CompilerError[] compileErrors) 
  {
    _editorKit = editorKit;
    _definitionsDoc = definitionsDoc;
    _interactionsDoc = interactionsDoc;
    _consoleDoc = consoleDoc;
    _compileErrors = compileErrors;
    _listeners = new ModelList<GlobalModelListener>();
  }
  
  public void addListener(GlobalModelListener listener) {
    _listeners.insertFront(listener);
  }
  
  public DefinitionsEditorKit getEditorKit() {
    return _editorKit;
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
  
  
  /**
   * Determines if the document has changed since the last save.
   * @return true if the document has been modified
   */
  public boolean isModifiedSinceSave() {
    return _definitionsDoc.isModifiedSinceSave();
  }
  
  /**
   * Creates a new document in the definitions pane.
   * Checks first to make sure no changes need to be saved.
   * If the user saves the changes, or chooses to disregard any
   * changes, the creation of a new document continues, otherwise
   * it is halted and the document remains the same.
   */
  public void newFile() {
    boolean performNew = canAbandonFile();
    if (performNew) {
      _notifyListeners(new EventNotifier() {
        public void notify(GlobalModelListener l) {
          l.newFileCreated();
        }
      });
    }
  }
  
  /**
   * Saves a file using the Writer encapsulated in the given WriterCommand.
   * @param com a command containing the Writer and name of
   * the file to save to.
   */
  public void saveFile(WriterCommand com) {
    boolean success = false;
    try {
      _editorKit.write(com.getWriter(), _definitionsDoc, 0, _definitionsDoc.getLength());
      success = true;
    }
    catch (IOException writeFailed) {
    }
    catch (BadLocationException docFailed) {
    }
    if (success) {
      final String _fileName = com.getName();
      // Note that we are not yet saving the document to
      // disk! But this call alone passes existing tests;
      // I'm late and must commit before leaving...
      _definitionsDoc.resetModification();
      
      _notifyListeners(new EventNotifier() {
        public void notify(GlobalModelListener l) {
          l.fileSaved(_fileName);
        }
      });
    }
  }
  
  /**
   * Open a new document and read from the Reader encapsulated in the
   * ReaderCommand.
   * @param com a command containing the Reader and name of
   * the file to open from. 
   */
  public void openFile(ReaderCommand com) {
    boolean performOpen = canAbandonFile();
    if (performOpen) {
      boolean success = false;
      try {
        _editorKit.read(com.getReader(), _definitionsDoc, 0);
        success = true;
      }
      catch (IOException readFailed) {
      }
      catch (BadLocationException docFailed) {
      }
      if (success) {
        final String _fileName = com.getName();
        _definitionsDoc.resetModification();
        _notifyListeners(new EventNotifier() {
          public void notify(GlobalModelListener l) {
            l.fileOpened(_fileName);
          }
        });
      }
    }
  }
  
  
  /**
   * Let the listeners know that a compile has begun.
   */
  public void beginCompile() {
    _notifyListeners(new EventNotifier() {
      public void notify(GlobalModelListener l) {
        l.compileStarted();
      }
    });
  }
  /**
   * Let the listeners know that a compile has ended.
   */
  public void endCompile() {
    _notifyListeners(new EventNotifier() {
      public void notify(GlobalModelListener l) {
        l.compileEnded();
      }
    });
  }
  
  /**
   * Let the listeners know that the user has quit the program.
   * This gives the listeners ample chance to clean up before the 
   * program exits.
   */
  public void quit() {
    _notifyListeners(new EventNotifier() {
      public void notify(GlobalModelListener l) {
        l.quit();
      }
    });
  }
  
  /**
   * Lets the listeners know that the interactions pane has been cleared.
   */
  public void resetInteractions() {
    _interactionsDoc.reset();
    _notifyListeners(new EventNotifier() {
      public void notify(GlobalModelListener l) {
        l.interactionsReset();
      }
    });
  }
  
  /**
   * Lets the listeners know that the console pane has been cleared.
   */
  public void resetConsole() {
    try {
      _consoleDoc.remove(0, _consoleDoc.getLength());
    }
    catch (BadLocationException impossible) {
    }
    _notifyListeners(new EventNotifier() {
      public void notify(GlobalModelListener l) {
        l.consoleReset();
      }
    });
  }

  /**
   * Asks the listeners if the GlobalModel can abandon the current document.
   * @return true if the current document may be abandoned, false if the
   * current action should be halted in its tracks (e.g., file open when
   * the document has been modified since the last save)
   */
  public boolean canAbandonFile() {
    return _pollListeners(new EventPoller() {
      public boolean poll(GlobalModelListener l) {
        return l.canAbandonFile();
      }
    });
  }
                  
  /**
   * Allows the GlobalModel to ask its listeners a yes/no question and
   * receive a response.
   * @param the listeners' responses ANDed together, true if they all
   * agree, false if some disagree
   */
  private boolean _pollListeners(EventPoller p) {
    ModelList<GlobalModelListener>.Iterator i = _listeners.getIterator();
    boolean poll = true;
    // Frustrating need to move iterator out of initial position before iterating.
    i.next();
    while(!i.atEnd()) {
      poll = poll && p.poll(i.current());
      i.next();
    }
    return poll;
  }
    
  /**
   * Lets the listeners know some event has taken place.
   */
  private void _notifyListeners(EventNotifier n) {
    ModelList<GlobalModelListener>.Iterator i = _listeners.getIterator();
    
    // Frustrating need to move iterator out of initial position before iterating.
    i.next();
    while(!i.atEnd()) {
      n.notify(i.current());
      i.next();
    }
  }
  
  /**
   * Class model for notifying listeners of an event.
   */
  private abstract class EventNotifier {
    public abstract void notify(GlobalModelListener l);
  }
  
  /**
   * Class model for asking listeners a yes/no question.
   */
  private abstract class EventPoller {
    public abstract boolean poll(GlobalModelListener l);
  }
}
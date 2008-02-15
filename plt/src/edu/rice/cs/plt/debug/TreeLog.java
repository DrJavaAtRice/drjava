/*BEGIN_COPYRIGHT_BLOCK*

PLT Utilities BSD License

Copyright (c) 2007-2008 JavaPLT group at Rice University
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

package edu.rice.cs.plt.debug;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Date;
import java.io.Serializable;

import edu.rice.cs.plt.swing.SwingUtil;
import edu.rice.cs.plt.swing.ShadedTreeCellRenderer;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.lambda.LazyThunk;
import edu.rice.cs.plt.lambda.Predicate2;
import edu.rice.cs.plt.lambda.WrappedException;
import edu.rice.cs.plt.concurrent.ConcurrentUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A GUI-based log that displays logged messages in a collapsible tree in a separate process.
 * Currently does not behave well in the presense of multi-threaded logging.
 */
public class TreeLog extends AbstractLog {
  
  private final Thunk<Viewer> _viewer;
  
  public TreeLog(String name) {
    super();
    _viewer = makeViewerThunk(name);
  }
  
  public TreeLog(String name, Predicate2<? super Thread, ? super StackTraceElement> filter) {
    super(filter);
    _viewer = makeViewerThunk(name);
  }
  
  private Thunk<Viewer> makeViewerThunk(String name) {
    return LazyThunk.make(ConcurrentUtil.computeInProcess(new MakeViewerTask(name), false));
  }
  
  protected void pop() {
    try { _viewer.value().pop(); }
    catch (RemoteException e) { /* Give up */ }
  }
  
  protected void push() {
    try { _viewer.value().push(); }
    catch (RemoteException e) { /* Give up */ }
  }
  
  protected void write(Date time, Thread thread, StackTraceElement location,
                       SizedIterable<? extends String> messages) {
    try {
      // take a snapshot of messages because AbstractLog doesn't construct a serializable SizedIterable
      _viewer.value().write(formatTime(time), formatThread(thread), formatLocation(location),
                            IterUtil.snapshot(messages));
    }
    catch (RemoteException e) { /* Give up */ }
  }
  
  /** Interface for RMI server. */
  private static interface Viewer extends Remote {
    public void write(String time, String thread, String location, SizedIterable<? extends String> messages)
      throws RemoteException;
    public void push() throws RemoteException;
    public void pop() throws RemoteException;
  }
  
  /** Task to be executed by the server process's main method. */
  private static class MakeViewerTask implements Thunk<Viewer>, Serializable {
    private final String _name;
    public MakeViewerTask(String name) { _name = name; }
    public Viewer value() {
      try { return (Viewer) UnicastRemoteObject.exportObject(new ViewerImpl(_name), 0); }
      catch (RemoteException e) { throw new WrappedException(e); }
    }
  }
  
  /** Implementation of the RMI server object. */
  private static class ViewerImpl implements Viewer {
    
    /** Path from current messages' parent to the root of the tree, in bottom-to-top order. */
    private final LinkedList<DefaultMutableTreeNode> _stack;
    
    /** Flag signalling that the next logged message should be an "end." */
    private boolean _pop;
    
    private final DefaultTreeModel _treeModel;
    
    public ViewerImpl(final String name) {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(Entry.ROOT);
      _stack = new LinkedList<DefaultMutableTreeNode>();
      _stack.addFirst(root); // root of the tree
      _pop = false;
      _treeModel = new DefaultTreeModel(root);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          JFrame frame = SwingUtil.makeMainApplicationFrame(name, 600, 600);
          
          final JTree tree = new JTree(_treeModel);
          tree.setRootVisible(false);
          tree.setShowsRootHandles(true);
          tree.setRowHeight(0);
          // Because the root is hidden, we must expand it programmatically
          // But we can't until it has a child, so we wait for a child via a listener
          _treeModel.addTreeModelListener(new TreeModelListener() {
            public void treeNodesInserted(TreeModelEvent e) {
              _treeModel.removeTreeModelListener(this);
              tree.expandPath(e.getTreePath());
            }
            public void treeNodesChanged(TreeModelEvent e) {}
            public void treeNodesRemoved(TreeModelEvent e) {}
            public void treeStructureChanged(TreeModelEvent e) {}
          });
          
          final JPanel entryCell = SwingUtil.makeVerticalBoxPanel(3, 5);
          final JPanel top = SwingUtil.makeHorizontalBoxPanel();
          final JPanel bottom = SwingUtil.makeBorderPanel(3, 15, 0, 0);
          SwingUtil.setOpaque(false, top, bottom);
          SwingUtil.setLeftAlignment(top, bottom);
          SwingUtil.add(entryCell, top, bottom);
          final JLabel location = new JLabel();
          final JLabel time = new JLabel();
          final JLabel descendents = new JLabel();
          final JTextArea messages = new JTextArea();
          SwingUtil.setMonospacedFont(12, location, time, descendents, messages);
          SwingUtil.setEmptyBorder(0, 10, 0, 0, location, descendents);
          messages.setOpaque(false);
          SwingUtil.add(top, time, location, descendents);
          bottom.add(messages);
          tree.setCellRenderer(new TreeCellRenderer() {
            public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                          boolean selected, boolean expanded,
                                                          boolean leaf, int row, boolean hasFocus) {
              Entry e = (Entry) ((DefaultMutableTreeNode) value).getUserObject();
              if (e.descendents() == 0) { descendents.setVisible(false); }
              else {
                descendents.setVisible(true);
                descendents.setText("(" + e.descendents() + ")");
              }
              time.setText(e.time());
              location.setText(e.location());
              messages.setText(IterUtil.multilineToString(e.messages()));
              bottom.setVisible(!messages.getText().equals(""));
              return entryCell;
            }
          });
          ShadedTreeCellRenderer.shadeTree(tree, SwingUtil.gray(.1f), SwingUtil.gray(.03f));
          
          frame.getContentPane().add(new JScrollPane(tree));
          SwingUtil.displayWindow(frame);
        }
      });
    }
    
    public void write(final String time, final String thread, final String location,
                      final SizedIterable<? extends String> messages) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          Entry entry = new Entry(time, thread, location, messages);
          DefaultMutableTreeNode parent = _stack.getFirst();
          _treeModel.insertNodeInto(new DefaultMutableTreeNode(entry), parent, parent.getChildCount());
          for (DefaultMutableTreeNode ancestor : _stack) {
            ((Entry) ancestor.getUserObject()).incrementDescendents();
            _treeModel.nodeChanged(ancestor);
          }
          if (_pop) { _stack.removeFirst(); _pop = false; }
        }
      });
    }
    
    public void push() {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          _stack.addFirst((DefaultMutableTreeNode) _stack.getFirst().getLastChild());
        }
      });
    }
    
    public void pop() {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (_stack.size() > 1) { _pop = true; }
        }
      });
    }
    
  }
    
  private static class Entry {
    private final String _time;
    private final String _thread;
    private final String _location;
    private final SizedIterable<? extends String> _messages;
    private int _descendents;
    
    private static final Entry ROOT = new Entry("<root>", "<root>", "<root>", IterUtil.<String>empty());
    
    public Entry(String time, String thread, String location, SizedIterable<? extends String> messages) {
      _time = time;
      _thread = thread;
      _location = location;
      _messages = messages;
      _descendents = 0;
    }
    
    public int descendents() { return _descendents; }
    public String time() { return _time; }
    public String thread() { return _thread; }
    public String location() { return _location; }
    public SizedIterable<? extends String> messages() { return _messages; }
    public void incrementDescendents() { _descendents++; }
    public String toString() {
      return _location + " " + _time;
    }
    
  }
    
}

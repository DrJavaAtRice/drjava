package edu.rice.cs.plt.debug;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.awt.*;
import java.io.Serializable;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import edu.rice.cs.plt.iter.IterUtil;
import edu.rice.cs.plt.iter.SizedIterable;
import edu.rice.cs.plt.lambda.Thunk;
import edu.rice.cs.plt.swing.ShadedTreeCellRenderer;
import edu.rice.cs.plt.swing.SwingUtil;

public class TreeLogSink extends TextLogSink {

  private final String _name;
  private final Map<Long, Tree> _trees; // maps thread IDs to trees
  private final boolean _exitOnClose;
  
  /** Convenience constructor, with {@code exitOnClose} set to {@code false}. */
  public TreeLogSink(String name) { this(name, false); }
  
  /**
   * @param name  Name to use in the window title
   * @param exitOnClose  Whether {@link System#exit} should be invoked when the last open tree window is closed
   */
  public TreeLogSink(String name, boolean exitOnClose) {
    _name = name;
    _trees = new HashMap<Long, Tree>();
    _exitOnClose = exitOnClose;
  }
  
  public void close() {
    for (Tree t : IterUtil.snapshot(_trees.values())) { t.dispose(); }
  }
  
  @Override protected void write(Message m, SizedIterable<String> text) {
    final Tree tree = getTree(m.thread());
    final Entry entry = new Entry(m, text);
    tree.checkQueue();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { tree.addEntry(entry); }
    });
  }
  
  @Override protected void writeStart(StartMessage m, SizedIterable<String> text) {
    final Tree tree = getTree(m.thread());
    final Entry entry = new Entry(m, text);
    tree.checkQueue();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { tree.addEntry(entry); tree.push(); }
    });
  }

  @Override protected void writeEnd(EndMessage m, SizedIterable<String> text) {
    final Tree tree = getTree(m.thread());
    final Entry entry = new Entry(m, text);
    tree.checkQueue();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() { tree.addEntry(entry); tree.pop(); }
    });
  }
  
  private synchronized Tree getTree(ThreadSnapshot thread) {
    final Long id = thread.getId();
    if (!_trees.containsKey(id)) {
      _trees.put(id, new Tree(_name + ": " + formatThread(thread), new Runnable() {
        public void run() {
          _trees.remove(id);
          if (_exitOnClose && _trees.isEmpty()) { System.exit(0); }
        }
      }));
    }
    return _trees.get(id);
  }
  
  /** Create a serializable TreeLogSink factory.  (For compatibility with {@link RMILogSink}.) */
  public static Thunk<TreeLogSink> factory(String name) { return factory(name, false); }
  
  public static Thunk<TreeLogSink> factory(String name, boolean exitOnClose) {
    return new Factory(name, exitOnClose);
  }
  
  private static class Factory implements Thunk<TreeLogSink>, Serializable {
    private final String _name;
    private final boolean _exitOnClose;
    public Factory(String name, boolean exitOnClose) { _name = name; _exitOnClose = exitOnClose; }
    public TreeLogSink value() { return new TreeLogSink(_name, _exitOnClose); }
  }
  
  /**
   * An entry in the tree.  To minimize its memory footprint, only holds onto user-visible information from
   * the given message.
   */
  private static class Entry {
    private final String _time;
    private final String _location;
    private final SizedIterable<String> _text;
    private int _descendents;
    
    private static final Entry ROOT = new Entry();
    
    public Entry(Message m, SizedIterable<String> text) {
      _time = formatTime(m.time());
      _location = formatLocation(m.caller());
      _text = text;
      _descendents = 0;
    }
    
    /** Constructor for the dummy root entry. */
    private Entry() {
      _time = "<root>";
      _location = "<root>";
      _text = IterUtil.empty();
      _descendents = 0;
    }
    
    public int descendents() { return _descendents; }
    public String time() { return _time; }
    public String location() { return _location; }
    public SizedIterable<String> text() { return _text; }
    public void incrementDescendents() { _descendents++; }
    public String toString() {
      return _location + " " + _time;
    }
    
  }
  
  /** A tree of Entries, presented in a Swing window. */
  private static class Tree {
    private volatile long _lastPainted;
    private volatile JFrame _frame;
    private final Runnable _onClose;

    // These fields are synchronized by only accessing them via the event thread
    /** Path from current messages' parent to the root of the tree, in bottom-to-top order. */
    private final LinkedList<DefaultMutableTreeNode> _stack;
    private final DefaultTreeModel _treeModel;

    public Tree(final String name, final Runnable onClose) {
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(Entry.ROOT);
      _stack = new LinkedList<DefaultMutableTreeNode>();
      _stack.addFirst(root); // root of the tree
      _treeModel = new DefaultTreeModel(root);
      _lastPainted = 0l;
      _onClose = onClose;
      _frame = null;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { initGUI(name); }
      });
    }
    
    /** Should be invoked from the event thread. */
    private void initGUI(final String name) {
      _frame = SwingUtil.makeDisposableFrame(name, 600, 600);
      SwingUtil.onWindowClosed(_frame, _onClose);
      
      final JTree tree = new JTree(_treeModel) {
        public void paint(Graphics g) {
          super.paint(g);
          // keep lastPainted up to date
          _lastPainted = System.currentTimeMillis();
        }
      };
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
      final JTextArea text = new JTextArea();
      SwingUtil.setMonospacedFont(12, location, time, descendents, text);
      SwingUtil.setEmptyBorder(0, 10, 0, 0, location, descendents);
      text.setOpaque(false);
      SwingUtil.add(top, time, location, descendents);
      bottom.add(text);
      tree.setCellRenderer(new TreeCellRenderer() {
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean selected, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
          Entry e = (Entry) ((DefaultMutableTreeNode) value).getUserObject();
          if (e.descendents() == 0) { descendents.setVisible(false); }
          else {
            descendents.setVisible(true);
            descendents.setText("[" + e.descendents() + "]");
          }
          time.setText(e.time());
          location.setText(e.location());
          text.setText(IterUtil.multilineToString(e.text()));
          bottom.setVisible(!text.getText().equals(""));
          return entryCell;
        }
      });
      ShadedTreeCellRenderer.shadeTree(tree, SwingUtil.gray(.1f), SwingUtil.gray(.03f));
      
      _frame.getContentPane().add(new JScrollPane(tree));
      SwingUtil.displayWindow(_frame);
    }

    
    /**
     * If a sufficient amount of time has passed, clear the event queue.  Without this check, expensive
     * or frequent logging can lead to all paint events being coalesced (and placed on the *back* of the
     * queue) before they have a chance to run.  To be effective, this method must <em>not</em> be called
     * from the event queue.
     */
    public void checkQueue() {
      if (System.currentTimeMillis() - _lastPainted > 200) {
        // Ensure that paint events are handled periodically.  
        SwingUtil.attemptClearEventQueue();
      }
    }
    
    /** Add an entry to the tree.  This method must be called from the event queue. */
    public void addEntry(Entry e) {
      DefaultMutableTreeNode parent = _stack.getFirst();
      _treeModel.insertNodeInto(new DefaultMutableTreeNode(e), parent, parent.getChildCount());
      for (DefaultMutableTreeNode ancestor : _stack) {
        ((Entry) ancestor.getUserObject()).incrementDescendents();
        _treeModel.nodeChanged(ancestor);
      }
    }
    
    /** Make the last entry a parent for future entries.  This method must be called from the event queue. */
    public void push() {
      _stack.addFirst((DefaultMutableTreeNode) _stack.getFirst().getLastChild());
    }
    
    /** Add future entries as children of the current grandparent.  This method must be called from the event queue. */
    public void pop() {
      if (_stack.size() > 1) { _stack.removeFirst(); }
    }
    
    /** Programmatically discard the tree. */
    public void dispose() {
      if (_frame != null) _frame.dispose();
      _onClose.run();
    }
    
  }
    
}

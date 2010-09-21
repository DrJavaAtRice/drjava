package edu.rice.cs.plt.swing;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JComponent;

import edu.rice.cs.plt.io.IOUtil;
import edu.rice.cs.plt.iter.IterUtil;

/**
 * A JComponent wrapper for an Applet, providing a lightweight context for testing or reusing
 * applet code.  Applets wrapped here behave differently in some ways than they do in a standard
 * browser environment:
 * <ul>
 * <li>No special SecurityManager controls the applet's access to system resources, so certain
 *     operations typically prohibited for applets may be permitted.</li>
 * <li>The {@code stop()} method is never called (by the current wrapper implementation, at least).</li>
 * <li>{@code showStatus()} has no effect.</li>
 * <li>{@code getDocumentBase()} and {@code getCodeBase()} return a root URL provided to the constructor,
 *     or (by default) the JVM's working directory.</li>
 * <li>{@code getAudioClip()} returns a dummy object whose methods have no effect.</li>
 * <li>{@code getAppletContext().getApplet()} always returns {@code null}.</li>
 * <li>{@code getAppletContext().showDocument()} has no effect.</li>
 * <li>The InputStream map maintained by the AppletContext is unique for each instance -- it can't
 *     be used for inter-applet communication.</li>
 * </ul>
 */
public class AppletComponent extends JComponent {
  
  private enum State { FRESH, PAUSED, RUNNING };
  
  private final Applet _applet;
  private final Map<String, String> _params;
  private final URL _root;
  private State _state;
  
  public AppletComponent(Applet applet) { 
    this(applet, null, Collections.<String, String>emptyMap());
  }
  
  /**
   * Constructor.
   * @param applet  A freshly constructed applet (will be initialized and started).
   * @param root  The code's root, or {@code null} to use the JVM's working directory.  If provided, should
   *              be a jar file or a directory (directory URLs end in '/'). 
   * @param params  Any custom parameters for the applet.
   */
  public AppletComponent(Applet applet, URL root, Map<String, String> params) {
    _applet = applet;
    _params = params;
    if (root == null) {
      try { _root = IOUtil.WORKING_DIRECTORY.toURI().toURL(); }
      catch (MalformedURLException e) { throw new RuntimeException("Can't convert the working dir to a URL"); }
    }
    else { _root = root; }
    _state = State.FRESH;
    add(_applet);
    _applet.setStub(new Stub());
  }
  
  public AppletComponent(Applet applet, int width, int height) {
    this(applet, width, height, null, Collections.<String, String>emptyMap());
  }
  
  /**
   * Constructor.
   * @param applet  A freshly constructed applet (will be initialized and started).
   * @param width  Width for the applet and this enclosing component.
   * @param height  Height for the applet and this enclosing component.
   * @param root  The code's root, or {@code null} to use the JVM's working directory.  If provided, should
   *              be a jar file or a directory (directory URLs end in '/'). 
   * @param params  Any custom parameters for the applet.
   */
  public AppletComponent(Applet applet, int width, int height, URL root, Map<String, String> params) {
    this(applet, root, params);
    _applet.setSize(width, height); // should trigger a change in this component's size, too
  }
  
  protected void paintComponent(Graphics g) {
    updateState();
    super.paintComponent(g);
  }
  
  private void updateState() {
    if (!_state.equals(State.RUNNING)) {
      if (_state.equals(State.FRESH)) {
        _state = State.PAUSED;
        // run Applet.init() outside teh event thread, as per bug report 3069101:
        // https://sourceforge.net/tracker/?func=detail&atid=438935&aid=3069101&group_id=44253
        edu.rice.cs.plt.concurrent.ConcurrentUtil.runInThread(new Runnable() {
          public void run() {
            _applet.init();
            _applet.start();
            _state = State.RUNNING;
            if (!_applet.getSize().equals(getSize())) { _applet.setSize(getSize()); }
            validate();
          }
        });
      }
    }
    if (_state.equals(State.RUNNING)) {
      // lazily update size rather than trying to intercept all resizing methods
      if (!_applet.getSize().equals(getSize())) { _applet.setSize(getSize()); }
    }
  }
  
  private class Stub implements AppletStub, AppletContext {
    private final Map<String, InputStream> _streams;
    public Stub() { _streams = new HashMap<String, InputStream>(); }
    public boolean isActive() { return _state.equals(State.RUNNING); }
    public URL getDocumentBase() { return _root; }
    public URL getCodeBase() { return _root; }
    public String getParameter(String name) { return _params.get(name); }
    public AppletContext getAppletContext() { return this; }
    public void appletResize(int width, int height) { AppletComponent.this.setSize(width, height); }
    public AudioClip getAudioClip(URL url) {
      return new AudioClip() {
        public void play() {}
        public void loop() {}
        public void stop() {}
      };
    }
    public Image getImage(URL url) { return Toolkit.getDefaultToolkit().getImage(url); }
    public Applet getApplet(String name) { return null; }
    public Enumeration<Applet> getApplets() {
      return IterUtil.asEnumeration(IterUtil.singleton(_applet).iterator());
    }
    public void showDocument(URL url) {}
    public void showDocument(URL url, String target) {}
    public void showStatus(String status) {}
    public void setStream(String key, InputStream stream) { _streams.put(key, stream); }
    public InputStream getStream(String key) { return _streams.get(key); }
    public Iterator<String> getStreamKeys() { return _streams.keySet().iterator(); }
  }

}

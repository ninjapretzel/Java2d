
import java.applet.Applet;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import java.util.Map;
import javax.swing.JApplet;
import javax.swing.Timer;

/** Base class for a simple applet */
public class AppBase extends Applet implements MouseListener, KeyListener, WindowListener {
	
	public static final Font DEFAULT_FONT = Font.decode("Tahoma");
	public static final Color BG_COLOR = new Color(44, 49, 54);
	
	/** Key states to be modified by event thread */
	public Map<Integer, Boolean> liveStates;
	/** Key states for the current frame */
	public Map<Integer, Boolean> currStates;
	/** Key states for the previous frame */
	public Map<Integer, Boolean> lastStates;
	
	/** Thread holding primary update loop logic */
	public Thread updateThread;
	
	/** Assigned to be the deltatime in seconds every frame  */
	public float delta = 0f;
	/** Assigned to be the total elapsed time in seconds every frame */
	public float time = 0f;
	/** Target for FPS */
	public float targetFPS = 60.0f;
	
	/** Has an exit request been sent? */
	public boolean exitRequest = false;
	/** Force close, even if not requested. */
	private boolean forceClose = false;
	/** Does the current window have focus? */
	public boolean hasFocus = false;
	/** Is the applet playing? */
	public boolean playing = false;
	
	/** Place we actually draw to, which has a buffer we display after drawing */
	public Canvas buffer;
	
	
	/** Initialization logic */
	public void init() {
		setBackground(BG_COLOR);
		
		currStates = new HashMap<>();
		lastStates = new HashMap<>();
		liveStates = new HashMap<>();
		
		Container c = this.getParent();
        while (c.getParent() != null) { c = c.getParent(); }
		if (c instanceof Window) {
			Window w = (Window) c;
			w.setLocationRelativeTo(null);
        }
		
		// Threads are another line of execution running in the same program.
		updateThread = new Thread( ()-> {
			buffer = new Canvas();
			buffer.setSize(getWidth(), getHeight());
			add(buffer);
			buffer.createBufferStrategy(2);
			setIgnoreRepaint(true);
			
			
			long end = System.currentTimeMillis();
			
			try {
				// Primary Game Logic Loop
				while (!exitRequest && !forceClose) {
					if (!playing) { continue; }
					// Copy the states from the input
					currStates.clear();
					for (Integer key : liveStates.keySet()) { currStates.put(key, liveStates.get(key)); }
					
					long start = System.currentTimeMillis();

					// Actually do the stuff if we are playing
					if (playing) {
						step();
						
						draw();
					}

					// Swap keyStates and lastKeyStates to prevent extra allocations
					Map<Integer, Boolean> temp = lastStates;
					lastStates = currStates;
					currStates = temp;
					for (Integer key : lastStates.keySet()) { currStates.put(key, lastStates.get(key)); }
					
					// Calculate the busy time for the frame, update the deltaTime
					long done = System.currentTimeMillis();
					long dur = done - start;
					
					// Pause the game for only about as long as we need to hit our target framerate
					long sleepMillis = ((long) (1000f / targetFPS)) - dur;
					if (sleepMillis > 0) {
						Thread.sleep((int)sleepMillis);
					}
					
					// Calculate the delta time for the next frame 
					end = System.currentTimeMillis();
					dur = end - start;
					delta = dur / 1000.0f;
					time += delta;
				}
				
			} catch (Exception e) {
				System.err.println("Error crept up to main game loop: " + e);
			}
		});
		updateThread.start();
	}
	
	public void start() {
		playing = true;
	}
	
	public void stop() {
		playing = false;
	}
	
	public void destroy() {
		exitRequest = forceClose = true;
	}
	
	public void draw() {
		BufferStrategy bs = buffer.getBufferStrategy();
		bs.show();
		if (!bs.contentsLost()) { bs.show(); }
		
		Graphics2D g = (Graphics2D)bs.getDrawGraphics();
		g.setFont(DEFAULT_FONT);
		update(g);
		draw(g);
		
		g.dispose();
	}
	
	public void draw(Graphics2D g) {
		g.setFont(DEFAULT_FONT);
		g.setColor(BG_COLOR);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		
		g.setColor(Color.WHITE);
		String data = "Test\n\tdelta: " + delta + "\n\ttime: " + time + "";
		drawString(g, data, 55, 55);
		// Note: This string gets drawn on one line
		g.drawString(data, 55, 20);
		
	}
	
	public void drawString(Graphics2D g, String text, int x, int y) {
		String[] strs = text.split("\n");
		int lineSize = g.getFont().getSize();
		for (int i = 0; i < strs.length; i++) {
			int yy = y + i * lineSize;
			g.drawString(strs[i], x, yy);
			
		}
		
	}
	
	
	// Main Logic method
	public void step() {
		
	}
		
	/** Gets the current state of a given key */
	public boolean getKey(int keyCode) { return currStates.get(keyCode); }
	
	/** Gets the if a given key was released between the previous frame and now */
	public boolean getKeyPress(int keyCode) { return currStates.get(keyCode) && !lastStates.get(keyCode); }

	@Override public void mouseClicked(MouseEvent e) {
		
	}

	@Override public void mousePressed(MouseEvent e) {
		
	}

	@Override public void mouseReleased(MouseEvent e) {
		
	}

	@Override public void mouseEntered(MouseEvent e) {
		
	}

	@Override public void mouseExited(MouseEvent e) {
		
	}
	

	/** Required for KeyListener, even if we don't use it */
	@Override public void keyTyped(KeyEvent e) { }
	
	

	/** Register a key as pressed in the map */
	@Override public void keyPressed(KeyEvent e) { liveStates.put(e.getKeyCode(), true); }

	/** Register a key as released in the map */
	@Override public void keyReleased(KeyEvent e) { liveStates.put(e.getKeyCode(), false); }

	@Override public void windowOpened(WindowEvent e) { }

	@Override public void windowClosing(WindowEvent e) { exitRequest = true; }

	@Override
	public void windowClosed(WindowEvent e) { }

	@Override
	public void windowIconified(WindowEvent e) { }

	@Override
	public void windowDeiconified(WindowEvent e) { }

	@Override public void windowActivated(WindowEvent e) { hasFocus = true; }

	@Override public void windowDeactivated(WindowEvent e) { hasFocus = false; }
		
		
	
	
	
}

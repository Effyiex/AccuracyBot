package cf.effyiex.bot;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JWindow;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

public class Bot extends Robot implements NativeKeyListener, MouseListener {

	public Bot() throws AWTException { super(); }
	
	public static void main(String[] args) {
		try {
			LogManager.getLogManager().reset();
			Logger.getLogger(GlobalScreen.class.getPackage().getName());
			GlobalScreen.registerNativeHook();
			new Bot().initialize();
		} catch (AWTException | NativeHookException e) { e.printStackTrace(); }
	}
	
	private Window marker;
	private Rectangle area;
	
	private Thread thread;
	
	private Color color;
	
	private boolean state = false;
	private boolean dragging = false;
	
	public void initialize() {
		GlobalScreen.addNativeKeyListener(Bot.this);
		this.marker = new JWindow();
		this.marker.setBackground(new Color(255, 55, 255, 55));
		this.marker.setAlwaysOnTop(true);
		this.marker.addMouseListener(Bot.this);
		this.setAutoDelay(72);
		this.thread = new Thread(() -> {
			try {
				while(true) {
					if(!state && area != null) marker.setBounds(area);
					if(!state) {
						Thread.sleep(this.getAutoDelay());
						continue;
					}
					if(area == null) {
						log("The Area hasn't been defined!");
						state = false;
						continue;
					}
					if(color == null) {
						log("The Color hasn't been defined!");
						state = false;
						continue;
					}
					BufferedImage screenshot = this.createScreenCapture(area);
					Point last = new Point(-100, -100);
					for(int y = 0; y < area.height; y++)
						for(int x = 0; x < area.width; x++) {
							Color pc = new Color(screenshot.getRGB(x, y));
							int totalDiff = 0;
							totalDiff += difference(color.getRed(), pc.getRed());
							totalDiff += difference(color.getGreen(), pc.getGreen());
							totalDiff += difference(color.getBlue(), pc.getBlue());
							if(totalDiff > 64 || !state) continue;
							if(difference(last.x, x) < 128) continue;
							if(difference(last.y, y) < 128) continue;
							this.mouseMove(x + area.x, y + area.y);
							this.mousePress(InputEvent.BUTTON1_DOWN_MASK);
							this.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
							last = new Point(x, y);
						}
				}
			} catch(InterruptedException e) { e.printStackTrace(); }
		});
		this.marker.setVisible(true);
		this.thread.start();
		log("Initialized the Bot!");
		log("Controls:");
		log(" 'V' = toggle bot");
		log(" 'X' = catch color");
		log(" 'C' = drag area\n  (left-top -> right-bottom)");
	}
	
	@Override
	public void nativeKeyReleased(NativeKeyEvent event) {
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		if(event.getKeyCode() == NativeKeyEvent.VC_V) {
			state = !state;
			marker.setVisible(!state);
		} else if(!state && event.getKeyCode() == NativeKeyEvent.VC_X) {
			marker.setVisible(false);
			color = this.getPixelColor(mouse.x, mouse.y);
			log("Set the searching color to: " + color.toString());
			marker.setVisible(true);
		} else if(!state && dragging && event.getKeyCode() == NativeKeyEvent.VC_C) {
			area.width = (mouse.x - area.x);
			area.height = (mouse.y - area.y);
			dragging = false;
			log("Successfully marked the Check-Area!");
		} else if(state) log("You can't redefine stuff while the bot is working!");
	}

	@Override
	public void nativeKeyPressed(NativeKeyEvent event) {
		if(dragging || event.getKeyCode() != NativeKeyEvent.VC_C) return;
		Point mouse = MouseInfo.getPointerInfo().getLocation();
		area = new Rectangle();
		area.x = mouse.x;
		area.y = mouse.y;
		dragging = true;
	}
	
	public static void log(String text) {
		for(String line : text.split("\n"))
			System.out.println(line);
	}


	@Override
	public void mouseReleased(MouseEvent event) {
		if(marker.isVisible()) {
			marker.setVisible(false);
			new Thread(() -> {
				try {
					Thread.sleep(10L);
					this.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					Thread.sleep(1L);
					this.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
				} catch(InterruptedException e) { e.printStackTrace(); }
				marker.setVisible(true);
			}).start();
		}
	}

	public int difference(int a, int b) {
		if(a > b) return a - b;
		else return b - a;
	}

	@Override
	public void nativeKeyTyped(NativeKeyEvent event) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	@Override
	public void mousePressed(MouseEvent e) {}
	@Override
	public void mouseEntered(MouseEvent e) {}
	@Override
	public void mouseExited(MouseEvent e) {}
	
}

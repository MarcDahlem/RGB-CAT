package com.example.dahlem.RGBAdjustment;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class RGBAdjustment {

	private volatile Display display;
	private volatile int r;
	private volatile int g;
	private volatile int b;
	private KeyListener listener_keypress;
	private volatile Image bgImmage;
	private volatile Label rgb_label;
	private volatile Timer timer;
	private final static Logger LOGGER = Logger.getLogger(RGBAdjustment.class
			.getName());

	/**
	 * Maximum/slowest for the changing rate in ms
	 */
	private static final int CHANGING_RATE_MAX = 5000;

	/**
	 * Minimum/fastest for the changing rate in ms
	 */
	private static final int CHANGING_RATE_MIN = 1;

	/**
	 * Maximum for the color update interval
	 */
	private static final int INTERVAL_MAX = 255;

	/**
	 * Minimum for the color update interval
	 */
	private static final int INTERVAL_MIN = 1;
	
	/**
	 * delay until the info/help window is closed in ms
	 */
	private static final int CLOSE_INFO_DELAY = 5000;

	private volatile int changingRate = 500;
	private volatile int interval = 25;

	/**
	 * last time in system millis when the auto update timer was fired. Used to
	 * calculate remaining timer time
	 */
	private volatile long lastAutoUpdate;
	private Runnable color_updater;
	private volatile Label updateRateLabel;
	private volatile Label intervalLabel;
	private Label helpLabel;
	private Timer helpTimer;
	private Runnable hideHelpRunnable;
	
	private static String helpText = "'f':\t\t\t\t\t\t\t\tToggle Fullscreen\n"
			+ "'x','q' or 'ESC':\t\t\t\tClose program\n"
			+ "'Space' or 'Enter':\t\t\t\tPause/Resume auto increment colors\n"
			+ "'-' / '+':\t\t\t\t\t\t\tDecrease/Increase Color update frequency for the auto increment. Value is given in ms\n"
			+ "'Down-Arrow' / 'Up-Arrow':\tDecrease/Increase the interval for color changes\n"
			+ "'Left-Arrow' / 'Right-Arrow':\tManually decrease/increase color\n"
			+ "'r':\t\t\t\t\t\t\t\tSet color to constant 'red' <-> RGB(255,0,0)\n"
			+ "'g':\t\t\t\t\t\t\t\tSet color to constant 'green' <-> RGB(0,255,0)\n"
			+ "'b':\t\t\t\t\t\t\t\tSet color to constant 'blue' <-> RGB(0,0,255)\n"
			+ "'i','h' or 'F1':\t\t\t\t\tShow this help";

	/**
	 * The constructur of the Gui initialises listeners and default colors The
	 * GUI is not configured after the constructor finishes. Use the method
	 * {@link #init()} to initialize the gui. And {@link #run()} after that to
	 * run the gui, until it will be disposed.
	 */
	public RGBAdjustment() {
		// create default color
		this.r = 0;
		this.g = 0;
		this.b = 0;

		this.color_updater = new Runnable() {

			@Override
			public void run() {
				increaseColor();
				RGBAdjustment.this.refreshBackground(display.getActiveShell(),
						r, g, b);
				RGBAdjustment.this.lastAutoUpdate = System.currentTimeMillis();
			}
		};
		
		this.hideHelpRunnable = new Runnable() {

			@Override
			public void run() {
				if (!RGBAdjustment.this.helpLabel.isDisposed()) {
					RGBAdjustment.this.helpLabel.setVisible(false);
				}
			}
			
		};

		// create the listener to react on key presses
		this.listener_keypress = new KeyListener() {

			@Override
			public void keyReleased(KeyEvent e) {
				// nothing to do
			}

			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.keyCode) {
				case 'f':
					RGBAdjustment.this.changeFullscreen();

					break;
				case 'x':
					// x pressed, shutdown program
				case 'q':
					// q pressed, shutdown program
				case SWT.ESC:
					// Escape pressed, shutdown programm
					RGBAdjustment.this.closeProgram();
					break;
				case ' ':
					// space pressed, stop/restart auto incrementing
				case SWT.CR:
					// enter pressed, stop/restart auto incrementing
					if (timer != null) {
						RGBAdjustment.this.stopTimer();
					} else {
						RGBAdjustment.this.startTimer();
					}

					break;
				case SWT.KEYPAD_SUBTRACT:
				case '-':
					// minus pressed.decrease changingrate
					RGBAdjustment.this.changeUpdateRate(false);
					break;
				case SWT.KEYPAD_ADD:
				case '+':
					// plus pressed. Increase changingrate
					RGBAdjustment.this.changeUpdateRate(true);
					break;
				case SWT.ARROW_DOWN:
					// Arrow down decreases update interval
					RGBAdjustment.this.changeInterval(false);
					break;
				case SWT.ARROW_UP:
					// Arrow UP will increase update interval
					RGBAdjustment.this.changeInterval(true);
					break;
				case SWT.ARROW_LEFT:
					// decrease color one step.
					RGBAdjustment.this.stopTimer();
					RGBAdjustment.this.decreaseColor();
					RGBAdjustment.this.refreshBackground(
							display.getActiveShell(), r, g, b);
					break;
				case SWT.ARROW_RIGHT:
					// increase color one step.
					RGBAdjustment.this.stopTimer();
					RGBAdjustment.this.increaseColor();
					RGBAdjustment.this.refreshBackground(
							display.getActiveShell(), r, g, b);
					break;
				case 'r':
					// set color to red
					RGBAdjustment.this.stopTimer();
					RGBAdjustment.this.r = 255;
					RGBAdjustment.this.g = 0;
					RGBAdjustment.this.b = 0;
					RGBAdjustment.this.refreshBackground(
							display.getActiveShell(), r, g, b);
					break;
				case 'g':
					// set color to green
					RGBAdjustment.this.stopTimer();
					RGBAdjustment.this.r = 0;
					RGBAdjustment.this.g = 255;
					RGBAdjustment.this.b = 0;
					RGBAdjustment.this.refreshBackground(
							display.getActiveShell(), r, g, b);
					break;
				case 'b':
					// set color to blue;
					RGBAdjustment.this.stopTimer();
					RGBAdjustment.this.r = 0;
					RGBAdjustment.this.g = 0;
					RGBAdjustment.this.b = 255;
					RGBAdjustment.this.refreshBackground(
							display.getActiveShell(), r, g, b);
					break;

				case 'i':
					// show help on i
				case 'h':
					// show help on h
				case SWT.F1:
					// show help on F1
					RGBAdjustment.this.showHelp();
					break;
				default:
					// do nothing, unimplmeneted key pressed
				}

			}
		};
	}

	private void showHelp() {
		this.helpLabel.setVisible(true);
		if (this.helpTimer!=null) {
			this.helpTimer.cancel();
			this.helpTimer = null;
		}
		
		this.helpTimer=new Timer();
		this.helpTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				display.asyncExec(hideHelpRunnable);
			}
		}, CLOSE_INFO_DELAY);

	}

	private void changeFullscreen() {
		// change fullscreen mode
		Shell shell = RGBAdjustment.this.display.getActiveShell();
		shell.setFullScreen(!shell.getFullScreen());
		this.helpLabel.getParent().layout();
		this.rgb_label.getParent().layout();
		this.intervalLabel.getParent().layout();
		this.updateRateLabel.getParent().layout();

		LOGGER.info("Fullscreen mode changed to "
				+ (shell.getFullScreen() ? "on" : "off"));
	}

	private void changeInterval(boolean increase) {
		// increase or dercrease the update interval
		if (increase) {
			// increase
			if (this.interval < INTERVAL_MAX) {
				this.interval++;
			}
		} else {
			// decrease
			if (this.interval > INTERVAL_MIN) {
				this.interval--;
			}
		}

		LOGGER.info("Color update interval changed to " + interval);

		// update interval label
		this.intervalLabel.setText("Color update interval: " + interval);
		// force resize
		this.intervalLabel.pack();
		//bugfix: hidden label (right) did overlap and was not resized correctly
		this.intervalLabel.getParent().getParent().layout();
	}

	private void changeUpdateRate(boolean increase) {

		// increase or dercrease the update rate
		if (increase) {
			// increase
			if (this.changingRate < CHANGING_RATE_MAX) {
				this.changingRate++;
			}
		} else {
			// decrease
			if (this.changingRate > CHANGING_RATE_MIN) {
				this.changingRate--;
			}
		}

		LOGGER.info("Update rate changed to " + changingRate + " ms");

		// update timer if there was one
		if (this.timer != null) {
			this.timer.cancel();
			long currentTime = System.currentTimeMillis();
			long timedifference = currentTime - this.lastAutoUpdate;
			// at maximum changingrate
			timedifference = timedifference > changingRate ? changingRate
					: timedifference;
			this.timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					display.asyncExec(color_updater);
				}
			}, timedifference, changingRate);
			LOGGER.info("Restarting auto increment with delay of "
					+ timedifference + " ms");
		}

		// update updaterate label
		this.updateRateLabel.setText("Update rate: " + changingRate + " ms");
		// force resize
		this.updateRateLabel.pack();

	}

	protected void startTimer() {
		if (this.timer == null) {
			this.timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {

				@Override
				public void run() {
					display.asyncExec(color_updater);

				}
			}, changingRate, changingRate);
			this.lastAutoUpdate = System.currentTimeMillis();
			LOGGER.info("Starting auto increment");
		}
	}

	/**
	 * This method creates the GUI
	 * 
	 * @param shell
	 *            the shell used as parent for the whole application / hidden
	 *            hidden hidden / help info hidden / hidden hidden hidden
	 */
	private void configureGUI(Shell shell) {
		// set the titel
		shell.setText("Ambilight RGB Color Adjustment");

		// add the key listener for controlling the program
		shell.addKeyListener(this.listener_keypress);

		// set to fullscreen
		shell.setFullScreen(true);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		// TODO gridLayout.makeColumnsEqualWidth = true;
		shell.setLayout(gridLayout);

		Composite help = new Composite(shell, SWT.NONE);
		Composite info = new Composite(shell, SWT.NONE);
		Composite hidden = new Composite(shell, SWT.NONE);

		configureHelpComposite(help);
		configureInfoComposite(info);
		configureHiddenComposite(hidden);

		// set starting background
		this.setBackground(shell);
	}

	private void configureHiddenComposite(Composite parent) {
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		// to center the rest add hidden labels left
		Label hidden_label = new Label(parent, SWT.CENTER);
		hidden_label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				true));
		hidden_label.setText(helpText);
		hidden_label.setVisible(false);
		hidden_label.moveBelow(this.intervalLabel);
	}

	private void configureInfoComposite(Composite parent) {
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, true));

		// to center the rest add hidden labels at bottom and top
		Label hidden_label = new Label(parent, SWT.CENTER);
		hidden_label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				true));
		hidden_label.setVisible(false);

		this.rgb_label = new Label(parent, SWT.CENTER);
		rgb_label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false));

		this.updateRateLabel = new Label(parent, SWT.CENTER);
		this.updateRateLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				false, false));
		this.updateRateLabel.setText("Update rate: " + changingRate + " ms");

		this.intervalLabel = new Label(parent, SWT.CENTER);
		this.intervalLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				false, false));
		this.intervalLabel.setText("Color update interval: " + interval);

		// to center the rest add hidden labels at bottom and top
		hidden_label = new Label(parent, SWT.CENTER);
		hidden_label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				true));
		hidden_label.setVisible(false);
	}

	private void configureHelpComposite(Composite parent) {
		//TODO just a link
		GridLayout gridLayout = new GridLayout();
		parent.setLayout(gridLayout);
		parent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		
		// to center the rest add hidden labels at bottom and top
		Label hidden_label = new Label(parent, SWT.CENTER);
		hidden_label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				true,2,1));
		hidden_label.setVisible(false);

	    this.helpLabel= new Label(parent, SWT.WRAP);
	    this.helpLabel.setText(helpText);
	    this.helpLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				false));
	    this.helpLabel.setVisible(false);

		// to center the rest add hidden labels at bottom and top
		hidden_label = new Label(parent, SWT.CENTER);
		hidden_label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true,
				true));
		hidden_label.setVisible(false);

	}

	/**
	 * This method will set the default background to all composites used in
	 * this gui
	 * 
	 * @param shell
	 *            the shell where the background should be set
	 */
	private void setBackground(final Shell shell) {
		// first set the inherit mode, so that the background is inherited from
		// all subparts of the shell
		shell.setBackgroundMode(SWT.INHERIT_DEFAULT);

		// add a listener to recompute the background if the window is resized
		shell.addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				RGBAdjustment.this.refreshBackground(shell,
						RGBAdjustment.this.r, RGBAdjustment.this.g,
						RGBAdjustment.this.b);
			}
		});
	}

	/**
	 * This method recalculates the background. Used when color is changed
	 * 
	 * @param shell
	 *            the shell used to refresh the background on
	 */
	private void refreshBackground(Shell shell, int r, int g, int b) {

		if (shell == null) {
			return;
		}

		// get the new size and create the new image
		Rectangle rect = shell.getClientArea();
		Image newImage = new Image(this.display, 1, Math.max(1, rect.height));

		// create a new flowding image
		GC gc = new GC(newImage);

		// with the sizes given in the new size of the shell.
		gc.setBackground(new Color(display, r, g, b));
		gc.fillRectangle(rect);
		// like shown in the API has this image to be disposed after creation
		gc.dispose();

		// set the new background image
		shell.setBackgroundImage(newImage);

		// dispose the old image if existent and set it to the new image
		if (bgImmage != null) {
			bgImmage.dispose();
		}
		bgImmage = newImage;

		// finally set the new color to the label
		this.rgb_label.setText("RGB(" + r + "," + g + "," + b + ")");
		LOGGER.info("New color: " + r + "," + g + "," + b);

		// update label colors
		Color color;
		Display current_display = shell.getDisplay();
		if (r > 150 && g > 150 && b > 150) {
			color = current_display.getSystemColor(SWT.COLOR_BLACK);
		} else {
			color = current_display.getSystemColor(SWT.COLOR_WHITE);
		}

		this.updateRateLabel.setForeground(color);
		this.rgb_label.setForeground(color);
		this.intervalLabel.setForeground(color);
		this.helpLabel.setForeground(color);

		// force to recalculate its sizes
		this.rgb_label.pack();
		this.updateRateLabel.pack();
		this.intervalLabel.pack();
		// TODO this.helpLabel.pack();
	}

	/**
	 * Runs the program, until it is closed by a user. This method will first
	 * return, when the window is disposed and will fail, if the GUI was not
	 * initialized by using the method {@link #init()}.
	 */
	public void run() {

		// get the shell and open it
		Shell shell = this.display.getShells()[0];
		shell.open();

		// starting auto incrementing colors;
		startTimer();

		// run as long as it isnt disposed
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}

		this.stopTimer();
		if (this.helpTimer != null) {
			this.helpTimer.cancel();
			this.helpTimer = null;
		}
		// diposed command received: free the background image and close the
		// display
		if (bgImmage != null) {
			bgImmage.dispose();
		}
		this.display.dispose();
		LOGGER.info("Program closed!");
	}

	private void stopTimer() {
		if (this.timer != null) {
			timer.cancel();
			this.timer = null;
			timer = null;
			LOGGER.info("Stopping auto increment");
		}
	}

	/**
	 * Initializes all the GUI parts and has to be called before using the
	 * {@link #run()} method. Will also set the {@link #display} variable in
	 * this gui.
	 */
	public void init() {
		// create the display and shell
		Display display = new Display();
		Shell shell = new Shell(display);
		// create all the subparts of the shell
		this.configureGUI(shell);

		// finally set display variable
		this.display = display;
	}

	private void closeProgram() {
		this.stopTimer();
		RGBAdjustment.this.display.getActiveShell().dispose();
		LOGGER.info("Closing program...");
	}

	private void increaseColor() {
		if (RGBAdjustment.this.b < 255) {
			RGBAdjustment.this.b += interval;
			RGBAdjustment.this.b = RGBAdjustment.this.b > 255 ? 255
					: RGBAdjustment.this.b;
		} else {
			if (RGBAdjustment.this.g < 255) {
				RGBAdjustment.this.g += interval;
				RGBAdjustment.this.g = RGBAdjustment.this.g > 255 ? 255
						: RGBAdjustment.this.g;
				RGBAdjustment.this.b = 0;
			} else {
				if (RGBAdjustment.this.r < 255) {
					RGBAdjustment.this.r += interval;
					RGBAdjustment.this.r = RGBAdjustment.this.r > 255 ? 255
							: RGBAdjustment.this.r;
					RGBAdjustment.this.g = 0;
				} else {
					RGBAdjustment.this.r = 0;
					RGBAdjustment.this.g = 0;
					RGBAdjustment.this.b = 0;
				}
			}
		}
	}

	private void decreaseColor() {
		if (RGBAdjustment.this.b > 0) {
			RGBAdjustment.this.b -= interval;
			RGBAdjustment.this.b = RGBAdjustment.this.b < 0 ? 0
					: RGBAdjustment.this.b;
		} else {
			if (RGBAdjustment.this.g > 0) {
				RGBAdjustment.this.g -= interval;
				RGBAdjustment.this.g = RGBAdjustment.this.g < 0 ? 0
						: RGBAdjustment.this.g;
				RGBAdjustment.this.b = 255;
			} else {
				if (RGBAdjustment.this.r > 0) {
					RGBAdjustment.this.r -= interval;
					RGBAdjustment.this.r = RGBAdjustment.this.r < 0 ? 0
							: RGBAdjustment.this.r;
					RGBAdjustment.this.g = 255;
				} else {
					RGBAdjustment.this.r = 255;
					RGBAdjustment.this.g = 255;
					RGBAdjustment.this.b = 255;
				}
			}
		}
	}

	public static void main(String[] args) {
		// create and init a gui
		RGBAdjustment gui = new RGBAdjustment();
		gui.init();
		// run the gui
		gui.run();
	}
}

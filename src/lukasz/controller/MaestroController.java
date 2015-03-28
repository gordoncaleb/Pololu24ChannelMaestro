package lukasz.controller;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Main class to call while controlling Pololu Maestro. Works with Pololu
 * Maestro 12, 18 and 24. Its probably not compatible with Maestro 6. Created by
 * skokys@gmail.com 16.5.2014.
 * 
 * Edited on 28.3.2015 for 'Maestro Controller 24 channel'
 * 
 * by (L) [Lukasz Marczak]. I'm not 'Legia' fan.
 * 
 * more details about Pololu protocol: https://www.pololu.com/docs/0J40/5.e
 * 
 */
public class MaestroController {
	private static final int DEFAULT_SPEED = 40;
	private static final int DEFAULT_ACCELERATION = 20; // max is 255
	private final boolean verbose; // if true: messages in console are shown
	private int POS_MID = 1500;
	private int POS_MAX = 2100; // in fact: 2000
	private int POS_MIN = 800; // in fact: 900

	public static class Command {
		public static final byte SET_SPEED = (byte) 0x87;
		public static final byte SET_ACCELERATION = (byte) 0x89;
		public static final byte GET_POSITION = (byte) 0x90;
		public static final byte SET_POSITION = (byte) 0x84;
		public static final byte SET_PWM = (byte) 0x8A;
		public static final byte GO_HOME = (byte) 0xA2;
		public static final byte GET_ERRORS = (byte) 0xA1;
		public static final byte GET_MOVING_STATE = (byte) 0x93;
		public static final String[] ERROR_MESSAGES = {
				"\nSerial Signal Error (0)", "\nSerial Overrun Error (1)",
				"\nSerial RX buffer full (2)", "\nSerial CRC error (3)",
				"\nSerial protocol error (4)", "\nSerial timeout error (5)",
				"\nScript stack error (6)", "\nScript call stack error (7)",
				"\nScript program counter error (8)", };// detailed description
														// at:
														// https://www.pololu.com/docs/0J40/5.e
	}

	private final PololuSerialPort port;

	/**
	 * Pololu Maestro controller sets servo positions. The "USB Dual port" must
	 * be set in Pololu control center to work
	 * 
	 * @param portS
	 *            port name like COM23 or /dev/acm0 on linux. Use maestro's
	 *            controller port
	 * 
	 *            (L): on Windows 8: COM5 or COM4 (check at Control Panel)
	 * @param verbose
	 *            log to system out or not
	 * @throws Exception
	 */
	public MaestroController(String portS, boolean verbose) throws Exception {
		this.verbose = verbose;
		port = new PololuSerialPort(portS);
		if (verbose)
			System.out.println("Port created:" + port);
		init();
	}

	/**
	 * (L) sets all channels up for a 100 ms
	 * 
	 * @throws InterruptedException
	 *             - needed. We using Thread.sleep()
	 */
	private void init() throws InterruptedException {

//		for (int i = 0; i < 24; i++) {
//			setSpeed(i, DEFAULT_SPEED);
//			setAcceleration(i, 20);
//		}
//		Thread.sleep(100);
//		goHome();

		if (verbose)
			System.out.println("Init done");
	}

	/**
	 * @param servo
	 *            : range: 0-23
	 * @param acc
	 *            : range: 0-255
	 */
	private void setAcceleration(int servo, int acc) {
		try {
			port.out.write(new byte[] { Command.SET_ACCELERATION, (byte) servo,
					(byte) acc, 0 });
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param servo
	 *            : range: 0-23
	 * @param speed
	 *            : range 0-255
	 */
	private void setSpeed(int servo, int speed) {
		try {
			port.out.write(new byte[] { Command.SET_SPEED, (byte) servo,
					(byte) speed, 0 });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			port.out.close();
			port.in.close();// (L) added this stuff
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param channel
	 *            : [0; 23]
	 * @param angle
	 *            : [-90; 90] degrees
	 */
	public void setTargetAngle(int channel, int angle) {
		setPosition(channel, POS_MID + 500 * angle / 90);
	}

	public void setPosition(int servo, int pos, int speed, int acc) {
		if (verbose)
			System.out.println("Setting position " + servo + "/" + pos + "/"
					+ speed + "/" + acc);
		setSpeed(servo, speed);
		setAcceleration(servo, acc);
		setPositionOnly(servo, pos);
	}

	/**
	 * sets servo position from 900-2000 - these are default limits and may be
	 * changed in the Pololu Maestro Control center
	 * 
	 * @param servo
	 * @param pos
	 */
	public void setPosition(int servo, int pos) {
		if (verbose)
			System.out.println("Setting position " + servo + "/" + pos);
		setSpeed(servo, DEFAULT_SPEED);
		setAcceleration(servo, DEFAULT_ACCELERATION);
		setPositionOnly(servo, pos);
	}

	private void setPositionOnly(int servo, int pos) {
		if (verbose)
			logPosition(servo);
		int p = pos * 8;
		if (pos < POS_MIN || pos > POS_MAX)
			throw new IllegalStateException("Wrong position " + servo + "/" + p);

		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(p);
		byte low = (byte) (bb.array()[3] & 0x7f);
		byte high = (byte) (bb.array()[2] & 0x7f);

		byte[] bbb = new byte[] { Command.SET_POSITION, (byte) servo, low, high };
		try {
			port.out.flush();
			port.out.write(bbb);
			port.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO:
	private void setPWM() {
		byte[] bbb = new byte[] { Command.SET_PWM,
		// on_time_low_bits, on_time_high_bits,
		// period_low_bits, period_high_bits
		};

		try {
			port.out.flush();
			port.out.write(bbb);
			port.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * gets position of servo
	 * 
	 * @param servo
	 */
	private void logPosition(int servo) {
		byte[] bbb = new byte[] { Command.GET_POSITION, (byte) servo };
		try {
			port.out.flush();
			port.out.write(bbb);
			port.out.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized String getErrors() {
		if (verbose)
			System.out.println("\nUpcoming errors: ");
		byte[] bbb = new byte[] { Command.GET_ERRORS };
		byte[] error_byte = new byte[] { 0 };
		String errors = "";
		try {
			port.out.flush();
			port.out.write(bbb);
			port.out.flush();
			error_byte[0] = (byte) port.in.read();

		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < 8; i++) {
			if ((error_byte[0] >> i) == 1) {
				errors += Command.ERROR_MESSAGES[i];
			}
		}
		return errors;
	}

	public synchronized boolean areServosMoving() {
		if (verbose)
			System.out.println("\nUpcoming errors: ");
		byte[] bbb = new byte[] { Command.GET_MOVING_STATE };
		try {
			port.out.flush();
			port.out.write(bbb);
			port.out.flush();
			return (port.in.read() == 1) ? true : false;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Disables all channels (no voltages are sent to all channels)
	 */
	public void goHome() {
		try {
			port.out.write(Command.GO_HOME);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeCommand(byte... command) {
		try {
			port.out.write(command);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
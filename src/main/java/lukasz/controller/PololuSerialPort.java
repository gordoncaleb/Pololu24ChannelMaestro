package lukasz.controller;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Optional;

/**
 * Created by skokys@gmail.com on 16.5.2014.
 * <p>
 * edited by (L) on 28.3.2015
 */
public class PololuSerialPort {

    public InputStream in;
    public OutputStream out;
    ByteBuffer inputBuffer = ByteBuffer.allocate(1024);

    public PololuSerialPort(String port) throws Exception {
        if (out != null || in != null) {
            in.close();
            out.close();
        }
        connect(port);
        if (in == null || out == null)
            throw new IllegalStateException("Not connected to port " + port);
    }

    @Override
    public String toString() {
        return " Prototype PololuPort";
    }

    // unused
    // private void setAcceleration(int servo, int acc) {
    // try {
    // out.write(new byte[] { MaestroController.Command.SET_ACCELERATION,
    // (byte) servo, (byte) acc, 0 });
    // out.flush();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    //
    // }

    private void connect(String portName) throws Exception {
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
        CommPortIdentifier port = (CommPortIdentifier) e.nextElement();
        System.out.println("Port:" + port == null ? "Error" : port.getName());

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
            throw new IllegalStateException("COM port in use");
        } else {
            int timeout = 2000;
            System.out.println("Opening port");
            CommPort commPort = portIdentifier.open(this.getClass().getName(),
                    timeout);

            if (commPort instanceof gnu.io.SerialPort) {
                gnu.io.SerialPort serialPort = (gnu.io.SerialPort) commPort;
                serialPort.setSerialPortParams(9600,
                        gnu.io.SerialPort.DATABITS_8,
                        gnu.io.SerialPort.STOPBITS_1,
                        gnu.io.SerialPort.PARITY_NONE);

                in = serialPort.getInputStream();
                (new Thread(new SerialReader(in))).start();
                out = serialPort.getOutputStream();
            } else {
                throw new IllegalArgumentException("Invalid use");
            }
        }
        // indicates baud rate(at starting communication)
        try {
//			out.write(new byte[] { (byte) 0xAA });
            out.write((byte) 0xAA);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public class SerialReader implements Runnable {

        InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void run() {
            Thread.currentThread().setName("Reader");
            System.out.println("Starting reader thread ["
                    + Thread.currentThread().getName() + "]");
            // BufferedReader br = new BufferedReader(new
            // InputStreamReader(in));

            while (true) {
                try {
                    // String line = br.readLine();
                    while (true) {
                        int r = in.read();
                        if (r != -1) {
                            System.out.println(">" + r);
                            inputBuffer.put((byte) r);
                        }
                    }
                    // System.out.println(">" + line);
                } catch (IOException e) {
                    System.out.println("E:" + e.getMessage());
                }
            }
        }
    }
}
package lukasz.controller;

import java.util.Random;

public class Test {
    static boolean isAlive = false;
    public static final int SEKUNDY = 5;
    static Random rand = new Random();

    public static void main(String[] args) {

        String comRef = args.length > 0 ? args[0] : "COM5";

        try {
            MaestroController controller = new MaestroController(comRef, true);
            isAlive = true;

            Thread.sleep(1000);
            System.out.println("Let's play!");

            int channel = 0;
            while (isAlive) {
                blink(controller, channel);
            }

            controller.goHome();
            controller.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void blink(MaestroController controller, int channel)
            throws InterruptedException {
        controller.setPosition(channel, 600);
        Thread.sleep(100);
        controller.setPosition(channel, 2000);
        long milis = (long) (100 + rand.nextFloat() * 1000 * SEKUNDY);
        Thread.sleep(milis);
        System.out.println("\nMrugam: " + milis / 1000 + " s\n");

    }
}

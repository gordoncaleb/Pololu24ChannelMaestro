package lukasz.controller;

public class Test {
	static boolean isAlive = false;

	public static void main(String[] args) {
		MaestroController controller = null;
		
		try {
			controller = new MaestroController("COM5", true);
			isAlive = true;
			Thread.sleep(1000);
			System.out.println("Let's play!");
			while (isAlive) {
				playSequence(controller);
			}
			
			controller.goHome();
			controller.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void playSequence(MaestroController controller)
			throws InterruptedException {
		
		for (int i = 0; i < 50; i++) {
			controller.setPosition(0, 2000);
			Thread.sleep(500);
			controller.goHome();
			Thread.sleep(500);
		}
		
		isAlive = false;
	}
}

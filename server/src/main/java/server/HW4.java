package server;

public class HW4 {
    static Object monitor = new Object();
    static int current = 1;
    static int number = 5;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                for (int i = 0; i < number; i++) {
                    synchronized (monitor) {
                        while (current != 1) {
                            monitor.wait();
                        }
                        System.out.print("A");
                        current = 2;
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < number; i++) {
                    synchronized (monitor) {
                        while (current != 2) {
                            monitor.wait();
                        }
                        System.out.print("B");
                        current = 3;
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < number; i++) {
                    synchronized (monitor) {
                        while (current != 3) {
                            monitor.wait();
                        }
                        System.out.print("C");
                        System.out.println("");
                        current = 1;
                        monitor.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

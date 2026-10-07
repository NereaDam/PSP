package org.example.CicloDeVida;

public class CocineroTemporizadorCancelable implements Runnable{

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println("cocinando " + i + " segundos ");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Coccion cancelada");

            }
        }
    }


     void main(String[] args) throws InterruptedException {

        Thread threadCocinero = new Thread(new CocineroTemporizadorCancelable());
         System.out.println("Estado antes de start: " + threadCocinero.getState());
        threadCocinero.start();
        Thread.sleep(3000);
         System.out.println(" Interrumpiendo coccion");
         threadCocinero.interrupt();
         threadCocinero.join();
         System.out.println("Estado final: " + threadCocinero.getState());


    }




}

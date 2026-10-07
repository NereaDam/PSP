package org.example.EjerciciosDescargaArchivos;

public class DescargaArchivos  extends Thread{
    @Override
    public void run() {
        for (int i = 0; i <10 ; i++) {
            System.out.println(getName() + " : " + i);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public void main (String[] args){
        Thread thread1 = new Thread();
        Thread thread2 = new Thread();

        thread1.start();
        thread2.start();
    }



}

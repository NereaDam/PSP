package org.example.EjerciciosDescargaArchivos;

public class DescargaArchivosRunnable implements Runnable{

    @Override
    public void run() {

        for (int i = 0; i < 10; i++) {

            System.out.println(Thread.currentThread().getName() + " : " + i);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

    }

    void main (String[] args){

        Thread thread1 = new Thread(new DescargaArchivosRunnable());
        Thread thread2 = new Thread(new DescargaArchivosRunnable());
        Thread thread3 = new Thread(new DescargaArchivosRunnable());
        Thread thread4 = new Thread(new DescargaArchivosRunnable());

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

    }



}

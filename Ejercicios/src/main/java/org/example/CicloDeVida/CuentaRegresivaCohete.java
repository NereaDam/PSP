package org.example.CicloDeVida;



public class CuentaRegresivaCohete implements Runnable{
    /*Un hilo hace una cuenta regresiva de 10 a 0, durmiendo 1 segundo
    entre números. El hilo principal espera a que termine con join()
    y después imprime "¡Despegue!".*/

    @Override
    public void run() {
        for (int i = 10; i >=0 ; i++) {

            System.out.println(i + " ");

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void main(String[] args) throws InterruptedException {

        Thread threadCohete = new Thread( new CuentaRegresivaCohete());

        threadCohete.start();
        threadCohete.join();
        System.out.println(" ¡Despegue! ");
    }

}

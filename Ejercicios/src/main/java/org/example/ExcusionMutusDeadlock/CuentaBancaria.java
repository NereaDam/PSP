package org.example.ExcusionMutusDeadlock;

public class CuentaBancaria {
/*Crear una CuentaBancaria con un saldo y un metodo depositar(int cantidad)
 que haga saldo += cantidad. Lanzar 100 hilos que depositen 1 euro cada uno
  y comprobar que el resultado final puede no ser 100*/

    private int saldo = 0;

    /*public void depositar(int cantidad) {
         //synchronized (this) {
         //es otra forma de "proteger" el metodo
        saldo += cantidad;
        //Esto hace condicion de carrera, sin synchronized
    }*/

    public synchronized void depositar(int cantidad) {
        saldo += cantidad;
    }

    public int getSaldo() {
        return saldo;
    }

     void main( String[] args) throws InterruptedException {

        CuentaBancaria cuentaBancaria = new CuentaBancaria();

        Thread[] hilos = new Thread[100];

         for (int i = 0; i < 100; i++) {

             hilos[i] = new Thread(() -> {
                cuentaBancaria.depositar(1);

             });

             hilos[i].start();

         }
         for (Thread hilo : hilos) {
             hilo.join();
         }
         System.out.println("Saldo final: " + cuentaBancaria.getSaldo());

    }


}

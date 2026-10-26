package org.example.ExcusionMutusDeadlock;

public class CuentaBancaria2 {

    private final int id;
    private int saldo;

    public CuentaBancaria2(int id, int saldo) {

        this.id = id;
        this.saldo = saldo;
    }

    public int getId() {
        return id;
    }

    public void transferir(CuentaBancaria2 destino, int cantidad) {

        synchronized (this) {

            synchronized (destino) {

                saldo -= cantidad;
                destino.saldo += cantidad;
            }
        }
    }

    public synchronized int getSaldo() {
        return saldo;
    }

    public static void main(String[] args) throws InterruptedException {

        CuentaBancaria2 cuentaA = new CuentaBancaria2(1, 100);
        CuentaBancaria2 cuentaB = new CuentaBancaria2(2, 100);

        Thread hilo1 = new Thread(() -> {
            cuentaA.transferir(cuentaB, 10);
        });

        Thread hilo2 = new Thread(() -> {
            cuentaB.transferir(cuentaA, 10);
        });

        hilo1.start();
        hilo2.start();

        hilo1.join();
        hilo2.join();

        System.out.println("Saldo A: " + cuentaA.getSaldo());
        System.out.println("Saldo B: " + cuentaB.getSaldo());
    }




}

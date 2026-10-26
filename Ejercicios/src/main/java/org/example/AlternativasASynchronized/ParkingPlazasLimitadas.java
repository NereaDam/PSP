package org.example.AlternativasASynchronized;

import java.util.concurrent.Semaphore;

public class ParkingPlazasLimitadas {

    private final Semaphore plazas = new Semaphore(3);

    void entrar(int id) throws InterruptedException {

        if (plazas.availablePermits() == 0) { /*availablePermits() solo nos sirve aquí para mostrar el mensaje*/
            System.out.println("Coche " + id + " espera porque no hay plazas.");
        }

        plazas.acquire();

        System.out.println("Coche " + id + " ha entrado al parking.");

        Thread.sleep((int) (Math.random() * 3000) + 1000);

        System.out.println("Coche " + id + " ha salido del parking.");

        plazas.release();
    }


    void main(String[] args) throws InterruptedException {

        ParkingPlazasLimitadas parking = new ParkingPlazasLimitadas();

        Thread[] coches = new Thread[10];

        for (int i = 0; i < 10; i++) {

            int id = i + 1;

            coches[i] = new Thread(() -> {
                try {
                    parking.entrar(id);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });

            coches[i].start();
        }

        for (Thread coche : coches) {
            coche.join();
        }

        System.out.println("Todos los coches han terminado.");
    }
}



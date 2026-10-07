package org.example.AlternativasASynchronized;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class RecursoCompartido {


    private final ReentrantLock lock = new ReentrantLock();

    public void usar(int id) {

        while (true) {

            boolean conseguido = false;

            try {

                conseguido =
                        lock.tryLock(500, TimeUnit.MILLISECONDS);

                if (conseguido) {

                    try {

                        System.out.println(
                                "Hilo " + id +
                                        " ha conseguido el recurso."
                        );

                        Thread.sleep(2000);

                        System.out.println(
                                "Hilo " + id +
                                        " ha terminado."
                        );

                        return;

                    } finally {

                        lock.unlock();
                    }

                } else {

                    System.out.println(
                            "Hilo " + id +
                                    ": Ocupado, lo intento más tarde"
                    );
                }

            } catch (InterruptedException e) {

                Thread.currentThread().interrupt();
                return;
            }
        }
    }


    void main(String[] args) throws InterruptedException {

        RecursoCompartido recurso = new RecursoCompartido();

        Thread[] hilos = new Thread[5];

        for (int i = 0; i < 5; i++) {

            int id = i + 1;

            hilos[i] = new Thread(() -> {
                recurso.usar(id);
            });

            hilos[i].start();
        }

        for (Thread hilo : hilos) {
            hilo.join();
        }

        System.out.println("Todos los hilos han terminado.");
    }


}

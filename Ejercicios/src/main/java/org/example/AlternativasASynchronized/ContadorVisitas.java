package org.example.AlternativasASynchronized;

import java.util.concurrent.atomic.AtomicInteger;

public class ContadorVisitas {


    private final AtomicInteger contador = new AtomicInteger(0);

    public void incrementar() {
        contador.incrementAndGet();
    }

    public int getContador() {
        return contador.get();
    }


    void main(String[] args) throws InterruptedException {

        ContadorVisitas contador = new ContadorVisitas();

        Thread[] hilos = new Thread[1000];

        for (int i = 0; i < 1000; i++) {

            hilos[i] = new Thread(() -> {
                contador.incrementar();
            });

            hilos[i].start();
        }

        for (Thread hilo : hilos) {
            hilo.join();
        }

        System.out.println(
                "Número de visitas: " + contador.getContador()
        );
    }
}





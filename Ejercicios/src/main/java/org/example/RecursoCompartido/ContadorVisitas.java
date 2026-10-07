package org.example.RecursoCompartido;

import java.util.Random;

public class ContadorVisitas {

    private int visitas = 0;

    public void incrementarVisita() {
        visitas++;
    }

    public int getVisitas() {
        return visitas;
    }

    void main(String[] args) throws InterruptedException {

        System.out.println("=== CONTADOR DE VISITAS WEB ===");
        System.out.println("Esperando 1000 visitantes...");

        ContadorVisitas contador = new ContadorVisitas();

        Thread[] hilos = new Thread[1000];

        Random random = new Random();

        long inicio = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {

            hilos[i] = new Thread(() -> {

                try {
                    int espera = 50 + random.nextInt(101);
                    Thread.sleep(espera);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                contador.incrementarVisita();
            });

            hilos[i].start();
        }

        for (Thread hilo : hilos) {
            hilo.join();
        }

        long fin = System.currentTimeMillis();

        System.out.println();
        System.out.println("--- SIN SINCRONIZACIÓN ---");
        System.out.println("Visitas esperadas: 1000");
        System.out.println("Visitas contadas: " + contador.getVisitas());
        System.out.println("Tiempo: " + (fin - inicio) + "ms");
    }




}

package es.nerea;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public class Main {


    // =========================================================
    // TIPOS DE PLATO Y PEDIDOS
    // =========================================================

    enum TipoPlato {
        ENSALADA(2000, 800),
        PASTA(3000, 1200),
        PIZZA(4000, 1500),
        CARNE(5000, 2000);

        final int tiempoMs;
        final int precioCentimos;

        TipoPlato(int tiempoMs, int precioCentimos) {
            this.tiempoMs = tiempoMs;
            this.precioCentimos = precioCentimos;
        }

        static TipoPlato aleatorio(Random r) {
            return values()[r.nextInt(values().length)];
        }

        double precioEuros() {
            return precioCentimos / 100.0;
        }
    }

    // Cada pedido conserva los datos necesarios para coordinar
    // al cliente, al camarero y al cocinero.

    static class Pedido {

        final int cliente;
        final TipoPlato plato;
        final int numeroMesa;
        final long horaCreacionMs;

        final boolean cierre;

        final CountDownLatch platoPreparado = new CountDownLatch(1);
        final CountDownLatch platoServido = new CountDownLatch(1);

        Pedido(int cliente, TipoPlato plato, int numeroMesa,
               long horaCreacionMs, boolean cierre) {

            this.cliente = cliente;
            this.plato = plato;
            this.numeroMesa = numeroMesa;
            this.horaCreacionMs = horaCreacionMs;
            this.cierre = cierre;
        }

        static Pedido pedidoDeCierre() {
            return new Pedido(-1, null, -1, 0, true);
        }
    }


    // =========================================================
    //  CONSTANTES
    // =========================================================

    static final int COCINEROS = 3;
    static final int CAMAREROS = 5;
    static final int CLIENTES = 100;

    static final int CAPACIDAD_MESA = 10;
    static final int NUM_MESAS = 5;

    static final long LLEGADA_CLIENTE_MS = 500;
    static final long TIEMPO_CAMARERO_MS = 1000;

    static final long TIEMPO_COMIENDO_MIN_MS = 1000;
    static final long TIEMPO_COMIENDO_MAX_MS = 3000;


    // =========================================================
    // RECURSOS COMPARTIDOS
    // =========================================================

    // Cola de pedidos pendientes de los camareros.
    static final BlockingQueue<Pedido> colaClientes =
            new ArrayBlockingQueue<>(CLIENTES);

    // Cola de pedidos que esperan ser preparados por los cocineros.
    static final BlockingQueue<Pedido> mesa =
            new ArrayBlockingQueue<>(CAPACIDAD_MESA);

    // Semáforo para limitar el restaurante a cinco mesas ocupadas.
    // El modo justo favorece a los clientes que llevan más tiempo esperando.
    static final Semaphore mesasDisponibles =
            new Semaphore(NUM_MESAS, true);

    // Cola con los números de las mesas libres.
    static final BlockingQueue<Integer> mesasLibres =
            new ArrayBlockingQueue<>(NUM_MESAS);

    // Esperar a que todos los clientes terminen.
    static final CountDownLatch clientesAtendidos =
            new CountDownLatch(CLIENTES);

    static final AtomicInteger clientesAtendidosTotal =
            new AtomicInteger();

    static final AtomicInteger platosServidos =
            new AtomicInteger();

    static final AtomicInteger mesaLlenaCount =
            new AtomicInteger();

    static final AtomicInteger clientesEnCola =
            new AtomicInteger();

    // Tiempos acumulados en milisegundos.
    static final AtomicLong esperaTotalMs =
            new AtomicLong();

    static final AtomicLong esperaMesaTotalMs =
            new AtomicLong();

    static final AtomicLong cocinerosEsperandoMs =
            new AtomicLong();

    // Ganancias expresadas en céntimos para evitar errores
    // de acumulación de decimales.
    static final AtomicLong gananciasCentimos =
            new AtomicLong();

    static long esperaMesaMaximaMs = 0;

    static final Object bloqueoEstadisticas = new Object();


    // =========================================================
    //  METODO AUXILIAR PARA MOSTRAR LA HORA
    // =========================================================

    static String ahora() {
        return LocalTime.now().format(
                DateTimeFormatter.ofPattern("HH:mm:ss")
        );
    }


    // =========================================================
    // METODO MAIN
    // Arranque, coordinacion y cierre del restaurante
    // =========================================================

    public static void main(String[] args) throws InterruptedException {

        for (int i = 1; i <= NUM_MESAS; i++) {
            mesasLibres.put(i);
        }

        System.out.println("=== RESTAURANTE CONCURRENTE ===");

        System.out.println(
                "Mesas: " + NUM_MESAS
                        + " | Camareros: " + CAMAREROS
                        + " | Cocineros: " + COCINEROS
                        + " | Clientes: " + CLIENTES
        );

        System.out.println("Iniciando servicio...\n");


        // -----------------------------------------------------
        //  ARRANCAR LOS COCINEROS (CONSUMIDORES)
        // -----------------------------------------------------

        Thread[] hilosCocineros = new Thread[COCINEROS];

        for (int i = 1; i <= COCINEROS; i++) {

            final int id = i;

            hilosCocineros[i - 1] =
                    Thread.ofPlatform()
                            .name("cocinero-" + id)
                            .start(() -> {

                                try {
                                    while (true) {

                                        long t0 = System.currentTimeMillis();

                                        // Esperar un pedido.
                                        Pedido p = mesa.take();

                                        cocinerosEsperandoMs.addAndGet(
                                                System.currentTimeMillis() - t0
                                        );

                                        // Señal de cierre.
                                        if (p.cierre) {
                                            break;
                                        }

                                        System.out.println(
                                                "[" + ahora() + "] Cocinero-" + id
                                                        + " prepara " + p.plato
                                                        + " para Cliente-" + p.cliente
                                        );

                                        // Preparar el plato.
                                        Thread.sleep(p.plato.tiempoMs);

                                        System.out.println(
                                                "[" + ahora() + "] Cocinero-" + id
                                                        + " termina " + p.plato
                                                        + " para Cliente-" + p.cliente
                                        );

                                        // Registrar que el plato está listo.
                                        p.platoPreparado.countDown();
                                    }

                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }

                                System.out.println(
                                        "Cocinero-" + id + " termina su turno."
                                );
                            });
        }


        // -----------------------------------------------------
        // ARRANCAR LOS CAMAREROS (PRODUCTORES)
        // -----------------------------------------------------

        Thread[] hilosCamareros = new Thread[CAMAREROS];

        for (int i = 1; i <= CAMAREROS; i++) {

            final int id = i;

            hilosCamareros[i - 1] =
                    Thread.ofPlatform()
                            .name("camarero-" + id)
                            .start(() -> {

                                try {
                                    while (true) {

                                        // Esperar un pedido de un cliente.
                                        Pedido p = colaClientes.take();

                                        // Señal de cierre.
                                        if (p.cierre) {
                                            break;
                                        }

                                        // Simular la toma del pedido.
                                        Thread.sleep(TIEMPO_CAMARERO_MS);

                                        System.out.println(
                                                "[" + ahora() + "] Camarero-" + id
                                                        + " toma pedido " + p.plato
                                                        + " del Cliente-" + p.cliente
                                        );

                                        // Enviar el pedido a cocina.
                                        if (mesa.remainingCapacity() == 0) {
                                            mesaLlenaCount.incrementAndGet();
                                        }

                                        mesa.put(p);

                                        // Esperar hasta que cocina termine.
                                        p.platoPreparado.await();

                                        // Servir el plato al cliente.
                                        System.out.println(
                                                "[" + ahora() + "] Camarero-" + id
                                                        + " sirve " + p.plato
                                                        + " al Cliente-" + p.cliente
                                        );

                                        // Registrar el tiempo desde la creación
                                        // del pedido hasta que se sirve.
                                        esperaTotalMs.addAndGet(
                                                System.currentTimeMillis()
                                                        - p.horaCreacionMs
                                        );

                                        platosServidos.incrementAndGet();

                                        // Avisar al cliente de que puede comer.
                                        p.platoServido.countDown();
                                    }

                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }

                                System.out.println(
                                        "Camarero-" + id + " termina su turno."
                                );
                            });
        }


        // -----------------------------------------------------
        // GENERAR LOS CLIENTES
        // Cada cliente tiene su propio hilo virtual.
        // -----------------------------------------------------

        Thread generadorClientes =
                Thread.ofPlatform()
                        .name("puerta")
                        .start(() -> {

                            Random r = new Random();

                            for (int i = 1; i <= CLIENTES; i++) {

                                final int id = i;
                                final TipoPlato plato =
                                        TipoPlato.aleatorio(r);

                                // Cada cliente ejecuta su propio recorrido.
                                Thread.ofVirtual()
                                        .name("cliente-" + id)
                                        .start(() -> atenderCliente(id, plato));

                                try {
                                    Thread.sleep(LLEGADA_CLIENTE_MS);

                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                            }
                        });


        // -----------------------------------------------------
        // ESPERAR A QUE TODOS LOS CLIENTES TERMINEN
        // -----------------------------------------------------

        clientesAtendidos.await();
        generadorClientes.join();


        // -----------------------------------------------------
        // CERRAR A LOS CAMAREROS
        // Todos los clientes han terminado y sus pedidos se han servido.
        // -----------------------------------------------------

        for (int i = 0; i < CAMAREROS; i++) {
            colaClientes.put(Pedido.pedidoDeCierre());
        }

        for (Thread hilo : hilosCamareros) {
            hilo.join();
        }


        // -----------------------------------------------------
        // CERRAR A LOS COCINEROS
        // Los camareros ya han terminado de enviar pedidos a cocina.
        // -----------------------------------------------------

        for (int i = 0; i < COCINEROS; i++) {
            mesa.put(Pedido.pedidoDeCierre());
        }

        for (Thread hilo : hilosCocineros) {
            hilo.join();
        }


        // -----------------------------------------------------
        // MOSTRAR LAS ESTADISTICAS FINALES
        // -----------------------------------------------------

        mostrarEstadisticas();

        System.out.println("Restaurante cerrado.");
    }


    // =========================================================
    // ATENCION DE UN CLIENTE
    // Llegada, cola de espera, mesa, pedido, comida y pago
    // =========================================================

    static void atenderCliente(int id, TipoPlato plato) {

        long horaLlegada = System.currentTimeMillis();

        int numeroMesa = -1;
        boolean entroEnCola = false;

        try {

            System.out.println(
                    "[" + ahora() + "] Cliente-" + id
                            + " llega y quiere " + plato
            );


            // -------------------------------------------------
            // ESPERAR PARA CONSEGUIR MESA
            // -------------------------------------------------

            if (mesasDisponibles.availablePermits() == 0) {

                entroEnCola = true;

                clientesEnCola.incrementAndGet();
                mesaLlenaCount.incrementAndGet();

                System.out.println(
                        "[" + ahora() + "] Cliente-" + id
                                + " espera en la cola de mesas. En espera: "
                                + clientesEnCola.get()
                );
            }

            // Bloquearse hasta que haya una mesa disponible.
            mesasDisponibles.acquire();

            if (entroEnCola) {
                clientesEnCola.decrementAndGet();
            }

            numeroMesa = mesasLibres.take();

            long tiempoEspera =
                    System.currentTimeMillis() - horaLlegada;

            esperaMesaTotalMs.addAndGet(tiempoEspera);

            synchronized (bloqueoEstadisticas) {

                if (tiempoEspera > esperaMesaMaximaMs) {
                    esperaMesaMaximaMs = tiempoEspera;
                }
            }

            System.out.println(
                    "[" + ahora() + "] Cliente-" + id
                            + " se sienta en la mesa " + numeroMesa
                            + ". Espera: " + tiempoEspera / 1000.0 + " s"
            );


            // -------------------------------------------------
            // PEDIR COMIDA
            // -------------------------------------------------

            long horaPedido = System.currentTimeMillis();

            Pedido pedido = new Pedido(
                    id,
                    plato,
                    numeroMesa,
                    horaPedido,
                    false
            );

            System.out.println(
                    "[" + ahora() + "] Cliente-" + id
                            + " solicita " + plato
                            + " (" + plato.precioEuros() + " €)"
            );

            // El cliente pone el pedido en la cola de camareros.
            colaClientes.put(pedido);


            // -------------------------------------------------
            // ESPERAR A QUE LE SIRVAN
            // -------------------------------------------------

            pedido.platoServido.await();


            // -------------------------------------------------
            // COMER Y PAGAR
            // -------------------------------------------------

            System.out.println(
                    "[" + ahora() + "] Cliente-" + id
                            + " come en la mesa " + numeroMesa
            );

            Thread.sleep(
                    ThreadLocalRandom.current().nextLong(
                            TIEMPO_COMIENDO_MIN_MS,
                            TIEMPO_COMIENDO_MAX_MS + 1
                    )
            );

            gananciasCentimos.addAndGet(plato.precioCentimos);

            clientesAtendidosTotal.incrementAndGet();

            System.out.printf(
                    "[%s] Cliente-%d paga %.2f € y termina de comer.%n",
                    ahora(), id, plato.precioEuros()
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            System.out.println(
                    "Cliente-" + id + " ha sido interrumpido."
            );

        } finally {

            // Liberar la mesa incluso si el cliente es interrumpido.
            if (numeroMesa != -1) {

                mesasLibres.offer(numeroMesa);
                mesasDisponibles.release();

                System.out.println(
                        "[" + ahora() + "] Cliente-" + id
                                + " libera la mesa " + numeroMesa
                );
            }

            clientesAtendidos.countDown();
        }
    }


    // =========================================================
    // ESTADISTICAS FINALES
    // =========================================================

    static void mostrarEstadisticas() {

        System.out.println("\n========== ESTADISTICAS FINALES ==========");

        System.out.println(
                "Clientes atendidos: "
                        + clientesAtendidosTotal.get()
                        + "/" + CLIENTES
        );

        System.out.println(
                "Platos servidos: " + platosServidos.get()
        );

        System.out.printf(
                "Ganancias totales: %.2f €%n",
                gananciasCentimos.get() / 100.0
        );

        System.out.printf(
                "Tiempo medio de espera para conseguir mesa: %.2f s%n",
                esperaMesaTotalMs.get() / 1000.0 / CLIENTES
        );

        synchronized (bloqueoEstadisticas) {

            System.out.printf(
                    "Tiempo maximo de espera para mesa: %.2f s%n",
                    esperaMesaMaximaMs / 1000.0
            );
        }

        System.out.printf(
                "Tiempo medio desde pedido hasta servicio: %.2f s%n",
                esperaTotalMs.get() / 1000.0 / CLIENTES
        );

        System.out.println(
                "Veces que un cliente llega con todas las mesas ocupadas: "
                        + mesaLlenaCount.get()
        );

        System.out.println(
                "Clientes en cola al cierre: " + clientesEnCola.get()
        );

        System.out.println(
                "Cocineros esperando pedidos: "
                        + cocinerosEsperandoMs.get() / 1000.0
                        + " s acumulados"
        );

        System.out.println(
                "Mesas libres al cierre: " + mesasLibres.size()
                        + "/" + NUM_MESAS
        );

        System.out.println("==========================================");
    }
}


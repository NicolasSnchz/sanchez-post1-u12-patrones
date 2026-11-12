package com.empresa.reembolsos;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/** Captura lo que una accion imprime en System.out, para probar los notificadores de consola. */
public final class Consola {

    private Consola() {
    }

    public static String durante(Runnable accion) {
        PrintStream original = System.out;
        ByteArrayOutputStream capturada = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturada, true, StandardCharsets.UTF_8));
        try {
            accion.run();
        } finally {
            System.setOut(original);
        }
        return capturada.toString(StandardCharsets.UTF_8);
    }
}

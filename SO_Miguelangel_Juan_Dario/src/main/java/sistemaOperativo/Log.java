/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemaOperativo;
import java.util.concurrent.Semaphore;
/**
 *
 * @author Miguel
 */
public class Log {
    private static final int MAX_LINEAS = 500;
    private final String[] lineas = new String[MAX_LINEAS];
    private int inicio = 0;
    private int cantidad = 0;
    private int version = 0;
    private final Semaphore mutex = new Semaphore(1);

    public void registrar(String evento) {
        try {
            mutex.acquire();
            lineas[(inicio + cantidad) % MAX_LINEAS] = evento;
            if (cantidad < MAX_LINEAS) {
                cantidad++;
            } else {
                inicio = (inicio + 1) % MAX_LINEAS;
            }
            version++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            mutex.release();
        }
    }

    public String getTexto() {
        try {
            mutex.acquire();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cantidad; i++) {
                sb.append(lineas[(inicio + i) % MAX_LINEAS]).append("\n");
            }
            return sb.toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } finally {
            mutex.release();
        }
    }

    public int getVersion() {
        try {
            mutex.acquire();
            return version;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0;
        } finally {
            mutex.release();
        }
    }
}

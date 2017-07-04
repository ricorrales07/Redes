import java.net.*;
import java.lang.Thread;
import java.io.*;

// <>

/**
 * Write a description of class Main here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Router
{
    // constante
    public static final int PUERTO_ENTRADA = 57809;

    private static ServerSocket sSocket;
    private static InterfazDeOperador interfaz;
    public static Servidor server;

    public static void main(String[] args)
    {
        interfaz = new InterfazDeOperador();

        try
        {
            System.out.println("Iniciando router...");
            sSocket = new ServerSocket(PUERTO_ENTRADA);
        }
        catch (IOException e)
        {
            System.out.println("ERROR: No se pudo obtener el puerto " + PUERTO_ENTRADA + ". Abortando...");
            //return 1;
        }

        while (server == null)
        {
            try
            {
                String[] s = interfaz.inicializar();
                server = new Servidor(s[0], s[1], s[2]);
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
                System.out.println("Inténtelo de nuevo: ");
            }
        }

        //String ip = "", mascara = "", as = "";
        //System.out.println("Insertar dirección IP: ");
        //ip = System.console().readLine();
        // etc... Esto podría pasarse a la interfaz.
        // server = new Servidor(ip, mascara, as);

        // Inicializar servicios del despachador:
        DespachadorDePaquetes.subscribe(Paquete_t.SOLICITUD_DE_CONEXION, server);
        DespachadorDePaquetes.subscribe(Paquete_t.CONEXION_ACEPTADA, server);
        DespachadorDePaquetes.subscribe(Paquete_t.SOLICITUD_DE_DESCONEXION, server);
        DespachadorDePaquetes.subscribe(Paquete_t.CONFIRMACION_DE_DESCONEXION, server);
        DespachadorDePaquetes.subscribe(Paquete_t.PAQUETE_DE_ALCANZABILIDAD, server);

        // Un hilo para atender la interfaz de usuario.
        interfaz = new InterfazDeOperador();
        Thread i = new Thread(interfaz);
        i.start();

        while (true)
        {
            // Se crea un hilo nuevo con cada nueva conexión.
            DespachadorDePaquetes despachador;
            try
            {
                despachador = new DespachadorDePaquetes(sSocket.accept());
            }
            catch (IOException e)
            {
                System.out.println("Error en la conexión.");
                continue;
            }
            Thread t = new Thread(despachador);
            t.start();
        }
    }

    // Para concatenar arreglos de bytes.
    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            for (byte element : array) {
                result[pos] = element;
                pos++;
            }
        }
        return result;
    }
}

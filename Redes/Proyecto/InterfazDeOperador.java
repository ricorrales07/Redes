import java.util.Scanner;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.*;

/**
 * FALTA:   Necesita tener acceso al hilo de alcanzabilidad para
 *              poder interrumpirlo y que este envíe la info de
 *              alcanzabilidad antes de tiempo. --> LISTO
 *          Tiene que acceder a la tabla de hilos para poder
 *              interrumpir uno y pedirle que elimine la conexión
 *              con el vecino. --> LISTO (bvecino)
 *          Falta controlar bien las excepciones.
 *          Buscar cómo formatear bonito las tablas.
 *          Casi todo hay que cambiarlo aquí dentro.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class InterfazDeOperador implements Runnable
{
    Scanner s = new Scanner(System.in);

    private static final String ayuda =
        "| Comandos:                                                                                                            |\n" +
        "| * ayuda : despliega este mensaje.                                                                                    |\n" +
        "| * nvecino <IP> <Máscara> : envía una solicitud de vecino al router con dirección <IP> y máscara <Máscara>.           |\n" +
        "| * muestra <tabla> : despliega distinta información dependiendo del valor de <tabla>:                                 |\n" +
        "|      + vecinos : despliega la tabla de vecinos.                                                                      |\n" +
        "|      + destinos : despliega la tabla de alcanzabilidad.                                                              |\n" +
        "| * bvecino <IP> : borra un vecino de la tabla de vecinos y envía una solicitud de borrado al vecino corresponiente.   |\n" +
        "| * ndestino <IP> <Máscara> <Lista de sistemas autónomos> : agrega un nuevo destino a la talba de alcanzabilidad.      |\n" +
        "| * enviara : enviar alcanzabilidad cortando el hilo antes de tiempo.                                                  |\n" +
        "| * salir : termina el proceso.                                                                                        |\n";

    public String[] inicializar()
    {
        String[] result = new String[3];

        System.out.println("Insertar dirección IP: ");
        result[0] = s.nextLine();
        System.out.println("Insertar máscara: ");
        result[1] = s.nextLine();
        System.out.println("Insertar número de sistema autónomo: ");
        result[2] = s.nextLine();

        return result;
    }

    public void inicializarDestinos()
    {
        String input;
        System.out.println("Inserte a continuación la IP, máscara y ruta de sistemas autónomos de cada destino alcanzable desde este router (escriba \"fin\" para terminar):");
        input = s.nextLine();
        while (!input.equals("fin"))
        {
            String[] destino = input.split(" ");
            try
            {
                Router.agregarNuevoDestino(destino);
            }
            catch(IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
                input = s.nextLine();
                continue;
            }
            System.out.println("Destino agregado exitosamente");
            input = s.nextLine();
        }
    }

    public void run()
    {
        while (true)
        {
            String input;
            boolean exito;

            System.out.print(">> ");
            input = s.nextLine();

            String[] comando = input.split(" ");
            switch(comando[0])
            {
                case "ayuda":
                    System.out.println(ayuda);
                    break;

                case "nvecino":
                    System.out.println("Enviando solicitud de conexión a " + comando[1] + "...");
                    Conexion c;
                    try
                    {
                        c = new Conexion(comando[1], comando[2]);
                    }
                    catch(IllegalArgumentException e)
                    {
                        synchronized(System.out)
                        {
                            System.out.println(e.getMessage());
                        }
                        break;
                    }
                    catch(IOException e)
                    {
                        synchronized(System.out)
                        {
                            System.out.println(e.getMessage());
                        }
                        break;
                    }
                    Thread t = new Thread(c);
                    synchronized(Router.hilosActivos)
                    {
                        Router.hilosActivos.put(c.getIPVecino(), t);
                        Router.memoriaCompartida.put(c.getIPVecino(), new ConcurrentLinkedQueue<Integer>());
                    }
                    t.start();
                    synchronized(System.out)
                    {
                        System.out.println("Vecino agregado con éxito.");
                    }
                    break;

                case "mostrar":
                    switch(comando[1])
                    {
                        case "vecinos":
                            try
                            {
                                System.out.println(TablaVecinos.getTabla().toString());
                            }
                            catch(IOException e)
                            {
                                synchronized(System.out)
                                {
                                    System.out.println(e.getMessage());
                                }
                            }
                            break;
                        case "destinos":
                            try
                            {
                                System.out.println(TablaAlcanzabilidad.getTabla().toString());
                            }
                            catch(IOException e)
                            {
                                synchronized(System.out)
                                {
                                    System.out.println(e.getMessage());
                                }
                            }
                            break;
                    }
                    break;

                case "bvecino":
                    InetAddress vecino;
                    try
                    {
                        vecino = InetAddress.getByName(comando[1]);
                    }
                    catch (UnknownHostException e)
                    {
                        synchronized(System.out)
                        {
                            System.out.println("Dirección IP no válida.");
                        }
                        break;
                    }
                    synchronized(Router.hilosActivos)
                    {
                        Router.memoriaCompartida.get(vecino).add(0);
                        Router.hilosActivos.get(vecino).interrupt();
                    }
                    break;

                case "enviara": // Interrumpe el hilo de alcanzabilidad y envía la info antes de tiempo
                    synchronized(System.out)
                    {
                        System.out.println("Interrumpiendo hilo de alcanzabilidad...");
                    }
                    Router.hiloAlcanzabilidad.interrupt();
                    synchronized(System.out)
                    {
                        System.out.println("Información de alcanzabilidad enviada.");
                    }
                    break;
                case "salir":
                    System.exit(0);
            }
        }
    }
}

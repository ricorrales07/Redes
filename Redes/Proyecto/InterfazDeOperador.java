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
        "| Comandos:                                                                                                            |\n\n" +
        "| * ayuda : despliega este mensaje.                                                                                    |\n" +
        "| * nvecino <IP> <Máscara> : envía una solicitud de vecino al router con dirección <IP> y máscara <Máscara>.           |\n" +
        "| * bvecino <IP> : borra un vecino de la tabla de vecinos y envía una solicitud de borrado al vecino corresponiente.   |\n\n" +
        "| * ndestino <IP> <Máscara> <Lista de sistemas autónomos> : agrega un nuevo destino a la tabla de alcanzabilidad.      |\n\n" +
        "| * mostrar <tabla> : despliega distinta información dependiendo del valor de <tabla>:                                 |\n" +
        "|      + vecinos : despliega la tabla de vecinos.                                                                      |\n" +
        "|      + destinos : despliega la tabla de alcanzabilidad.                                                              |\n\n" +
        "| * forzar-envio : forzar envío de información de alcanzabilidad a vecinos.                                            |\n" +
        "| * salir : finaliza el programa.                                                                                      |\n";

    public void imprimirSeguro(String x)
    {
        synchronized(System.out)
        {
            System.out.println(x);
        }
    }
        
    public String[] inicializar()
    {
        String[] result = new String[3];

        imprimirSeguro("Insertar dirección IP: ");
        result[0] = s.nextLine();
        imprimirSeguro("Insertar máscara: ");
        result[1] = s.nextLine();
        imprimirSeguro("Insertar número de sistema autónomo: ");
        result[2] = s.nextLine();

        return result;
    }

    public void inicializarDestinos()
    {
        String input;
        imprimirSeguro("Inserte a continuación la IP, máscara y ruta de sistemas autónomos de cada destino\n"
                            + "alcanzable desde este router (escriba \"fin\" para terminar):");
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
                imprimirSeguro(e.getMessage());
                input = s.nextLine();
                continue;
            }
            catch(IOException e)
            {
                imprimirSeguro(e.getMessage());
                input = s.nextLine();
                continue;
            }
            imprimirSeguro("Destino agregado exitosamente");
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
            try
            {
                switch(comando[0])
                {
                    case "ayuda":
                            imprimirSeguro(ayuda);
                        break;
    
                    case "nvecino":
                    
                        Conexion c;
                        try
                        {
                            System.out.println("Enviando solicitud de conexión a " + comando[1] + "...");
                            c = new Conexion(comando[1], comando[2]);
                        }
                        catch(IllegalArgumentException e)
                        {
                            imprimirSeguro(e.getMessage());
                            break;
                        }
                        catch(IOException e)
                        {
                            imprimirSeguro(e.getMessage());
                            break;
                        }
                        Thread t = new Thread(c);
                        synchronized(Router.hilosActivos)
                        {
                            Router.hilosActivos.put(c.getIPVecino(), t);
                            Router.memoriaCompartida.put(c.getIPVecino(), new ConcurrentLinkedQueue<Integer>());
                        }
                        t.start();
                        imprimirSeguro("Vecino agregado con éxito.");
                        break;
    
                    case "mostrar":
                        switch(comando[1])
                        {
                            case "vecinos":
                                try
                                {
                                    imprimirSeguro(TablaVecinos.getTabla().toString());
                                }
                                catch(IOException e)
                                {
                                    imprimirSeguro(e.getMessage());
                                }
                                break;
                            case "destinos":
                                try
                                {
                                    imprimirSeguro(TablaAlcanzabilidad.getTabla().toString());
                                }
                                catch(IOException e)
                                {
                                    imprimirSeguro(e.getMessage());
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
                            imprimirSeguro("Dirección IP no válida.");
                            break;
                        }
                        synchronized(Router.hilosActivos)
                        {
                            Router.memoriaCompartida.get(vecino).add(0);
                            Router.hilosActivos.get(vecino).interrupt();
                        }
                        break;
    
                    case "enviara": // Interrumpe el hilo de alcanzabilidad y envía la info antes de tiempo
                        imprimirSeguro("Enviando información de alcanzabilidad...");
                        Router.hiloAlcanzabilidad.interrupt();
                        imprimirSeguro("Información de alcanzabilidad enviada.");
                        break;
                    case "salir":
                        System.exit(0);
                    default:
                        imprimirSeguro("No se reconoció el comando. Escriba \"ayuda\" para ver la lista de comandos disponibles.");
                        break;
                }
            }
            catch(Exception e)
            {
                imprimirSeguro("Error: " + e.toString());
            }
        }
    }
}

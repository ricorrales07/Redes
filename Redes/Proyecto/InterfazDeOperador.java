import java.util.Scanner;
import java.net.*;

/**
 * FALTA:   Necesita tener acceso al hilo de alcanzabilidad para
 *              poder interrumpirlo y que este envíe la info de
 *              alcanzabilidad antes de tiempo. --> LISTO
 *          Tiene que acceder a la tabla de hilos para poder
 *              interrumpir uno y pedirle que elimine la conexión
 *              con el vecino. --> LISTO (bvecino)
 *          Falta controlcar bien las excepciones.
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
            try
            {
                Router.agregarNuevoDestino(input);
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
                    exito = false;
                    try
                    {
                        exito = Router.server.solicitarNuevoVecino(comando[1], comando[2]);
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                        break;
                    }
                    if (exito)
                        System.out.println("Se agregó al vecino nuevo con éxito.");
                    else
                        System.out.println("No se pudo agregar al vecino.");
                    break;

                case "mostrar":
                    switch(comando[1])
                    {
                        case "vecinos":
                            System.out.println(Router.server.desplegarTablaDeVecinos());
                            break;
                        case "destinos":
                            System.out.println(Router.server.desplegarTablaAlcanzabilidad());
                    }
                    break;

                case "bvecino":
                    exito = false;
                    System.out.println("Borrando vecino " + comando[1] + "...");
                    try
                    {
                        exito = Router.server.solicitarDesconexion(comando[1]);
                    }
                    catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                    if (exito)
                        System.out.println("El vecino aceptó la solicitud de desconexión.");
                    else
                        System.out.println("No se obtuvo respuesta del vecino.");
                    break;

                case "enviara": // Interrumpe el hilo de alcanzabilidad y envía la info antes de tiempo
                    synchronized(System.out){"Interrumpiendo hilo de alcanzabilidad...\n"};
                    Router.hiloAlcanzabilidad.interrupt();
                    synchronized(System.out){"Nueva tabla de alcanzabilidad: \n"};
                    synchronized(System.out){Router.server.desplegarTablaAlcanzabilidad()};
                    break;
                case "salir":
                    System.exit(0);
            }
        }
    }
}

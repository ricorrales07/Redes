import java.util.Scanner;
import java.net.*;

/**
 * Write a description of class Interfaz here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class InterfazDeOperador implements Runnable
{
    Scanner s = new Scanner(System.in);

    private static final String ayuda =
        "Comandos:\n" +
        "ayuda : despliega este mensaje.\n" +
        "nvecino <IP> <Máscara> : envía una solicitud de vecino al router con dirección <IP> y máscara <Máscara>.\n" +
        "muestra <tabla> : despliega distinta información dependiendo del valor de <tabla>:\n" +
        "\tvecinos : despliega la tabla de vecinos.";

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

    public void run()
    {
        while (true)
        {
            String opcion;

            System.out.print(">> ");
            opcion = s.nextLine();

            String[] comando = opcion.split(" ");
            switch(comando[0])
            {
                case "ayuda":
                    System.out.println(ayuda);
                case "nvecino":
                    boolean exito = false;
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
                        System.out.println("No se pudo agregar al vecino");
                    break;

                case "muestra":
                    switch(comando[1])
                    {
                        case "vecinos":
                            System.out.println(Router.server.desplegarTablaDeVecinos());
                    }
                    break;
                    
                case "exit":
                case "quit":
                    System.exit(0);
            }
        }
    }
}

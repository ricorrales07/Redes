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
    // TODO: Falta controlar bien todas las excepciones.
    
    // TODO: Buscar cómo formatear bonito las tablas.
    
    Scanner s = new Scanner(System.in);
    
    private static final String ayuda =
        "Comandos:\n" +
        "ayuda : despliega este mensaje.\n" +
        "nvecino <IP> <Máscara> : envía una solicitud de vecino al router con dirección <IP> y máscara <Máscara>.\n" +
        "muestra <tabla> : despliega distinta información dependiendo del valor de <tabla>:\n" +
        "\tvecinos : despliega la tabla de vecinos." +
        "\tdestinos : despliega la tabla de alcanzabilidad." +
        "bvecino <IP> : borra un vecino de la tabla de vecinos y envía una solicitud de borrado al vecino corresponiente.\n" +
        "ndestino <IP> <Máscara> <Lista de sistemas autónomos> : agrega un nuevo destino a la talba de alcanzabilidad" +
        "salir : termina el proceso.";
    
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
                Router.server.agregarNuevoDestino(input);
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
                    
                case "salir":
                    System.exit(0);
            }
        }
    }
}

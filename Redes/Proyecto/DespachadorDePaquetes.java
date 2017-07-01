import java.net.*;
import java.util.Hashtable;
import java.io.*;

/**
 * Write a description of class DespachadorDePaquetes here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class DespachadorDePaquetes implements Runnable
{   
    private final static Hashtable<Paquete_t, ManejadorDePaquetes> servicios = new Hashtable<Paquete_t, ManejadorDePaquetes>();
    
    public static void subscribe(Paquete_t tipoPaquete, ManejadorDePaquetes m)
    {
        servicios.put(tipoPaquete, m);
    }
    
    private Socket conexion;
    
    public DespachadorDePaquetes(Socket s)
    {
        conexion = s;
    }
    
    public void run()
    {
        InputStream i;
        Paquete_t tipo;
        try
        {
            i = conexion.getInputStream();
            tipo = Paquete_t.values()[i.read()];
        }
        catch(IOException e)
        {
            System.out.println("Error en la conexión.");
            return;
        }
        
        ManejadorDePaquetes m = servicios.get(tipo);
        m.maneja(tipo, conexion, i);
    }
}

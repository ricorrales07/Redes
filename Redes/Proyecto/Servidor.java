import java.net.*;
import java.io.IOException;

/**
 * Write a description of class Servidor here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Servidor
{
    private InetAddress direccion;
    private InetAddress mascara; //todavía no estoy seguro de para qué vamos a usar la máscara...
    private byte[] numAS;
    private static final int puertoEntrada = 57809;
    private int puertoSalida;
    
    private ServerSocket server;

    /**
     * Constructor for objects of class Servidor
     */
    public Servidor(String ip, String mask, byte[] as) throws IllegalArgumentException, IOException
    {
        try
        {
            direccion = InetAddress.getByName(ip);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException("Dirección IP del servidor inválida.");
        }
        try
        {
            mascara = InetAddress.getByName(mask);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException("Máscara inválida.");
        }
        
        numAS = as;
        
        server = new ServerSocket(puertoEntrada);
    }
    
    
}

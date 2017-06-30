import java.net.*;
import java.io.*;

/**
 * Write a description of class Servidor here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Servidor implements ManejadorDePaquetes
{
    private InetAddress direccion;
    private InetAddress mascara; //todavía no estoy seguro de para qué vamos a usar la máscara...
    private byte[] numAS;

    /**
     * Constructor for objects of class Servidor
     */
    // Tal vez debería recibir el AS como un String.
    public Servidor(String ip, String mask, String as) throws IllegalArgumentException, IOException
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
        
        numAS = asStringToBytes(as);
    }
    
    private byte[] asStringToBytes(String as) throws IllegalArgumentException
    {
        byte[] answer = new byte[2];
         
        String[] decimales = as.split(".");
        if (decimales.length != 2)
              throw new IllegalArgumentException("Dirección de Sistema Autónomo inválida. Debe llevar 2 bytes.");
        else
        {
            int[] decimales_int = new int[2];
            for (int i = 0; i < 2; i++)
            {
                try
                {
                    decimales_int[i] = Integer.parseInt(decimales[i]);
                }
                catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException("Dirección de Sistema Autónomo inválida. " + decimales[i] + " no es un número.");
                }
                if (decimales_int[i] < 0 || decimales_int[i] > 255)
                {
                    throw new IllegalArgumentException("Dirección de Sistema Autónomo inválida. " + decimales[i] 
                        + " no corresponde a un valor válido para un byte.");
                }
                else
                {
                    answer[i] = (byte) decimales_int[i];
                }
            }
        }
         
        return answer;
    }
    
    public void maneja(Paquete_t tipoPaquete, InputStream input)
    {
        switch(tipoPaquete)
        {
            case SOLICITUD_DE_CONEXION:
            case CONEXION_ACEPTADA:
            case SOLICITUD_DE_DESCONEXION:
            case PAQUETE_DE_ALCANZABILIDAD:
            default:
        }
    }
    
    public void nuevoVecino(String ip, String mask, byte[] as)
    {
        
    }
}

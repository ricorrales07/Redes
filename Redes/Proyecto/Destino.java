import java.util.Arrays;
import java.net.*;


/**
 * Write a description of class PaqueteVecino here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Destino
{
    InetAddress ip;
    InetAddress mascara;
    NumeroAS [] as;
    
    public Destino(byte[] paquete)
    {
        try
        {
            ip = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 6, 10));
            mascara = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 10, 14));
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Esto no debería pasar");
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException("Esto no debería pasar");
        }
    }
    
    
    public InetAddress getIP()
    {
        return ip;
    }
    
    public InetAddress getMascara()
    {
        return mascara;
    }
 
}

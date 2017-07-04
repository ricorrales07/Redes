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
    byte [] cantAS;
    
    
    
    public InetAddress getIP()
    {
        return ip;
    }
    
    public InetAddress getMascara()
    {
        return mascara;
    }
    
    public byte[] getCantAs()
    {
        return cantAS;
    }
 
}

import java.util.Arrays;
import java.net.*;


/**
 * Write a description of class PaqueteVecino here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class PaqueteVecino
{
    Paquete_t tipo;
    InetAddress ip;
    InetAddress mascara;
    NumeroAS as;
    
    public PaqueteVecino(Paquete_t tipo, byte[] paquete)
    {
        this.tipo = tipo;
        try
        {
            as = new NumeroAS(Arrays.copyOfRange(paquete, 0, 2));
            ip = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 2, 6));
            mascara = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 6, 10));
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
    
    public PaqueteVecino(Paquete_t tipo, InetAddress ip, InetAddress mascara, NumeroAS as)
    {
        this.tipo = tipo;
        this.as = as;
        this.ip = ip;
        this.mascara = mascara;
    }
    
    public byte[] getBytes()
    {
        byte[] type = { (byte) ((tipo == Paquete_t.SOLICITUD_DE_CONEXION) ? 1 : 2) };
        
        return Router.concat(type, as.getBytes(), ip.getAddress(), mascara.getAddress());
    }
    
    public InetAddress getIP()
    {
        return ip;
    }
    
    public InetAddress getMascara()
    {
        return mascara;
    }
    
    public NumeroAS getAS()
    {
        return as;
    }
}

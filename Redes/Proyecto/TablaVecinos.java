import java.util.logging.*;
import java.util.Dictionary;
import java.net.*;

// <>

/**
 * Write a description of class TablaVecinos here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class TablaVecinos
{
    private class Vecino
    {
        private InetAddress ip;
        private InetAddress mask;
        private byte[] as;
        
        public Vecino(InetAddress ipVecino, InetAddress mascaraVecino, byte[] asVecino)
        {
            ip = ipVecino;
            mask = mascaraVecino;
            as = asVecino;
        }
    }
    
    private Dictionary<InetAddress, Vecino> tabla;
    
    //private Logger registro;
    
    public void addVecino(InetAddress ipVecino, InetAddress mascaraVecino, byte[] asVecino)
    {
        tabla.put(ipVecino, new Vecino(ipVecino, mascaraVecino, asVecino));
    }

    public void removeVecino(InetAddress ipVecino)
    {
        tabla.remove(ipVecino);
    }
}

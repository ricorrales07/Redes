import java.net.*;

/**
 * Write a description of class Servidor here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Servidor
{
    private DireccionIPv4 direccion;
    private DireccionIPv4 mascara;
    private byte[] numAS;
    private static final int puertoEscucha = 57809;
    private int puertoEnvia;
    
    //private Socket s;

    /**
     * Constructor for objects of class Servidor
     */
    public Servidor(DireccionIPv4 ip, DireccionIPv4 mask, byte[] as)
    {
        direccion = ip;
        mascara = mask;
        numAS = as;
        
        //s = new Socket(ip.toString(), puertoEscucha);
    }
    
}

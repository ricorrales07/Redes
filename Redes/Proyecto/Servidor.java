import java.net;

/**
 * Write a description of class Servidor here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Servidor
{
    private byte[] direccionIPv4;
    private struct mascaraIPv4_t
    {
        int decimal;
        byte[] bytes;
    } mascaraIPv4;
    private byte[] numAS;
    private static final int puertoEscucha = 57809;
    private int puertoEnvia;
    
    private Socket s;

    /**
     * Constructor for objects of class Servidor
     */
    public Servidor(byte[] ip, int mask, byte[] as)
    {
        direccionIPv4 = ip;
        mascaraIPv4.decimal = mask;
        numAS = as;
        
        //s = new Socket("" + ip, );
    }
    
}

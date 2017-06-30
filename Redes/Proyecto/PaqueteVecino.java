
/**
 * Write a description of class PaqueteVecino here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class PaqueteVecino
{
    byte tipo;
    byte[] as;
    byte[] ip;
    byte[] mascara;
    
    public PaqueteVecino(byte tipo, byte[] as, byte[] ip, byte[] mascara)
    {
        this.tipo = tipo;
        this.as = as;
        this.ip = ip;
        this.mascara = mascara;
    }
}

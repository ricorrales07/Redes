import java.util.Arrays;
import java.net.*;
import java.util.ArrayList;
import java.nio.ByteBuffer;

/**
 * Write a description of class PaqueteVecino here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class PaqueteAlcanzabilidad
{
    NumeroAS as;
    ArrayList<Destino> listaDestinos; 
    
    public PaqueteAlcanzabilidad(byte[] paquete)
    {
        try
        {
            as = new NumeroAS(Arrays.copyOfRange(paquete, 0 , 2)); 
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Esto no debería pasar");
        }
        
        listaDestinos = new ArrayList<Destino>();
    }
    
    public PaqueteAlcanzabilidad(NumeroAS as)
    {
        this.as = as;
        listaDestinos = new ArrayList<Destino>();
    }
      
    public NumeroAS getAS()
    {
        return as;
    }
    
    public void addDestino(Destino d)
    {
        listaDestinos.add(d);
    }
    
    public ArrayList<Destino> getListaDestinos()
    {
        return listaDestinos;
    }
    
    public byte[] getBytes()
    {
        byte[] tipo = {(byte) Paquete_t.PAQUETE_DE_ALCANZABILIDAD.ordinal()};
        byte[] as = this.as.getBytes();
        byte[] cantDestinos = new byte[4];
        cantDestinos = ByteBuffer.wrap(cantDestinos).putInt(this.listaDestinos.size()).array();
        
        byte[] destinos = new byte[0];
        for (Destino d : this.listaDestinos)
            destinos = Router.concat(destinos, d.getBytes());
        
        return Router.concat(tipo, as, cantDestinos, destinos);
    }
}

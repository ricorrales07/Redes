import java.util.Arrays;
import java.net.*;


/**
 * Write a description of class PaqueteVecino here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class PaqueteAlcanzabilidad
{
    Paquete_t tipo;
    NumeroAS as;
    Destino [] listaDestinos; 
    
    public PaqueteAlcanzabilidad(Paquete_t tipo, byte[] paquete)
    {
        this.tipo = tipo;
        try
        {
            as = new NumeroAS(Arrays.copyOfRange(paquete, 0 , 2)); 
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Esto no debería pasar");
        }
    }
    
    public PaqueteAlcanzabilidad(Paquete_t tipo, NumeroAS as, Destino [] listaDestinos)
    {
        this.tipo = tipo;
        this.as = as;
        this.listaDestinos = listaDestinos;
    }
    
    
    public Paquete_t getTipo()
    {
        return tipo;
    }
      
    public NumeroAS getAS()
    {
        return as;
    }
    
    
    public Destino[] getListaDestinos()
    {
        return listaDestinos;
    }
}

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
	byte [] cantDestinos;
	Destino [] listaDestinos; 
	
    
    public PaqueteAlcanzabilidad(Paquete_t tipo, byte[] paquete)
    {
        this.tipo = tipo;
        try
        {
            as = new NumeroAS(Arrays.copyOfRange(paquete, 0, 2));
	    cantDestinos = new byte [(Arrays.copyOfRange(paquete, 2, 6))];	
            ip = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 6, 10));
            mascara = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 10, 14));
	    listaDestinos = new Destino[(Arrays.copyOfRange(paquete, 14, paquete.length)];
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
	
    public PaqueteAlcanzabilidad(Paquete_t tipo, NumeroAS as, byte [] cantDestinos, Destino [] listaDestinos)
    {
        this.tipo = tipo;
        this.as = as;
        this.cantDestinos = cantDestinos;
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
	
    public byte[] getCantDest()
    {
    	return cantDestinos;
    }	
	
    public Destino[] getListaDestinos()
    {
    	return listaDestinos;
    }
}

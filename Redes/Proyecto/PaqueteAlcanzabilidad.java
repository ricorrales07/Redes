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
    Paquete_t tipo
	NumeroAS as;
	byte [] cantDestinos;
	Destino [] listaDestinos; 
	
    
    public PaqueteAlcanzabilidad(Paquete_t tipo, NumeroAS as, byte [] cantDestinos, Destino [] listaDestinos)
    {
        this.tipo = tipo;
        this.as = as;
        this.cantDestinos = ip;
        this.listaDestinos = mascara;
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
	
	public Destino[] listaDestinos()
	{
		return listaDestinos;
	}
}
import java.util.logging.*;
import java.util.Hashtable;
import java.net.*;
import java.io.IOException;

// <>

/**
 * Write a description of class TablaVecinos here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class TablaAlcanzabilidad
{
    private class PaqueteAlcanzabilidad
    {
		
		private Paquete_t tipo;
		private NumeroAS as;
		private byte [] cantDestinos;
		private Destino [] listaDestinos; 
        
        public PaqueteAlcanzabilidad(Paquete_t tipoV,NumeroAS asVecino, byte [] cantDV, Destino [] listaDV)
        {
            tipo = tipoV;
			as = asVecino;
            cantDestinos = cantDV;
            listaDestinos = listaDV;
        }

    }
    
    private Hashtable<InetAddress, PaqueteAlcanzabilidad> tabla;
    
    private Logger registro;
    private FileHandler fHandler;
    private SimpleFormatter sFormatter;
    
    // patrón singleton
    private static TablaAlcanzabilidad tablaUnica;
    
    private TablaAlcanzabilidad() throws IOException
    {
        fHandler = new FileHandler("logTablaAlcnzabilidad.txt");
        sFormatter = new SimpleFormatter();
        registro = Logger.getLogger("Tabla de alcanzabilidad");
        
        fHandler.setFormatter(sFormatter);
        registro.addHandler(fHandler);
        registro.setLevel(Level.INFO);
        
        tabla = new Hashtable<InetAddress, PaqueteAlcanzabilidad>();        
    }
    
    public static synchronized TablaAlcanzabilidad getTabla() throws IOException
    {
        if (tablaUnica == null)
            tablaUnica = new TablaAlcanzabilidad();
        return tablaUnica;
    }
    
    /*public synchronized void addPaquete(PaqueteAlcanzabilidad pa)
    {
        addPaquete(pa.getAS(),pa.getCantDest(), pa.getListaDestinos());
    }*/
   
    
    public synchronized void addPaquete(InetAddress ipVecino,Paquete_t tipo,NumeroAS as, byte [] cantDestinos, Destino [] listaDestinos)
    {
        PaqueteAlcanzabilidad paqueteNuevo = new PaqueteAlcanzabilidad(tipo,as,cantDestinos,listaDestinos);
        tabla.put(ipVecino, paqueteNuevo);
        registro.log(Level.INFO, "Nuevo paquete de alcanzabilidad añadido a la tabla de alcanzabilidad");
    }
   
}
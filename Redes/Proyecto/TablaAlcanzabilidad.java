import java.util.logging.*;
import java.util.Hashtable;
import java.net.*;
import java.io.IOException;
import java.util.Arrays;

// <>

/**
 * Write a description of class TablaVecinos here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class TablaAlcanzabilidad
{
    private class Dest
    {
		private InetAddress ip;
    	private InetAddress mascara;
    	private NumeroAS [] as;
        public Dest(byte[] paquete)
        {
            try
            {
                ip = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 6, 10));
                mascara = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 10, 14));
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
    }
    
    private Hashtable<InetAddress, Dest> tabla;
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
        
        tabla = new Hashtable<InetAddress, Dest>();        
    }
    
    public static synchronized TablaAlcanzabilidad getTabla() throws IOException
    {
        if (tablaUnica == null)
            tablaUnica = new TablaAlcanzabilidad();
        return tablaUnica;
    }
    
    public synchronized void addPaquete(InetAddress ipVecino ,byte [] paquete)
    {
        Dest paqueteNuevo = new Dest(paquete);
        tabla.put(ipVecino, paqueteNuevo);
        registro.log(Level.INFO, "Nuevo paquete de alcanzabilidad añadido a la tabla de alcanzabilidad");
    }
   
}

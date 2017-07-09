import java.util.logging.*;
import java.util.Hashtable;
import java.net.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

// <>

/**
 * Write a description of class TablaVecinos here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class TablaAlcanzabilidad
{
    private Hashtable<InetAddress, Destino> tabla;
    private Logger registro;
    private FileHandler fHandler;
    private SimpleFormatter sFormatter;
    
    // patrón singleton
    private static TablaAlcanzabilidad tablaUnica;

    private TablaAlcanzabilidad() throws IOException
    {
        fHandler = new FileHandler("logTablaAlcanzabilidad.txt");
        sFormatter = new SimpleFormatter();
        registro = Logger.getLogger("Tabla de alcanzabilidad");
        
        fHandler.setFormatter(sFormatter);
        registro.addHandler(fHandler);
        registro.setLevel(Level.INFO);
        
        tabla = new Hashtable<InetAddress, Destino>();        
    }
    
    public static synchronized TablaAlcanzabilidad getTabla() throws IOException
    {
        if (tablaUnica == null)
            tablaUnica = new TablaAlcanzabilidad();
        return tablaUnica;
    }
    
    public synchronized void addDestino(Destino d)
    {
        addDestino(d, true, InetAddress.getLoopbackAddress());
    }
    
    public synchronized void addDestino(Destino d, boolean manual, InetAddress origen)
    {
        tabla.put(d.getIP(), d);
        if (manual)
            registro.log(Level.INFO, "Nuevo destino añadido a la tabla de alcanzabilidad (vía manual): {0}", d.logInfo());
        else
            registro.log(Level.INFO, "Nuevo destino añadido a la tabla de alcanzabilidad (vía " + origen.getHostAddress()  + "): {0}", d.logInfo());
    }
    
    public synchronized void removeAll(InetAddress ipVecino, boolean manual, InetAddress origen) throws IllegalArgumentException
    {
        for (Destino d : tabla.values())
            if (d.getIPSalida().equals(ipVecino)){
                try
                {
                    Destino destinoEliminado = tabla.remove(d.getIPSalida());
                    if (manual)
                        registro.log(Level.INFO, "Vecino eliminado (vía manual): {0}", destinoEliminado.logInfo());
                    else
                        registro.log(Level.INFO, "Vecino eliminado (vía " + origen.getHostAddress() + "): {0}", destinoEliminado.logInfo());
                }
                catch (NullPointerException e)
                {
                    registro.warning("Se recibió un puntero nulo para eliminar");
                    throw new IllegalArgumentException("Se recibió un puntero nulo para eliminar");
                }
            }
    }
    
    public synchronized void removeAll(InetAddress ipVecino) throws IllegalArgumentException
    {
        removeAll(ipVecino, true, InetAddress.getLoopbackAddress());
    }
    
    public synchronized Collection<Destino> getAllDestinos()
    {
        return tabla.values();
    }
    
    public synchronized String toString()
    {
        String retornable = "Dirección IP \t Máscara de red \t IP Salida \t Ruta \n";
        Collection<Destino> tablaIterable = tabla.values();
        for (Destino v : tablaIterable)
            retornable += v.toString() + "\n";
        return retornable;
    }
    
    public synchronized Destino getDestino(InetAddress ip)
    {
        return tabla.get(ip);
    }
}

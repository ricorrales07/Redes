import java.util.logging.*;
import java.util.Hashtable;
import java.net.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.ArrayList;

// <>

/**
 * Write a description of class TablaVecinos here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class TablaAlcanzabilidad
{
    private Hashtable<InetAddress, HashSet<Destino>> tabla;
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
        
        tabla = new Hashtable<InetAddress, HashSet<Destino>>();        
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
        InetAddress ip = d.getIP();
        if (tabla.containsKey(ip))
        {
            tabla.get(ip).add(d);
        }
        else
        {
            HashSet<Destino> h = new HashSet<Destino>();
            h.add(d);
            tabla.put(ip, h);
        }
        if (manual)
            registro.log(Level.INFO, "Nuevo destino añadido a la tabla de alcanzabilidad (vía manual): {0}", d.logInfo());
        else
            registro.log(Level.INFO, "Nuevo destino añadido a la tabla de alcanzabilidad (vía " + origen.getHostAddress()  + "): {0}", d.logInfo());
    }
    
    /*public synchronized void removeDestino(InetAddress ip)
    {
        tabla.remove(ip);
    }*/
    
    public synchronized Collection<Destino> getAllDestinos()
    {
        ArrayList<Destino> a = new ArrayList<Destino>();
        for (HashSet<Destino> h : tabla.values())
            a.addAll(h);
        return a;
    }
    
    public synchronized String toString()
    {
        String retornable = "Dirección IP \t Máscara de red \t IP Salida \t Ruta \n";
        Collection<Destino> tablaIterable = getAllDestinos();
        for (Destino v : tablaIterable)
            retornable += v.toString() + "\n";
        return retornable;
    }
    
    public synchronized void removeAll(InetAddress ip){
        for (Destino v : getAllDestinos())
            if (v.getIPSalida().equals(ip)){
                tabla.get(v.getIP()).remove(v);
            }
    }
    
}

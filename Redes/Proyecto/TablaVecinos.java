import java.util.logging.*;
import java.util.Hashtable;
import java.net.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

// <>

/**
 * Write a description of class TablaVecinos here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class TablaVecinos
{
    private class Vecino
    {
        private InetAddress ip;
        private InetAddress mask;
        private NumeroAS as;
        
        public Vecino(InetAddress ipVecino, InetAddress mascaraVecino, NumeroAS asVecino)
        {
            ip = ipVecino;
            mask = mascaraVecino;
            as = asVecino;
        }
        
        public InetAddress getIP()
        {
            return ip;
        }
        
        public String toString()
        {
            return ip.getHostAddress() + "\t" + mask.getHostAddress() + "\t" + as.toString();
        }
        
        public String logInfo()
        {
            return "IP: " + ip.getHostAddress() + ", Máscara: " + mask.getHostAddress() + ", AS:" + as.toString();
        }
    }
    
    private Hashtable<InetAddress, Vecino> tabla;
    
    private Logger registro;
    private FileHandler fHandler;
    private SimpleFormatter sFormatter;
    
    // patrón singleton
    private static TablaVecinos tablaUnica;
    
    private TablaVecinos() throws IOException
    {
        fHandler = new FileHandler("logTablaVecinos.txt");
        sFormatter = new SimpleFormatter();
        registro = Logger.getLogger("Tabla de vecinos");
        
        fHandler.setFormatter(sFormatter);
        registro.addHandler(fHandler);
        registro.setLevel(Level.INFO);
        
        tabla = new Hashtable<InetAddress, Vecino>();        
    }
    
    public static synchronized TablaVecinos getTabla() throws IOException
    {
        if (tablaUnica == null)
            tablaUnica = new TablaVecinos();
        return tablaUnica;
    }
    
    public synchronized void addVecino(PaqueteVecino pv, boolean manual)
    {
        addVecino(pv.getIP(), pv.getMascara(), pv.getAS(), manual, pv.getIP());
    }
    
    public synchronized void addVecino(InetAddress ipVecino, InetAddress mascaraVecino, NumeroAS asVecino)
    {
        addVecino(ipVecino, mascaraVecino, asVecino, true, InetAddress.getLoopbackAddress());
    }
    
    public synchronized void addVecino(InetAddress ipVecino, InetAddress mascaraVecino, NumeroAS asVecino, boolean manual, InetAddress origen)
    {
        Vecino vecinoNuevo = new Vecino(ipVecino, mascaraVecino, asVecino);
        tabla.put(ipVecino, vecinoNuevo);
        if (manual)
            registro.log(Level.INFO, "Nuevo vecino añadido a la tabla de vecinos (via manual): {0}", vecinoNuevo.logInfo());
        else
            registro.log(Level.INFO, "Nuevo vecino añadido a la tabla de vecinos (vía " + origen.getHostAddress() + "): {0}", vecinoNuevo.logInfo());
    }

    public synchronized void removeVecino(InetAddress ipVecino) throws IllegalArgumentException
    {
        removeVecino(ipVecino, true, InetAddress.getLoopbackAddress());
    }
    
    public synchronized void removeVecino(InetAddress ipVecino, boolean manual, InetAddress origen) throws IllegalArgumentException
    {
        try
        {
            Vecino vecinoEliminado = tabla.remove(ipVecino);
            if (vecinoEliminado == null)
            {
                registro.warning("No se encontró la IP " + ipVecino.getHostAddress() + " para eliminar de la tabla.");
                throw new IllegalArgumentException("No se encontró la IP " + ipVecino.getHostAddress() + " para eliminar de la tabla.");
            }
            else if (manual)
                registro.log(Level.INFO, "Vecino eliminado (vía manual): {0}", vecinoEliminado.logInfo());
            else
                registro.log(Level.INFO, "Vecino eliminado (vía " + origen.getHostAddress() + "): {0}", vecinoEliminado.logInfo());
        }
        catch (NullPointerException e)
        {
            registro.warning("Se recibió un puntero nulo para eliminar");
            throw new IllegalArgumentException("Se recibió un puntero nulo para eliminar");
        }
    }
    
    public synchronized boolean esVecino(InetAddress ip)
    {
        return tabla.containsKey(ip);
    }
    
    public String toString()
    {
        String retornable = "Dirección IP \t Máscara de red \t Sistema Autónomo \n";
        Collection<Vecino> tablaIterable = tabla.values();
        for (Vecino v : tablaIterable)
            retornable += v.toString() + "\n";
        return retornable;
    }
    
    public List<InetAddress> getListaIPs()
    {
        ArrayList<InetAddress> lista = new ArrayList<InetAddress>();
        for (Vecino v : tabla.values())
            lista.add(v.getIP());
        return lista;
    }
}

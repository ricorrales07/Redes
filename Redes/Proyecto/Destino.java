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
public class Destino
{
    InetAddress ip;
    InetAddress mascara;
    ArrayList<NumeroAS> ruta;
    InetAddress ipSalida;
    
    public Destino(byte[] paquete, InetAddress ipSalida)
    {
        try
        {
            ip = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 0, 4));
            mascara = InetAddress.getByAddress(Arrays.copyOfRange(paquete, 4, 8));
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException("Esto no debería pasar");
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException("Esto no debería pasar");
        }
        this.ipSalida = ipSalida;
        ruta = new ArrayList<NumeroAS>();
    }
    
    public Destino(InetAddress ip, InetAddress mascara, InetAddress ipSalida)
    {
        this.ip = ip;
        this.mascara = mascara;
        this.ipSalida = ipSalida;
        ruta = new ArrayList<NumeroAS>();
    }
    
    public InetAddress getIP()
    {
        return ip;
    }
    
    public InetAddress getMascara()
    {
        return mascara;
    }
    
    public int getLongRuta()
    {
        return ruta.size();
    }
    
    public InetAddress getIPSalida(){
        return ipSalida;
    }

    public byte[] getBytes()
    {
        byte[] ip = this.ip.getAddress();
        byte[] mask = this.mascara.getAddress();
        byte[] cantAS = ByteBuffer.allocate(2).putShort((short) this.ruta.size()).array();
        
        byte[] ruta = new byte[0];
        for (NumeroAS a : this.ruta)
            ruta = Router.concat(ruta, a.getBytes());
        
        return Router.concat(ip, mask, cantAS, ruta);
    }
    
    public void addAS (NumeroAS as)
    {
        ruta.add(as);
    }
    
    public String logInfo()
    {
        String logInfo = "IP: " + ip + ", máscara: " + mascara + ", IP Salida: " + ipSalida + "\n" + "Ruta: ";
        for (NumeroAS n : ruta)
            logInfo += n.toString() + ", ";
        return logInfo.substring(0, logInfo.length()-2);
    }
    
    public String toString()
    {
        String retornable = ip.getHostAddress() + "\t\t" + mascara.getHostAddress() + "\t\t\t" + ipSalida.getHostAddress() + "\t";
        for(NumeroAS n : ruta)
            retornable += n.toString() + ", ";
        return retornable;
    }
    
    @Override 
    public boolean equals(Object obj) 
    { 
        if (obj == this) 
        { 
            return true; 
        } 
        if (obj == null || obj.getClass() != this.getClass()) 
        { 
            return false; 
        } 
        Destino d = (Destino) obj;
        if (d.ruta.size() != this.ruta.size())
            return false;
        for (int i = 0; i < ruta.size(); i++)
            if(!d.ruta.get(i).equals(this.ruta.get(i)))
                return false;
        return ip.equals(d.ip) && mascara.equals(d.mascara) && ipSalida.equals(d.ipSalida);
    } 
        
    @Override 
    public int hashCode() 
    { 
        final int prime = 31; 
        int result = 1; 
        result = prime * result + ip.hashCode(); 
        result = prime * result + mascara.hashCode(); 
        result = prime * result + ipSalida.hashCode();
        for (NumeroAS n : ruta)
            result = prime * result + n.hashCode();
        return result; 
    }

}

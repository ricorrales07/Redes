import java.net.*;
import java.io.*;

/**
 * Write a description of class Servidor here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Servidor implements ManejadorDePaquetes
{
    private final InetAddress direccion;
    private final InetAddress mascara; //todavía no estoy seguro de para qué vamos a usar la máscara...
    private final NumeroAS numAS;
    
    private TablaVecinos vecinos;

    /**
     * Constructor for objects of class Servidor
     */
    // Tal vez debería recibir el AS como un String.
    public Servidor(String ip, String mask, String as) throws IllegalArgumentException, IOException
    {
        try
        {
            direccion = InetAddress.getByName(ip);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException("Dirección IP del servidor inválida.");
        }
        try
        {
            mascara = InetAddress.getByName(mask);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException("Máscara inválida.");
        }
        
        numAS = new NumeroAS(as);
        
        vecinos = TablaVecinos.getTabla();
    }
    
    public void maneja(Paquete_t tipoPaquete, Socket s, InputStream input)
    {
        switch(tipoPaquete)
        {
            case SOLICITUD_DE_CONEXION:
                byte[] paquete = new byte[10];
                
                try
                {
                    input.read(paquete);
                }
                catch (IOException e)
                {
                    System.out.println("Error al recibir paquete.");
                    return;
                }
                
                PaqueteVecino pv = new PaqueteVecino(tipoPaquete, paquete);
                
                nuevoVecino(pv, false, s);
                
                break;
                
            case CONEXION_ACEPTADA:
            case SOLICITUD_DE_DESCONEXION:
            case PAQUETE_DE_ALCANZABILIDAD:
            default:
        }
    }
    
    /*public boolean nuevoVecino(String ip, String mascara, String as)
    {
        InetAddress ipV, maskV;
        NumeroAS num;
        try
        {
            ipV = InetAddress.getByName(ip);
            maskV = InetAddress.getByName(mascara);
            num = new NumeroAS(as);
        }
        catch (UnknownHostException e)
        {
            return false;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
        PaqueteVecino pv = new PaqueteVecino(
        return true;
    }*/
    
    public void nuevoVecino(PaqueteVecino pv, boolean manual, Socket s)
    {
        vecinos.addVecino(pv, manual);
        
        PaqueteVecino respuesta = new PaqueteVecino(Paquete_t.CONEXION_ACEPTADA, this.direccion, this.mascara, this.numAS);
        
        OutputStream output;
        try
        {
            output = s.getOutputStream();
            output.write(respuesta.getBytes());
        }
        catch (IOException e)
        {
            System.out.println("Error de comunicación.");
            return;
        }
    }
}

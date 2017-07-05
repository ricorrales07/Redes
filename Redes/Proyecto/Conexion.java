import java.net.*;
import java.io.*;

/**
 * Esta clase representa una conexión con un vecino.
 * Se mantiene hasta que alguno de los dos decida desconectarse del otro.
 * 
 * FALTA:   Mover código para procesar los distintos tipos de paquetes de la clase Servidor a esta clase.
 *          El hilo que corre aquí podría ser interrumpido por el hilo de la interfaz para enviar un 
 *              paquete de desconexión o para enviar info de alcanzabilidad. Leer sobre métodos interrupt()
 *              e interrupted() de la clase Thread para ver cómo manejar eso. Se necesita una estructura
 *              compartida de paso de mensajes (i.e. un objeto Integer que indique cuál es el propósito
 *              de la interrupción).
 *          Este hilo también podría ser interrumpido por otro hilo que se encarga de enviar info de
 *              alccanzabilidad cada 30 segundos.
 *              
 * OJO: recordar borrarse de la lista de hilos activos cuando se desconecta del vecino.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class Conexion implements Runnable
{
    private InetAddress direccion;
    private InetAddress mascara;
    private NumeroAS numAS;
    
    private Socket s;
    private InputStream input;
    private InputStream output;
    private InetAddress ipVecino;
    private InetAddress mascaraVecino;
    private NumeroAS asVecino;
    
    private TablaVecinos vecinos;
    private TablaAlcanzabilidad alcanzabilidad;
    
    // Para usar por vía manual.
    public Conexion(InetAddress ip)
    {
        // Nuevo socket, establecer conexión con vecino nuevo.
        // Inicializar input, output, tablas.
        // Solicitar conexión con vecino (método aparte).
        // Si no exitosa, tirar excepción.
    }
    
    // Para usar por vía no manual.
    public Conexion (Socket ss)
    {
        s = ss;
        try
        {
            input = s.getInputStream();
            output = s.getOutputStream();
            vecinos = TablaVecinos.getTabla();
            alcanzabilidad = TablaAlcanzabilidad.getTabla();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Imposible obtener tablas de vecinos y alcanzabilidad.");
        }
        
        // Procesar acá la solicitud de nuevo vecino.
    }
    
    public void run()
    {
        // Mover esto al contstructor no manual.
        try
        {
            procesarSolicitudDeNuevoVecino();
        }
        catch(IOException e)
        {
            synchronized(System.out)
            {
                System.out.println(e.getMessage());
            }
            return;
        }
        
        while (true)
        {
            try
            {
                Paquete_t tipo = Paquete_t.values()[input.read()];
                manejarPaquete(tipo);
            }
            catch(IOException e)
            {
                synchronized(System.out)
                {
                    System.out.println(e.getMessage());
                }
                return;
            }
        }
    }
    
    /*Para cuando llega la solicitud de otro router.*/
    private void procesarSolicitudDeNuevoVecino() throws IOException
    {
        // Lee el paquete.
        byte[] paquete = new byte[10];        
        try
        {
            input.read(paquete);
        }
        catch (IOException e)
        {
            throw new IOException("Error al recibir paquete.");
        }
        
        // Lo mete en un objeto para manejarlo maś fácil.
        PaqueteVecino pv = new PaqueteVecino(Paquete_t.SOLICITUD_DE_CONEXION, paquete);
        
        // Se agrega a la tabla de vecinos.
        vecinos.addVecino(pv, false);
        
        // Se agrega a la tabla de alcanzabilidad.
        Destino d = new Destino(pv.getIP(), pv.getMascara());
        d.addAS(pv.getAS());
        alcanzabilidad.addDestino(d, false, pv.getIP());
        
        // Armamos el paquete de confirmación.
        PaqueteVecino respuesta = new PaqueteVecino(Paquete_t.CONEXION_ACEPTADA, this.direccion, this.mascara, this.numAS);
        
        // Le enviamos el paquete al vecino.
        try
        {
            output.write(respuesta.getBytes());
        }
        catch (IOException e)
        {
            throw new IOException("No se pudo enviar confirmación a IP " + pv.getIP().getHostAddress() + ".");
        }
    }
    
    private boolean manejarPaquete(Paquete_t tipoPaquete) throws IOException
    {
        byte[] paquete;
        PaqueteVecino pv;
        PaqueteAlcanzabilidad pa;
        switch(tipoPaquete)
        {
            case SOLICITUD_DE_CONEXION:
                procesarSolicitudDeNuevoVecino();
                break;
            
            case SOLICITUD_DE_DESCONEXION:
                procesarSolicitudDeDesconexion();
                return false;
                
            case PAQUETE_DE_ALCANZABILIDAD:
                procesarPaqueteDeAlcanzabilidad();
                break;
                
            default:
        }
    }
}

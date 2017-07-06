import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

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
    private Socket s;
    private InputStream input;
    private OutputStream output;
    private InetAddress ipVecino;
    private InetAddress mascaraVecino;
    private NumeroAS asVecino;
    
    private TablaVecinos vecinos;
    private TablaAlcanzabilidad alcanzabilidad;
    
    public InetAddress getIPVecino()
    {
        return ipVecino;
    }
    
    public InetAddress getMascaraVecino()
    {
        return mascaraVecino;
    }
    
    // Para usar por vía manual.
    public Conexion(String ipV, String mascaraV) throws IOException, IllegalArgumentException // ¿Necesita la máscara?
    {
        try
        {
            ipVecino = InetAddress.getByName(ipV);
            mascaraVecino = InetAddress.getByName(mascaraV);
        }
        catch(UnknownHostException e)
        {
            throw new IllegalArgumentException("Dirección IP o máscara de red inválidas.");
        }
        
        // Nuevo socket, establecer conexión con vecino nuevo.
        try
        {
            s = new Socket(ipVecino, Router.PUERTO_ENTRADA);
            s.setSoTimeout(5000);
        }
        catch(IOException e)
        {
            throw new IOException("No se pudo establecer conexión con el vecino.");
        }
        
        // Inicializar input, output, tablas.
        try
        {
            input = s.getInputStream();
            output = s.getOutputStream();
            vecinos = TablaVecinos.getTabla();
            alcanzabilidad = TablaAlcanzabilidad.getTabla();
        }
        catch (IOException e)
        {
            throw new IOException("Imposible obtener tablas de vecinos y alcanzabilidad.");
        }
        
        // Solicitar conexión con vecino (método aparte).
        // Si no exitosa, tirar excepción.
        try
        {
            solicitarConexion();
        }
        catch(IOException e)
        {
            throw new IOException("No fue posible enviar paquete de solicitud de conexión.");
        }
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
			s.setSoTimeout(5000);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Imposible obtener tablas de vecinos y alcanzabilidad.");
        }
        
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
        
    }
    
    public void run()
    {
        while (true)
        {
            if(Thread.interrupted())
            {
                synchronized(Router.hilosActivos)
                {
                    Queue<Integer> q = Router.memoriaCompartida.get(ipVecino);
                    while(!q.isEmpty())
                    {
                        Integer comando = q.poll();
                        if (comando.equals(0))
                        {
                            try
                            {
                                cerrarConexion();
                                return;
                            }
                            catch (IOException e)
                            {
                                synchronized(System.out)
                                {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                        else
                        {
                            System.out.println("Enviando info alcanzabilidad a " + ipVecino.getHostAddress()); 
                            
                            PaqueteAlcanzabilidad paquete = new PaqueteAlcanzabilidad(Router.numASLocal);
                             for (Destino d : alcanzabilidad.getAllDestinos())
                                 paquete.addDestino(d);
                             
                             for (InetAddress ip : vecinos.getListaIPs())
                             {
                                 try
                                 {
                                     output.write(paquete.getBytes());
                                 }
                                 catch (IOException e)
                                 {
                                     System.out.println("Error al conectar con el vecino " + ip.getHostAddress() + ".");
                                     continue;
                                 }
                             }
                        }
                    }
                }
            }
            try
            {
                Paquete_t tipo = Paquete_t.values()[input.read()];
                if (!manejarPaquete(tipo))
                    return;
            }
            catch(IOException e)
            {
                synchronized(System.out)
                {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    
    private void solicitarConexion() throws IOException
    {
        // Armamos el paquete que vamos a enviar.
        PaqueteVecino paqueteParaEnviar = new PaqueteVecino(Paquete_t.SOLICITUD_DE_CONEXION, Router.ipLocal, Router.mascaraLocal, Router.numASLocal);
        
        // Le enviamos el mensaje al otro Router.
        output.write(paqueteParaEnviar.getBytes());
        
        // Esperamos 5 segundos por la respuesta.
        //s.setSoTimeout(5000);
        byte[] respuesta = new byte[11];
        input.read(respuesta);
        //s.setSoTimeout(0);
        
        PaqueteVecino pv = new PaqueteVecino(Paquete_t.CONEXION_ACEPTADA, Arrays.copyOfRange(respuesta, 1, 11));
        
        vecinos.addVecino(pv, true);
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
        
        ipVecino = pv.getIP();
        mascaraVecino = pv.getMascara();
        asVecino = pv.getAS();
        
        // Se agrega a la tabla de vecinos.
        vecinos.addVecino(pv, false);
        
        // Armamos el paquete de confirmación.
        PaqueteVecino respuesta = new PaqueteVecino(Paquete_t.CONEXION_ACEPTADA, Router.ipLocal, Router.mascaraLocal, Router.numASLocal);
        
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
    
    private void cerrarConexion() throws IOException
    {
        // Lo sacamos de la tabla, puesto que no importa si responde o no.
        vecinos.removeVecino(ipVecino);
        
        // Borramos todos los destinos que eran alcanzables a través de este vecino.
        // TODO: Implementar este método.
        TablaAlcanzabilidad.getTabla().removeAll(ipVecino);
        
        // Armamos el paquete que vamos a enviar.
        PaqueteVecino paqueteParaEnviar = new PaqueteVecino(Paquete_t.SOLICITUD_DE_DESCONEXION, Router.ipLocal, Router.mascaraLocal, Router.numASLocal);
        
        // Enviamos el mensaje.
        try
        {
            output.write(paqueteParaEnviar.getBytes());
        }
        catch (IOException e)
        {
            throw new IOException("No se pudo enviar la solicitud de cierre de conexión.");
        }
        
        // Esperamos 5 segundos por la respuesta.
        //s.setSoTimeout(5000);
        byte[] respuesta = new byte[11];
        try
        {
            input.read(respuesta);
            s.close();
        }
        catch (SocketTimeoutException e)
        {
            synchronized (System.out)
            {
                System.out.println("No se recibió confirmación de desconexión. Conexión cerrada de todas maneras.");
            }
        }
        //s.setSoTimeout(0);
    }
    
    private void procesarSolicitudDeDesconexion() throws IOException
    {
        // Lee el paquete.      
        try
        {
            input.skip(10);
        }
        catch (IOException e)
        {
            throw new IOException("Error al recibir paquete.");
        }
        
        // Armamos el paquete de confirmación.
        PaqueteVecino respuesta = new PaqueteVecino(Paquete_t.CONFIRMACION_DE_DESCONEXION, Router.ipLocal, Router.mascaraLocal, Router.numASLocal);
        
        // Le enviamos el paquete al vecino.
        try
        {
            output.write(respuesta.getBytes());
            s.close();
        }
        catch (IOException e)
        {
            synchronized(System.out)
            {
                System.out.println("No se pudo enviar confirmación de desconexión a IP " + respuesta.getIP().getHostAddress() + ".");
            }
        }
        
        // Se borra de la tabla de vecinos.
        vecinos.removeVecino(respuesta.getIP(), false, respuesta.getIP());
        
        // Borramos todos los destinos que eran alcanzables a través de este vecino.
        // TODO: Implementar este método.
        TablaAlcanzabilidad.getTabla().removeAll(ipVecino);
    }
    
    private void procesarPaqueteDeAlcanzabilidad() throws IOException
    {
        NumeroAS origen;
        byte[] ASorigen = new byte[2];
        byte[] numDestinos = new byte[4];
        
        input.read(ASorigen);
        input.read(numDestinos);
        try
        {
            origen = new NumeroAS(ASorigen);
        }
        catch(IllegalArgumentException e)
        {
            throw new RuntimeException("Error en el paquete de alcanzabilidad (no debería ocurrir).");
        }
        
        int nDestinos = ByteBuffer.wrap(numDestinos).getInt();
        
        PaqueteAlcanzabilidad pa = new PaqueteAlcanzabilidad(origen);
        
        for (int i = 0; i < nDestinos; i++)
        {
            byte[] headerDestino = new byte[8];
            input.read(headerDestino);
            Destino d = new Destino(headerDestino, ipVecino);
            
            byte[] cantAS = new byte[2];
            input.read(cantAS);
            short cAS = ByteBuffer.wrap(cantAS).getShort();
            
            Destino dAnterior = alcanzabilidad.getDestino(d.getIP());
            if(dAnterior != null && cAS + 1 > dAnterior.getLongRuta())
            {
                input.skip(cAS * 2);
                continue;
            }
            
            d.addAS(origen);
            
            for (int j = 0; j < cAS; j++)
            {
                byte[] as = new byte[2];
                input.read(as);
                d.addAS(new NumeroAS(as));
            }
            
            pa.addDestino(d);
        }
        
        List<Destino> nuevosDestinos = pa.getListaDestinos();
        
        for (Destino d : nuevosDestinos)
            alcanzabilidad.addDestino(d, false, ipVecino);
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
                return true;
            
            case SOLICITUD_DE_DESCONEXION:
                procesarSolicitudDeDesconexion();
                return false;
                
            case PAQUETE_DE_ALCANZABILIDAD:
                System.out.println("Recibida info de alcanzabilidad de " + ipVecino.getHostAddress());
                procesarPaqueteDeAlcanzabilidad();
                return true;
                
            default:
                return true;
        }
    }
}

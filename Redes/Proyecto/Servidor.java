import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Write a description of class Servidor here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Servidor implements ManejadorDePaquetes, Runnable
{
    private final InetAddress direccion;
    private final InetAddress mascara; //todavía no estoy seguro de para qué vamos a usar la máscara...
    private final NumeroAS numAS;
    
    private TablaVecinos vecinos;
    private TablaAlcanzabilidad alcanzabilidad;
    
    private Semaphore sRespuestaConexion;
    private Semaphore sRespuestaDesconexion;
    private InetAddress esperando;

    /**
     * Constructor for objects of class Servidor
     */
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
        alcanzabilidad = TablaAlcanzabilidad.getTabla();
        
        sRespuestaConexion = new Semaphore(0);
        sRespuestaDesconexion = new Semaphore(0);
    }
    
    public void run()
    {
        while (true)
        {
            PaqueteAlcanzabilidad paquete = new PaqueteAlcanzabilidad(this.numAS);
            for (Destino d : alcanzabilidad.getAllDestinos())
                paquete.addDestino(d);
            
            for (InetAddress ip : vecinos.getListaIPs())
            {
                try
                {
                    Socket s = new Socket(ip, Router.PUERTO_ENTRADA);
                    OutputStream output = s.getOutputStream();
     
                    output.write(paquete.getBytes());
                    s.close();
                }
                catch (IOException e)
                {
                    System.out.println("Error al conectar con el vecino " + ip.getHostAddress() + ".");
                    continue;
                }
            }
            
            try
            {
                Thread.sleep(30000);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException("Error: El hilo de envío de información de alcanzabilidad fue interrumpido.");
            }
        }
    }
    
    public void maneja(Paquete_t tipoPaquete, Socket s, InputStream input)
    {
        byte[] paquete;
        PaqueteVecino pv;
        PaqueteAlcanzabilidad pa;
        switch(tipoPaquete)
        {
            case SOLICITUD_DE_CONEXION:
                paquete = new byte[10];
                
                try
                {
                    input.read(paquete);
                    s.close();
                }
                catch (IOException e)
                {
                    System.out.println("Error al recibir paquete.");
                    return;
                }
                
                pv = new PaqueteVecino(tipoPaquete, paquete);
                
                procesarSolicitudDeNuevoVecino(pv);
                
                break;
                
            case CONEXION_ACEPTADA:
                paquete = new byte[10];
                
                try
                {
                    input.read(paquete);
                    s.close();
                }
                catch (IOException e)
                {
                    System.out.println("Error al recibir paquete.");
                    return;
                }
                
                pv = new PaqueteVecino(tipoPaquete, paquete);
                
                if(sRespuestaConexion.hasQueuedThreads() && esperando.equals(pv.getIP()))
                {
                    sRespuestaConexion.release();
                    vecinos.addVecino(pv, true);
                }
                    
                break;
            
            case SOLICITUD_DE_DESCONEXION:
                paquete = new byte[10];
                
                try
                {
                    input.read(paquete);
                    s.close();
                }
                catch (IOException e)
                {
                    System.out.println("Error al recibir paquete.");
                    return;
                }
                
                pv = new PaqueteVecino(tipoPaquete, paquete);
                
                procesarSolicitudDeDesconexion(pv);
                
                break;
                
            case CONFIRMACION_DE_DESCONEXION:
                paquete = new byte[10];
                
                try
                {
                    input.read(paquete);
                    s.close();
                }
                catch (IOException e)
                {
                    System.out.println("Error al recibir paquete.");
                    return;
                }
                
                pv = new PaqueteVecino(tipoPaquete, paquete);
                
                if(sRespuestaDesconexion.hasQueuedThreads() && esperando.equals(pv.getIP()))
                {
                    sRespuestaDesconexion.release();
                }
                    
                break;
                
            case PAQUETE_DE_ALCANZABILIDAD:
                try
                {
                    procesarPaqueteDeAlcanzabilidad(input);
                    s.close();
                }
                catch (IOException e)
                {
                    System.out.println ("Error al recibir paquete.");
                }
            
                break;
                
            default:
        }
    }
    
    /*Para usar desde la interfaz*/
    public boolean solicitarNuevoVecino(String ip, String mascara) throws IllegalArgumentException, IOException // ¿Para qué ocupa la máscara?
    {
        // Chequeamos entradas.
        InetAddress ipV, maskV;
        try
        {
            ipV = InetAddress.getByName(ip);
            maskV = InetAddress.getByName(mascara);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException("Dirección IP inválida.");
        }
        
        // Armamos el paquete que vamos a enviar.
        PaqueteVecino paqueteParaEnviar = new PaqueteVecino(Paquete_t.SOLICITUD_DE_CONEXION, this.direccion, this.mascara, this.numAS);
        
        // Establecemos conexión con el otro router y enviamos el mensaje.
        Socket conexion;
        OutputStream output;
        try
        {
            conexion = new Socket(ipV, Router.PUERTO_ENTRADA);
            output = conexion.getOutputStream();
            output.write(paqueteParaEnviar.getBytes());
            conexion.close();
        }
        catch (IOException e)
        {
            throw new IOException("No se pudo establecer la conexión con la dirección IP provista");
        }
        
        // Esperamos 5 segundos por la respuesta.
        boolean respuesta = false;
        try
        {
            esperando = ipV;
            respuesta = sRespuestaConexion.tryAcquire(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("El hilo correspondiente a la interfaz gráfica fue interrumpido (esto no debería ocurrir).");
        }
        
        // Le avisamos al usuario si llegó una respuesta o no.
        return respuesta;
    }
    
    /*Para cuando llega la solicitud de otro router.*/
    private void procesarSolicitudDeNuevoVecino(PaqueteVecino pv)
    {
        // Se agrega a la tabla de vecinos.
        vecinos.addVecino(pv, false);
        
        // Se agrega a la tabla de alcanzabilidad.
        Destino d = new Destino(pv.getIP(), pv.getMascara());
        d.addAS(pv.getAS());
        alcanzabilidad.addDestino(d, false, pv.getIP());
        
        // Armamos el paquete de confirmación.
        PaqueteVecino respuesta = new PaqueteVecino(Paquete_t.CONEXION_ACEPTADA, this.direccion, this.mascara, this.numAS);
        
        // Nos conectamos con el vecino nuevo y le enviamos el paquete.
        Socket s;
        OutputStream output;
        try
        {
            s = new Socket(pv.getIP(), Router.PUERTO_ENTRADA);
            output = s.getOutputStream();
            output.write(respuesta.getBytes());
            s.close();
        }
        catch (IOException e)
        {
            System.out.println("No se pudo enviar confirmación a IP " + pv.getIP().getHostAddress() + ".");
            return;
        }
        //System.out.println("Solicitud de vecino confirmada a " + pv.getIP());
    }
    
    public String desplegarTablaDeVecinos()
    {
        return vecinos.toString();
    }
    
    public boolean solicitarDesconexion(String ipVecino) throws IllegalArgumentException, IOException
    {
        // Chequeamos la entradaa.
        InetAddress ipV;
        try
        {
            ipV = InetAddress.getByName(ipVecino);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException("Dirección IP inválida.");
        }
        
        // Lo sacamos de la tabla, puesto que no importa si responde o no.
        vecinos.removeVecino(ipV);
        
        // Lo eliminamos de la tabla de alcanzabilidad.
        alcanzabilidad.removeDestino(ipV);
        
        // TODO: También hay que eliminar todas las entradas en la tabla de alcanzabilidad que comiencen por este sistema autónomo (tal vez?).
        // Problema: No sé si estoy conectado a más de un router en el mismo sistema autónomo. ¿Y si los destinos siguen siendo alcanzables a través del otro?
        // Solución posible: eliminar estas entradas solo si ya no quedan vecinos de ese sistema autónomo?
        
        // Armamos el paquete que vamos a enviar.
        PaqueteVecino paqueteParaEnviar = new PaqueteVecino(Paquete_t.SOLICITUD_DE_DESCONEXION, this.direccion, this.mascara, this.numAS);
        
        // Establecemos conexión con el otro router y enviamos el mensaje.
        Socket conexion;
        OutputStream output;
        try
        {
            conexion = new Socket(ipV, Router.PUERTO_ENTRADA);
            output = conexion.getOutputStream();
            output.write(paqueteParaEnviar.getBytes());
            conexion.close();
        }
        catch (IOException e)
        {
            throw new IOException("No se pudo establecer la conexión con la dirección IP provista");
        }
        
        // Esperamos 5 segundos por la respuesta.
        boolean respuesta = false;
        try
        {
            esperando = ipV;
            respuesta = sRespuestaDesconexion.tryAcquire(5, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("El hilo correspondiente a la interfaz gráfica fue interrumpido (esto no debería ocurrir).");
        }
        
        // Le avisamos al usuario si llegó una respuesta o no.
        return respuesta;
    }
    
    /*Para cuando llega la solicitud de otro router.*/
    private void procesarSolicitudDeDesconexion(PaqueteVecino pv)
    {
        // Armamos el paquete de confirmación.
        PaqueteVecino respuesta = new PaqueteVecino(Paquete_t.CONFIRMACION_DE_DESCONEXION, this.direccion, this.mascara, this.numAS);
        
        // Nos conectamos con el vecino nuevo y le enviamos el paquete.
        Socket s;
        OutputStream output;
        try
        {
            s = new Socket(pv.getIP(), Router.PUERTO_ENTRADA);
            output = s.getOutputStream();
            output.write(respuesta.getBytes());
            s.close();
        }
        catch (IOException e)
        {
            System.out.println("No se pudo enviar confirmación a IP " + pv.getIP().getHostAddress() + ".");
            return;
        }
        
        // Se borra de la tabla de vecinos.
        vecinos.removeVecino(pv.getIP(), false, pv.getIP());
        
        // Lo eliminamos de la tabla de alcanzabilidad.
        alcanzabilidad.removeDestino(pv.getIP());
        
        // TODO: También hay que eliminar todas las entradas en la tabla de alcanzabilidad que comiencen por este sistema autónomo (tal vez?).
        // Problema: No sé si estoy conectado a más de un router en el mismo sistema autónomo. ¿Y si los destinos siguen siendo alcanzables a través del otro?
        // Solución posible: eliminar estas entradas solo si ya no quedan vecinos de ese sistema autónomo?
    }
    
    private void procesarPaqueteDeAlcanzabilidad(InputStream input) throws IOException
    {
        // TODO: Debería ignorarlo si no viene de un vecino, pero hace falta el IP para saber si lo es o no...
        
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
            throw new IOException();
        }
        
        int nDestinos = ByteBuffer.wrap(numDestinos).getInt();
        
        PaqueteAlcanzabilidad pa = new PaqueteAlcanzabilidad(origen);
        
        for (int i = 0; i < nDestinos; i++)
        {
            byte[] headerDestino = new byte[8];
            input.read(headerDestino);
            Destino d = new Destino(headerDestino);
            
            byte[] cantAS = new byte[2];
            input.read(cantAS);
            short cAS = ByteBuffer.wrap(cantAS).getShort();
            
            Destino dAnterior = alcanzabilidad.getDestino(d.getIP());
            if(cAS + 1 > dAnterior.getLongRuta())
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
        
        
        // ¿De dónde sacamos la dirección IP de la que proviene el paquete?
        for (Destino d : nuevosDestinos)
            alcanzabilidad.addDestino(d, false, /*origen*/ InetAddress.getLoopbackAddress());
    }
    
    public void agregarNuevoDestino(String info) throws IllegalArgumentException
    {
        String[] params = info.split(" ");
        
        InetAddress ipDestino;
        InetAddress mascaraDestino;
        try
        {
             ipDestino = InetAddress.getByName(params[0]);
             mascaraDestino = InetAddress.getByName(params[1]);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException("Dirección IP de destino inválida.");
        }
        
        Destino d = new Destino(ipDestino, mascaraDestino);
        
        for (int i = 2; i < params.length; i++)
            d.addAS(new NumeroAS(params[i]));
            
        alcanzabilidad.addDestino(d);
    }
    
    public String desplegarTablaAlcanzabilidad()
    {
        return alcanzabilidad.toString();
    }
}

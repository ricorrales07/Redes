import java.net.*;
import java.io.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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
    private TablaAlcanzabilidad alcanzabilidad;

    private Semaphore sRespuesta;
    private InetAddress esperando;

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

        sRespuesta = new Semaphore(0);
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

                if(sRespuesta.hasQueuedThreads() && esperando.equals(pv.getIP()))
                {
                    sRespuesta.release();
                    vecinos.addVecino(pv, true);
                }

                break;

            case SOLICITUD_DE_DESCONEXION:
            case PAQUETE_DE_ALCANZABILIDAD:
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
            respuesta = sRespuesta.tryAcquire(5, TimeUnit.SECONDS);
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

    public TablaVecinos getTablaVecinos(){
        return vecinos;
    }
}

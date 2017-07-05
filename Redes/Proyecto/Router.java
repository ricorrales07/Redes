import java.net.*;
import java.lang.Thread;
import java.io.*;
import java.util.Queue; 
import java.util.Hashtable;
// <>

/**
 * Contiene el hilo principal, que escucha conexiones nuevas y
 * crea hilos hijos para manejarlas.
 * 
 * FALTA:   Cada vez que se crea un hilo hijo, hay que agregarlo
 *              a la lista de hilos activos en HiloAlcanzabilidad.
 *          Probablemente hay que cambiar algunas cosas durante la
 *              inicialización.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Router
{
    /** Todavía no estoy seguro de si hay que quitar esta constante de acá. **/
    // constante
    public static final int PUERTO_ENTRADA = 57809;
    
	public static InetAddress ipLocal;
	public static InetAddress mascaraLocal;
	public static NumeroAS numASLocal;
    
    private static ServerSocket sSocket;
    private static InterfazDeOperador interfaz;
    
    public static Hashtable<InetAddress,Queue<Integer>> memoriaCompartida;
    public static Hashtable<InetAddress, Thread> hilosActivos;
    
    public static Thread hiloAlcanzabilidad;
   
    
    public static int main()
    {
        interfaz = new InterfazDeOperador();
        
        try
        {
            System.out.println("Iniciando router...");
            sSocket = new ServerSocket(PUERTO_ENTRADA);
        }
        catch (IOException e)
        {
            System.out.println("ERROR: No se pudo obtener el puerto " + PUERTO_ENTRADA + ". Abortando...");
            return 1;
        }
        
        while (ipLocal == null || mascaraLocal == null || numASLocal == null )
        {
            try
            {
                String[] s = interfaz.inicializar();
                ipLocal = InetAddress.getByName(s[0]);
                mascaraLocal = InetAddress.getByName(s[1]);
                numASLocal = new NumeroAS(s[2]);
            }
            catch (Exception e)
            {
                System.out.println(e.toString());
                System.out.println("Inténtelo de nuevo: ");
            }
        }
        
        interfaz.inicializarDestinos();
        
        // Un hilo para atender la interfaz de usuario.
        interfaz = new InterfazDeOperador();
        Thread i = new Thread(interfaz);
        i.start();
        
        // Un hilo para enviar información de alcanzabilidad.
        hiloAlcanzabilidad = new Thread(new HiloAlcanzabilidad());
        hiloAlcanzabilidad.start();
        
        while (true)
        {
            Socket ss;
            InputStream input;
            Paquete_t tipo;
            try
            {
                ss = sSocket.accept();
                input = ss.getInputStream();
                tipo = Paquete_t.values()[input.read()];
            }
            catch(IOException e)
            {
                System.out.println("Error en la conexión.");
                continue;
            }
            
            if (tipo == Paquete_t.SOLICITUD_DE_CONEXION)
            {
                Thread t = new Thread(new Conexion(ss));
                t.start();
            }
        }
    }
    
    // Para concatenar arreglos de bytes.
    public static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            for (byte element : array) {
                result[pos] = element;
                pos++;
            }
        }
        return result;
    }
    
    public static void agregarNuevoDestino(String ipD, String mascaraD) throws IllegalArgumentException
    {
     
        InetAddress ipDestino;
        InetAddress mascaraDestino;
        try
        {
             ipDestino = InetAddress.getByName(ipD);
             mascaraDestino = InetAddress.getByName(mascaraD);
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
}

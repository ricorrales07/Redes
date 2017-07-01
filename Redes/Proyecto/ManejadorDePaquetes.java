import java.io.InputStream;
import java.net.Socket;

/**
 * Write a description of interface ManejadorDePaquetes here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public interface ManejadorDePaquetes
{
    public void maneja(Paquete_t tipoDePaquete, Socket s, InputStream input);
}

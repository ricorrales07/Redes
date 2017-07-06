import java.util.Hashtable;
import java.net.*;
import java.util.Queue;

/**
 * Esta clase se encarga de transmitir la información de alcanzabilidad cada
 *      30 segundos. Para eso, tiene que interrumpir a todos los demás hilos
 *      para que cada uno envíe su info de alcanzabilidad. Necesita escribir
 *      algo en alguna estructura de datos de las conexiones, para
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class HiloAlcanzabilidad implements Runnable
{
    /** 
     * Tabla de hilos activos. Cada 30 segundos se interrumpen todos para
     * enviar información de alcanzabilidad. Hace falta un método para
     * agregar hilos a la tabla.
     * 
     * Creo que también hace falta el objeto Conexion correspondiente...
     */ 
      
    public void run()
    {
       while(true){
           try
            {
                Thread.sleep(30000);
            }
            catch (InterruptedException e)
            {
                
            }
           enviar();
           if(Thread.interrupted()){
               enviar();
             
           }
       }
    }
    
    private void enviar(){
      synchronized(Router.hilosActivos)
        {
            System.out.println("Enviando info alcanzabilidad");
            for(Queue cola : Router.memoriaCompartida.values()){
                cola.add(5);       
            }
                for(Thread hiloActivo : Router.hilosActivos.values())
                {
                   hiloActivo.interrupt();
                } 
        }
    }
}

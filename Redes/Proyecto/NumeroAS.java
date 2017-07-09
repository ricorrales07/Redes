import java.lang.Byte;

/**
 * Write a description of class NumeroAS here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class NumeroAS
{
    private String legible;
    private byte[] as;
    
    public NumeroAS(String num) throws IllegalArgumentException
    {
        as = new byte[2];
         
        String[] decimales = num.split("\\.");
        if (decimales.length != 2)
              throw new IllegalArgumentException("Dirección de Sistema Autónomo inválida. Debe llevar 2 bytes.");
        else
        {
            int[] decimales_int = new int[2];
            for (int i = 0; i < 2; i++)
            {
                try
                {
                    decimales_int[i] = Integer.parseInt(decimales[i]);
                }
                catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException("Dirección de Sistema Autónomo inválida. " + decimales[i] + " no es un número.");
                }
                if (decimales_int[i] < 0 || decimales_int[i] > 255)
                {
                    throw new IllegalArgumentException("Dirección de Sistema Autónomo inválida. " + decimales[i] 
                        + " no corresponde a un valor válido para un byte.");
                }
                else
                {
                    as[i] = (byte) decimales_int[i];
                    legible = num;
                }
            }
        }
    }
    
    public NumeroAS(byte[] bytes) throws IllegalArgumentException
    {
        if (bytes.length != 2)
        {
            throw new IllegalArgumentException("Cantidad inválida de bytes en número de Sistema Autónomo.");
        }
        else
        {
            as = bytes;
            legible = Byte.toUnsignedInt(bytes[0]) + "." + Byte.toUnsignedInt(bytes[1]);
        }
    }
    
    public String toString()
    {
        return legible;
    }
    
    public byte[] getBytes()
    {
        return as;
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
        NumeroAS otroAS = (NumeroAS) obj;
        return this.as[0] == otroAS.as[0] && this.as[1] == otroAS.as[1];
    } 
        
    @Override 
    public int hashCode() 
    { 
        return (int)as[0] * 256 + (int)as[1];
    }
}

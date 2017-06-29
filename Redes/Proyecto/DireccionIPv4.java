
// <>

/**
 * Write a description of class DireccionIPv4 here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class DireccionIPv4
{
    private String decimalConPunto;
    private byte[] bytesBinarios;

    /**
     * Construye un objeto DireccionIPv4 a partir de una representación en String como "10.1.101.42".
     * Tira excepción si el formato no es correcto.
     */
    public DireccionIPv4(String decimal) throws IllegalArgumentException
    {
        decimalConPunto = decimal; //esto está bien?
        bytesBinarios = decimalConPuntoABinario(decimal);
    }
    
    /**
     * Construye un objeto DireccionIPv4 a partir de cuatro bytes.
     * Tira excepción si no son 4.
     */
    public DireccionIPv4(byte[] bytes) throws IllegalArgumentException
    {
        bytesBinarios = bytes;
        decimalConPunto = binarioADecimalConPunto(bytes);
    }
    
    public String toString()
    {
        return decimalConPunto;
    }
    
    /**
     * Retorna un nuevo objeto DireccionIPv4 que es el resultado de aplicarle la máscara al objeto actual.
     */
    public DireccionIPv4 enmascarar(DireccionIPv4 mascara)
    {
        byte[] temp = new byte[4];
        for (int i = 0; i < 4; i++)
        {
            temp[i] = (byte) (this.bytesBinarios[i] & mascara.bytesBinarios[i]);
        }
        return new DireccionIPv4(temp);
    }

    private static String binarioADecimalConPunto(byte[] bytes) throws IllegalArgumentException
    {
        if (bytes.length != 4)
            throw new IllegalArgumentException("Dirección IP inválida. Debe llevar 4 bytes.");
        
        String[] temp_strings = new String[4];
        for (int i = 0; i < 4; i++)
        {
            Integer temp = bytes[i] & 0xFF;
            temp_strings[i] = temp.toString();
        }
        return String.join(".", temp_strings);
    }
    
    private static byte[] decimalConPuntoABinario(String decimal) throws IllegalArgumentException
    {
        byte[] answer = new byte[4];
        
        String[] decimales = decimal.split(".");
        if (decimales.length != 4)
            throw new IllegalArgumentException("Dirección IP inválida. Debe llevar 4 bytes.");
        else
        {
            int[] decimales_int = new int[4];
            for (int i = 0; i < 4; i++)
            {
                try
                {
                    decimales_int[i] = Integer.parseInt(decimales[i]);
                }
                catch (NumberFormatException e)
                {
                    throw new IllegalArgumentException("Dirección IP inválida. " + decimales[i] + " no es un número.");
                }
                if (decimales_int[i] < 0 || decimales_int[i] > 255)
                {
                    throw new IllegalArgumentException("Dirección IP inválida. " + decimales[i] 
                        + " no corresponde a un valor válido para un byte.");
                }
                else
                {
                    answer[i] = (byte) decimales_int[i];
                }
            }
        }
        
        return answer;
    }
}

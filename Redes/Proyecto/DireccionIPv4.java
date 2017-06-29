
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
     * Constructor for objects of class DireccionIPv4
     */
    public DireccionIPv4(String decimal)
    {
        decimalConPunto = decimal;
        //bytesBinarios = 
    }
    
    public toString()
    {
        return decimalConPunto;
    }

    /*private static decimalToBinary(String decimal) throws IllegalArgumentException
    {
        String[] decimales = decimal.split(".");
        if (decimales.length != 4)
            throw new IllegalArgumentException("Dirección IP inválida. Debe llevar 4 bytes.");
        else
        {
            
            for (String str : decimales)
            {
                
            }
        }
    }*/
}

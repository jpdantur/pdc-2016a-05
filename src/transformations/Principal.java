package transformations;
import javaxt.io.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.Scanner;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * Created by wally on 5/25/16.
 */
public class Principal {
    public static void main (String [] args) {

        try {
            //System.out.println(new File("").getAbsolutePath());
            String codifiedImage = new Scanner(new File("textoprueba.txt")).useDelimiter("\\A").next();
            System.out.println(invertPic(codifiedImage, "PNG"));
        }
        catch (FileNotFoundException e)
        {
            System.out.println("No lo encontro");
        }

    }
    public static String invertPic(String codifiedImage, String format)
    {
        Decoder deco = Base64.getMimeDecoder();
        Encoder enco = Base64.getMimeEncoder();
        Image foto = new Image(deco.decode(codifiedImage));
        foto.rotate(180);
        return new String(enco.encode(foto.getByteArray(format)));
    }
}

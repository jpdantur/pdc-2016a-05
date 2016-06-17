package administrator;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
/**
 * Created by matias on 6/5/16.
 */

//link de refenrencia: http://www.mkyong.com/java/jaxb-hello-world-example/
public class JAXBParser {

    public static XMLConfiguration getConfiguration() {
         XMLConfiguration c = null;

        try {
            File file = new File("./Configuration.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLConfiguration.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            c = (XMLConfiguration) jaxbUnmarshaller.unmarshal(file);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return c;
    }


    public static void insertConfiguration(XMLConfiguration xmlc) {
        try {

            File file = new File("./Configuration.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(XMLMultiplex.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(xmlc, file);
            jaxbMarshaller.marshal(xmlc, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
package isis.ISISCapitalist.classes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class Services {

    void saveWorldToXml(World world, String pseudo) {
        if (!(Objects.isNull(pseudo) || (pseudo.isBlank()))) {
            JAXBContext jaxbContext;
            try {
                jaxbContext = JAXBContext.newInstance(World.class);
                Marshaller march = jaxbContext.createMarshaller();
                File file = new File("src/main/resources/" + pseudo + "-world.xml");
                file.createNewFile();
                OutputStream output = new FileOutputStream(file);
                march.marshal(world, output);
            } catch (Exception ex) {
                System.out.println("Erreur écriture du fichier:" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public World readWorldFromXml(String pseudo) {

        System.out.println(pseudo.getClass());
        World world = new World();
        InputStream input;

        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // Pointeur sur world.xml
            if ((Objects.isNull(pseudo)) || (pseudo.isBlank()) || (pseudo.equals("null"))) {
                input = getClass().getClassLoader().getResourceAsStream("world.xml");
                System.out.println("Null : " + input);
            } else {
                input = getClass().getClassLoader().getResourceAsStream(pseudo + "-world.xml");
            }
            System.out.println("Final : " + input);
            world = (World) jaxbUnmarshaller.unmarshal(input);

        } catch (Exception ex) {
            System.out.println("Erreur lecture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }

        // Return
        return world;
    }

    public World getWorld(String pseudo) {
        return readWorldFromXml(pseudo);
    }

}

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
            input.close();

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

    
    public Boolean updateProduct(String username, ProductType newProduct) {
        // Aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        ProductType oldProduct = new ProductType();
        // trouver dans ce monde, le produit équivalent à celui passé
        // en paramètre
        for (ProductType product : world.getProducts().getProduct()) {
            if (product.getId() == newProduct.getId()) {
                oldProduct = product;
                break;
            }
        };
        if (oldProduct == null) {
            return false;
        }
        // calculer la variation de quantité. Si elle est positive c'est
        // que le joueur a acheté une certaine quantité de ce produit
        // sinon c’est qu’il s’agit d’un lancement de production.
        int qteChange = newProduct.getQuantite() - oldProduct.getQuantite();
        if (qteChange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité achetée
            double un = newProduct.getCout() * Math.pow(oldProduct.getCroissance(), oldProduct.getQuantite());
            double numerator = 1 - Math.pow(oldProduct.getCroissance(), qteChange);
            double denominator = 1 - oldProduct.getCroissance();
            double quantityCost = (un * numerator) / denominator;
            
            double newMoney = world.getMoney() - quantityCost;
            world.setMoney(newMoney);
            // et mettre à jour la quantité de product
            oldProduct.setQuantite(newProduct.getQuantite());
        } else {
            // initialiser product.timeleft à product.vitesse
            oldProduct.setTimeleft(oldProduct.getVitesse());
            // pour lancer la production
            
        }
        // sauvegarder les changements du monde
        saveWorldToXml(world, username);
        return true;
    }

}

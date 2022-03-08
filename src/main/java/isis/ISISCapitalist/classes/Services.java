package isis.ISISCapitalist.classes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class Services {

    // Sauvegarde d'un monde
    void saveWorldToXml(World world, String pseudo) {
        // On vérifie que le pseudo n'est pas null ou vide
        if (!(Objects.isNull(pseudo) || (pseudo.isBlank()))) {
            JAXBContext jaxbContext;
            try {
                jaxbContext = JAXBContext.newInstance(World.class);
                Marshaller march = jaxbContext.createMarshaller();

                // On spécifie le chemin du fichier
                File file = new File("src/main/resources/" + pseudo + "-world.xml");

                // On crée le fichier s'il n'existe pas déjà
                file.createNewFile();

                // On déplace le contenu XML dans le fichier de sauvegarde
                OutputStream output = new FileOutputStream(file);

                //OutputStream output = Files.newOutputStream(Paths.get("src/main/resources/" + pseudo + "-world.xml"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                march.marshal(world, output);

                System.out.println("Saveing : " + getProduct(world, 2).getQuantite());

                // On ferme le OutputStream
                output.close();

            } catch (Exception ex) {
                System.out.println("Erreur écriture du fichier:" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // Lecture d'une sauvegarde d'un monde
    public World readWorldFromXml(String pseudo) {
        World world = new World();

        try {
            // Variables
            InputStream input;
            File file;
            JAXBContext jaxbContext;

            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // On vérifie que le pseudo n'est pas null ou vide
            if ((Objects.isNull(pseudo)) || (pseudo.isBlank()) || (pseudo.equals("null"))) { // ----> Déplacer vers WebService ou getWorld()
                // On récupère le fichier de sauvegarde de base
                input = getClass().getClassLoader().getResourceAsStream("world.xml");
            } else {
                // On récupère le fichier de sauvegarde en fonction du pseudo
                //input = getClass().getClassLoader().getResourceAsStream(pseudo + "-world.xml");
                input = new FileInputStream("src/main/resources/" + pseudo + "-world.xml");
            }

            // On lit la sauvegarde
            world = (World) jaxbUnmarshaller.unmarshal(input);
            System.out.println("Service : " + getProduct(world, 2).getQuantite());
            
            // On ferme le InputStream
            input.close();

        } catch (Exception ex) {
            System.out.println("Erreur lecture du fichier:" + ex.getMessage());
            ex.printStackTrace();
        }

        // Retour du monde
        return world;
    }

    // Retourne un monde
    public World getWorld(String pseudo) {
        return readWorldFromXml(pseudo);
    }

    // Actualise un produit
    public Boolean updateProduct(String username, ProductType newProduct) {
        // Récupération du monde
        World world = getWorld(username);

        // Récupération du produit à actualiser
        ProductType oldProduct = getProduct(world, newProduct.getId());

        // Si le produit récupéré est null
        if (oldProduct == null) {
            return false;
        }

        // On détecte si la quantité de produit a évolué
        int qteChange = newProduct.getQuantite() - oldProduct.getQuantite();

        // Si cette quantité a changé, on répercute les changements dans le monde
        if (qteChange > 0) {
            System.out.println("La quantité a changé");
            // Calcul et modification de la quantité achetée
            double un = newProduct.getCout() * Math.pow(oldProduct.getCroissance(), oldProduct.getQuantite());
            double numerator = 1 - Math.pow(oldProduct.getCroissance(), qteChange);
            double denominator = 1 - oldProduct.getCroissance();
            double quantityCost = (un * numerator) / denominator;

            System.out.println(oldProduct.getQuantite());
            oldProduct.setQuantite(newProduct.getQuantite());
            System.out.println(oldProduct.getQuantite());

            // Modification du score du monde
            double newMoney = world.getMoney() - quantityCost;
            world.setMoney(newMoney);

        } // Si cette quantité n'a pas changé, on active la production
        else {
            // Initialisation du temps de production
            oldProduct.setTimeleft(oldProduct.getVitesse());
        }

        // Sauvegarde des changements dans le monde
        saveWorldToXml(world, username);
        return true;
    }

    // Retourne un produit à partir de son id
    public ProductType getProduct(World world, int idProduct) {
        // Variable
        ProductType product = null;

        // On vérifie toute la liste des produits
        for (ProductType p : world.getProducts().getProduct()) {
            if (p.getId() == idProduct) {
                product = p;
                return product;
            }
        };

        // Retour du produit
        return product;
    }

}

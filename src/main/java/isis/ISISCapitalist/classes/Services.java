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

            JAXBContext jaxbContext;

            jaxbContext = JAXBContext.newInstance(World.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            // On vérifie que le pseudo n'est pas null ou vide
            if ((Objects.isNull(pseudo)) || (pseudo.isBlank()) || (pseudo.equals("null"))) { // ----> Déplacer vers WebService ou getWorld()
                // On récupère le fichier de sauvegarde de base
                input = getClass().getClassLoader().getResourceAsStream("world.xml");
            } else {
                File file = new File("src/main/resources/" + pseudo + "-world.xml");
                // On vérifie que le fichier existe bien
                if (file.isFile()) {
                    // On récupère le fichier de sauvegarde en fonction du pseudo
                    //input = getClass().getClassLoader().getResourceAsStream(pseudo + "-world.xml");
                    input = new FileInputStream("src/main/resources/" + pseudo + "-world.xml");
                } else {
                    input = getClass().getClassLoader().getResourceAsStream("world.xml");
                    world = (World) jaxbUnmarshaller.unmarshal(input);
                    saveWorldToXml(world, pseudo);

                }

            }

            // On lit la sauvegarde
            world = (World) jaxbUnmarshaller.unmarshal(input);

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
        // Récupération du monde
        World world = readWorldFromXml(pseudo);
        long timePassed = System.currentTimeMillis() - world.getLastupdate();

        // Calcul du score
        for (ProductType product : world.getProducts().getProduct()) {
            // Cas où le produit a un manager
            if (product.isManagerUnlocked()) {
                long numberProducted = Math.floorDiv(timePassed, product.getVitesse());
                double moneyProduced = numberProducted * product.getQuantite() * product.getRevenu();
                world.setMoney(world.getMoney() + moneyProduced);
            }
            /*
            // Cas où le produit n'a pas de manager
            if ((product.isManagerUnlocked() == false) && (product.getTimeleft() < timePassed) && (0 < product.getTimeleft())) {
                double moneyProduced = product.getQuantite() * product.getRevenu();
                world.setMoney(world.getMoney() + moneyProduced);
                product.setTimeleft(0);
                System.out.println("==> PRODUCTION " + product.getName() + " " + moneyProduced);
            }
             */
            if ((product.isManagerUnlocked() == false) && (product.getTimeleft() != 0)) {
                product.setTimeleft(product.getTimeleft() - timePassed);

                if (product.getTimeleft() <= 0) {
                    double moneyProduced = product.getQuantite() * product.getRevenu();
                    world.setMoney(world.getMoney() + moneyProduced);
                    product.setTimeleft(0);
                }
            }
        }

        // Actualisation de la dernière sauvegarde
        world.setLastupdate(System.currentTimeMillis());

        // Sauvegarde du monde
        saveWorldToXml(world, pseudo);
        return world;
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
            System.out.println("Ajout " + oldProduct.getName() + " +" + qteChange);
            // Calcul et modification de la quantité achetée
            double un = newProduct.getCout() * Math.pow(oldProduct.getCroissance(), oldProduct.getQuantite());
            double numerator = 1 - Math.pow(oldProduct.getCroissance(), qteChange);
            double denominator = 1 - oldProduct.getCroissance();
            double quantityCost = (un * numerator) / denominator;

            oldProduct.setQuantite(newProduct.getQuantite());

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

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newManager) {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        // trouver dans ce monde, le manager équivalent à celui passé
        // en paramètre
        PallierType manager = getManager(world, newManager.getName());
        if (manager == null) {
            return false;
        }
        // débloquer ce manager
        manager.setUnlocked(true);
        // trouver le produit correspondant au manager
        ProductType product = getProduct(world, manager.getIdcible());
        if (product == null) {
            return false;
        }
        // débloquer le manager de ce produit
        product.setManagerUnlocked(true);

        // soustraire de l'argent du joueur le cout du manager
        double newMoney = world.getMoney() - manager.getSeuil();
        world.setMoney(newMoney);

        // sauvegarder les changements au monde
        saveWorldToXml(world, username);
        return true;
    }

    // Retourne un manager à partir de son nom
    public PallierType getManager(World world, String nameManager) {
        // Variable
        PallierType manager = null;

        // On vérifie toute la liste des produits
        for (PallierType m : world.getManagers().getPallier()) {
            if (m.getName() == nameManager) {
                manager = m;
                return manager;
            }
        };

        // Retour du produit
        return manager;
    }

}

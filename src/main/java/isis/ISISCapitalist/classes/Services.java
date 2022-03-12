package isis.ISISCapitalist.classes;

import static isis.ISISCapitalist.classes.TyperatioType.GAIN;
import static isis.ISISCapitalist.classes.TyperatioType.VITESSE;
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
                // file.createNewFile();

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
                }

            }

            // On lit la sauvegarde
            world = (World) jaxbUnmarshaller.unmarshal(input);
            saveWorldToXml(world, pseudo);

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

            if ((product.isManagerUnlocked() == false) && (product.getTimeleft() != 0)) {
                product.setTimeleft(product.getTimeleft() - timePassed);

                if (product.getTimeleft() <= 0) {
                    double moneyProduced = product.getQuantite() * product.getRevenu();
                    world.setMoney(world.getMoney() + moneyProduced);
                    product.setTimeleft(0);
                }
            }
            /*
            else if (product.isManagerUnlocked() == true) {
                double quotient = timePassed / product.getVitesse();
                
                long reste = timePassed % product.getVitesse();
                product.setTimeleft(reste);
                
                double producedMoney = product.getQuantite() * product.getRevenu() * quotient;
                world.setMoney(world.getMoney() + producedMoney);
            }
            */
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
            // Calcul et modification de la quantité achetée
            // double un = newProduct.getCout() * Math.pow(oldProduct.getCroissance(), oldProduct.getQuantite());
            double un = oldProduct.getCout();
            double numerator = 1 - Math.pow(oldProduct.getCroissance(), qteChange);
            double denominator = 1 - oldProduct.getCroissance();
            double quantityCost = (un * numerator) / denominator;

            oldProduct.setCout(newProduct.getCout());
            oldProduct.setQuantite(newProduct.getQuantite());

            // Modification du score du monde
            double newMoney = world.getMoney() - quantityCost;
            world.setMoney(newMoney);

            updateUnlock(world);

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
            if (m.getName().equals(nameManager)) {
                manager = m;
                return manager;
            }
        };

        // Retour du produit
        return manager;
    }

    public void updateUnlock(World world) {
        for (PallierType unlock : world.getAllunlocks().getPallier()) {
            // On vérifie que l'unlock n'est pas déjà dévérouillé
            if (unlock.isUnlocked() == false) {

                // Si c'est un unlock pour un produit particulier
                if (unlock.getIdcible() != 0) {
                    ProductType product = getProduct(world, unlock.getIdcible());

                    // On vérifie que l'on a dépassé le seuil produit
                    if (product.getQuantite() >= unlock.getSeuil()) {
                        // On dévérouille l'unlock
                        unlock.setUnlocked(true);

                        // Appliquer les changements
                        applyBonusToProduct(product, unlock.getRatio(), unlock.getTyperatio());
                    }
                } // Si c'est un unlock global
                else if (unlock.getIdcible() == 0) {
                    boolean status = true;

                    for (ProductType p1 : world.getProducts().getProduct()) {
                        if (p1.getQuantite() < unlock.getSeuil()) {
                            status = false;
                            break;
                        }
                    }

                    // Si tous les produits valident les seuils, on applique le changement
                    if (status == true) {
                        unlock.setUnlocked(true);
                        for (ProductType product : world.getProducts().getProduct()) {
                            applyBonusToProduct(product, unlock.getRatio(), unlock.getTyperatio());
                        }
                    }
                }
            }
        }
    }

    public boolean updateUpgrade(String username, PallierType newUpgrade) {
        World world = getWorld(username);

        PallierType oldUpgrade = getUpgrade(world, newUpgrade.getName());
        if (oldUpgrade == null) {
            return false;
        }

        world.setMoney(world.getMoney() - oldUpgrade.getSeuil());
        oldUpgrade.setUnlocked(true);

        if (oldUpgrade.getIdcible() != 0) {
            ProductType product = getProduct(world, oldUpgrade.getIdcible());
            if (product == null) {
                return false;
            }
            
            applyBonusToProduct(product, oldUpgrade.getRatio(), oldUpgrade.getTyperatio());
            
        }
        else if (oldUpgrade.getIdcible() == 0) {
            for (ProductType product : world.getProducts().getProduct()) {
                applyBonusToProduct(product, oldUpgrade.getRatio(), oldUpgrade.getTyperatio());
            }
        }
        
        saveWorldToXml(world, username);
        return true;
    }

    
    public PallierType getUpgrade(World world, String nameUpgrade) {
        PallierType u = null;
        for (PallierType upgrade : world.getUpgrades().getPallier()) {
            if (upgrade.getName().equals(nameUpgrade)) {
                u = upgrade;
                return u;
            }
        }
        return u;
    }

    public void applyBonusToProduct(ProductType product, double ratio, TyperatioType type) {
        switch (type) {
            case VITESSE:
                product.setVitesse((int) Math.round(product.getVitesse() / ratio));
                product.setTimeleft((long) Math.round(product.getTimeleft() / ratio));
                break;
            case GAIN:
                product.setRevenu(product.getRevenu() * ratio);
                break;
        };
    }

}

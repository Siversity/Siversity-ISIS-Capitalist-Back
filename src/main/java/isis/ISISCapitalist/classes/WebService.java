package isis.ISISCapitalist.classes;

import java.lang.reflect.Field;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("adventureisis/generic")
@CrossOrigin
public class WebService {

    Services services;

    public WebService() {
        services = new Services();
    }

    // Affichage du monde customisé
    @GetMapping(value = "world", produces = {"application/xml", "application/json"})
    public ResponseEntity<World> getWorld(@RequestHeader(value = "X-User", required = false) String username) {
        // On récupère le monde à partir du pseudo
        World world = services.getWorld(username);
        System.out.println("Verif : " + services.getProduct(world, 2).getQuantite());
        
        // On retourne le monde existant ou nouvellement crée
        return ResponseEntity.ok(world);
    }
    
    // Modification d'un produit
    @PutMapping(value = "product", consumes = {"application/xml", "application/json"})
    public ResponseEntity<ProductType> modifyProduct(@RequestBody ProductType product, @RequestHeader(value = "X-User", required = false) String username) {
        // On récupère le monde à partir du pseudo
        World world = services.getWorld(username);
        
        // On actualise le produit
        if (services.updateProduct(username, product)) {
            return ResponseEntity.ok(product);
        }
        else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
    
    
    // Modification d'un manager
    @PutMapping(value = "manager", consumes = {"application/xml", "application/json"})
    public ResponseEntity<PallierType> modifyManager(@RequestBody PallierType newManager, @RequestHeader(value = "X-User", required = false) String username) {
        // On récupère le monde à partir du pseudo
        World world = services.getWorld(username);
        
        // On renvoie le manager
        return ResponseEntity.ok(newManager);
    } 
}
package isis.ISISCapitalist.classes;

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
        
        // On retourne le monde existant ou nouvellement crée
        return ResponseEntity.ok(world);
    }
    
    // Modification d'un produit
    @PutMapping(value = "product", consumes = {"application/xml", "application/json"})
    public ResponseEntity<ProductType> modifyProduct(@RequestBody ProductType product, @RequestHeader(value = "X-User", required = false) String username) {
        
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
    public ResponseEntity<PallierType> modifyManager(@RequestBody PallierType manager, @RequestHeader(value = "X-User", required = false) String username) {

        // On actualise le manager
        if (services.updateManager(username, manager)) {
            return ResponseEntity.ok(manager);
        }
        else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    } 
    
    
    // Modification d'un upgrade
    @PutMapping(value = "upgrade", consumes = {"application/xml", "application/json"})
    public ResponseEntity<PallierType> modifyUpgrade(@RequestBody PallierType upgrade, @RequestHeader(value = "X-User", required = false) String username) {

        // On actualise le manager
        if (services.updateUpgrade(username, upgrade)) {
            return ResponseEntity.ok(upgrade);
        }
        else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    } 
}
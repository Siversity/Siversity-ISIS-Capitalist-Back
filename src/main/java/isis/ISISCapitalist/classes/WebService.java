package isis.ISISCapitalist.classes;

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
        World world = services.getWorld(username);
        return ResponseEntity.ok(world);
    }
}
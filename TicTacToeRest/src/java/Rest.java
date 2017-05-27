import java.util.concurrent.ThreadLocalRandom;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Konrad
 */
@Path("/rest")
public class Rest {
    
    static int cnt = 0;
    
    @GET
    @Path("{x}/{y}")
    public String getXY(@PathParam("x") String x, @PathParam("y") String y) {
        cnt++;
        int x1 = ThreadLocalRandom.current().nextInt(-10, 10 + 1);
        int y1 = ThreadLocalRandom.current().nextInt(-10, 10 + 1);
        return x1 + " " + y1 + " " + cnt;
    }
    
    @GET
    @Path("reset")
    public void reset() {
        cnt = 0;
    }
}

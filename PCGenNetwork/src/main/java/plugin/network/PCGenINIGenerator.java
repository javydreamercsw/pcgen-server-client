package plugin.network;

import org.openide.util.lookup.ServiceProvider;
import simple.server.DefaultINIGenerator;
import simple.server.INIGenerator;

/**
 *
 * @author Javier A. Ortiz Bultrón javier.ortiz.78@gmail.com
 */
@ServiceProvider(service = INIGenerator.class)
public class PCGenINIGenerator extends DefaultINIGenerator implements INIGenerator {

    public PCGenINIGenerator() {
        super();
        defaults.put("jdbc_url",
                "jdbc:h2:file:./data/pcgen;CREATE=TRUE;AUTO_SERVER=TRUE;"
                + "LOCK_TIMEOUT=10000;MVCC=true;DB_CLOSE_ON_EXIT=FALSE;"
                + "MVCC=true;LOCK_MODE=1");
        defaults.put("server_typeGame", "PCGen");
        defaults.put("server_name", "PCGen");
        defaults.put("server_version", "1.00");
        defaults.put("tcp_port", "32190");
    }
}

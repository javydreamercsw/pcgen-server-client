package plugin.network;

import org.openide.util.lookup.ServiceProvider;
import simple.server.extension.MarauroaServerExtension;
import simple.server.extension.SimpleServerExtension;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
@ServiceProvider(service = MarauroaServerExtension.class, position = 1)
public class PCGenExtension extends SimpleServerExtension {

    @Override
    public String getName() {
        return "PCGen Extension";
    }
}

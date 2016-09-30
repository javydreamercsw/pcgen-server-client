package plugin.network;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import marauroa.client.net.PerceptionHandler;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import simple.client.ClientFrameworkProvider;
import simple.client.DefaultClient;
import simple.client.LoginProvider;
import simple.client.MessageProvider;
import simple.client.api.AddListener;
import simple.client.api.DeleteListener;
import simple.client.api.IWorldManager;
import simple.client.api.SelfChangeListener;
import simple.client.event.listener.ClientRPEventNotifier;
import simple.server.core.event.PrivateTextEvent;
import simple.server.core.event.TextEvent;

/**
 *
 * @author Javier A. Ortiz Bultrón javier.ortiz.78@gmail.com
 */
@ServiceProviders({
    @ServiceProvider(service = ClientFrameworkProvider.class)
    ,
    @ServiceProvider(service = SelfChangeListener.class)
    ,
    @ServiceProvider(service = AddListener.class)
    ,
    @ServiceProvider(service = DeleteListener.class)
    ,
    @ServiceProvider(service = LoginProvider.class)
    ,
    @ServiceProvider(service = MessageProvider.class)})
public class PCGenClient extends DefaultClient implements SelfChangeListener,
        AddListener, DeleteListener, LoginProvider, MessageProvider {

    private RPObject myself;
    private final List<String> queue = new ArrayList<>();
    private static final Logger LOG
            = Logger.getLogger(PCGenClient.class.getSimpleName());

    public PCGenClient() {
        setClientManager(new PCGenClientManager(this));
        setCreateDefaultCharacter(true);
    }

    @Override
    public boolean onMyRPObject(RPObject added, RPObject deleted) {
        RPObject.ID id = null;
        IWorldManager worldManager
                = Lookup.getDefault().lookup(IWorldManager.class);
        if (added != null) {
            id = added.getID();
        }
        if (deleted != null) {
            id = deleted.getID();
        }
        if (id == null) {
            // Unchanged.
            return true;
        }
        final RPObject object = worldManager.get(id);
        if (object != null) {
            object.applyDifferences(added, deleted);
            //Check if ID is different from last time (zone change, etc)
            if (myself != null && !myself.getID().equals(object.getID())) {
                //Delete the old id from the map.
                worldManager.getWorld().remove(myself.getID());
            }
            myself = object;
            worldManager.getWorld().put(object.getID(), object);
            //Get the object's events
            for (RPEvent event : object.events()) {
                if (event.has("event_id")) {
                    if (!queue.contains(event.get("event_id"))) {
                        queue.add(event.get("event_id"));
                        try {
                            ClientRPEventNotifier.get().logic(Arrays.asList(event));
                            LOG.log(Level.FINE, "Processing event: {0}", event.getName());
                            switch (event.getName()) {
                                case TextEvent.RPCLASS_NAME:
                                    processChat(event);
                                    break;
                                case PrivateTextEvent.RPCLASS_NAME:
                                    processChat(event);
                                    break;
                                default:
                                    //TODO: Handle other events
                                    LOG.log(Level.WARNING, "Received the following "
                                            + "event but didn't know how to handle "
                                            + "it: \n{0}", event);
                                    break;
                            }
                        } catch (Exception e) {
                            LOG.log(Level.SEVERE, null, e);
                            break;
                        }
                    }
                } else {
                    LOG.log(Level.SEVERE, "Invalid event:\n{0}", event);
                }
            }
        }
        return true;
    }

    @Override
    public RPObject getMyObject() {
        return myself;
    }

    @Override
    public boolean onAdded(RPObject object) {
        return true;
    }

    @Override
    public boolean onDeleted(RPObject object) {
        return true;
    }

    @Override
    public void displayLoginDialog() {

    }

    @Override
    public void getEmailFromUser() {

    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void displayWarning(String title, String message) {
        LOG.log(Level.WARNING, "{0}: {1}", new Object[]{title, message});
    }

    @Override
    public void displayError(String title, String message) {
        LOG.log(Level.SEVERE, "{0}: {1}", new Object[]{title, message});
    }

    @Override
    public void displayInfo(String title, String message) {
        LOG.log(Level.INFO, "{0}: {1}", new Object[]{title, message});
    }

    private void processChat(RPEvent event) {
        LOG.info(event.toString());
    }

    public static void main(String[] args) {
        new PCGenClient().startClient();
    }

    private void startClient() {
        int port = 32190;
        String host = "localhost";
        try {
            connect(host, "user", "user", "user", "" + port, "PCGen", "1.00");
            run();
        } catch (SocketException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}

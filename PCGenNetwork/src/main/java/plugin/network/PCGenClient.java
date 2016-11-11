package plugin.network;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import simple.client.ClientFrameworkProvider;
import simple.client.DefaultClient;
import simple.client.LoginProvider;
import simple.client.api.AddListener;
import simple.client.api.DeleteListener;
import simple.client.api.IWorldManager;
import simple.client.api.SelfChangeListener;
import simple.client.event.listener.ClientRPEventNotifier;
import static simple.server.core.action.WellKnownActionConstant.FROM;
import static simple.server.core.action.WellKnownActionConstant.TARGET;
import static simple.server.core.action.WellKnownActionConstant.TEXT;
import simple.server.core.action.chat.PrivateChatAction;
import simple.server.core.action.chat.PublicChatAction;
import simple.server.core.event.PrivateTextEvent;
import simple.server.core.event.TextEvent;
import simple.server.core.tool.Tool;

/**
 *
 * @author Javier A. Ortiz Bultrón javier.ortiz.78@gmail.com
 */
@ServiceProviders({
    @ServiceProvider(service = ClientFrameworkProvider.class)
    ,@ServiceProvider(service = SelfChangeListener.class)
    ,@ServiceProvider(service = AddListener.class)
    ,@ServiceProvider(service = DeleteListener.class)
    ,@ServiceProvider(service = LoginProvider.class)})
public final class PCGenClient extends DefaultClient implements SelfChangeListener,
        AddListener, DeleteListener, LoginProvider, INetworkClient {

    private RPObject myself;
    private final List<String> queue = new ArrayList<>();
    private static final Logger LOG
            = Logger.getLogger(PCGenClient.class.getSimpleName());
    private INetworkView view;

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
        //Not to be implemented since it is in the plugin set up.
    }

    @Override
    public void getEmailFromUser() {
        //Not to be implemented since it is in the plugin set up.
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
        int port = 32190;
        String host = "localhost";
        new PCGenClient().connect(port, host, "user", "user", "user",
                "PCGen", "1.00");
    }

    @Override
    public void connect(int port, String host, String user, String pw,
            String character, String game, String version) {
        try {
            connect(host, user, pw, character, "" + port, game, version);
        } catch (SocketException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public INetworkView getView() {
        return view;
    }

    @Override
    public void setView(INetworkView view) {
        this.view = view;
    }

    @Override
    public void sendIM(String target, String text) {
        RPAction action = new RPAction();
        action.put("type", PrivateChatAction.PRIVATE_CHAT);
        action.put(TEXT, text);
        action.put(FROM, Tool.extractName(getMyObject()));
        action.put(TARGET, target);
        //Send private chat action
        getClientManager().send(action);
    }

    @Override
    public void sendBroadcast(String message) {
        if (!message.isEmpty()) {
            RPAction action = new RPAction();
            action.put("type", PublicChatAction.CHAT);
            action.put(TEXT, message);
            getClientManager().send(action);
        }
    }
}

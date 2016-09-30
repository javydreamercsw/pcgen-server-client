package plugin.network;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import marauroa.client.BannedAddressException;
import marauroa.client.ClientFramework;
import marauroa.client.TimeoutException;
import marauroa.common.game.CharacterResult;
import marauroa.common.game.RPObject;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.MessageS2CPerception;
import marauroa.common.net.message.TransferContent;
import org.openide.util.Lookup;
import simple.client.ClientFrameworkProvider;
import simple.client.api.IWorldManager;
import simple.server.core.entity.clientobject.ClientObject;

/**
 *
 * @author Javier A. Ortiz Bultrón javier.ortiz.78@gmail.com
 */
public class PCGenClientManager extends ClientFramework {

    private static final Logger LOG
            = Logger.getLogger(PCGenClientManager.class.getSimpleName());
    private final ClientFrameworkProvider client;

    public PCGenClientManager(ClientFrameworkProvider client) {
        this.client = client;
    }

    @Override
    protected String getGameName() {
        return "PCGen";
    }

    @Override
    protected String getVersionNumber() {
        return "1.00";
    }

    @Override
    protected void onPerception(MessageS2CPerception message) {
        try {
            client.getPerceptionHandler().apply(message,
                    Lookup.getDefault().lookup(IWorldManager.class).getWorld());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, null, e);
        }
    }

    @Override
    protected List<TransferContent> onTransferREQ(List<TransferContent> items) {
        items.stream().forEach((item) -> {
            item.ack = true;
        });
        return items;
    }

    @Override
    protected void onTransfer(List<TransferContent> items) {
        LOG.log(Level.FINE, "Transfering ----");
        items.stream().forEach((item) -> {
            LOG.log(Level.FINE, item.toString());
        });
    }

    @Override
    protected void onAvailableCharacters(String[] characters) {
        //See onAvailableCharacterDetails
    }

    @Override
    protected void onAvailableCharacterDetails(Map<String, RPObject> characters) {
        client.getCharacters().clear();
        client.getCharacters().putAll(characters);
        // If there are no characters, create one with the specified name automatically
        if (characters.isEmpty() && client.isCreateDefaultCharacter()) {
            LOG.log(Level.WARNING,
                    "The requested character is not available, trying "
                    + "to create character {0}", client.getCharacter());
            final ClientObject template = new ClientObject();
            try {
                final CharacterResult result = createCharacter(client.getCharacter(),
                        template);
                if (result.getResult().failed()) {
                    LOG.log(Level.WARNING, result.getResult().getText());
                }
            } catch (final BannedAddressException | TimeoutException | InvalidVersionException e) {
                LOG.log(Level.SEVERE, null, e);
            }
            return;
        }
        // Autologin if a valid character was specified.
        if ((client.getCharacter() != null)
                && (characters.keySet().contains(client.getCharacter()))
                && client.isCreateDefaultCharacter()) {
            try {
                chooseCharacter(client.getCharacter());
            } catch (final BannedAddressException | TimeoutException | InvalidVersionException e) {
                LOG.log(Level.SEVERE, null, e);
            }
        }
    }

    @Override
    protected void onServerInfo(String[] info) {
        LOG.log(Level.FINE, "Server info");
        for (String info_string : info) {
            LOG.log(Level.FINE, info_string);
        }
    }

    @Override
    protected void onPreviousLogins(List<String> previousLogins) {
        LOG.log(Level.FINE, "Previous logins");
        previousLogins.stream().forEach((info_string) -> {
            LOG.log(Level.FINE, info_string);
        });
    }
}

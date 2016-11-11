package plugin.network;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public interface INetworkClient extends Runnable {

    /**
     * Get the INetworkView.
     *
     * @return the INetworkView
     */
    INetworkView getView();

    /**
     * Set the INetworkView.
     *
     * @param view INetworkView to set
     */
    void setView(INetworkView view);

    /**
     * Start client and connect to server.
     *
     * @param port target port
     * @param host target host
     * @param user username
     * @param pw user's password
     * @param character user's character
     * @param game target game
     * @param version target game version
     */
    void connect(int port, String host, String user, String pw,
            String character, String game, String version);

    /**
     * Disconnect the client.
     */
    void disconnect();

    /**
     * Send public chat.
     *
     * @param message message to send.
     */
    void sendBroadcast(String message);

    /**
     * Send private message.
     *
     * @param target target of the message.
     * @param text message to send.
     */
    void sendIM(String target, String text);
}

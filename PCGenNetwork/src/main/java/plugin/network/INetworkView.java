package plugin.network;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

/**
 *
 * @author javier
 */
public interface INetworkView {

    JRadioButton getClientRadioButton();

    JButton getConnectButton();

    JTabbedPane getLogPane();

    JButton getMessageButton();

    JTextField getMessageTextField();

    JTextField getServerAddressTextField();

    JRadioButton getServerRadioButton();

    JList getUserList();

    void hideClientPanel();

    void hideConnectionPanel();

    void processRadioButton();

    void setConnectionText(String title, String text);

    void setLocalAddressText(String address);

    void showClientPanel();

    void showConnectionPanel();
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package plugin.network;

/**
 *
 * @author javier
 */
public interface INetworkModel {

    void addUser(String user);

    /**
     * This method currently does nothing
     */
    void applyPrefs();

    void clearIcon();

    /**
     * This method currently does nothing
     */
    void closeWindow();

    String getSelectedUser();

    int getUserNumber(String user);

    INetworkView getView();

    void log(String title, String message);

    void log(String title, String owner, String message);

    void refresh();

    void removeUser(String user);

    void resetClient();

    void sendMessage();

    void setPaneIcon();
    
}

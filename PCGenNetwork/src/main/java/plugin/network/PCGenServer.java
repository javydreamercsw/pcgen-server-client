package plugin.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import marauroa.common.crypto.RSAKey;
import org.openide.util.Lookup;
import simple.server.INIGenerator;
import simple.server.SimpleServer;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public class PCGenServer extends SimpleServer {

    private final static Logger LOG
            = Logger.getLogger(PCGenServer.class.getName());
    private static RSAKey rsakey;
    private static final String SERVER_FILE = "server.ini";
    private int port;
    private INetworkView view = null;

    protected PCGenServer() {
        this.port =  32190;
        //Check ini file
        loadConfiguration();
        setStartCLI(false);
    }

    public PCGenServer(INetworkModel model, int port) {
        view = model.getView();
        this.port = port;
        //Check ini file
        loadConfiguration();
        setStartCLI(false);
    }

    private void setConnectionText(String title, String text) {
        if (view != null) {
            view.setConnectionText(title, text);
        }
    }

    @Override
    public void stopServer() {
        super.stopServer();
        setConnectionText("Server Status", "Stopped");
    }

    @Override
    public void startServer(Properties conf) {
        setConnectionText("Server Status", "Starting");
        super.startServer(conf);
        setConnectionText("Server Status", "Started");
    }

    @Override
    public void startServer() {
        setConnectionText("Server Status", "Starting");
        super.startServer();
        setConnectionText("Server Status", "Started");
    }

    /**
     * If the file doesn't exists, it'll create a default one and generate the
     * encryption keys.
     */
    private void loadConfiguration() {
        setConnectionText("Server Status", "Loading Configuration...");
        //Check if file exists
        File ini = new File(SERVER_FILE);
        Properties prop = new Properties();
        if (ini.exists()) {
            InputStream input = null;
            try {
                input = new FileInputStream(ini);
                // load a properties file
                prop.load(input);
                if(prop.containsKey("tcp_port")){
                    port=Integer.parseInt(prop.getProperty("tcp_port"));
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        LOG.log(Level.SEVERE, null, e);
                    }
                }
            }
        } else {
            try {
                setConnectionText("Server Status", "Configuring");
                LOG.info("Creating default ini file...");
                INIGenerator generator
                        = Lookup.getDefault().lookup(INIGenerator.class);
                if (generator != null) {
                    ini = generator.generateDefault();
                    InputStream input = null;
                    try {
                        input = new FileInputStream(ini);
                        // load a properties file
                        prop.load(input);
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    } finally {
                        if (input != null) {
                            try {
                                input.close();
                            } catch (IOException e) {
                                LOG.log(Level.SEVERE, null, e);
                            }
                        }
                    }
                    LOG.info("Done!");
                    setConnectionText("Server Status",
                            "Configuration Complete!");
                } else {
                    setConnectionText("Server Status",
                            "Error Configuring server!");
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        prop.put("tcp_port", "" + getPort());
        //Check if it's missing the keys
        if (!prop.contains("d")
                || !prop.contains("n")
                || !prop.contains("e")) {
            Thread thread = new Thread(() -> {
                setConnectionText("Server Status",
                        "Creating server key...");
                rsakey = RSAKey.generateKey(512);
                prop.put("d", rsakey.getD().toString());
                prop.put("e", rsakey.getE().toString());
                prop.put("n", rsakey.getN().toString());
                setConnectionText("Server Status", "Done!");
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        //Update the file if needed
        saveConfiguration(prop);
    }

    private static void saveConfiguration(Properties prop) {
        OutputStream output = null;
        File ini = new File(SERVER_FILE);
        try {
            output = new FileOutputStream(ini);
            prop.store(output, null);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, null, e);
                }
            }
        }
    }

    public static void main(String[] args) {
        PCGenServer s = new PCGenServer();
        s.startServer();
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
}

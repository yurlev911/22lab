import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame {
    private final String SERVER_ADDR = "localhost";
    private final int SERVER_PORT = 1235;
    private JTextField msgInputField;
    private JTextField loginField;
    private JTextField passField;
    private JTextArea chatArea;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public Client() {
        try {
            this.openConnection();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

        this.prepareGUI();
    }

    public void openConnection() throws IOException {
        this.socket = new Socket("localhost", 1235);
        this.in = new DataInputStream(this.socket.getInputStream());
        this.out = new DataOutputStream(this.socket.getOutputStream());
        (new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        String strFromServer = Client.this.in.readUTF();
                        if (strFromServer.startsWith("/authok")) {
                            while(true) {
                                strFromServer = Client.this.in.readUTF();
                                if (strFromServer.equalsIgnoreCase("/end")) {
                                    return;
                                }

                                Client.this.chatArea.append(strFromServer);
                                Client.this.chatArea.append("\n");
                            }
                        }

                        Client.this.chatArea.append(strFromServer + "\n");
                    }
                } catch (EOFException var2) {
                } catch (Exception var3) {
                    var3.printStackTrace();
                }

            }
        })).start();
    }

    public void closeConnection() {
        try {
            this.in.close();
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        try {
            this.out.close();
        } catch (IOException var3) {
            var3.printStackTrace();
        }

        try {
            this.socket.close();
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void sendMessage() {
        if (!this.msgInputField.getText().trim().isEmpty()) {
            try {
                this.out.writeUTF(this.msgInputField.getText());
                this.msgInputField.setText("");
                this.msgInputField.grabFocus();
            } catch (IOException var2) {
                var2.printStackTrace();
                JOptionPane.showMessageDialog((Component)null, "Ошибка отправки сообщения");
            }
        }

    }

    public void prepareGUI() {
        this.setBounds(600, 300, 500, 500);
        this.setTitle("Клиент");
        this.setDefaultCloseOperation(3);
        this.loginField = new JTextField();
        this.passField = new JTextField();
        this.chatArea = new JTextArea();
        this.chatArea.setEditable(false);
        this.chatArea.setLineWrap(true);
        this.add(new JScrollPane(this.chatArea), "Center");
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton btnSendMsg = new JButton("Отправить");
        bottomPanel.add(btnSendMsg, "East");
        this.msgInputField = new JTextField();
        this.add(bottomPanel, "South");
        bottomPanel.add(this.msgInputField, "Center");
        btnSendMsg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Client.this.sendMessage();
            }
        });
        this.msgInputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Client.this.sendMessage();
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                try {
                    Client.this.out.writeUTF("/end");
                    Client.this.closeConnection();
                } catch (IOException var3) {
                    var3.printStackTrace();
                }

            }
        });
        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Client();
            }
        });
    }
}

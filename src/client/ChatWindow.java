package client;

import config.ChatConfig;
import general.General;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ChatWindow extends javax.swing.JFrame {

    private Socket tcpSocket;
    private MulticastSocket mcastSocket;

    private int udpPort = 0;

    private final String nickname;
    private final String roomName;
    private final String multicastIp;

    private DefaultListModel model = new DefaultListModel();
    private String lastUserList = "";

    /**
     * Creates new form ChatWindow
     */
    public ChatWindow(String nickname,
            Socket tcpSocket,
            String roomName,
            String multicastIp,
            int udpPort)
            throws
            ParserConfigurationException, SAXException, IOException {
        initComponents();

        this.nickname = nickname;
        this.tcpSocket = tcpSocket;
        this.roomName = roomName;
        this.multicastIp = multicastIp;
        this.udpPort = udpPort;

        jLabel1.setText(nickname);
        jLabel2.setText(roomName);

        jTextField1.requestFocus();
        
        model.clear();
        model.addElement("TODOS");
        jList1.setModel(model);
        jList1.setSelectedValue("TODOS", false);

        // para fazer o <ENTER> no jTextField realizar o clique do botão Enviar
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jButton1.doClick();
            }
        };

        jTextField1.addActionListener(action);

        // centralizar a janela
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(dim.width / 2 - getSize().width / 2, dim.height / 2 - getSize().height / 2);

        // mudar o comportamento do botao de fechar a janela
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                setVisible(false);

                try {
                    General.sendStringTCP(
                            ChatConfig.getServerIp(),
                            ChatConfig.getTcpPort2(), "DISCONNECT:" + nickname + ":");
                } catch (IOException ex) {
                    Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
                }

                dispose();

                System.exit(0);
            }
        });

        Runnable mcastListener;
        mcastListener = () -> {
            try {
                while (true) {

                    mcastSocket = new MulticastSocket(ChatConfig.getMcastPort());
                    mcastSocket.joinGroup(InetAddress.getByName(multicastIp));

                    byte[] buffer = new byte[ChatConfig.getMessageMaxLength()];

                    DatagramPacket rec = new DatagramPacket(buffer, buffer.length);

                    mcastSocket.setSoTimeout(500000);
                    mcastSocket.receive(rec);

                    String msg = new String(rec.getData());

                    mcastSocket.close();

                    if (msg.startsWith("LISTA:")) {
                        System.out.println(msg);
                        updateUserList(msg);
                    } else {
                        updateMessageArea(msg);
                    }

                }

            } catch (IOException ex) {
                Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
            }

        };

        Thread threadMcastListener = new Thread(mcastListener);
        threadMcastListener.start();

        Runnable udpReceiver;
        udpReceiver = () -> {
            while (true) {
                try {
                    // o socket UDP ficará aguardando os mensagens:
                    // NICKNAME_DESTINO:NICKNAME_ORIGEM:TEXTO

                    DatagramSocket socketUDP;
                    if (getUdpPort() == 0) {
                        socketUDP = new DatagramSocket();
                        System.out.println(socketUDP.getLocalPort());

                        this.setUdpPort(socketUDP.getLocalPort());

                        String command = "UDP-PORT:"
                                + nickname
                                + ":"
                                + String.valueOf(getUdpPort())
                                + ":";

                        General.sendStringTCP(
                                ChatConfig.getServerIp(),
                                ChatConfig.getTcpPort2(),
                                command);
                    } else {
                        socketUDP = new DatagramSocket(getUdpPort());
                    }
                    byte[] receiveData = new byte[ChatConfig.getMessageMaxLength()];

                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    socketUDP.receive(receivePacket);

                    String msg = new String(receivePacket.getData());

                    updateMessageArea(msg);

                    socketUDP.close();
                } catch (SocketException ex) {
                    Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };

        Thread threadUdpReceiver = new Thread(udpReceiver);
        threadUdpReceiver.start();

    }

    public int getUdpPort() {
        return udpPort;
    }

    public void setUdpPort(int udpPort) {
        this.udpPort = udpPort;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setSize(new java.awt.Dimension(783, 445));

        jList1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jList1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jList1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jList1);

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setDragEnabled(true);
        jScrollPane2.setViewportView(jTextArea1);

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Send File");

        jLabel1.setText("jLabel1");

        jLabel2.setText("jLabel2");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 394, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 164, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(30, 30, 30))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE))
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        if (!jTextField1.getText().equals("")) {

            String msg = nickname.toUpperCase() + ": " + jTextField1.getText();

            if (jList1.getSelectedValue().equals("TODOS")) {
                General.sendStringMulticast(multicastIp, msg);
            } else {
                General.sendStringToUDPServer(jList1.getSelectedValue() + ":" + msg);
                updateMessageArea (msg);
            }

            jTextField1.setText("");
            jTextField1.requestFocus();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jList1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jList1MouseClicked
        // TODO add your handling code here:
        jTextField1.requestFocus();
    }//GEN-LAST:event_jList1MouseClicked

    private void updateUserList(String userList) {

        String[] split;
        String selectedValue = null;

        if (!(lastUserList.equals(userList))) {

            selectedValue = jList1.getSelectedValue();
            lastUserList = userList;

            split = userList.split(":");

            model.clear();

            model.addElement("TODOS");

            for (int i = 3; i < (split.length - 1); i++) {
                if (!split[i].equals(nickname)) {
                    model.addElement(split[i]);
                }
            }

            split = null;

            jList1.setModel(model);
        }

        jList1.repaint();

        jList1.setSelectedValue(selectedValue, false);
    }

    private void updateMessageArea(String msg) {

        jTextArea1.setText(jTextArea1.getText() + "\n" + msg);
        jTextArea1.setCaretPosition(jTextArea1.getDocument().getLength());
        jTextArea1.repaint();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList<String> jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}

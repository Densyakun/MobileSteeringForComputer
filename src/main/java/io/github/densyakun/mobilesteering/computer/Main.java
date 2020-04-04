package io.github.densyakun.mobilesteering.computer;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MyThread extends Thread {

    DatagramSocket sock;
    DatagramPacket packet;

    List<String> on = new ArrayList<>();

    public MyThread(DatagramSocket sock, DatagramPacket packet) {
        this.sock = sock;
        this.packet = packet;
    }

    @Override
    public void run() {
        Robot robot;
        try {
            robot = new Robot();

            while (true) {
                if (sock.isClosed()) {
                    for (String k : on)
                        if (k.equals("ML"))
                            robot.mouseRelease(InputEvent.BUTTON1_MASK);
                        else if (k.equals("MR"))
                            robot.mouseRelease(InputEvent.BUTTON3_MASK);
                    break;
                }

                try {
                    sock.receive(packet);
                    String s = new String(Arrays.copyOf(packet.getData(), packet.getLength()));

                    if (s.substring(0, 2).equals("ST")) {
                        float sw = Toolkit.getDefaultToolkit().getScreenSize().width / 2f;
                        robot.mouseMove((int) (sw + Float.parseFloat(s.substring(2)) * sw),
                                MouseInfo.getPointerInfo().getLocation().y);
                    } else {
                        String key = s.substring(0, s.length() - 1);

                        boolean on__ = false;
                        for (String k : on) {
                            if (k.equals(key)) {
                                on__ = true;
                                break;
                            }
                        }

                        int on_ = s.charAt(s.length() - 1);
                        if (on_ == '2') {
                            robot.keyPress(Integer.parseInt(key));
                            robot.keyRelease(Integer.parseInt(key));
                        } else if (on_ == '1') {
                            if (!on__) {
                                on.add(key);
                                switch (key) {
                                    case "ML":
                                        robot.mousePress(InputEvent.BUTTON1_MASK);
                                        break;
                                    case "MR":
                                        robot.mousePress(InputEvent.BUTTON3_MASK);
                                        break;
                                    case "LB":
                                        robot.keyPress(91);
                                        break;
                                    case "RB":
                                        robot.keyPress(93);
                                        break;
                                    default:
                                        robot.keyPress(Integer.parseInt(key));
                                        break;
                                }
                            }
                        } else if (on_ == '0' && on__) {
                            on.remove(key);
                            switch (key) {
                                case "ML":
                                    robot.mouseRelease(InputEvent.BUTTON1_MASK);
                                    break;
                                case "MR":
                                    robot.mouseRelease(InputEvent.BUTTON3_MASK);
                                    break;
                                case "LB":
                                    robot.keyRelease(91);
                                    break;
                                case "RB":
                                    robot.keyRelease(93);
                                    break;
                                default:
                                    robot.keyRelease(Integer.parseInt(key));
                                    break;
                            }
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        } catch (AWTException ignored) {
        }
    }
}

@SuppressWarnings("serial")
class MyFrame extends Frame {

    TextField portField;
    DatagramSocket sock;

    public MyFrame() {
        setTitle("MobileSteering");
        setSize(300, 200);

        setLayout(new FlowLayout());
        add(new Label("Port"));
        add(portField = new TextField());
        Button startButton = new Button("Start Server");
        startButton.addActionListener(e -> {
            try {
                int port = Integer.parseInt(portField.getText());

                sock = new DatagramSocket(port);
                byte[] data = new byte[16];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                new MyThread(sock, packet).start();

                removeAll();
                setLayout(new FlowLayout());
                add(new Label("Server is started."));
                setVisible(true);
            } catch (NumberFormatException | SocketException e1) {
                e1.printStackTrace();
            }
        });
        add(startButton);

        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                e.getWindow().dispose();
                if (sock != null)
                    sock.close();
            }
        });
    }
}

public class Main {

    public static void main(String[] args) {
        Frame f = new MyFrame();
        f.setVisible(true);
    }

}

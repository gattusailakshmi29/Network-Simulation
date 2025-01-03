package animatorPackage;

import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.net.URL;
import javax.swing.*;

// Represents an animator application for simulating transmission delay versus propagation delay
public class Animator extends JFrame {
    private JButton startButton = new JButton("Start");
    private JButton resetButton = new JButton("Reset");

    // Selector for length, rate, and packet size parameters
    private ParameterSelector lengthSelector = new ParameterSelector(
        new String[] { "10 km", "100 km", "500 km", "1000 km" },
        new double[] { 10E3, 100E3, 500E3, 1E6 }, 1
    );
    private ParameterSelector rateSelector = new ParameterSelector(
        new String[] { "512 kps", "1 Mbps", "10 Mbps", "100 Mbps" },
        new double[] { 512E3, 1E6, 10E6, 100E6 }, 1
    );
    private ParameterSelector sizeSelector = new ParameterSelector(
        new String[] { "100 Bytes", "500 Bytes", "1 kBytes" },
        new double[] { 8E2, 4E3, 8E3 }, 1
    );

    private Thread simulationThread;
    private SimulationTask simulationTask;
    private boolean pktrun = false;
    
    // Main method to start the animator application
    public static void main(String[] args) {
        Animator app = new Animator();
        app.setVisible(true);
    }

    // Represents a parameter selector choice with associated values
    private static class ParameterSelector extends Choice {
        private double values[];

        // Constructor to initialize the selector with items, values, and default selection
        public ParameterSelector(String items[], double values[], int defaultValue) {
            for (int i = 0; i < items.length; i++) {
                super.addItem(items[i]);
                Font font = new Font("Arial", Font.BOLD, 12);
                super.setFont(font);
            }
            this.values = values;
            select(defaultValue - 1);
        }

        // Returns the selected value from the selector
        public double getSelectedValue() {
            return values[super.getSelectedIndex()];
        }
    }

    // Handles resource loading for images
    final public static class ResourceLoader {
        public static InputStream load(String path) {
            InputStream input = ResourceLoader.class.getResourceAsStream(path);
            if (input == null) {
                input = ResourceLoader.class.getResourceAsStream("/" + path);
            }
            return input;
        }
    }

    private CommunicationLink communicationLink;

    // Initializes the animator application
    public Animator() {
        setTitle("Transmission delay versus propagation delay");
        setSize(700, 200);
        setLocationRelativeTo(null);
        setBackground(Color.WHITE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout());

        // Panel for parameter selection
        JPanel primaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        Font labelFont = new Font("Calibri", Font.BOLD, 15);
        primaryPanel.add(new JLabel("Length", JLabel.RIGHT)).setFont(labelFont);
        primaryPanel.add(lengthSelector);
        primaryPanel.add(new JLabel("Rate", JLabel.RIGHT)).setFont(labelFont);
        primaryPanel.add(rateSelector);
        primaryPanel.add(new JLabel("Packet size", JLabel.RIGHT)).setFont(labelFont);
        primaryPanel.add(sizeSelector);

        // Communication link for visualization
        communicationLink = new CommunicationLink(80, 22, 510, 18);
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                communicationLink.drawCommunicationLink(g);

                // Draws images for sender and receiver
                drawImage(g, "/animatorPackage/icon.png", 15, 14, 60, 35, "Sender", 17, 65);
                drawImage(g, "/animatorPackage/icon.png", 597, 14, 60, 35, "Receiver", 595, 65);

                // Displays propagation speed information
                g.drawString("Propagation speed : 2.8 x 10^8 m/sec", 160, 90);
            }
        };

        primaryPanel.setBackground(Color.WHITE);
        mainPanel.setBackground(Color.WHITE);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        Font buttonFont = new Font("Arial", Font.BOLD, 12);
        startButton.setFont(buttonFont);
        resetButton.setFont(buttonFont);
        
        // Disables focus painting for buttons
        startButton.setFocusPainted(false);
        resetButton.setFocusPainted(false);

        // ActionListener for start button
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                launchSimulation();
            }
        });
        primaryPanel.add(startButton);

        // ActionListener for reset button
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                stopSimulation();
                communicationLink.updateTime(0);
                mainPanel.repaint();
            }
        });
        primaryPanel.add(resetButton);

        contentPane.add(primaryPanel, BorderLayout.NORTH);

        setContentPane(contentPane);
    }

    // Launches the simulation
    private void launchSimulation() {
        setComponentsEnabled(false);
        communicationLink.configure(lengthSelector.getSelectedValue(), rateSelector.getSelectedValue());
        communicationLink.sendDataPacket(sizeSelector.getSelectedValue(), 0);
        simulationTask = new SimulationTask(1E-5, communicationLink.getTotalTime());
        simulationThread = new Thread(simulationTask);
        pktrun = true;
        simulationThread.start();
    }

    // Stops the simulation
    private void stopSimulation() {
        if (simulationTask != null) {
            simulationTask.endNow();
        }
        rootPaneCheckingEnabled = false;
        setComponentsEnabled(true);
    }


    // Sets the enabled state of components
    private void setComponentsEnabled(boolean value) {
        startButton.setEnabled(value);
        lengthSelector.setEnabled(value);
        rateSelector.setEnabled(value);
        sizeSelector.setEnabled(value);
    }

    // Draws an image with specified details
    private void drawImage(Graphics g, String imagePath, int x, int y, int width, int height, String text, int textX, int textY) {
        URL imgURL = ResourceLoader.class.getResource(imagePath);
        Image img = Toolkit.getDefaultToolkit().getImage(imgURL);
        g.drawImage(img, x, y, width, height, this);
        g.setColor(Color.BLACK);
        Font font = new Font("Calibri", Font.BOLD, 15);
        g.setFont(font);
        g.drawString(text, textX, textY);
    }

    // Represents a simulation task for animation
    class SimulationTask implements Runnable {
        private double simulationTimeCounter;
        private double simulationLength;
        private double simulationTick;

        // Initializes the simulation task with tick and length
        public SimulationTask(double tick, double length) {
            simulationTick = tick;
            simulationLength = length;
            simulationTimeCounter = 0;
        }

        // Runs the simulation task
        public void run() {
            while (Animator.this.pktrun) {
                simulationTimeCounter += simulationTick;
                Animator.this.communicationLink.updateTime(simulationTimeCounter);
                Animator.this.repaint();
                if (simulationTimeCounter >= simulationLength) {
                    Animator.this.communicationLink.clearDataPackets();
                    Animator.this.simulationThread.suspend();
                }
                try {
                    Animator.this.simulationThread.sleep(50);
                } catch (Exception e) {
                    // Handle exception 
                }
            }
        }

        // Ends the simulation task
        public void endNow() {
            simulationLength = simulationTimeCounter;
        }
    }

}

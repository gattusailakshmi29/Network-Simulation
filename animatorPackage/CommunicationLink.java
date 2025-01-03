package animatorPackage;

import java.awt.*;

// Represents a communication link with graphical and simulation properties
class CommunicationLink {
    private int positionX;
    private int positionY;
    private int linkWidth;
    private int linkHeight;

    final double WAVE_PROPAGATION_SPEED = 2.8E+8;
    private double linkLength;
    private double dataTransmissionRate;

    private double currentSimulationTime;
    private DataPacket currentDataPacket;

    // Constructor to initialize the communication link with position and size
    public CommunicationLink(int x, int y, int width, int height) {
        positionX = x;
        positionY = y;
        linkWidth = width;
        linkHeight = height;
    }

    // Configures the communication link with length and data transmission rate
    public void configure(double length, double rate) {
        linkLength = length;
        dataTransmissionRate = rate;
    }

    // Updates the simulation time and removes data packets that have exceeded transmission time
    void updateTime(double simulationTime) {
        currentSimulationTime = simulationTime;
        removeReceivedDataPackets(simulationTime);
    }

    // Sends a data packet with specified size and emission time
    void sendDataPacket(double size, double packetEmissionTime) {
        currentDataPacket = new DataPacket(size, packetEmissionTime);
    }

    // Removes data packets that have exceeded transmission time
    private void removeReceivedDataPackets(double simulationTime) {
        if (!(currentDataPacket == null)) {
            if (simulationTime > currentDataPacket.packetEmissionTime + (currentDataPacket.dataSize / dataTransmissionRate) + linkLength * WAVE_PROPAGATION_SPEED) {
                clearDataPackets();
            }
        }
    }

    // Clears the current data packet
    public void clearDataPackets() {
        currentDataPacket = null;
    }

    // Calculates the total time for the communication link
    public double getTotalTime() {
        double packetEmissionTime = (currentDataPacket.dataSize / dataTransmissionRate);
        double onLinkTime = (linkLength / WAVE_PROPAGATION_SPEED);
        return (packetEmissionTime + onLinkTime);
    }

    // Draws the communication link and data packets
    public void drawCommunicationLink(Graphics graphics) {
        graphics.setColor(Color.white);
        graphics.fillRect(positionX, positionY + 1, linkWidth, linkHeight - 2);
        graphics.setColor(Color.black);
        graphics.drawRect(positionX, positionY, linkWidth, linkHeight);
        graphics.setColor(new Color(255, 182, 193)); // Pink color
        Font font = new Font("Times New Roman", Font.BOLD, 17);
        graphics.setFont(font);
        graphics.drawString(convertTimeToString(currentSimulationTime), positionX + linkWidth / 2 - 30, positionY + linkHeight + 20);
        drawDataPackets(graphics);
    }

    // Draws data packets on the communication link
    private void drawDataPackets(Graphics graphics) {
        if (!(currentDataPacket == null)) {
            double timeRelativeToEmission = currentSimulationTime - currentDataPacket.packetEmissionTime;
            double packetStartTime = timeRelativeToEmission - (currentDataPacket.dataSize / dataTransmissionRate);
            double packetEndTime = timeRelativeToEmission;
            packetStartTime = packetStartTime * WAVE_PROPAGATION_SPEED * linkWidth / linkLength;
            packetEndTime = packetEndTime * WAVE_PROPAGATION_SPEED * linkWidth / linkLength;
            if (packetStartTime < 0) {
                packetStartTime = 0;
            }
            if (packetEndTime > linkWidth) {
                packetEndTime = linkWidth;
            }
            graphics.setColor(Color.red);
            graphics.fillRect(positionX + (int) (packetStartTime), positionY + 1, (int) (packetEndTime - packetStartTime), linkHeight - 1);
        }
    }

    // Converts simulation time to string format
    private static String convertTimeToString(double simulationTime) {
        String result = Double.toString(simulationTime * 1000);
        int dotIndex = result.indexOf('.');
        String decimalPart = result.substring(dotIndex + 1) + "000";
        decimalPart = decimalPart.substring(0, 3);
        String integerPart = result.substring(0, dotIndex);
        return integerPart + "." + decimalPart + " ms";
    }
}

// Represents a data packet with size and emission time
class DataPacket {
    double dataSize;
    double packetEmissionTime;

    // Constructor to initialize data packet with size and emission time
    DataPacket(double size, double emissionTime) {
        this.dataSize = size;
        this.packetEmissionTime = emissionTime;
    }
}

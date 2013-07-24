package br.ufc.great.syssu.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UdpResponser implements Runnable {

	private DatagramSocket mDatagramSocket;
	private int DISCOVERY_PORT = 2020;

	@Override
	public void run() {
		// Receive UDP broadcast message
		String ackData = "HAS SYSSU!!!";
		String received;
		DatagramPacket responsePacket;
		DatagramPacket receivePacket = new DatagramPacket (ackData.getBytes(), ackData.length());

		try {
			mDatagramSocket = new DatagramSocket(DISCOVERY_PORT);
		} catch (Exception e) {
			Logger.getLogger("UbiCentre").log(Level.SEVERE, "Error in send UbiCentre adress.", e);
		}

		while (true) {
			try {
				mDatagramSocket.receive(receivePacket);
				received = new String(receivePacket.getData(), 0,receivePacket.getLength());
				
				System.out.println("receivePacket with " + received  + " from " + receivePacket.getSocketAddress().toString());
				
				if (received.equalsIgnoreCase("HAS SYSSU???")) {
					// Send SysSU ack
					System.out.println("Send SysSU ack to " + receivePacket.getSocketAddress().toString());
					responsePacket = new DatagramPacket (ackData.getBytes(), ackData.length(), receivePacket.getSocketAddress());
					mDatagramSocket.send(responsePacket);
				}
			} catch (IOException e) {
				Logger.getLogger("UbiCentre").log(Level.SEVERE, "Error in send UbiCentre adress.", e);
			}
		}

	}

}

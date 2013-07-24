package br.ufc.great.syssu.net;

import java.util.HashMap;
import java.util.Map;

public class TS_Monitor {

	private Map<String, String> availableDevices;
	private Map<String, String> notNeighborDevices;

	public TS_Monitor() {
		this.availableDevices = new HashMap<String, String>();
		this.notNeighborDevices = new HashMap<String, String>();
	}

	public Map<String, String> getAvailableDevice() {
		return this.availableDevices;
	}

	public void addAvailableDevice(String deviceName, String deviceAddress) {
		this.availableDevices.put(deviceName,deviceAddress);
	}

	public void removeAvailableDevice(String deviceName) {
		this.availableDevices.remove(deviceName);
	}
	
	public Map<String, String> getNotNeighborDevices() {
		return this.notNeighborDevices;
	}

	public void addNotNeighborDevices(String deviceName, String deviceAddress) {
		System.out.println("addNotNeighborDevices " + deviceName + deviceAddress);
		this.notNeighborDevices.put(deviceName,deviceAddress);
	}

	public void removeNotNeighborDevices(String deviceName) {
		this.notNeighborDevices.remove(deviceName);
	}

}

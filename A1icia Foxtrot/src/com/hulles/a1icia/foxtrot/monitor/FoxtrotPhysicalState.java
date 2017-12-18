/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.hulles.a1icia.foxtrot.monitor;

import java.util.List;
import java.util.Map;

import com.hulles.a1icia.tools.A1iciaUtils;

final public class FoxtrotPhysicalState {
	private String hostName;			// "betty"
	private String architecture;		// "amd64"
	private String osName; 				// "Linux"
	private String osVersion;			// "4.2.0-35-generic"
	private String osFlavor;			// "Ubuntu 15.10"
	private List<Processor> processors;
	
    private String dbProductName;
    private String dbProductVersion;
    private String dbDriverName;
    private String dbDriverVersion;
    private String dbURL;
    private String dbUserName;

	// volatile
	private Long upTimeInSeconds;
	private Long totalMemoryKb;
	private Long freeMemoryKb;
	private Long totalSwapKb;
	private Long freeSwapKb;
	private Boolean haveInternet;
	private Boolean haveWebServer;
	private Boolean haveTomcat;
	private Boolean haveDatabase;

	// Java™
	private String javaVendor;			// "Oracle Corporation"
	private String javaVersion;			// "1.8.0_77"
	private String javaHome;
	private String javaUserHome;
	private String javaUserName;
	
	// Apache 2
	private String serverName;
	private String serverVersion;
	private Long serverUptime;
	private Long serverAccesses;
	private Long serverKBytes;
	
	// volatile
	private Integer jvmProcessors;
	private Long jvmFreeMemoryKb;
	private Long jvmTotalMemoryKb;
	private Long jvmMaxMemoryKb;

	private Map<String, SensorValue> sensorValues;
	private List<FoxtrotFS> fileSystems;
	private List<NetworkDevice> networkDevices;
	
	public Map<String, SensorValue> getSensorValues() {
		
		return sensorValues;
	}

	public void setSensorValues(Map<String, SensorValue> sensorValues) {
		
		A1iciaUtils.checkNotNull(sensorValues);
		this.sensorValues = sensorValues;
	}

	public String getHostName() {
		
		return hostName;
	}

	public void setHostName(String hostName) {
		
		A1iciaUtils.checkNotNull(hostName);
		this.hostName = hostName;
	}

	public String getArchitecture() {
		
		return architecture;
	}

	public void setArchitecture(String architecture) {
		
		A1iciaUtils.checkNotNull(architecture);
		this.architecture = architecture;
	}

	public String getOSName() {
		return osName;
	}

	public void setOSName(String osName) {
		
		A1iciaUtils.checkNotNull(osName);
		this.osName = osName;
	}

	public String getOSVersion() {
		
		return osVersion;
	}

	public void setOSVersion(String osVersion) {
		
		A1iciaUtils.checkNotNull(osVersion);
		this.osVersion = osVersion;
	}

	public String getOSFlavor() {
		
		return osFlavor;
	}

	public void setOSFlavor(String osFlavor) {
		
		A1iciaUtils.checkNotNull(osFlavor);
		this.osFlavor = osFlavor;
	}

	public Long getUpTimeInSeconds() {
		
		return upTimeInSeconds;
	}

	public void setUpTimeInSeconds(Long upTime) {
		
		A1iciaUtils.checkNotNull(upTime);
		this.upTimeInSeconds = upTime;
	}

	public List<Processor> getProcessors() {

		return processors;
	}

	public void setProcessors(List<Processor> processors) {
		
		A1iciaUtils.checkNotNull(processors);
		this.processors = processors;
	}

	public Long getTotalMemoryKb() {
		
		return totalMemoryKb;
	}

	public void setTotalMemoryKb(Long totalMemoryKb) {
		
		A1iciaUtils.checkNotNull(totalMemoryKb);
		this.totalMemoryKb = totalMemoryKb;
	}

	public Long getFreeMemoryKb() {
		
		return freeMemoryKb;
	}

	public void setFreeMemoryKb(Long freeMemoryKb) {
		
		A1iciaUtils.checkNotNull(freeMemoryKb);
		this.freeMemoryKb = freeMemoryKb;
	}

	public Long getTotalSwapKb() {
		
		return totalSwapKb;
	}

	public void setTotalSwapKb(Long totalSwapKb) {
		
		A1iciaUtils.checkNotNull(totalSwapKb);
		this.totalSwapKb = totalSwapKb;
	}

	public Long getFreeSwapKb() {
		
		return freeSwapKb;
	}

	public void setFreeSwapKb(Long freeSwapKb) {
		
		A1iciaUtils.checkNotNull(freeSwapKb);
		this.freeSwapKb = freeSwapKb;
	}

	public String getServerName() {
		
		return serverName;
	}

	public void setServerName(String serverName) {
		
		A1iciaUtils.checkNotNull(serverName);
		this.serverName = serverName;
	}

	public String getServerVersion() {
		
		return serverVersion;
	}

	public void setServerVersion(String serverVersion) {
		
		A1iciaUtils.checkNotNull(serverVersion);
		this.serverVersion = serverVersion;
	}

	public Long getServerUptime() {
		
		return serverUptime;
	}

	public void setServerUptime(Long serverUptime) {
		
		A1iciaUtils.checkNotNull(serverUptime);
		this.serverUptime = serverUptime;
	}

	public Long getServerAccesses() {
		
		return serverAccesses;
	}

	public void setServerAccesses(Long serverAccesses) {
		
		A1iciaUtils.checkNotNull(serverAccesses);
		this.serverAccesses = serverAccesses;
	}

	public Long getServerKBytes() {
		
		return serverKBytes;
	}

	public void setServerKBytes(Long serverKBytes) {
		
		A1iciaUtils.checkNotNull(serverKBytes);
		this.serverKBytes = serverKBytes;
	}

	public String getJavaVendor() {
		
		return javaVendor;
	}

	public void setJavaVendor(String javaVendor) {
		
		A1iciaUtils.checkNotNull(javaVendor);
		this.javaVendor = javaVendor;
	}

	public String getJavaVersion() {
		
		return javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		
		A1iciaUtils.checkNotNull(javaVersion);
		this.javaVersion = javaVersion;
	}

	public String getJavaHome() {
		
		return javaHome;
	}

	public void setJavaHome(String javaHome) {
		
		A1iciaUtils.checkNotNull(javaHome);
		this.javaHome = javaHome;
	}

	public String getJavaUserHome() {
		
		return javaUserHome;
	}

	public void setJavaUserHome(String userHome) {
		
		A1iciaUtils.checkNotNull(userHome);
		this.javaUserHome = userHome;
	}

	public String getJavaUserName() {
		
		return javaUserName;
	}

	public void setJavaUserName(String userName) {
		
		A1iciaUtils.checkNotNull(userName);
		this.javaUserName = userName;
	}

	public Integer getJVMProcessors() {
		
		return jvmProcessors;
	}

	public void setJVMProcessors(Integer jvmProcessors) {
		
		A1iciaUtils.checkNotNull(jvmProcessors);
		this.jvmProcessors = jvmProcessors;
	}

	public Long getJVMFreeMemoryKb() {
		
		return jvmFreeMemoryKb;
	}

	public void setJVMFreeMemoryKb(Long jvmFreeMemoryKb) {
		
		A1iciaUtils.checkNotNull(jvmFreeMemoryKb);
		this.jvmFreeMemoryKb = jvmFreeMemoryKb;
	}

	public Long getJVMTotalMemoryKb() {
		
		return jvmTotalMemoryKb;
	}

	public void setJVMTotalMemoryKb(Long jvmTotalMemoryKb) {
		
		A1iciaUtils.checkNotNull(jvmTotalMemoryKb);
		this.jvmTotalMemoryKb = jvmTotalMemoryKb;
	}

	public Long getJVMMaxMemoryKb() {
		
		return jvmMaxMemoryKb;
	}

	public void setJVMMaxMemoryKb(Long jvmMaxMemoryKb) {
		
		A1iciaUtils.checkNotNull(jvmMaxMemoryKb);
		this.jvmMaxMemoryKb = jvmMaxMemoryKb;
	}

	public String getDbProductName() {
		
		return dbProductName;
	}

	public void setDbProductName(String dbProductName) {
		
		A1iciaUtils.checkNotNull(dbProductName);
		this.dbProductName = dbProductName;
	}

	public String getDbProductVersion() {
		
		return dbProductVersion;
	}

	public void setDbProductVersion(String dbProductVersion) {
		
		A1iciaUtils.checkNotNull(dbProductVersion);
		this.dbProductVersion = dbProductVersion;
	}

	public String getDbDriverName() {
		
		return dbDriverName;
	}

	public void setDbDriverName(String dbDriverName) {
		
		A1iciaUtils.checkNotNull(dbDriverName);
		this.dbDriverName = dbDriverName;
	}

	public String getDbDriverVersion() {
		
		return dbDriverVersion;
	}

	public void setDbDriverVersion(String dbDriverVersion) {
		
		A1iciaUtils.checkNotNull(dbDriverVersion);
		this.dbDriverVersion = dbDriverVersion;
	}

	public String getDbURL() {
		
		return dbURL;
	}

	public void setDbURL(String dbURL) {
		
		A1iciaUtils.checkNotNull(dbURL);
		this.dbURL = dbURL;
	}

	public String getDbUserName() {
		
		return dbUserName;
	}

	public void setDbUserName(String dbUserName) {
		
		A1iciaUtils.checkNotNull(dbUserName);
		this.dbUserName = dbUserName;
	}

	public List<NetworkDevice> getNetworkDevices() {
		
		return networkDevices;
	}

	public void setNetworkDevices(List<NetworkDevice> networkDevices) {
		
		A1iciaUtils.checkNotNull(networkDevices);
		this.networkDevices = networkDevices;
	}

	public List<FoxtrotFS> getFileSystems() {
		
		return fileSystems;
	}

	public void setFileSystems(List<FoxtrotFS> fileSystems) {
		
		A1iciaUtils.checkNotNull(fileSystems);
		this.fileSystems = fileSystems;
	}
	
	public Boolean haveInternet() {
		
		return haveInternet;
	}

	public void setHaveInternet(Boolean haveInternet) {
		
		A1iciaUtils.checkNotNull(haveInternet);
		this.haveInternet = haveInternet;
	}

	public Boolean haveWebServer() {
		
		return haveWebServer;
	}

	public void setHaveWebServer(Boolean haveServer) {
		
		A1iciaUtils.checkNotNull(haveServer);
		this.haveWebServer = haveServer;
	}

	public Boolean haveTomcat() {
		
		return haveTomcat;
	}

	public void setHaveTomcat(Boolean haveTomcat) {
		
		A1iciaUtils.checkNotNull(haveTomcat);
		this.haveTomcat = haveTomcat;
	}

	public Boolean haveDatabase() {
		
		return haveDatabase;
	}

	public void setHaveDatabase(Boolean haveDatabase) {
		
		A1iciaUtils.checkNotNull(haveDatabase);
		this.haveDatabase = haveDatabase;
	}

	@Override
	public String toString() {
		StringBuilder sb;
		String kbStr;
		long seconds;
		String etStr;
		
		sb = new StringBuilder();
		sb.append("<h3>SYSTEM INFO</h3>\n");
		sb.append("<dl>\n");
        sb.append("<dt>Host Name</dt>\n");
        sb.append("<dd>" + this.getHostName() + "</dd>\n");
        sb.append("<dt>Architecture</dt>\n");
        sb.append("<dd>" + this.getArchitecture() + "</dd>\n");
        sb.append("<dt>OS Name</dt>\n");
        sb.append("<dd>" + this.getOSName() + "</dd>\n");
        sb.append("<dt>OS Version</dt>\n");
        sb.append("<dd>" + this.getOSVersion() + "</dd>\n");
        sb.append("<dt>OS Flavor</dt>\n");
        sb.append("<dd>" + this.getOSFlavor() + "</dd>\n");
		seconds = this.getUpTimeInSeconds();
		sb.append("<dt>Up Time</dt>\n");
		sb.append("<dd>" + FoxtrotUtils.formatElapsedSeconds(seconds) + "</dd>\n");
		sb.append("<dt>CPU Info</dt>\n");
		sb.append("<dd>");
		for (Processor cpu : this.getProcessors()) {
			sb.append("Processor " + cpu.getProcessorNumber());
			sb.append(" " + cpu.getModelName());
			sb.append(" " + cpu.getCpuMhz() + "MHz<br />\n");
		}
		sb.append("</dd>\n");
		sb.append("<dt>Total memory</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getTotalMemoryKb());
		sb.append("<dd>" + kbStr + "</dd>\n");
		sb.append("<dt>Free memory</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getFreeMemoryKb());
		sb.append("<dd>" + kbStr + "</dd>\n");
		sb.append("<dt>Total swap</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getTotalSwapKb());
		sb.append("<dd>" + kbStr + "</dd>\n");
		sb.append("<dt>Free swap</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getFreeSwapKb());
		sb.append("<dd>" + kbStr + "</dd>\n");
		sb.append("<dt>Java Vendor</dt>\n");
		sb.append("<dd>" + this.getJavaVendor() + "</dd>\n");
        sb.append("<dt>Java Version</dt>\n");
        sb.append("<dd>" + this.getJavaVersion() + "</dd>\n");
        sb.append("<dt>Java Home</dt>\n");
        sb.append("<dd>" + this.getJavaHome() + "</dd>\n");
        sb.append("<dt>Java User Name</dt>\n");
        sb.append("<dd>" + this.getJavaUserName() + "</dd>\n");
        sb.append("<dt>Java User Home Directory</dt>\n");
        sb.append("<dd>" + this.getJavaUserHome() + "</dd>\n");
		sb.append("<dt>JVM Processors</dt>\n");
		sb.append("<dd>" + this.getJVMProcessors() + "</dd>\n");
		sb.append("<dt>JVM Free Memory</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getJVMFreeMemoryKb());
		sb.append("<dd>" + kbStr + "</dd>\n");
		sb.append("<dt>JVM Max Memory</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getJVMMaxMemoryKb());
		sb.append("<dd>" + kbStr + "</dd>\n");
		sb.append("<dt>JVM Total Memory</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getJVMTotalMemoryKb());
		sb.append("<dd>" + kbStr + "</dd>\n");

		sb.append("<dt>Database Product</dt>\n");
		sb.append("<dd>" + this.getDbProductName() + "</dd>\n");
		sb.append("<dt>Database Version</dt>\n");
		sb.append("<dd>" + this.getDbProductVersion() + "</dd>\n");
		sb.append("<dt>Database Driver Name</dt>\n");
		sb.append("<dd>" + this.getDbDriverName() + "</dd>\n");
		sb.append("<dt>Database Driver Version</dt>\n");
		sb.append("<dd>" + this.getDbDriverVersion() + "</dd>\n");
		sb.append("<dt>Database User Name</dt>\n");
		sb.append("<dd>" + this.getDbUserName() + "</dd>\n");
		sb.append("<dt>Database URL</dt>\n");
		sb.append("<dd>" + this.getDbURL() + "</dd>\n");

		sb.append("<dt>Apache Server Name</dt>\n");
		sb.append("<dd>" + this.getServerName() + "</dd>\n");
		sb.append("<dt>Apache Server Version</dt>\n");
		sb.append("<dd>" + this.getServerVersion() + "</dd>\n");
		sb.append("<dt>Apache Total Uptime</dt>\n");
		etStr = FoxtrotUtils.formatElapsedSeconds(this.getServerUptime());
		sb.append("<dd>" + etStr + "</dd>\n");
		sb.append("<dt>Apache Total Accesses</dt>\n");
		sb.append("<dd>" + this.getServerAccesses() + "</dd>\n");
		sb.append("<dt>Apache Total kBytes Served</dt>\n");
		kbStr = FoxtrotUtils.formatKb(this.getServerKBytes());
		sb.append("<dd>" + kbStr + "</dd>\n");
		
		
		for (SensorValue sv : this.getSensorValues().values()) {
			sb.append("<dt>Sensor " + sv.getLabel() + "</dt>\n");
			sb.append("<dd>" + sv.getValue());
			if (sv.getAlarm()) {
				sb.append(" ALARM");
			}
			sb.append("</dd>\n");
		}
		for (FoxtrotFS fs : this.getFileSystems()) {
			sb.append("<dt>File System " + fs.getFsName() + "</dt>\n");
			kbStr = FoxtrotUtils.formatKb(fs.getTotalSpaceKb());
			sb.append("<dd>Total Space " + kbStr);
			kbStr = FoxtrotUtils.formatKb(fs.getUsedSpaceKb());
			sb.append(" | Used Space " + kbStr);
			kbStr = FoxtrotUtils.formatKb(fs.getFreeSpaceKb());
			sb.append(" | Free Space " + kbStr);
			sb.append(" | Used " + fs.getUsedPercent() + "%");
			sb.append(" | Mounted at " + fs.getMountPoint() + "</dd>\n");
		}
		for (NetworkDevice device : this.getNetworkDevices()) {
			sb.append("<dt>Network Device " + device.getDeviceName() + "</dt>\n");
			kbStr = FoxtrotUtils.formatKb(device.getTransmitKb());
			sb.append("<dd>Transmitted " +  kbStr);
			kbStr = FoxtrotUtils.formatKb(device.getReceiveKb());
			sb.append(" | Received " + kbStr + "</dd>\n");
		}
		sb.append("<dt>Have Internet</dt>\n");
		sb.append("<dd>"+ this.haveInternet() + "</dd>\n");
		sb.append("<dt>Have Http Server</dt>\n");
		sb.append("<dd>"+ this.haveWebServer() + "</dd>\n");
		sb.append("<dt>Have Tomcat</dt>\n");
		sb.append("<dd>"+ this.haveTomcat() + "</dd>\n");
		sb.append("<dt>Have Database</dt>\n");
		sb.append("<dd>"+ this.haveDatabase() + "</dd>\n");
		sb.append("</dl>\n");
		return sb.toString();
	}

	public class NetworkDevice {
		private String deviceName;
		private Long transmitKb;
		private Long receiveKb;
		
		public String getDeviceName() {
			
			return deviceName;
		}
		
		public void setDeviceName(String deviceName) {
			
			A1iciaUtils.checkNotNull(deviceName);
			this.deviceName = deviceName;
		}
		
		public Long getTransmitKb() {
			
			return transmitKb;
		}
		
		public void setTransmitKb(Long transmitKb) {
			
			A1iciaUtils.checkNotNull(transmitKb);
			this.transmitKb = transmitKb;
		}
		
		public Long getReceiveKb() {
			
			return receiveKb;
		}
		
		public void setReceiveKb(Long receiveKb) {
			
			A1iciaUtils.checkNotNull(receiveKb);
			this.receiveKb = receiveKb;
		}
		
	}

	public class FoxtrotFS {
		private String fsName;
		private Long totalSpaceKb;
		private Long usedSpaceKb;
		private Long freeSpaceKb;
		private Integer usedPercent; 
		private String mountPoint;
		
		public String getFsName() {
			
			return fsName;
		}
		
		public void setFsName(String fsName) {
			
			A1iciaUtils.checkNotNull(fsName);
			this.fsName = fsName;
		}
		
		public Long getTotalSpaceKb() {
			
			return totalSpaceKb;
		}
		
		public void setTotalSpaceKb(Long totalSpaceKb) {
			
			A1iciaUtils.checkNotNull(totalSpaceKb);
			this.totalSpaceKb = totalSpaceKb;
		}
		
		public Long getUsedSpaceKb() {
			
			return usedSpaceKb;
		}
		
		public void setUsedSpaceKb(Long usedSpaceKb) {
			
			A1iciaUtils.checkNotNull(usedSpaceKb);
			this.usedSpaceKb = usedSpaceKb;
		}
		
		public Long getFreeSpaceKb() {
			
			return freeSpaceKb;
		}
		
		public void setFreeSpaceKb(Long freeSpaceKb) {
			
			A1iciaUtils.checkNotNull(freeSpaceKb);
			this.freeSpaceKb = freeSpaceKb;
		}
		
		public Integer getUsedPercent() {
			
			return usedPercent;
		}
		
		public void setUsedPercent(Integer usedPercent) {
			
			A1iciaUtils.checkNotNull(usedPercent);
			this.usedPercent = usedPercent;
		}
		
		public String getMountPoint() {
			
			return mountPoint;
		}
		
		public void setMountPoint(String mountPoint) {
			
			A1iciaUtils.checkNotNull(mountPoint);
			this.mountPoint = mountPoint;
		}

	}
	
	public class SensorValue {
		private String chip;
		private String label;
		private Float value;
		private Boolean alarm;
		
		public String getChip() {
			
			return chip;
		}

		public void setChip(String chip) {
			
			A1iciaUtils.checkNotNull(chip);
			this.chip = chip;
		}
		
		public String getLabel() {
			
			return label;
		}

		public void setLabel(String label) {
			
			A1iciaUtils.checkNotNull(label);
			this.label = label;
		}
		
		public Float getValue() {
			
			return value;
		}
		
		public void setValue(Float value) {
			
			A1iciaUtils.checkNotNull(value);
			this.value = value;
		}
		
		public Boolean getAlarm() {
			
			return alarm;
		}
		
		public void setAlarm(Boolean alarm) {
			
			A1iciaUtils.checkNotNull(alarm);
			this.alarm = alarm;
		}
		
	}

	public class Processor {
		private String modelName;
		private Integer processorNumber;
		private Float cpuMhz;
		
		public String getModelName() {
			
			return modelName;
		}
		
		public void setModelName(String modelName) {
			
			A1iciaUtils.checkNotNull(modelName);
			this.modelName = modelName;
		}
		
		public Integer getProcessorNumber() {
			
			return processorNumber;
		}
		
		public void setProcessorNumber(Integer processorNumber) {
			
			A1iciaUtils.checkNotNull(processorNumber);
			this.processorNumber = processorNumber;
		}
		
		public Float getCpuMhz() {
			
			return cpuMhz;
		}
		
		public void setCpuMhz(Float cpuMhz) {
			
			A1iciaUtils.checkNotNull(cpuMhz);
			this.cpuMhz = cpuMhz;
		}
		
		
	}
}
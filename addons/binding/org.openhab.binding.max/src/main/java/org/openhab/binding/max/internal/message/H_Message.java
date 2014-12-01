/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.max.internal.Utils;
import org.slf4j.Logger;

import com.google.common.base.Strings;


/**
* The H message contains information about the MAX!Cube. 
* 
* @author Andreas Heil (info@aheil.de)
* @author Marcel Verpaalen - Details parsing
* @since 1.4.0
*/
public final class H_Message extends Message {
	
	private Calendar cal = Calendar.getInstance();
	private Map<String, Object> properties = new HashMap<>();

	
	private String rawSerialNumber = null;
	private String rawRfHexAddress = null;
	private String rawFirmwareVersion = null;
	private String rawConnectionId = null;
	private String rawDutyCycle = null;
	private String rawFreeMemorySlots = null;
	private String rawCubeTimeState = null;
	private String rawNTPCounter = null; 
	
	// yet unknown fields
	private String rawUnknownfield4 = null;

	public H_Message(String raw) {
		super(raw);

		String[] tokens = this.getPayload().split(Message.DELIMETER);

		if (tokens.length < 11)
		{
			throw new ArrayIndexOutOfBoundsException("MAX!Cube raw H_Message corrupt");
		}
		
		rawSerialNumber = tokens[0];
		rawRfHexAddress = tokens[1];
		rawFirmwareVersion = tokens[2];
		rawUnknownfield4 = tokens[3];
		rawConnectionId = tokens[4];
		rawDutyCycle =  Integer.toString(Utils.fromHex(tokens[5]));
		rawFreeMemorySlots  =  Integer.toString(Utils.fromHex(tokens[6]));
		
		setDateTime(tokens[7], tokens[8]);
		
		rawCubeTimeState  = tokens[9];
		rawNTPCounter  = Integer.toString(Utils.fromHex(tokens[10]) ) ;
		properties.put("Serial number", rawSerialNumber);
		properties.put("RF address (HEX)", rawRfHexAddress);
		properties.put("Firmware version", rawFirmwareVersion);
		properties.put("Connection ID", rawConnectionId);
		properties.put("Unknown", rawUnknownfield4);
		properties.put("Duty Cycle", rawDutyCycle);
		properties.put("FreeMemorySlots", rawFreeMemorySlots);
		properties.put("CubeTimeState", rawCubeTimeState);
		properties.put("NTPCounter", rawNTPCounter);
	}

	private final void setDateTime(String hexDate, String hexTime) {
		
		int year = Utils.fromHex(hexDate.substring(0,2));
		int month = Utils.fromHex(hexDate.substring(2, 4));
		int date = Utils.fromHex(hexDate.substring(4, 6));
		
		int hours = Utils.fromHex(hexTime.substring(0, 2));
		int minutes = Utils.fromHex(hexTime.substring(2, 4));

		cal.set(year, month, date, hours, minutes, 0);
	}
	
	@Override
	public void debug(Logger logger) {
		logger.debug("=== H_Message === ");
		logger.trace("\tRAW:            : {}", this.getPayload());
		logger.debug("\tReading Time    : {}", cal.getTime());
		for(String key: properties.keySet()){
    		logger.debug("\t{}:{}{}", key , Strings.repeat(" ",25 - key.length() ), properties.get(key));
        }
	}

	@Override
	public MessageType getType() {
		return MessageType.H;
	}
}

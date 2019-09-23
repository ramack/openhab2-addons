/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.heliosventilation.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link HeliosVentilationDataPoint} is a description of a datapoint in the Helios ventilation system.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentilationDataPoint {
    public enum DataType {
        TEMPERATURE,
        HYSTERESIS,
        FANSPEED,
        SWITCH,
        BYTE_PERCENT,
        PERCENT,
        NUMBER
    }

    /**
     * mapping from temperature byte values to °C
     */
    private static final int TEMP_MAP[] = { -74, -70, -66, -62, -59, -56, -54, -52, -50, -48, -47, -46, -44, -43, -42,
            -41, -40, -39, -38, -37, -36, -35, -34, -33, -33, -32, -31, -30, -30, -29, -28, -28, -27, -27, -26, -25,
            -25, -24, -24, -23, -23, -22, -22, -21, -21, -20, -20, -19, -19, -19, -18, -18, -17, -17, -16, -16, -16,
            -15, -15, -14, -14, -14, -13, -13, -12, -12, -12, -11, -11, -11, -10, -10, -9, -9, -9, -8, -8, -8, -7, -7,
            -7, -6, -6, -6, -5, -5, -5, -4, -4, -4, -3, -3, -3, -2, -2, -2, -1, -1, -1, -1, 0, 0, 0, 1, 1, 1, 2, 2, 2,
            3, 3, 3, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 7, 7, 7, 8, 8, 8, 9, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 13,
            13, 13, 14, 14, 14, 15, 15, 15, 16, 16, 16, 17, 17, 18, 18, 18, 19, 19, 19, 20, 20, 21, 21, 21, 22, 22, 22,
            23, 23, 24, 24, 24, 25, 25, 26, 26, 27, 27, 27, 28, 28, 29, 29, 30, 30, 31, 31, 32, 32, 33, 33, 34, 34, 35,
            35, 36, 36, 37, 37, 38, 38, 39, 40, 40, 41, 41, 42, 43, 43, 44, 45, 45, 46, 47, 48, 48, 49, 50, 51, 52, 53,
            53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 65, 66, 68, 69, 71, 73, 75, 77, 79, 81, 82, 86, 90, 93, 97, 100,
            100, 100, 100, 100, 100, 100, 100, 100 };

    /**
     * mapping from human readable fanspeed to raw value
     */
    private static final int FANSPEED_MAP[] = { 0, 1, 3, 7, 15, 31, 63, 127, 255 };

    private static final int BYTE_PERCENT_OFFSET = 52;

    private String name;
    private boolean writable;
    private DataType datatype;
    private byte address;
    private int bitStart;
    private int bitLength;

    private @Nullable HeliosVentilationDataPoint link;

    /**
     * parse fullSpec in the properties format to declare a datapoint
     *
     * @param name the name of the datapoint
     * @param fullSpec datapoint specification, see format in datapoints.properties
     * @throws HeliosPropertiesFormatException in case fullSpec is not parsable
     */
    public HeliosVentilationDataPoint(String name, String fullSpec) throws HeliosPropertiesFormatException {
        String specWithoutComment;
        if (fullSpec.contains("#")) {
            specWithoutComment = fullSpec.substring(0, fullSpec.indexOf("#"));
        } else {
            specWithoutComment = fullSpec;
        }
        String[] tokens = specWithoutComment.split(",");
        this.name = name;
        if (tokens.length != 3) {
            throw new HeliosPropertiesFormatException("invalid length", name, fullSpec);
        }
        try {
            String addr = tokens[0];
            String[] addrTokens;
            if (addr.contains(":")) {
                addrTokens = addr.split(":");
            } else {
                addrTokens = new String[] { addr };
            }
            bitLength = 8;
            bitStart = 0;
            this.address = (byte) (int) Integer.decode(addrTokens[0]);
            if (addrTokens.length > 1) {
                bitStart = (byte) (int) Integer.decode(addrTokens[1]);
                bitLength = 1;
            }
            if (addrTokens.length > 2) {
                bitLength = (byte) (int) Integer.decode(addrTokens[2]) - bitStart + 1;
            }
            if (addrTokens.length > 3) {
                throw new HeliosPropertiesFormatException(
                        "invalid address spec: too many separators in bit specification", name, fullSpec);
            }
        } catch (NumberFormatException e) {
            throw new HeliosPropertiesFormatException("invalid address spec", name, fullSpec);
        }

        this.writable = Boolean.parseBoolean(tokens[1]);
        try {
            this.datatype = DataType.valueOf(tokens[2].replaceAll("\\s+", ""));
        } catch (IllegalArgumentException e) {
            throw new HeliosPropertiesFormatException("invalid type spec", name, fullSpec);
        }
    }

    public HeliosVentilationDataPoint(String name, byte address, boolean writable, DataType datatype) {
        this.datatype = datatype;
        this.writable = writable;
        this.name = name;
        this.address = address;
    }

    public boolean isWritable() {
        return writable;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     *
     * @return the name of the variable, which is also the channel name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return address of the variable
     */
    public byte address() {
        return address;
    }

    /**
     * @return the bit mask of the data point. 0xFF in case the full byte is used.
     */
    public byte bitMask() {
        byte mask = (byte) 0xff;
        if (datatype == DataType.NUMBER || datatype == DataType.SWITCH) {
            mask = (byte) (((1 << bitLength) - 1) << bitStart);
        }
        return mask;
    }

    /**
     * interpret the given byte b and return the value as State.
     *
     * @param b
     * @return state representation of byte value b in current datatype
     */
    public State asState(byte b) {
        int val = b & 0xff;
        if (datatype == DataType.TEMPERATURE) {
            return new QuantityType<>(TEMP_MAP[val], SIUnits.CELSIUS);
        } else if (datatype == DataType.BYTE_PERCENT) {
            return new QuantityType<>((int) ((val - BYTE_PERCENT_OFFSET) * 100.0 / (255 - BYTE_PERCENT_OFFSET)),
                    SmartHomeUnits.PERCENT);
        } else if (datatype == DataType.SWITCH && bitLength == 1) {
            if ((b & (1 << bitStart)) != 0) {
                return OnOffType.ON;
            } else {
                return OnOffType.OFF;
            }
        } else if (datatype == DataType.NUMBER) {
            int value = (b & bitMask()) >> bitStart;
            return new DecimalType(value);
        } else if (datatype == DataType.PERCENT) {
            return new QuantityType<>(val, SmartHomeUnits.PERCENT);
        } else if (datatype == DataType.FANSPEED) {
            int i = 1;
            while (i < FANSPEED_MAP.length && FANSPEED_MAP[i] < val) {
                i++;
            }
            return new DecimalType(i);
        } else if (datatype == DataType.HYSTERESIS) {
            return new QuantityType<>(val / 3, SIUnits.CELSIUS);
        }

        return UnDefType.UNDEF;
    }

    /**
     * interpret the given byte b and return the value as string.
     *
     * @param b
     * @return sting representation of byte value b in current datatype
     */
    public String asString(byte b) {
        State ste = asState(b);
        String str = ste.toString();
        if (ste instanceof UnDefType) {
            return String.format("<unknown type> %02X ", b);
        } else {
            return str;
        }
    }

    /**
     * generate byte data to transmit
     *
     * @param val is the state of a channel
     * @return byte value with RS485 representation. Bit level values are returned in the correct location, but other
     *         bits/datapoints in the same address are zero.
     */
    public byte getTransmitDataFor(State val) {
        byte result = 0;
        DecimalType value = val.as(DecimalType.class);
        if (value == null) {
            /*
             * if value is not convertible to a numeric type we cannot do anything reasonable with it, let's use the
             * initial value for it
             */
        } else if (datatype == DataType.TEMPERATURE) {
            int temp = (int) Math.round(value.doubleValue());
            int i = 0;
            while (i < TEMP_MAP.length && TEMP_MAP[i] < temp) {
                i++;
            }
            result = (byte) i;
        } else if (datatype == DataType.FANSPEED) {
            int i = value.intValue();
            if (i < 0) {
                i = 0;
            } else if (i > 8) {
                i = 8;
            }
            result = (byte) FANSPEED_MAP[i];
        } else if (datatype == DataType.BYTE_PERCENT) {
            result = (byte) ((value.doubleValue() / 100.0) * (255 - BYTE_PERCENT_OFFSET) + BYTE_PERCENT_OFFSET);
        } else if (datatype == DataType.PERCENT) {
            double d = (Math.round(value.doubleValue()));
            if (d < 0.0) {
                d = 0.0;
            } else if (d > 100.0) {
                d = 100.0;
            }
            result = (byte) d;
        } else if (datatype == DataType.HYSTERESIS) {
            result = (byte) (value.intValue() * 3);
        } else if (datatype == DataType.SWITCH || datatype == DataType.NUMBER) {
            // those are the types supporting bit level specification
            // output only the relevant bits
            result = (byte) (value.intValue() << bitStart);
        }

        return result;
    }

    /**
     * Get further datapoint linked to the same address.
     *
     * @return sister datapoint
     */
    public @Nullable HeliosVentilationDataPoint link() {
        return link;
    }

    /**
     * Add a link to a datapoint on the same address.
     * Caller has to ensure that identical datapoints are not added several times.
     *
     * @param link is the sister datapoint
     */
    public void append(HeliosVentilationDataPoint link) {
        HeliosVentilationDataPoint existing = this.link;
        if (this == link) {
            // this datapoint is already there, so we do nothing and return
            return;
        } else if (existing != null) {
            existing.append(link);
        } else {
            this.link = link;
        }
    }

    /**
     * @return true iff writing to this datapoint requires a read-modify-write on the address
     */
    public boolean requiresReadModifyWrite() {
        /*
         * the address either has multiple datapoints linked to it or is a bit-level point
         * this means we need to do read-modify-write on udpate and therefore we store the data in memory
         */
        return (bitMask() != (byte) 0xFF || link != null);
    }
}

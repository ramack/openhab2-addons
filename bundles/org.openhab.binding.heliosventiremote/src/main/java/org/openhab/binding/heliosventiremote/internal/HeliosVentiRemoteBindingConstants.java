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
package org.openhab.binding.heliosventiremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HeliosVentiRemoteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Raphael Mack - Initial contribution
 */
@NonNullByDefault
public class HeliosVentiRemoteBindingConstants {

    public static final String BINDING_ID = "heliosventiremote";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HELIOS_VENTILATION = new ThingTypeUID(BINDING_ID, "heliosVentilation");

    // List of all Channel ids
    public static final String CHANNEL_OUTSIDE_TEMP = "outsideTemp";
    public static final String CHANNEL_OUTGOING_TEMP = "outgoingTemp";
    public static final String CHANNEL_EXTRACT_TEMP = "extractTemp";
    public static final String CHANNEL_SUPPLY_TEMP = "supplyTemp";
    public static final String CHANNEL_BYPASS_TEMP = "bypass_temp";
    public static final String CHANNEL_RH_LIMIT = "rh_limit";
    public static final String CHANNEL_HYSTERESIS = "hysteresis";
    public static final String CHANNEL_FANSPEED = "fanspeed";
    public static final String CHANNEL_MIN_FANSPEED = "min_fanspeed";
    public static final String CHANNEL_MAX_FANSPEED = "max_fanspeed";
}

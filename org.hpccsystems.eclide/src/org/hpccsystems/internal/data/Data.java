/*******************************************************************************
 * Copyright (c) 2011 HPCC Systems.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     HPCC Systems - initial API and implementation
 ******************************************************************************/
package org.hpccsystems.internal.data;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Observable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationListener;
import org.hpccsystems.ws.wsworkunits.ArrayOfEspException;
import org.hpccsystems.ws.wsworkunits.WUQuery;
import org.hpccsystems.ws.wsworkunits.WUQueryResponse;
import org.hpccsystems.ws.wsworkunits.WsWorkunitsServiceSoap;

public class Data extends Observable {
	private static Data singletonFactory;
	
	private Collection<Platform> platforms;	
	
	//  Singleton Pattern
	private Data() {
		this.platforms = new ArrayList<Platform>();
	}

	public static synchronized Data get() {
		if (singletonFactory == null) {
			singletonFactory = new Data();
		}
		return singletonFactory;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	//  Platform  ---
	public Platform GetPlatform(ILaunchConfiguration launchConfiguration) {
		Platform retVal = null;
		String ip = "";
		int port = 0;
		try {
			ip = launchConfiguration.getAttribute(Platform.P_IP, "");
		} catch (CoreException e) {
		} 
		
		try {
			port = launchConfiguration.getAttribute(Platform.P_PORT, 8010);
		} catch (CoreException e) {
		}
		
		if (port == 0) {
			port = 8010;
		}
		
		if (!ip.isEmpty() && port != 0) {
			retVal = Platform.get(ip, port);
			retVal.update(launchConfiguration);	
		}
		return retVal;
	}

	public Platform GetPlatformNoCreate(String ip, int port) {
		return Platform.getNoCreate(ip, port);
	}

	public final Platform[] getPlatforms() {
		platforms.clear();
		ILaunchConfiguration[] configs;
		try {
			configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
			for(int i = 0; i < configs.length; ++i) {
				Platform p = GetPlatform(configs[i]);
				if (p != null && !platforms.contains(p))
					platforms.add(p);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return platforms.toArray(new Platform[0]);
	}

	public Collection<Workunit> getWorkunits(Platform platform, String cluster, String startDate, String endDate) {
		Collection<Workunit> workunits = new HashSet<Workunit>();
		try {
			Workunit.All.pushTransaction("Data.getWorkunits");
			for (Platform p : getPlatforms()) {
				if (platform == null || platform.equals(p)) {
					workunits.addAll(p.getWorkunits(cluster, startDate, endDate));
				}
			}
		}
		finally {
			Workunit.All.popTransaction();
		}
		return workunits;
	}
}

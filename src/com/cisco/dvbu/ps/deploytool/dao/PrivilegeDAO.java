/**
 * (c) 2014 Cisco and/or its affiliates. All rights reserved.
 * 
 * This software is released under the Eclipse Public License. The details can be found in the file LICENSE. 
 * Any dependent libraries supplied by third parties are provided under their own open source licenses as 
 * described in their own LICENSE files, generally named .LICENSE.txt. The libraries supplied by Cisco as 
 * part of the Composite Information Server/Cisco Data Virtualization Server, particularly csadmin-XXXX.jar, 
 * csarchive-XXXX.jar, csbase-XXXX.jar, csclient-XXXX.jar, cscommon-XXXX.jar, csext-XXXX.jar, csjdbc-XXXX.jar, 
 * csserverutil-XXXX.jar, csserver-XXXX.jar, cswebapi-XXXX.jar, and customproc-XXXX.jar (where -XXXX is an 
 * optional version number) are provided as a convenience, but are covered under the licensing for the 
 * Composite Information Server/Cisco Data Virtualization Server. They cannot be used in any way except 
 * through a valid license for that product.
 * 
 * This software is released AS-IS!. Support for this software is not covered by standard maintenance agreements with Cisco. 
 * Any support for this software by Cisco would be covered by paid consulting agreements, and would be billable work.
 * 
 */
package com.cisco.dvbu.ps.deploytool.dao;

import com.cisco.dvbu.ps.common.exception.CompositeException;
import com.cisco.dvbu.ps.deploytool.modules.PrivilegeModule;
import com.compositesw.services.system.admin.resource.GetResourcePrivilegesRequest.Entries;
import com.compositesw.services.system.admin.resource.UpdateResourcePrivilegesRequest.PrivilegeEntries;

public interface PrivilegeDAO {

   	/*
	 * mtinius: 2012-08-07, added static parameter (CaResourceUtil.RECURSE_DEPENDENCIES) for new api
	 * 						call to updateResourcePrivileges
	 */
	public static boolean RECURSE_DEPENDENCIES = false;
	
	public static enum action {UPDATE};

	/**
	 * Take Privilege Action method takes  passed action on the resource privilege with the passed in privilege attributes for the passed in target server name 
	 * @param actionname the type of action to take
	 * @param PrivilegeModule privilegeModule entry list
	 * @param serverId target server config name
	 * @param pathToServersXML path to the server values xml
	 * @return void
	 * @throws CompositeException
	 */
	public void takePrivilegeAction(String actionName, PrivilegeModule privilegeModule, String serverId, String pathToServersXML) throws CompositeException;

	/**
	 * Get all the resource privileges for the set of entries pass in
	 * @param privilegeEntries a list of resources to get privileges for (resourcePath, resourceType)
	 * @param filter A filter string.  The only legal values in this release are an empty string and "ALL_EXPLICIT"
	 * @param includeColumnPrivileges (true or false) on whether to include column privileges or not
	 * @param serverId target server config name
	 * @param pathToServersXML path to the server values xml
	 * @return PrivilegeEntries list with all the privileges for the passed in Entry list
	 * @throws CompositeException
	 */	
	public PrivilegeModule getResourcePrivileges(Entries privilegeEntries, String filter, boolean includeColumnPrivileges, String serverId, String pathToServersXML);

}

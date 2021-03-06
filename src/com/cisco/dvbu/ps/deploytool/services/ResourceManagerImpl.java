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
package com.cisco.dvbu.ps.deploytool.services;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContextException;

import com.cisco.dvbu.ps.common.CommonConstants;
import com.cisco.dvbu.ps.common.exception.CompositeException;
import com.cisco.dvbu.ps.common.exception.ValidationException;
import com.cisco.dvbu.ps.common.util.CommonUtils;
import com.cisco.dvbu.ps.common.util.XMLUtils;
import com.cisco.dvbu.ps.deploytool.dao.ResourceDAO;
import com.cisco.dvbu.ps.deploytool.dao.wsapi.ResourceWSDAOImpl;
import com.cisco.dvbu.ps.deploytool.util.DeployUtil;
import com.cisco.dvbu.ps.deploytool.modules.ResourceModule;
import com.cisco.dvbu.ps.deploytool.modules.ResourceType;
import com.compositesw.services.system.admin.resource.Resource;
import com.compositesw.services.system.admin.resource.ResourceList;

public class ResourceManagerImpl implements ResourceManager{

	private ResourceDAO resourceDAO = null;
	// Get the configuration property file set in the environment with a default of deploy.properties
	String propertyFile = CommonUtils.getFileOrSystemPropertyValue(CommonConstants.propertyFile, "CONFIG_PROPERTY_FILE");
	
	private static Log logger = LogFactory.getLog(ResourceManagerImpl.class);

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#executeConfiguredProcedure(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void executeConfiguredProcedures(String serverId, String procedureIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "executeConfiguredProcedures";
		String outputReturnVariables = "false";
		
		// Extract variables for the procedureIds
		procedureIds = CommonUtils.extractVariable(prefix, procedureIds, propertyFile, true);

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToResourceXML)) {
			throw new CompositeException("File ["+pathToResourceXML+"] does not exist.");
		}
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		List<ResourceType> resourceList = getResources(serverId, procedureIds, pathToResourceXML, pathToServersXML);

		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) 
			{
				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				 if(DeployUtil.canProcessResource(procedureIds, resourceId)){
					 String arguments ="";
					 if(resource.getArgument() != null){
						 
						 List<String> argumentList = resource.getArgument();
						 int i = 0;
						 for (String argument : argumentList) {
							 
							 if(argument != null) 
							 {
								 // If the argument value contains $ or % then treat it as a variable and resolve the variable
								 argument = CommonUtils.extractVariable(prefix, argument, propertyFile, false);
								 if(i > 0){
									 arguments +=",";
								 }
								 arguments += "'"+argument.trim()+"'";
							 }
							 i++;
						 }
					 }
					 if (resource.getOutputReturnVariables() != null) 
						 outputReturnVariables = resource.getOutputReturnVariables();
					 
					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							if(logger.isInfoEnabled()){
								 logger.info("Executing Procedure "+resourcePath+" with arguments "+arguments+"  outputReturnVariables="+outputReturnVariables);
							 }
							 executeProcedure(serverId, resourcePath, resource.getDataServiceName(), pathToServersXML, arguments, outputReturnVariables); 
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}
				}
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#executeProcedure(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void executeProcedure(String serverId, String procedureName, String dataServiceName, String pathToServersXML, String arguments) throws CompositeException {

		boolean outputReturnVariables = false;
		
		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		if(logger.isInfoEnabled()){
			 logger.info("Executing Procedure "+procedureName+" with arguments "+arguments);
		 }

		getResourceDAO().executeProcedure(serverId, procedureName, dataServiceName, pathToServersXML, arguments, outputReturnVariables);
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#executeProcedure(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void executeProcedure(String serverId, String procedureName, String dataServiceName, String pathToServersXML, String arguments, String outputReturnVariables) throws CompositeException {

		Boolean outputReturnVariablesBool = true;
		if (outputReturnVariables != null && (outputReturnVariables.equalsIgnoreCase("true") || outputReturnVariables.equalsIgnoreCase("false"))) 
			outputReturnVariablesBool = Boolean.valueOf(outputReturnVariables);
		
		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		if(logger.isInfoEnabled()){
			 logger.info("Executing Procedure "+procedureName+" with arguments "+arguments);
		 }

		getResourceDAO().executeProcedure(serverId, procedureName, dataServiceName, pathToServersXML, arguments, outputReturnVariablesBool);
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#deleteResource(java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void deleteResource(String serverId, String resourcePath, String pathToServersXML) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		try {
			String resourceType = getResourceType(serverId, resourcePath, pathToServersXML);

			if(resourceType !=null){
				 if(logger.isInfoEnabled()){
					logger.info("Deleting resource "+resourcePath+" on server "+serverId);
				 }
	
				getResourceDAO().deleteResource(serverId, resourcePath, resourceType, pathToServersXML);
			}
		} catch (CompositeException e) {
			if (e.getMessage().contains("does not exist")) {
				if(logger.isInfoEnabled()){
					logger.info("Ignoring Fault Message...Resource does not exist: "+resourcePath+" on server "+serverId);
				}
			} else {
				throw new CompositeException(e);
			}
		}

	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#deleteResources(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void deleteResources(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "deleteResources";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				 if(DeployUtil.canProcessResource(resourceIds,resourceId)){

					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							deleteResource(serverId, resourcePath, pathToServersXML);
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}					 
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#renameResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void renameResource(String serverId, String resourcePath, String pathToServersXML, String newName) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		Resource resource = getResourceDAO().getResource(serverId, resourcePath, pathToServersXML);
		
		if(resource !=null){
			 if(logger.isInfoEnabled()){
				logger.info("Renaming resource "+resourcePath+" to new name "+newName+" on server "+serverId);
			 }

			getResourceDAO().renameResource(serverId, resourcePath, resource.getType().name(), newName, pathToServersXML);
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#renameResources(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void renameResources(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "renameResources";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToResourceXML)) {
			throw new CompositeException("File ["+pathToResourceXML+"] does not exist.");
		}
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				 if(DeployUtil.canProcessResource(resourceIds,resourceId)){
					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							renameResource(serverId, resourcePath, pathToServersXML, resource.getNewName());
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}					 
				}
			}
		}
	}


	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#copyResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void copyResource(String serverId, String resourcePath, String pathToServersXML, String targetContainerPath, String newName, String copyMode) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		Resource resource = getResourceDAO().getResource(serverId, resourcePath, pathToServersXML);
		
		if(resource !=null){
			 if(logger.isInfoEnabled()){
				logger.info("Copy resource "+resourcePath+" to new path "+targetContainerPath+"/"+newName+" on server "+serverId);
			 }

			 if (copyMode == null) {
				 copyMode = "FAIL_IF_EXISTS";
			 }
			getResourceDAO().copyResource(serverId, resourcePath, resource.getType().name(), targetContainerPath, newName, copyMode, pathToServersXML);
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#copyResources(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void copyResources(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "copyResources";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToResourceXML)) {
			throw new CompositeException("File ["+pathToResourceXML+"] does not exist.");
		}
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				 if(DeployUtil.canProcessResource(resourceIds,resourceId)){
					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							copyResource(serverId,resourcePath, pathToServersXML, resource.getTargetContainerPath(), resource.getNewName(), resource.getCopyMode());
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}					 
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#moveResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void moveResource(String serverId, String resourcePath, String pathToServersXML, String targetContainerPath, String newName) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		Resource resource = getResourceDAO().getResource(serverId, resourcePath, pathToServersXML);
		
		if(resource !=null){
			 if(logger.isInfoEnabled()){
				logger.info("Move resource "+resourcePath+" to new path "+targetContainerPath+"/"+newName+" on server "+serverId);
			 }

			getResourceDAO().moveResource(serverId, resourcePath, resource.getType().name(), targetContainerPath, newName, pathToServersXML);
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#moveResources(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void moveResources(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "moveResources";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToResourceXML)) {
			throw new CompositeException("File ["+pathToResourceXML+"] does not exist.");
		}
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				 if(DeployUtil.canProcessResource(resourceIds,resourceId)){
					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							moveResource(serverId, resourcePath, pathToServersXML, resource.getTargetContainerPath(), resource.getNewName());
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}					 
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#doResourceExist(java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public boolean doResourceExist(String serverId, String resourcePath, String pathToServersXML) {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		Resource resource = getResourceDAO().getResource(serverId, resourcePath, pathToServersXML);
		
		boolean exists = false;
		if(resource !=null){
			exists = true;
		}
		if(logger.isInfoEnabled()){
			logger.info("Resource exists? ["+exists+"] "+resourcePath+" on server "+serverId);
		}
		return exists;
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#resourceExists(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public boolean resourceExists(String serverId, String resourcePath, String resourceType, String pathToServersXML) {
		
		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		boolean exists = getResourceDAO().resourceExists(serverId, resourcePath, resourceType, pathToServersXML);

		if(logger.isInfoEnabled()){
			logger.info("Resource exists? ["+exists+"] "+resourcePath+" on server "+serverId);
		}
		return exists;
	}
	
	
	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#doResourcesExist(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void doResourcesExist(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "doResourcesExist";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				 if(DeployUtil.canProcessResource(resourceIds,resourceId)){
					 
					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							 if(!doResourceExist(serverId, resourcePath, pathToServersXML))
							 {
								 throw new CompositeException("Resource Id "+resourceId +" does not exist on server "+serverId);
							 }
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}					 
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#lockResource(java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void lockResource(String serverId, String resourcePath, String pathToServersXML) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		Resource resource = getResourceDAO().getResource(serverId, resourcePath, pathToServersXML);
		
		if(resource !=null){
			if(logger.isInfoEnabled()){
				logger.info("Locking resource "+resourcePath+" on server "+serverId);
			}
			getResourceDAO().lockResource(serverId, resourcePath, resource.getType().name(), pathToServersXML);
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#lockResources(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void lockResources(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "lockResources";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToResourceXML)) {
			throw new CompositeException("File ["+pathToResourceXML+"] does not exist.");
		}
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				 if(DeployUtil.canProcessResource(resourceIds,resourceId)){
					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							lockResource(serverId, resourcePath, pathToServersXML);
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#unlockResource(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void unlockResource(String serverId, String resourcePath, String pathToServersXML, String comment) throws CompositeException {

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		Resource resource = getResourceDAO().getResource(serverId, resourcePath, pathToServersXML);
		
		if(resource !=null){
			if(logger.isInfoEnabled()){
				logger.info("Unlocking resource "+resourcePath+" on server "+serverId);
			}
			getResourceDAO().unlockResource(serverId, resourcePath, resource.getType().name(), pathToServersXML, comment);
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#unlockResources(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void unlockResources(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "unlockResources";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				if(DeployUtil.canProcessResource(resourceIds,resourceId)){
					 
					String comment = null;
					if (resource.getComment() != null) {
						comment = resource.getComment();
					}
					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							unlockResource(serverId, resourcePath, pathToServersXML, comment);
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#getResourceType(java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public String getResourceType(String serverId, String resourcePath, String pathToServersXML) {

		try {
			Resource resource = getResourceDAO().getResource(serverId, resourcePath, pathToServersXML);
	
			// Return the resource type
			return resource.getType().name();
			
		} catch (CompositeException e) {
			throw new CompositeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#getResourcesFromPath(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public ResourceList getResourcesFromPath(String serverId, String resourcePath, String resourceType, String resourceTypeFilter, String detailLevel,String pathToServersXML) {
		return getResourceDAO().getResourcesFromPath(serverId, resourcePath, resourceType, resourceTypeFilter, detailLevel, pathToServersXML);
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#getImmediateResourcesFromPath(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public ResourceList getImmediateResourcesFromPath(String serverId, String resourcePath, String resourceType, String detailLevel,String pathToServersXML) {
		return getResourceDAO().getImmediateResourcesFromPath(serverId, resourcePath, resourceType, detailLevel, pathToServersXML);
	}

	
	private List<ResourceType> getResources(String serverId, String resources, String pathToResourceXML, String pathToServersXML) {
		// validate incoming arguments
		if(serverId == null || serverId.trim().length() ==0 || resources == null || resources.trim().length() ==0 || pathToServersXML == null || pathToServersXML.trim().length() ==0 || pathToResourceXML == null || pathToResourceXML.trim().length() ==0){
			throw new ValidationException("Invalid Arguments");
		}

		try {
			//using jaxb convert xml to corresponding java objects
			ResourceModule resourceModuleType = (ResourceModule)XMLUtils.getModuleTypeFromXML(pathToResourceXML);
			if(resourceModuleType != null && resourceModuleType.getResource() != null && !resourceModuleType.getResource().isEmpty()){
				return resourceModuleType.getResource();
			}
		} catch (CompositeException e) {
			logger.error("Error while parsing resource xml" , e);
			throw new ApplicationContextException(e.getMessage(), e);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#createFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void createFolder(String serverId, String resourcePath, String pathToServersXML, String recursive) throws CompositeException {
		createFolder(serverId, resourcePath, pathToServersXML, recursive, "false");
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#createFolder(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void createFolder(String serverId, String resourcePath, String pathToServersXML, String recursive, String ignoreErrors) throws CompositeException {
		if(logger.isInfoEnabled()){
			logger.info("Creating folder "+resourcePath+" on server "+serverId);
		}
		getResourceDAO().createFolder(serverId, resourcePath, pathToServersXML, recursive, ignoreErrors);
	}

	/* (non-Javadoc)
	 * @see com.cisco.dvbu.ps.deploytool.services.ResourceManager#createFolders(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
//	@Override
	public void createFolders(String serverId, String resourceIds, String pathToResourceXML, String pathToServersXML) throws CompositeException {

		String prefix = "createFolders";
		
		// Extract variables for the resourceIds
		resourceIds = CommonUtils.extractVariable(prefix, resourceIds, propertyFile, true);

		// Validate whether the files exist or not
		if (!CommonUtils.fileExists(pathToServersXML)) {
			throw new CompositeException("File ["+pathToServersXML+"] does not exist.");
		}

		List<ResourceType> resourceList = getResources(serverId, resourceIds, pathToResourceXML, pathToServersXML);
		if (resourceList != null && resourceList.size() > 0) {
			
			for (ResourceType resource : resourceList) {
				String recursive = "true";
				String ignoreErrors = "false";

				if (resource.getRecursive() != null)
					recursive = resource.getRecursive();

				// Get the identifier and convert any $VARIABLES
				String resourceId = CommonUtils.extractVariable(prefix, resource.getId(), propertyFile, true);
				
				/**
				 * Possible values for archives 
				 * 1. csv string like import1,import2 (we process only resource names which are passed in)
				 * 2. '*' or what ever is configured to indicate all resources (we process all resources in this case)
				 * 3. csv string with '-' or what ever is configured to indicate exclude resources as prefix 
				 * 	  like -import1,import3 (we ignore passed in resources and process rest of the in the input xml
				 */
				if(DeployUtil.canProcessResource(resourceIds,resourceId)){

					// Get ignore errors
					if (resource.getIgnoreErrors() != null)
						ignoreErrors = CommonUtils.extractVariable(prefix, resource.getIgnoreErrors(), propertyFile, true);

					if (resource.getResourcePath() != null && resource.getResourcePath().size() > 0) {
						for (int i=0; i < resource.getResourcePath().size(); i++) {
							String resourcePath = resource.getResourcePath().get(i);
							createFolder(serverId, resourcePath, pathToServersXML, recursive, ignoreErrors);
						}
					} else {
						if(logger.isInfoEnabled()){
							logger.info("No resource paths found for resourceId "+resourceId);
						}						
					}
				}
			}
		}
		
	}
	

	/**
	 * @return the resourceDAO
	 */
	public ResourceDAO getResourceDAO() {
		if(this.resourceDAO == null){
			this.resourceDAO = new ResourceWSDAOImpl();
		}
		return resourceDAO;
	}

	/**
	 * @param resourceDAO the resourceDAO to set
	 */
	public void setResourceDAO(ResourceDAO resourceDAO) {
		this.resourceDAO = resourceDAO;
	}	
}
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
package com.cisco.dvbu.ps.deploytool;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.cisco.dvbu.ps.common.CommonConstants;
import com.cisco.dvbu.ps.common.exception.ApplicationException;
import com.cisco.dvbu.ps.common.exception.CompositeException;
import com.cisco.dvbu.ps.common.exception.ValidationException;
import com.cisco.dvbu.ps.common.util.CommonUtils;
import com.cisco.dvbu.ps.common.util.PropertyManager;

/**
 * @author mtinius, July 2011
 */

// -- CisDeployTool orchestrates over a properties file and executes the commands
public class CisDeployTool {
	
    /** manager instance variable */
    private static CisDeployTool cisDeployToolInstance = null;

    private static Log logger = LogFactory.getLog(CisDeployTool.class);

    private static String propertyFile = CommonConstants.propertyFile;
    private static String suppress = "";
    private static boolean debug1 = false;
    private static boolean debug2 = false;
    private static boolean debug3 = false;
    
    /**
     * returns an instance of the class
     * 
     * @return an instance of PropertyManager
     */
    public static CisDeployTool getInstance()
    {
        if (cisDeployToolInstance == null)
        {
            synchronized (CisDeployTool.class)
            {
                if (cisDeployToolInstance == null)
                {
                	cisDeployToolInstance = new CisDeployTool();
                }
            }
        }
        return cisDeployToolInstance;
    }
   
	/***************************************************************************************************************
	 * Execute the CisDeployTool and orchestrate over the passed in property file to execute actions.
	 * 
	 * @param file - the name of the property file to orchestrate through
	 * @param vcsUser - the VCS user passed in from the command-line invocation
	 * @param vcsPassword - the VCS password passed in from the command-line invocation
	 ***************************************************************************************************************/
	public static void execCisDeployTool(String file, String vcsUser, String vcsPassword) throws CompositeException {

		try {
			/*****************************************
			 * INITIALIZE VARIABLES
			 *****************************************/
			// Trim input variables to remove spaces
			file = file.trim();
			if (vcsUser == null) {
				vcsUser = "";
			}
			if (vcsPassword == null) {
				vcsUser = "";
			}
			vcsUser = vcsUser.trim();
			vcsPassword = vcsPassword.trim();
			
			String prefix = "CisDeployTool";
			boolean exitOrchestrationOnError = false;
			String expectedStatus = null;
			String exitOnError = null;
			String actionType = null;
			String moduleAction = null;
			String arguments = null;
	        String error = null;
	        String padCounter = null;
			String overallExecutionStatus="PASS";
	        String regressionStatus = null;
	        String actualStatus = null;
	        String exitOnErrorPad = null;
	        String actionTypePad = null;

			logger.info("--------------------------------------------------------");
			logger.info("--------------- COMMAND-LINE DEPLOYMENT ----------------");
			logger.info("--------------------------------------------------------");

			/*  Determine the property file name for this environment
			 *    1. Start with default file "deploy.properties"
			 *    2. Get Java Environment variables
			 *    3. Get OS System Environment variables
			 */
	        propertyFile = CommonUtils.getFileOrSystemPropertyValue(null, "CONFIG_PROPERTY_FILE");
	        String propertyOrderPrecedence = CommonUtils.getFileOrSystemPropertyValue(propertyFile, "propertyOrderPrecedence");
	        if (propertyOrderPrecedence == null || propertyOrderPrecedence.trim().length() == 0)
	        	propertyOrderPrecedence = CommonConstants.propertyOrderPrecedenceDefault;
	        
			logger.info("");
			logger.info("----------------------------------------------");
			logger.info("CONFIG_PROPERTY_FILE="+propertyFile);
			logger.info("propertyOrderPrecedence="+propertyOrderPrecedence);
			logger.info("----------------------------------------------");
			
			if (!PropertyManager.getInstance().doesPropertyFileExist(propertyFile)) {
				throw new ApplicationException("The property file does not exist for CONFIG_PROPERTY_FILE="+propertyFile);
			}

			// Set the global suppress and debug flags for this class based on properties found in the property file
			setGlobalProperties();

			/*****************************************
			 * INITIALIZE DEBUG LOG EXECUTION
			 *****************************************/
	        CommonUtils.writeOutput("---------------------------------------------------------------------------",	prefix,"-info",logger,debug1,debug2,debug3);
	        CommonUtils.writeOutput("***** BEGIN DEPLOYMENT ORCHESTRATION *****",									prefix,"-info",logger,debug1,debug2,debug3);
	        CommonUtils.writeOutput("---------------------------------------------------------------------------",	prefix,"-info",logger,debug1,debug2,debug3);
	
			/*****************************************
			 * DISPLAY/VALIDATE PROPERTY VARIABLES
			 *****************************************/
	        validateDeployProperties(prefix);
	        
			/*****************************************
			 * INITIALIZE THE SUMMARY LOG
			 *****************************************/
			// Get the summary log location
	        String summaryLogLocation = CommonUtils.extractVariable(prefix, CommonUtils.getFileOrSystemPropertyValue(propertyFile,"SUMMARY_LOG"), propertyFile, true);
			CommonUtils.createFileWithContent(summaryLogLocation, "");
	
			writeSummaryLog("========================================================================================================",null,null, summaryLogLocation);
			writeSummaryLog("Summary Status Log                                                                                      ",null,null, summaryLogLocation);
			writeSummaryLog("========================================================================================================",null,null, summaryLogLocation);
			writeSummaryLog("Regression Status=Did the execution meet expectations.                                                  ",null,null, summaryLogLocation);
			writeSummaryLog("Expected Status=Did the user expect this action to PASS or FAIL.                                        ",null,null, summaryLogLocation);
			writeSummaryLog("Actual Status=What really happened during execution of this action.                                     ",null,null, summaryLogLocation);
			writeSummaryLog("========================================================================================================",null,null, summaryLogLocation);
			writeSummaryLog("Line #     Regression  Expected  Actual  ExitOnError  ActionType             ModuleAction               ",null,null, summaryLogLocation);
			writeSummaryLog("---------  ----------  --------  ------  -----------  ---------------------  ------------------         ",null,null, summaryLogLocation);

			/*****************************************
			 * READ THE ORCHESTRATION PROPERTY FILE
			 *****************************************/
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = null;
			int lnCount = 0;
			// Iterate through the orchestration property file
			while ( !exitOrchestrationOnError && ((line = reader.readLine()) != null) ) {
				lnCount++;
				padCounter = CommonUtils.padCount(lnCount, 4, " ");

				/*****************************************
				 * TEST LINE FOR BLANK
				 *****************************************/
				if (line.trim().isEmpty() || line.trim().length() == 0) {
					
					CommonUtils.writeOutput("---------------------------------------------------------------------------",	prefix,"-info"+suppress,logger,debug1,debug2,debug3);
					CommonUtils.writeOutput("Line"+padCounter+" [SKIP BLANK]::"+line.trim(),								prefix,"-info"+suppress,logger,debug1,debug2,debug3);
					
				/*****************************************
				 * TEST LINE FOR COMMENT (#)
				 *****************************************/
				} else if (line.trim().startsWith("#")) {
					
					CommonUtils.writeOutput("---------------------------------------------------------------------------",	prefix,"-info"+suppress,logger,debug1,debug2,debug3);
					CommonUtils.writeOutput("Line"+padCounter+" [SKIP COMMENT]::"+line.trim(),								prefix,"-info"+suppress,logger,debug1,debug2,debug3);
					
				// Otherwise this is a line to be processed
				} else {
					
					CommonUtils.writeOutput("---------------------------------------------------------------------------",	prefix,"-info",logger,debug1,debug2,debug3);
					CommonUtils.writeOutput("Line"+padCounter+" "+line.trim(),												prefix,"-info",logger,debug1,debug2,debug3);	

					/*****************************************
					 * PARSE THE LINE
					 *****************************************/
					// Convert tabs to spaces
					line = line.replaceAll("\u0009" , " ");
					// Convert ' "" ' (space quote quote space) into (space quote quote quote quote space) so that the intention of this is not lost during the next conversion
					while (line.contains(" \"\" "))
						line = line.replaceAll(" \"\" "," \"\"\"\" ");
					// Convert double sets of quotes "" to a single double quote "
					line = line.replaceAll("\"\"" , "\"");
					// Trim the line of spaces
					line = line.trim();
					// Tokenize a space separated line
					StringTokenizer st = new StringTokenizer(line, " ");
				    int arg = 0;
				    arguments = "";
				    while (st.hasMoreTokens()) {
				    	arg++;
				    	String val = st.nextToken();
				    	// PASS or FAIL
				    	if (arg == 1) {
				            expectedStatus=CommonUtils.extractVariable(prefix, val, propertyFile, false).toUpperCase();   		
				            if ( expectedStatus.equalsIgnoreCase("PASS") || expectedStatus.equalsIgnoreCase("FAIL") ) {
						        CommonUtils.writeOutput("  expectedStatus="+expectedStatus,									prefix,"-info",logger,debug1,debug2,debug3);
				            } else {
				            	throw new CompositeException("Failure parsing property file.  PARAM1::expectedStatus=["+exitOnError+"] is invalid.  Must be PASS or FAIL.  File=["+file+"]"); 
				            }
				        }
				    	// TRUE or FALSE
				    	if (arg == 2) {
				            exitOnError=CommonUtils.extractVariable(prefix, val, propertyFile, false).toUpperCase(); 
				            if (exitOnError.equalsIgnoreCase("TRUE") || exitOnError.equalsIgnoreCase("FALSE")) {
						        CommonUtils.writeOutput("  exitOnError=   "+exitOnError,									prefix,"-info",logger,debug1,debug2,debug3);
				            } else {
				            	throw new CompositeException("Failure parsing property file.  PARAM2::exitOnError=["+exitOnError+"] is invalid.  Must be TRUE or FALSE.  File=["+file+"]"); 
				            }
				    	}
				    	if (arg == 3) {
				            actionType=val;
					        CommonUtils.writeOutput("  actionType=    "+actionType,											prefix,"-info",logger,debug1,debug2,debug3);
				            // Valid the actionType action against a valid list in the deploy.properties file [ExecuteAction]
				    	}
				    	if (arg == 4) {
				            moduleAction=val;	
					        CommonUtils.writeOutput("  moduleAction=  "+moduleAction,										prefix,"-info",logger,debug1,debug2,debug3);
				            // Valid the moduleAction action against a valid list in the deploy.properties file.
				    	}
				    	if (arg >= 5) {
				    		arguments = arguments.trim() + " " + val;		    		
				    	}
				    }
				    CommonUtils.writeOutput("  arguments=     "+arguments,													prefix,"-info",logger,debug1,debug2,debug3);
			        CommonUtils.writeOutput("",																				prefix,"-info",logger,debug1,debug2,debug3);

			        /*****************************************
					 * VALIDATE MODULE ACTION EXISTS
					 * 	ExecuteAction - executeAction()
					 *****************************************/
			        if ( actionType.contains("ExecuteAction")) {

			        	CompositeException exception = null;
			        	error = null;
				        /*****************************************
						 * INVOKE THE MODULE ACTION: 
						 *****************************************/
						try {
							if (actionType.contains("ExecuteAction")) {
								executeAction(prefix,moduleAction,arguments, vcsUser, vcsPassword);
							}

				        } catch(CompositeException e) {
				        	exception = e;
				        	error = "Review Stack Trace for details.";
				        	if (e.getMessage() != null) {
				        		error = e.getMessage().toString();
				        	}
				        }
			        
				        /*****************************************
						 * PROCESS SUMMARY LOG and HANDLE ERRORS
						 *****************************************/
				         if (exception != null) {
						        // Script executed with error

							    // ###########################################
					        	// # Begin::Write out the summary status
							    // ###########################################
					            if ( expectedStatus.equalsIgnoreCase("PASS")) {
					                regressionStatus="FAIL";
					                overallExecutionStatus="FAIL";
					            }
					            if (expectedStatus.equalsIgnoreCase("FAIL")) {
					                regressionStatus="PASS";
					            }
					            actualStatus="FAIL";
		
								exitOnErrorPad = CommonUtils.rpad(exitOnError, 13, " ");
								actionTypePad = CommonUtils.rpad(actionType, 23, " ");
								writeSummaryLog("Line "+padCounter+"  "+regressionStatus+"        "+expectedStatus+"      "+actualStatus+"    "+exitOnErrorPad+actionTypePad+moduleAction,null,null, summaryLogLocation);
					  		    // ###########################################
					            // # End::Write out the summary status
							    // ###########################################
								
					            if (exitOnError.equalsIgnoreCase("TRUE") ) {
					            	
					            	// Script was set to exit orchestration on error
						            if (expectedStatus.equalsIgnoreCase("FAIL")) {
						            	exitOrchestrationOnError = false;				
						            	CommonUtils.writeOutput("",																					prefix,"-info",logger,debug1,debug2,debug3);
						            	//CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,								prefix,"-info",logger,debug1,debug2,debug3);
										CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
										CommonUtils.writeOutput("Line "+padCounter+"   MESSAGE=Expected Script Error. Script will continue processing.  ERROR="+error,	prefix,"-info",logger,debug1,debug2,debug3);
						            	CommonUtils.writeOutput("",																					prefix,"-info",logger,debug1,debug2,debug3);
						            	
						            } else { // expectedStatus=PASS
						            	
						            	exitOrchestrationOnError = true;
						            	CommonUtils.writeOutput("",																					prefix,"-info",logger,debug1,debug2,debug3);
						            	//CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,								prefix,"-info",logger,debug1,debug2,debug3);
										CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
						            	CommonUtils.writeOutput("",																					prefix,"-info",logger,debug1,debug2,debug3);			
						            }
					            	
								} else { // exitOnError=FALSE
									
						            if (expectedStatus.equalsIgnoreCase("FAIL")) {
						            	exitOrchestrationOnError = false;				
						            	CommonUtils.writeOutput("",																					prefix,"-info",logger,debug1,debug2,debug3);
						            	//CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,								prefix,"-info",logger,debug1,debug2,debug3);
										CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
										CommonUtils.writeOutput("Line "+padCounter+"   MESSAGE=Expected Script Error. Script is set to continue processing.  ERROR="+error,	prefix,"-info",logger,debug1,debug2,debug3);
						            	CommonUtils.writeOutput("",																					prefix,"-info",logger,debug1,debug2,debug3);
						            	
						            } else { // expectedStatus=PASS
						            	
						            	exitOrchestrationOnError = false;
										CommonUtils.writeOutput("",																					prefix,"-info",logger,debug1,debug2,debug3);
										//CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,								prefix,"-info",logger,debug1,debug2,debug3);
										CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
										CommonUtils.writeOutput("Line "+padCounter+"   MESSAGE=Abnormal Script Termination. Script is set to continue processing.  ERROR="+error,	prefix,"-info",logger,debug1,debug2,debug3);
									  	CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3); 
						            }
								}						            
				        	 
				         } else {
				            // Script executed successfully
				            
						    // ###########################################
				            // # Begin::Write out the summary status
						    // ###########################################
				            if ( expectedStatus.equalsIgnoreCase("PASS") ) {
				                regressionStatus="PASS";
				            }
				            if ( expectedStatus.equalsIgnoreCase("FAIL") ) {
				               regressionStatus="FAIL";
				               overallExecutionStatus="FAIL";
				            }
				            actualStatus="PASS";

							exitOnErrorPad = CommonUtils.rpad(exitOnError,13," ");
							actionTypePad = CommonUtils.rpad(actionType,23," ");
							writeSummaryLog("Line "+padCounter+"  "+regressionStatus+"        "+expectedStatus+"      "+actualStatus+"    "+exitOnErrorPad+actionTypePad+moduleAction,null,null, summaryLogLocation); 
					  		// ###########################################
					        // # End::Write out the summary status
							// ###########################################
							
				            if (exitOnError.equalsIgnoreCase("TRUE")) {
				            	// Script is expected to FAIL and Script was set to exit orchestration on error
					            if (expectedStatus.equalsIgnoreCase("FAIL")) {
									exitOrchestrationOnError = true;
									error = "Expected error but execution was successful.";

								  	CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3); 
									CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
									CommonUtils.writeOutput("Line "+padCounter+"   MESSAGE=Abnormal Script Termination.  ERROR="+error,	prefix,"-info",logger,debug1,debug2,debug3);
								  	CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3);
								  	
					            } else { // expectedStatus=PASS
					            	
									exitOrchestrationOnError = false;
						            CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3); 
						            //CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,										prefix,"-info",logger,debug1,debug2,debug3); 
									CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
								  	CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3); 
					            }
				            } else { // exitOnError=FALSE
				            	
				            	// Script is expected to FAIL and Script was NOT set to exit orchestration on error
					            if (expectedStatus.equalsIgnoreCase("FAIL")) {
									exitOrchestrationOnError = false;
									error = "Expected error but execution was successful.";
								  	CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3); 
									CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
									CommonUtils.writeOutput("Line "+padCounter+"   MESSAGE=Abnormal Script Termination. Script is set to continue processing.  ERROR="+error,	prefix,"-info",logger,debug1,debug2,debug3);
									
					            } else { // expectedStatus=PASS
					            	
									exitOrchestrationOnError = false;
						            CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3); 
						            //CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,										prefix,"-info",logger,debug1,debug2,debug3); 
									CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS="+regressionStatus+"  EXPECTED="+expectedStatus+"  ACTUAL="+actualStatus+"  EXIT_ON_ERROR="+exitOnError+"  ACTION="+moduleAction, prefix,"-info",logger,debug1,debug2,debug3);
								  	CommonUtils.writeOutput("",																							prefix,"-info",logger,debug1,debug2,debug3); 
					            }				            	
				            }
				         }
				         
			        } else { // else if (actionType.contains("ExecuteAction") ) {

			        	 error = "Action Type ["+actionType+"] is not valid.";
			        	 
						 // ###########################################
				         // # Begin::Write out the summary status
						 // ###########################################
				         if ( expectedStatus.equalsIgnoreCase("PASS") ) {
				             regressionStatus="FAIL";
				             overallExecutionStatus="FAIL";
				         }
				         if ( expectedStatus.equalsIgnoreCase("FAIL") ) {
				             regressionStatus="PASS";
				         }
				         actualStatus="FAIL";
				         
						 exitOnErrorPad = CommonUtils.rpad(exitOnError,13," ");
						 actionTypePad = CommonUtils.rpad(actionType,23," ");
						 writeSummaryLog("Line "+padCounter+"  "+regressionStatus+"        "+expectedStatus+"      "+actualStatus+"    "+exitOnErrorPad+actionTypePad+moduleAction,null,null, summaryLogLocation); 
				  		 // ###########################################
				         // # End::Write out the summary status
						 // ###########################################
				        
				         if ( exitOnError.equalsIgnoreCase("TRUE") ) {
						  	CommonUtils.writeOutput("",																								prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,											prefix,"-info",logger,debug1,debug2,debug3);
						  	CommonUtils.writeOutput("",																								prefix,"-info",logger,debug1,debug2,debug3);
	
				            CommonUtils.writeOutput("---------------------------------------------------------------------------",					prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("Line "+padCounter+" EXIT_ON_ERROR::"+exitOnError,												prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("Line "+padCounter+"   ERROR_FOUND::"+actionType+" "+moduleAction+" "+arguments,				prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("Abnormal Script Termination. Action Type "+actionType+" does not exist. ERROR="+error,			prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("End of $CONTROLSCRIPT orchestration script.",													prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("---------------------------------------------------------------------------",					prefix,"-info",logger,debug1,debug2,debug3);
				            
				            // Script was set to exit orchestration on error
				            exitOrchestrationOnError = true;
				            
				         } else {
						  	CommonUtils.writeOutput("",																								prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("Line "+padCounter+"   SCRIPT_RESULTS::"+actualStatus,											prefix,"-info",logger,debug1,debug2,debug3);
						  	CommonUtils.writeOutput("",																								prefix,"-info",logger,debug1,debug2,debug3);
	
				            CommonUtils.writeOutput("Line "+padCounter+" EXIT_ON_ERROR::"+exitOnError,												prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("Line "+padCounter+"   ERROR_FOUND::"+actionType+" "+moduleAction+" "+arguments,				prefix,"-info",logger,debug1,debug2,debug3);
				            CommonUtils.writeOutput("Action Type ["+actionType+"] does not exist.  Script is set to continue processing.",			prefix,"-info",logger,debug1,debug2,debug3);
				         }

				  } // end inner if-then-else 
			        
				} // end outer if-then-else
			
			} // end while loop
			
			if (!exitOrchestrationOnError) { // Do Not Exit On Error
				/*****************************************
				 * CLOSE OUT THE SUMMRY LOG
				 *****************************************/
				writeSummaryLog("========================================================================================================",null,null, summaryLogLocation);
				writeSummaryLog("Overall Regression Execution Status="+overallExecutionStatus,											   null,null, summaryLogLocation);
				writeSummaryLog("																										 ",null,null, summaryLogLocation);
				writeSummaryLog("Script ran to completion.																				 ",null,null, summaryLogLocation);
				writeSummaryLog("========================================================================================================",null,null, summaryLogLocation);
	
				/*****************************************
				 * CLOSE OUT DEBUG LOG
				 *****************************************/
				CommonUtils.writeOutput("---------------------------------------------------------------------------",							prefix,"-info",logger,debug1,debug2,debug3);
				CommonUtils.writeOutput("***** COMPLETED DEPLOYMENT ORCHESTRATION SUCCESSFULLY *****",											prefix,"-info",logger,debug1,debug2,debug3);
				CommonUtils.writeOutput("End of CisDeployTool orchestration script.",															prefix,"-info",logger,debug1,debug2,debug3);
				CommonUtils.writeOutput("---------------------------------------------------------------------------",							prefix,"-info",logger,debug1,debug2,debug3);
			} else { // Exit On Error
				/*****************************************
				 * CLOSE OUT THE SUMMRY LOG
				 *****************************************/
            	writeSummaryLog("========================================================================================================",null,null, summaryLogLocation);
            	writeSummaryLog("Overall Regression Execution Status="+overallExecutionStatus,null,null, summaryLogLocation);
            	writeSummaryLog("                                                                                                        ",null,null, summaryLogLocation);
            	writeSummaryLog("Script was set to exit on error.                                                                        ",null,null, summaryLogLocation);
            	writeSummaryLog("========================================================================================================",null,null, summaryLogLocation);

				/*****************************************
				 * CLOSE OUT DEBUG LOG
				 *****************************************/
            	CommonUtils.writeOutput("---------------------------------------------------------------------------",		prefix,"-info",logger,debug1,debug2,debug3);
            	CommonUtils.writeOutput("Abnormal Script Termination. Script will exit.  ERROR="+error,						prefix,"-info",logger,debug1,debug2,debug3);
            	CommonUtils.writeOutput("End of CisDeployTool orchestration script.",										prefix,"-info",logger,debug1,debug2,debug3);
            	CommonUtils.writeOutput("---------------------------------------------------------------------------",		prefix,"-info",logger,debug1,debug2,debug3);				            							            	

            	throw new ApplicationException("Exiting Script with error.  ERROR="+error);
			}
			
		} catch (ValidationException e) {
			throw new CompositeException(e.getMessage(),e);
		} catch (ApplicationException e) {
			throw new CompositeException(e.getMessage(),e);
		} catch (CompositeException e) {
			throw new CompositeException(e.getMessage(),e);
		} catch (FileNotFoundException e) {
			throw new CompositeException(e.getMessage(),e);
		} catch (IOException e) {
			throw new CompositeException(e.getMessage(),e);
		}
	}
    
	// ***********************************************************************************************
	// Execute a Java Action
	// ***********************************************************************************************
	private static void executeAction(String prefix, String moduleAction, String arguments, String vcsUser, String vcsPassword) throws CompositeException {
	
		CommonUtils.writeOutput("***** BEGIN COMMAND: "+moduleAction+" *****",			prefix,"-info",logger,debug1,debug2,debug3);
		CommonUtils.writeOutput("",														prefix,"-info",logger,debug1,debug2,debug3);

		boolean vcsPasswordInArgs = false;
		// Only perform this when the method being invoked is a vcs method.  
		// All VCS methods begin with vcs.
		// Concatenate the vcsUser and vcsPassword to the arguments being passed in.
		if (moduleAction.contains("vcs")) {
			vcsUser = vcsUser.trim();
			vcsPassword = vcsPassword.trim();
			if (vcsUser != null && vcsUser.length() > 0) {
				arguments = arguments + " " + vcsUser;
				if (vcsPassword != null && vcsPassword.length() > 0) {
					arguments = arguments + " " + vcsPassword;
					vcsPasswordInArgs = true;
				} else {
					arguments = arguments + " \"\"";
				}
			} else {
				arguments = arguments + " \"\" \"\"";
			}
		}

		// Parse the arguments into separate variables -- look for double quotes enclosing a variable
		List<String> argList = new ArrayList<String>();
		argList = CommonUtils.parseArguments(argList, true, arguments, false, propertyFile);
	
		try {
			String[] args = new String[argList.size()+1];
			args[0] = moduleAction;
			CommonUtils.writeOutput("Call Action args[0]="+args[0]+" with arguments:",	prefix,"-debug2",logger,debug1,debug2,debug3);
			for (int i = 1; i < argList.size()+1; i++) {
				args[i] = argList.get(i-1);
				if (vcsPasswordInArgs && (i == args.length-1)) {
					CommonUtils.writeOutput("    arg["+i+"]=********",					prefix,"-debug2",logger,debug1,debug2,debug3);
				} else {
					CommonUtils.writeOutput("    arg["+i+"]="+args[i],					prefix,"-debug2",logger,debug1,debug2,debug3);
				}
			}
			CommonUtils.writeOutput("",													prefix,"-debug2",logger,debug1,debug2,debug3);

			// Invoke the DeployManagerUtil command dynamically with n number of arguments
			DeployManagerUtil.main(args);

			CommonUtils.writeOutput("Successfully completed "+moduleAction+".",			prefix,"-info",logger,debug1,debug2,debug3);
			CommonUtils.writeOutput("",													prefix,"-info",logger,debug1,debug2,debug3);

		} catch (CompositeException e) {
		    CommonUtils.writeOutput("Action ["+moduleAction+"] Failed.",				prefix,"-error",logger,debug1,debug2,debug3);
			logger.error("Error occured while executing "+ moduleAction,e);
			throw new CompositeException(e.getMessage(),e);
		}
		
	}

	

/******************************************************************************************
 * 
 *  COMMON METHODS
 * 
 *******************************************************************************************/	
	
	// Validate the common variables (deploy.properties)
	private static void validateDeployProperties(String prefix) throws ValidationException {
		
		// Validate the deploy.properties variables.
		//	1. validate all paths

		String PROJECT_HOME = 	CommonUtils.extractVariable(prefix, CommonUtils.getFileOrSystemPropertyValue(propertyFile,"PROJECT_HOME"), propertyFile, true);
		String MODULE_HOME = 	CommonUtils.extractVariable(prefix, CommonUtils.getFileOrSystemPropertyValue(propertyFile,"MODULE_HOME"), propertyFile, true);
		String SCHEMA_LOCATION = CommonUtils.extractVariable(prefix, CommonUtils.getFileOrSystemPropertyValue(propertyFile,"SCHEMA_LOCATION"), propertyFile, true);
		String SUPPRESS_COMMENTS = CommonUtils.getFileOrSystemPropertyValue(propertyFile, "SUPPRESS_COMMENTS");

		// Display property variables
        CommonUtils.writeOutput("---------------------------------------------------",	prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("Resolved Property Variables:",							prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("---------------------------------------------------",	prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("PROJECT_HOME=     "+PROJECT_HOME,						prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("MODULE_HOME=      "+MODULE_HOME,						prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("SCHEMA_LOCATION=  "+SCHEMA_LOCATION,					prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("SUPPRESS_COMMENTS="+SUPPRESS_COMMENTS,					prefix,"-info",logger,debug1,debug2,debug3);
        // Global debug values were already set prior to invocation of this method.
        CommonUtils.writeOutput("suppress option=  "+suppress,							prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("DEBUG1=           "+debug1,							prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("DEBUG2=           "+debug2,							prefix,"-info",logger,debug1,debug2,debug3);
        CommonUtils.writeOutput("DEBUG3=           "+debug3,							prefix,"-info",logger,debug1,debug2,debug3);
       
		// Validate whether the file or directory exists
		if (MODULE_HOME == null || !CommonUtils.fileExists(MODULE_HOME)) {
			throw new ValidationException("Directory ["+MODULE_HOME+"] does not exist.");
		}
		if (SCHEMA_LOCATION == null || !CommonUtils.fileExists(SCHEMA_LOCATION)) {
			throw new ValidationException("File ["+SCHEMA_LOCATION+"] does not exist.");
		}

	}
	
	// Write an output message to a log
	private static void writeSummaryLog(String message, String prefix, String options, String summaryLogLocation) throws CompositeException {

		// Determine if there is a prefix to prepend
		if (prefix == null) {
			prefix = "";
		} else {
			prefix = prefix+"::";
		}
		//Write out the log if not suppressed
		if (options == null || !options.contains("-suppress")) {
			
			//Write to log when -summary
			CommonUtils.appendContentToFile(summaryLogLocation, message);
		}
	}

	// Set the global suppress and debug properties used throughout this class
	private static void setGlobalProperties() {
		
    	//set to -suppress if the property file contains a SUPPRESS_COMMENTS=true
		suppress="";
		String SUPPRESS_COMMENTS = CommonUtils.getFileOrSystemPropertyValue(propertyFile, "SUPPRESS_COMMENTS");
		if (SUPPRESS_COMMENTS != null && SUPPRESS_COMMENTS.equalsIgnoreCase("true")) {
			suppress=" -suppress";
		} 
		
		// Set the global debug properties used throughout this class
		String validOptions = "true,false";
	    // Get the property from the property file
	    String debug = CommonUtils.getFileOrSystemPropertyValue(propertyFile, "DEBUG1");
	    debug1 = false;
	    if (debug != null && validOptions.contains(debug)) {
	    	debug1 = Boolean.valueOf(debug);
	    }
	    debug = CommonUtils.getFileOrSystemPropertyValue(propertyFile, "DEBUG2");
	    debug2 = false;
	    if (debug != null && validOptions.contains(debug)) {
	    	debug2 = Boolean.valueOf(debug);
	    }
	    debug = CommonUtils.getFileOrSystemPropertyValue(propertyFile, "DEBUG3");
	    debug3 = false;
	    if (debug != null && validOptions.contains(debug)) {
	    	debug3 = Boolean.valueOf(debug);
	    }
	}
	
}

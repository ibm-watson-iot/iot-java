/**
 *****************************************************************************
 * Copyright (c) 2017 IBM Corporation and other Contributors.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Amit M Mangalvedkar - Initial Contribution
 *****************************************************************************
 */

package com.ibm.iotf.client.application.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;

/**
 * This test verifies various IM ReST operations that can be performed on Watson IoT Platform.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IMOperationsTests extends TestCase {
	
	private final static String PROPERTIES_FILE_NAME = "/application.properties";
	private final static String DEVICE_TYPE = "SampleDT";
	private final static String DEVICE_ID1 = "Device01";

	private static boolean setUpIsDone = false;
	
	private static String EVENT_SCHEMA1 = null;
	private static String SCHEMA_NAME1 = null;
	private static String SCHEMA_DESCRIPTION1 = null;
	private static String SCHEMA_TYPE1 = null;
	
	private static String EVENT_SCHEMA2 = null;
	private static String SCHEMA_NAME2 = null;
	private static String SCHEMA_DESCRIPTION2 = null;
	private static String SCHEMA_TYPE2 = null;
	
	private static String EVENT_DESCRIPTION1 = null;

	private static String EVT_TOPIC = null;
	
	
	private static APIClient apiClient = null;
	private static String physicalSchemaId = null;
	private static String physicalInterfaceId = null;
	
	private static String logicalSchemaId = null;
	private static String logicalInterfaceId = null;
	
	private static String eventId = null;
	private static String API_KEY = null;
	
	/**
	 * This sample adds a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDeviceType(String deviceType) throws IoTFCReSTException {
		
		System.out.println("<-- Checking if device type "+deviceType +" already created in Watson IoT Platform");
		boolean exist = apiClient.isDeviceTypeExist(deviceType);
		try {
			if (!exist) {
				System.out.println("<-- Adding device type "+deviceType + " now..");
				// device type to be created in WIoTP
				apiClient.addDeviceType(deviceType, deviceType, null, null);
			}
		} catch(IoTFCReSTException e) {
			System.err.println("ERROR: unable to add manually device type " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * This sample adds a device type using the Java Client Library. 
	 * @throws IoTFCReSTException
	 */
	private void addDevice(String deviceType, String deviceId) throws IoTFCReSTException {
		
		System.out.println("<-- Checking if device " + deviceType + " already created in Watson IoT Platform");
		boolean deviceTypeExist = apiClient.isDeviceTypeExist(deviceType);
		boolean deviceExist = apiClient.isDeviceExist(deviceType, deviceId);
		try {
			if (!deviceTypeExist) {
				System.out.println("<-- Adding device type " + deviceType + " now..");
				// device type to be created in WIoTP
				apiClient.addDeviceType(deviceType, deviceType, null, null);
			} else {
				System.out.println("Device Type " + deviceType + " already exists");
			}
			if (!deviceTypeExist && !deviceExist) {
				System.out.println("<-- Adding device " + deviceId + " now..");
				// device type to be created in WIoTP
				apiClient.registerDevice(deviceType, deviceId, "password", null, null, null);
			} else {
				System.out.println("Device " + deviceId + " already exists");				
			}
			
		} catch(IoTFCReSTException e) {
			System.err.println("ERROR: unable to add manually device type or device s" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public synchronized void setUp() {
	    if (setUpIsDone) {
	        return;
	    }
	    
	    /**
		  * Load device properties
		  */
		Properties props = new Properties();
		try {
			props.load(IMOperationsTests.class.getResourceAsStream(PROPERTIES_FILE_NAME));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			System.exit(-1);
		}	
		
		try {
			//Instantiate the class by passing the properties file
			this.apiClient = new APIClient(props);
			addDeviceType(DEVICE_TYPE);
			addDevice(DEVICE_TYPE, DEVICE_ID1);
			
			API_KEY = props.getProperty("API-Key");
			EVENT_SCHEMA1 = props.getProperty("EVENT_SCHEMA1");
			EVENT_SCHEMA2 = props.getProperty("EVENT_SCHEMA2");
			
			SCHEMA_NAME1 = props.getProperty("SCHEMA_NAME1");
			SCHEMA_DESCRIPTION1 = props.getProperty("SCHEMA_DESCRIPTION1");
			SCHEMA_TYPE1 = props.getProperty("SCHEMA_TYPE1");
			
			SCHEMA_NAME2 = props.getProperty("SCHEMA_NAME2");
			SCHEMA_DESCRIPTION2 = props.getProperty("SCHEMA_DESCRIPTION2");
			SCHEMA_TYPE2 = props.getProperty("SCHEMA_TYPE2");
			
			EVENT_DESCRIPTION1 = props.getProperty("EVENT_DESCRIPTION1");
			EVT_TOPIC = props.getProperty("EVT_TOPIC");
			
			System.out.println("API KEY = " + API_KEY + 
					"\nEVENT_SCHEMA1 = " + EVENT_SCHEMA1 + 
					"\nEVENT_SCHEMA2 = " + EVENT_SCHEMA2 +
					
					"\nSCHEMA_NAME1 = " + SCHEMA_NAME1 + 
					"\nSCHEMA_DESCRIPTION1 = " + SCHEMA_DESCRIPTION1 +
					"\nSCHEMA_TYPE1 = " + SCHEMA_TYPE1 +
					
					"\nSCHEMA_NAME2 = " + SCHEMA_NAME2 + 
					"\nSCHEMA_DESCRIPTION2 = " + SCHEMA_DESCRIPTION2 +
					"\nSCHEMA_TYPE2 = " + SCHEMA_TYPE2 +
					"\nEVENT_DESCRIPTION1 = " + EVENT_DESCRIPTION1 +
					"\nEVT_TOPIC = " + EVT_TOPIC);
		} catch (Exception e) { 
			e.printStackTrace();
			// looks like the application.properties file is not updated properly
			apiClient = null;
		}
	    setUpIsDone = true;
	}
		
	/**
	 * This sample verifies the Schema addition in Watson IoT
	 * 
	 * @throws Exception 
	 */
	public void test01AddEventSchema() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		//Create Schema Resource
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(EVENT_SCHEMA1)));
			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				System.out.println(sCurrentLine);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonObject draftSchemaResponse = apiClient.addDraftSchemaDefinition(new File(EVENT_SCHEMA1), SCHEMA_NAME1, SCHEMA_DESCRIPTION1, SCHEMA_TYPE1);
		physicalSchemaId = draftSchemaResponse.get("id").getAsString();
		System.out.println("Schema created with id = " + physicalSchemaId);
		System.out.println("Schema Object = " + physicalSchemaId);

		try{
			Thread.sleep(5000);
		} catch(InterruptedException iex) {
			
		}
		assertTrue("Schema "+ physicalSchemaId + " got created in the Platform", (physicalSchemaId != null));
	}
	
	
	public void test02RetrieveSchemaDefinitionContent() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Retrieve Schema Content
		JsonObject draftSchemaResponse = apiClient.getDraftSchemaDefinitionContents(physicalSchemaId);
		// check if the devices are actually deleted from the platform1
		System.out.println("Retrieved " );
		assertTrue("Schema Content of Id = "+ physicalSchemaId + " got retrieved from the Platform", (! draftSchemaResponse.isJsonNull()));
	}
	
	public void test03ModifySchemaDefinitionMetadata() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		
//		String schemaIdReturned = apiClient.getDraftSchemaDefinitionMetadata(schemaId).get("schemaId").getAsString();
		JsonObject metadataToBeModified = new JsonObject();
		metadataToBeModified.addProperty("description", SCHEMA_DESCRIPTION1 + " Modified");
		metadataToBeModified.addProperty("name", SCHEMA_NAME1 + " new name");
		metadataToBeModified.addProperty("id", physicalSchemaId);
//		metadataToBeModified.addProperty("schemaId", schemaIdReturned);
		
		JsonObject draftSchemaResponse = apiClient.updateDraftSchemaDefinitionMetadata(physicalSchemaId, metadataToBeModified.toString());
		assertTrue("Schema Metadata of Id = "+ physicalSchemaId + " got modified from the Platform", (! draftSchemaResponse.isJsonNull()));
	}


	public void test05RetrieveSchemaDefinitionMetadata() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		JsonObject draftSchemaResponse = apiClient.getDraftSchemaDefinitionMetadata(physicalSchemaId);
		String schemaIdRetrieved = draftSchemaResponse.get("id").getAsString();
		System.out.println("Retrieved Schema Id from test05 = " + schemaIdRetrieved);
		assertTrue("Schema Metadata of Id = "+ physicalSchemaId + " got retrieved from the Platform", schemaIdRetrieved.equals(physicalSchemaId));
	}

	
	public void test07RetrieveAllDraftSchemas() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		List <NameValuePair> parameters = new ArrayList();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject draftSchemaResponse = apiClient.getDraftSchemas(parameters);
		
		// check if the devices are actually deleted from the platform1
		try {
			String noOfRows = draftSchemaResponse.getAsJsonObject("meta").get("total_rows").toString();
			assertTrue("Schemas retrieved from the Platform = ", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No schemas retrieved from the Platform", true);			
		}
	}

	
	public void test06RetrieveAllActiveSchemas() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Retrieve Schema Content
		List <NameValuePair> parameters = new ArrayList();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject schemaResponse = apiClient.getActiveSchemas(parameters);
		
		// check if the devices are actually deleted from the platform1
		try {
			String noOfRows = schemaResponse.getAsJsonObject("meta").get("total_rows").toString();
			assertTrue("Active Schemas retrieved from the Platform", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("Active Schemas retrieved from the Platform", true);
			
		}
	}

	
	public void test11CreateEventType() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		JsonObject draftEventTypeRequest = new JsonObject();

		Gson gson = new Gson();
		JsonElement element = gson.fromJson(physicalSchemaId, JsonElement.class);
				
		draftEventTypeRequest.add("schemaId", element);
		draftEventTypeRequest.addProperty("name", EVT_TOPIC);
		JsonObject draftEventTypeResponse = apiClient.addDraftEventType(draftEventTypeRequest.toString());
		eventId = draftEventTypeResponse.get("id").getAsString();
		assertTrue("Event "+ eventId + " got created in the Platform", !(eventId.isEmpty() || eventId == null));
	}

	
	public void test12RetrieveSingleEventType() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		JsonObject draftSchemaResponse = apiClient.getDraftEventType(eventId);
		System.out.println("Retrieved " );
		assertTrue("Event Type Id = "+ eventId + " got retrieved from the Platform", (! draftSchemaResponse.isJsonNull()));
		
	}

	public void test13RetrieveAllEventTypes() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		List <NameValuePair> parameters = new ArrayList();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject draftEventResponse = apiClient.getDraftEventTypes(parameters);
		
		// check if the devices are actually deleted from the platform1
		try {
			System.out.println("draftEventResponse = " + draftEventResponse.toString());
			String noOfRows = draftEventResponse.getAsJsonObject("meta").get("total_rows").toString();
			assertTrue("Event Types retrieved from the Platform = ", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No event types retrieved from the Platform", true);			
		}
	}
	
	
	public void test14UpdateEventType() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		
		String schemaId = apiClient.getDraftEventType(eventId).get("schemaId").getAsString();
		
		JsonObject metadataToBeModified = new JsonObject();
		metadataToBeModified.addProperty("description", EVENT_DESCRIPTION1 + " Modified");
		metadataToBeModified.addProperty("name", EVT_TOPIC);
		metadataToBeModified.addProperty("id", eventId);
		metadataToBeModified.addProperty("schemaId", schemaId);

		System.out.println("Event Id = " + eventId);
		JsonObject draftEventResponse = apiClient.updateDraftEventType(eventId, metadataToBeModified.toString());
		assertTrue("Schema Metadata of Id = "+ schemaId + " got modified from the Platform", (! draftEventResponse.isJsonNull()));
	}
	
	
	public void test21CreatePhysicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Create Physical Interfaces
		JsonObject draftPhysicalInterfaceRequest = new JsonObject();
		draftPhysicalInterfaceRequest.addProperty("name", "Env sensor physical interface 7");
		physicalInterfaceId = (apiClient.addDraftPhysicalInterface(draftPhysicalInterfaceRequest.toString()).get("id").getAsString());
		System.out.println("Draft Physical Interface created with id = " + physicalInterfaceId);
		try{
			Thread.sleep(1000);
		} catch(InterruptedException iex) {
			iex.printStackTrace();
		}
		
		assertTrue("Physical Interface with Id "+ physicalInterfaceId + " got created in the Platform", !(physicalInterfaceId == null));
		
	}

	
	public void test22RetrieveSingleDraftPhysicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Retrieve Physical Interfaces
		JsonObject piReturned = apiClient.getDraftPhysicalInterface(physicalInterfaceId);
		String piIdReturned = piReturned.get("id").getAsString();
		assertTrue("Draft Physical Interface Id "+ physicalInterfaceId + " got retrieved from the Platform", piIdReturned.equals(physicalInterfaceId));
	}

	
	public void test23RetrieveAllDraftPhysicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Retrieve Physical Interfaces
		//Retrieve Schema Content
		List <NameValuePair> parameters = new ArrayList();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject draftPIResponse = apiClient.getDraftPhysicalInterfaces(parameters);
		try {
			String noOfRows = draftPIResponse.getAsJsonObject("meta").get("total_rows").toString();
			System.out.println("Physical Interfaces Retrieved " + noOfRows);
			System.out.println("All Physical Interfaces retrieved = " + draftPIResponse);
			assertTrue("Number of Physical Interfaces retrieved from the Platform", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No Physical Interfaces retrieved from the Platform", true);
			
		}
		
	}
	

	public void test24UpdateDraftPhysicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		JsonObject piToBeModified = new JsonObject();
		piToBeModified.addProperty("description", SCHEMA_DESCRIPTION1 + " Modified");
		piToBeModified.addProperty("name", "Some Name");
		piToBeModified.addProperty("id", physicalInterfaceId);
		JsonObject draftPIResponse = apiClient.updateDraftPhysicalInterface(physicalInterfaceId, piToBeModified.toString());
		// check if the devices are actually deleted from the platform1
		assertTrue("Schema Metadata of Id = "+ physicalSchemaId + " got retrieved from the Platform", true);
	
	}

	public void test25MapEventToPhysicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		try{
			JsonObject eventTypeToPIRequest = new JsonObject();
			eventTypeToPIRequest.addProperty("eventId", EVT_TOPIC);
			eventTypeToPIRequest.addProperty("eventTypeId", eventId);
			JsonObject eventTypeToPIResponse = apiClient.addEventToPhysicalInterface(physicalInterfaceId, eventTypeToPIRequest.toString());
			System.out.println("\n4. Event Type to Draft Physical Interface added with eventTypeId = " + eventTypeToPIResponse.get("eventTypeId").getAsString());
			System.out.println("Event Type to Physical Interface = " + eventTypeToPIResponse.toString());		
			assertTrue(true);
		} catch(Exception ex) {
			ex.printStackTrace();
			assertFalse(true);
		}
	}
	

	public void test26RetrieveAllMappedEvents() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		
		List <NameValuePair> parameters = new ArrayList();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonArray draftEventMappingsResponse = apiClient.getEventsMappedToDraftPhysicalInterface(physicalInterfaceId);
		
		// check if the devices are actually deleted from the platform1
		try {
			int noOfRows = draftEventMappingsResponse.size();
			assertTrue("Event Types retrieved from the Platform = ", noOfRows != 0);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No schemas retrieved from the Platform", true);			
		}
	}
	
	public void test31CreateLogicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		
		JsonObject draftLogicalSchemaResponse = apiClient.addDraftSchemaDefinition(new File(EVENT_SCHEMA2), SCHEMA_NAME2, SCHEMA_DESCRIPTION2, SCHEMA_TYPE2);			
		JsonElement draftLogicalSchemaId = draftLogicalSchemaResponse.get("id");
		logicalSchemaId = draftLogicalSchemaId.getAsString();
		
		//Create Logical Interfaces
		//Create Draft Logical Interface
		JsonObject draftLogicalInterfaceRequest = new JsonObject();
		draftLogicalInterfaceRequest.addProperty("name", "environment sensor interface");
		draftLogicalInterfaceRequest.addProperty("schemaId", logicalSchemaId);
		JsonObject draftLogicalInterfaceResponse = apiClient.addDraftLogicalInterface(draftLogicalInterfaceRequest.toString());
		logicalInterfaceId = draftLogicalInterfaceResponse.get("id").getAsString();
		System.out.println("Draft Logical Interface created with id = " + logicalInterfaceId);
		try{
			Thread.sleep(1000);
		} catch(InterruptedException iex) {
			iex.printStackTrace();
		}
		
		assertTrue("Logical Interface with Id "+ logicalInterfaceId + " got created in the Platform", !(logicalInterfaceId == null));
		
	}

	public void test32RetrieveSingleDraftLogicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		JsonObject liReturned = apiClient.getDraftLogicalInterface(logicalInterfaceId);
		String liIdReturned = liReturned.get("id").getAsString();
		assertTrue("Draft Logical Interface Id "+ logicalInterfaceId + " got retrieved from the Platform", liIdReturned.equals(logicalInterfaceId));
	}

	
	public void test33RetrieveAllDraftLogicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		//Retrieve Schema Content
		List <NameValuePair> parameters = new ArrayList();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject draftLIResponse = apiClient.getDraftLogicalInterfaces(parameters);
		try {
			String noOfRows = draftLIResponse.getAsJsonObject("meta").get("total_rows").toString();
			System.out.println("Retrieved " + noOfRows);
			assertTrue("Number of Logical Interfaces retrieved from the Platform", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No Logical Interfaces retrieved from the Platform", true);
			
		}
		
	}


	public void test34UpdateDraftLogicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		JsonObject liToBeModified = new JsonObject();
		liToBeModified.addProperty("description", SCHEMA_DESCRIPTION1 + " Modified");
		liToBeModified.addProperty("name", "Some Name");
		liToBeModified.add("schemaId", apiClient.getDraftLogicalInterface(logicalInterfaceId).get("schemaId"));
		liToBeModified.addProperty("id", logicalInterfaceId);
		JsonObject draftLIResponse = apiClient.updateDraftLogicalInterface(logicalInterfaceId, liToBeModified.toString());
		// check if the devices are actually deleted from the platform1
		assertTrue("Schema Metadata of Id = "+ logicalSchemaId + " got retrieved from the Platform", true);
	
	}
	
	
	public void test41AssociateDraftLogicalInterfaceToDeviceType() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		JsonObject updateDraftDTToPIRequest = new JsonObject();
		JsonObject refs = new JsonObject();
		refs.addProperty("events", "/api/v0002/draft/physicalinterfaces/" + physicalInterfaceId + "/events");

		Gson gson = new Gson();
		JsonElement element = gson.fromJson(refs.toString(), JsonElement.class);

		updateDraftDTToPIRequest.add("refs", element);
		updateDraftDTToPIRequest.addProperty("version", "draft");
		updateDraftDTToPIRequest.addProperty("name", "Env sensor physical interface 1");
		updateDraftDTToPIRequest.addProperty("createdBy", API_KEY);
		updateDraftDTToPIRequest.addProperty("updatedBy", API_KEY);
		updateDraftDTToPIRequest.addProperty("id", physicalInterfaceId);
		JsonObject updateDraftDTToPIResponse = apiClient.addDraftPhysicalInterfaceToDeviceType(DEVICE_TYPE, updateDraftDTToPIRequest.toString());
		
	}
	
	public void test90DeleteMapping() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		boolean mappingDeletion = apiClient.deleteEventMappingFromPhysicalInterface(physicalInterfaceId, EVT_TOPIC);
		assertTrue("Event Type deletion = ", mappingDeletion);
	}

	
	public void test91DeleteEventType() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		boolean eventDeletion = apiClient.deleteDraftEventType(eventId);
		assertTrue("Event Type deletion = ", eventDeletion);
	}
	
	
	public void test94DeleteDraftPhysicalInterfaceFromDeviceType() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		boolean draftPIFromDeviceType = apiClient.deleteDraftPhysicalInterfaceFromDeviceType(DEVICE_TYPE);
		assertTrue("Draft Physical Interface deleted from "+ DEVICE_TYPE, draftPIFromDeviceType);		
	}


	public void test95DeleteLogicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Delete Logical Interfaces
		boolean liDeletion = apiClient.deleteDraftLogicalInterface(logicalInterfaceId);
		assertTrue("Logical Interface "+ logicalInterfaceId + " got deleted from the Platform", liDeletion);		
	}

		
	public void test96DeletePhysicalInterface() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}
		//Delete Physical Interfaces
		boolean piDeletion = apiClient.deleteDraftPhysicalInterface(physicalInterfaceId);
		assertTrue("Physical Interface "+ physicalInterfaceId + " got deleted from the Platform", piDeletion);		
	}
	
	
	public void test99DeleteSchemaDefinitionContent() throws IoTFCReSTException {
		if(apiClient == null) {
			return;
		}

		//Delete Schema Content
		System.out.println("Schema Id used for deletion" + physicalSchemaId);		
		boolean physicalSchemaDeletion = apiClient.deleteDraftSchemaDefinition(physicalSchemaId);
		boolean logicalSchemaDeletion = apiClient.deleteDraftSchemaDefinition(logicalSchemaId);

		// check if the devices are actually deleted from the platform1
		assertTrue("PhysicalSchema = "+ physicalSchemaId + " and LogicalSchema = " + logicalSchemaId + " got deleted from the Platform", physicalSchemaDeletion && logicalSchemaDeletion);
		
	}
	
}
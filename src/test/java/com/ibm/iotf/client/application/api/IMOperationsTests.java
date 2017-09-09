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
import com.ibm.iotf.client.api.APIClient.SchemaOperation;

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
			apiClient = new APIClient(props);
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
		System.out.println("\nInside test method test01AddEventSchema()");
		if(apiClient == null) {
			return;
		}

		//Create Schema Resource
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(EVENT_SCHEMA1)));
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
		try {
			if(br != null) {
				br.close();
			}
		} catch(IOException ioe) {
			br = null;
		}
		assertTrue("Schema "+ physicalSchemaId + " got created in the Platform", (physicalSchemaId != null));
	}
	
	
	public void test02RetrieveSchemaDefinitionContent() throws IoTFCReSTException {
		System.out.println("\nInside test method test02RetrieveSchemaDefinitionContent()");		
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
		System.out.println("\nInside test method test03ModifySchemaDefinitionMetadata()");		
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
		System.out.println("\nInside test method test05RetrieveSchemaDefinitionMetadata()");		
		if(apiClient == null) {
			return;
		}
		JsonObject draftSchemaResponse = apiClient.getDraftSchemaDefinitionMetadata(physicalSchemaId);
		String schemaIdRetrieved = draftSchemaResponse.get("id").getAsString();
		System.out.println("Retrieved Schema Id from test05 = " + schemaIdRetrieved);
		assertTrue("Schema Metadata of Id = "+ physicalSchemaId + " got retrieved from the Platform", schemaIdRetrieved.equals(physicalSchemaId));
	}

	
	public void test07RetrieveAllDraftSchemas() throws IoTFCReSTException {
		System.out.println("\nInside test method test07RetrieveAllDraftSchemas()");		
		if(apiClient == null) {
			return;
		}

		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
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
		System.out.println("\nInside test method test06RetrieveAllActiveSchemas()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Schema Content
		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
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
		System.out.println("\nInside test method test11CreateEventType()");		
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
		System.out.println("\nInside test method test12RetrieveSingleEventType()");		
		if(apiClient == null) {
			return;
		}
		JsonObject draftSchemaResponse = apiClient.getDraftEventType(eventId);
		System.out.println("Retrieved " );
		assertTrue("Event Type Id = "+ eventId + " got retrieved from the Platform", (! draftSchemaResponse.isJsonNull()));
		
	}

	public void test13RetrieveAllEventTypes() throws IoTFCReSTException {
		System.out.println("\nInside test method test14UpdateEventType()");		
		if(apiClient == null) {
			return;
		}
		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
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
		System.out.println("\nInside test method test14UpdateEventType()");		
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
		System.out.println("\nInside test method test21CreatePhysicalInterface()");		
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
		System.out.println("\nInside test method test22RetrieveSingleDraftPhysicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Physical Interfaces
		JsonObject piReturned = apiClient.getDraftPhysicalInterface(physicalInterfaceId);
		String piIdReturned = piReturned.get("id").getAsString();
		assertTrue("Draft Physical Interface Id "+ physicalInterfaceId + " got retrieved from the Platform", piIdReturned.equals(physicalInterfaceId));
	}

	
	public void test23RetrieveAllDraftPhysicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test23RetrieveAllDraftPhysicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Physical Interfaces
		//Retrieve Schema Content
		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
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
		System.out.println("\nInside test method test24UpdateDraftPhysicalInterface()");		
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
		System.out.println("\nInside test method test25MapEventToPhysicalInterface()");		
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
		System.out.println("\nInside test method test26RetrieveAllMappedEvents()");		
		if(apiClient == null) {
			return;
		}
		
		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
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
		System.out.println("\nInside test method test31CreateLogicalInterface()");		
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
		System.out.println("\nInside test method test32RetrieveSingleDraftLogicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		JsonObject liReturned = apiClient.getDraftLogicalInterface(logicalInterfaceId);
		String liIdReturned = liReturned.get("id").getAsString();
		assertTrue("Draft Logical Interface Id "+ logicalInterfaceId + " got retrieved from the Platform", liIdReturned.equals(logicalInterfaceId));
	}

	
	public void test33RetrieveAllDraftLogicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test33RetrieveAllDraftLogicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		//Retrieve Schema Content
		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
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
		System.out.println("\nInside test method test34UpdateDraftLogicalInterface()");		
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
	
	
	public void test35AssociateDraftPhysicalInterfaceToDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test35AssociateDraftPhysicalInterfaceToDeviceType()");		
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
		
		String version = updateDraftDTToPIResponse.get("version").getAsString();
		System.out.println("Response = " + version);
		assertTrue("Response obtained after mapping deviceType and Physical Interface ", version.equals("draft")) ;
	}

	
	public void test36AddDraftLogicalInterfaceToDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test36AddDraftLogicalInterfaceToDeviceType()");		
		if(apiClient == null) {
			return;
		}

		//Add Draft Logical Interface to Device Type
		JsonObject draftLIToDTRequest = new JsonObject();
		JsonObject refs = new JsonObject();
		refs.addProperty("schema", "/api/v0002/draft/schemas/" + logicalSchemaId);

		Gson gson = new Gson();
		JsonElement element = gson.fromJson(refs.toString(), JsonElement.class);

		draftLIToDTRequest.add("refs", element);
		draftLIToDTRequest.addProperty("version", "draft");
		draftLIToDTRequest.addProperty("name", "environment sensor interface");
		draftLIToDTRequest.addProperty("createdBy", API_KEY);
		draftLIToDTRequest.addProperty("updatedBy", API_KEY);
		draftLIToDTRequest.addProperty("schemaId", logicalSchemaId);
		draftLIToDTRequest.addProperty("id", logicalInterfaceId);
		JsonObject draftLIToDTResponse = apiClient.addDraftLogicalInterfaceToDeviceType(DEVICE_TYPE, draftLIToDTRequest.toString());
		System.out.println("Draft Logical Interface added to device Type with id = " + draftLIToDTResponse.get("id").getAsString());
		System.out.println("Draft Logical Interface to Device Type Object = " + draftLIToDTResponse.toString());
		assertTrue("Mapping of Logical Interface to ", true);
		
	}

	
	public void test37AddDraftLogicalInterfaceToDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test36AddDraftLogicalInterfaceToDeviceType()");		
		if(apiClient == null) {
			return;
		}

		JsonObject mappings = new JsonObject();
		mappings.addProperty("logicalInterfaceId", logicalInterfaceId);
		mappings.addProperty("notificationStrategy", "on-state-change");
		JsonObject propertyMapping = new JsonObject();
		propertyMapping.addProperty("temperature", "($event.temp - 32) / 1.8");
		JsonObject tempMapping = new JsonObject();
		Gson gson = new Gson();
		tempMapping.add(EVT_TOPIC, gson.fromJson(propertyMapping.toString(), JsonElement.class));
		JsonElement element = gson.fromJson(tempMapping.toString(), JsonElement.class);
		mappings.add("propertyMappings", tempMapping);
		JsonObject deviceToLImappings = apiClient.addDraftPropertyMappingsToDeviceType(DEVICE_TYPE, mappings.toString());
		System.out.println("\n9. Mapping created between device type and Logical Interface with Id = " + deviceToLImappings.
				get("logicalInterfaceId").getAsString());
		System.out.println("Mapping between device type and logical interface = " + deviceToLImappings.toString());
		
		assertTrue("Mapping created", true);
		
	}
	
	
	public void test38TestOperationAgainstDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test38TestOperationAgainstDeviceType()");		
		if(apiClient == null) {
			return;
		}
		JsonObject validateOperation = new JsonObject();
		validateOperation.addProperty("operation", SchemaOperation.VALIDATE.getOperation());
		JsonObject validated = apiClient.performOperationAgainstDeviceType(DEVICE_TYPE, validateOperation.toString());
		System.out.println("Validate operation = " + validated.toString());

		try {
			Thread.sleep(20000);			
		} catch (InterruptedException iex) {
			iex.printStackTrace();
		}
		assertTrue("Validation Operation completed", true);
	}
	
	public void test39TestOperationAgainstDraftLogicalInterface() throws IoTFCReSTException {
		//Activating configuration
		System.out.println("\nInside test method test39TestOperationAgainstDraftLogicalInterface()");		
		JsonObject activateOperation = new JsonObject();
		activateOperation.addProperty("operation", SchemaOperation.ACTIVATE.getOperation());
		System.out.println("id" + logicalInterfaceId + " activateOperation.toString()" + activateOperation.toString() );

		JsonObject activated = apiClient.performOperationAgainstDraftLogicalInterface(logicalInterfaceId, activateOperation.toString());
		System.out.println("Activate operation = " + activated.toString());

		try {
			Thread.sleep(20000);			
		} catch (InterruptedException iex) {
			iex.printStackTrace();
		}
		assertTrue("Activation Operation completed", true);
	}
	
	
	public void test42RetrieveDeviceTypesWithLogicalOrPhysical() throws IoTFCReSTException {
		System.out.println("\nInside test method test42RetrieveDeviceTypesWithLogicalOrPhysical()");		
		if(apiClient == null) {
			return;
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ie) {
			
		}
		try {
			List <NameValuePair> parameters = new ArrayList<NameValuePair>();
			//NameValuePair nvp1 = new BasicNameValuePair("_limit", "25");
			NameValuePair nvp2 = new BasicNameValuePair("physicalInterfaceId", physicalInterfaceId);
			
			//parameters.add(nvp1);
			parameters.add(nvp2);
			
			JsonObject draftDeviceTypesResponse = apiClient.getDeviceTypesAssociatedWithLogicalOrPhysicalInterface(parameters);
			if(draftDeviceTypesResponse != null) {
				System.out.println("THIS RESPONSE = " + draftDeviceTypesResponse);
				
				int size = draftDeviceTypesResponse.getAsJsonObject("meta").getAsJsonPrimitive("total_rows").getAsInt();
				System.out.println("Size retrieved = " + size);
				assertTrue("Property Mappings list has size = " + size, size != 0);
			} else {
				assertFalse("Unable to get response", true);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No size obtained ", true);
		}
	}
	

	public void test43RetrieveDraftProperyMappings() throws IoTFCReSTException {
		System.out.println("\nInside test method test43RetrieveDraftProperyMappings()");		
		if(apiClient == null) {
			return;
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ie) {
			
		}
		try {
			JsonArray propertyMappingsArray = apiClient.getDraftPropertyMappingsForDeviceType(DEVICE_TYPE);
			
			int size = propertyMappingsArray.size();
			System.out.println("Size retrieved = " + size);
			assertTrue("Property Mappings list has size = " + size, size != 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No size obtained ", true);
		}
	}
	
	
	public void test44RetrieveActiveLogicalInterfacesAssociatedWithDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test44RetrieveActiveLogicalInterfacesAssociatedWithDeviceType()");		
		if(apiClient == null) {
			return;
		}
		try {
			JsonArray activeLogicalInterfacesArray = apiClient.getActiveLogicalInterfacesForDeviceType(DEVICE_TYPE);
			
			int size = activeLogicalInterfacesArray.size();
			System.out.println("Size retrieved = " + size);
			assertTrue("Active Logical Interface list has size = " + size, size != 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No size obtained ", true);
		}
	}
	

	public void test45RetrieveActivePropertyMappingsForDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test45RetrieveActivePropertyMappingsForDeviceType()");		
		if(apiClient == null) {
			return;
		}
		try {
			JsonArray activePropertyMappings = apiClient.getActivePropertyMappingsForDeviceType(DEVICE_TYPE);
			
			int size = activePropertyMappings.size();
			System.out.println("Size retrieved = " + size);
			assertTrue("Active Property Mappings list has size = " + size, size != 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No size obtained ", true);
		}
	}
	

	public void test46RetrieveActivePropertyMappingsForSpecificLogicalInterfaceDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test46RetrieveActivePropertyMappingsForSpecificLogicalInterfaceDeviceType()");		
		if(apiClient == null) {
			return;
		}
		try {
			JsonObject activePropertyMappings = (apiClient.getActivePropertyMappingsForLogicalInterfaceOfDeviceType(DEVICE_TYPE, logicalInterfaceId)).getAsJsonObject("propertyMappings");
			
			System.out.println("Active Property Mappings retrieved = " + activePropertyMappings);
			assertTrue("Active Property Mappings = " + activePropertyMappings, activePropertyMappings != null);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No size obtained ", true);
		}
	}
	

	public void test47RetrieveActivePhysicalInterfaceAssociatedWithDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test47RetrieveActivePhysicalInterfaceAssociatedWithDeviceType()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		JsonObject activePIReturned = apiClient.getActivePhysicalInterfaceForDeviceType(DEVICE_TYPE);
		String activePIIdReturned = activePIReturned.get("id").getAsString();
		assertTrue("Active Physical Interface Id "+ activePIIdReturned + " got retrieved from the Platform", true);
	}

	
	public void test48RetrieveActiveEventType() throws IoTFCReSTException {
		System.out.println("\nInside test method test48RetrieveActiveEventType()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		JsonObject activeEventTypeReturned = apiClient.getActiveEventType(eventId);
		String activeEventTypeIdReturned = activeEventTypeReturned.get("id").getAsString();
		assertTrue("Active Event Type Returned Id "+ activeEventTypeIdReturned + " got retrieved from the Platform", true);
	}

	
	public void test49RetrieveAllActiveEventTypes() throws IoTFCReSTException {
		System.out.println("\nInside test method test49RetrieveAllActiveEventTypes()");		
		if(apiClient == null) {
			return;
		}

		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject activeEventTypesResponse = apiClient.getActiveEventTypes(parameters);
		System.out.println("Response = " + activeEventTypesResponse.toString());
		try {
			String noOfRows = activeEventTypesResponse.getAsJsonObject("meta").get("total_rows").toString();
			System.out.println("Retrieved " + noOfRows);
			assertTrue("Number of Active Event Types retrieved from the Platform", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No Active Event Types from the Platform", true);
			
		}
		
	}
	

	public void test50RetrieveActiveLogicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test50RetrieveActiveLogicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		JsonObject activeLogicalInterfaceReturned = apiClient.getActiveLogicalInterface(logicalInterfaceId);
		String activeLogicalInterfaceIdReturned = activeLogicalInterfaceReturned.get("id").getAsString();
		assertTrue("Active Logical Interface Returned Id "+ activeLogicalInterfaceIdReturned + " got retrieved from the Platform", true);
	}

	
	public void test51RetrieveAllActiveLogicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test51RetrieveAllActiveLogicalInterface()");		
		if(apiClient == null) {
			return;
		}

		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject activeLogicalInterfaceResponse = apiClient.getActiveLogicalInterfaces(parameters);
		System.out.println("Response = " + activeLogicalInterfaceResponse.toString());
		try {
			String noOfRows = activeLogicalInterfaceResponse.getAsJsonObject("meta").get("total_rows").toString();
			System.out.println("Retrieved " + noOfRows);
			assertTrue("Number of Active Logical Interface retrieved from the Platform", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No Active Logical Interfaces from the Platform", true);
			
		}
		
	}
	

	public void test52RetrieveActivePhysicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test50RetrieveActivePhysicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Physical Interfaces
		JsonObject activePhyicalInterfaceReturned = apiClient.getActivePhysicalInterface(physicalInterfaceId);
		String activePhysicalInterfaceIdReturned = activePhyicalInterfaceReturned.get("id").getAsString();
		assertTrue("Active Physical Interface Returned Id "+ activePhysicalInterfaceIdReturned + " got retrieved from the Platform", true);
	}

	
	public void test53RetrieveAllActivePhysicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test51RetrieveAllActivelogicalInterface()");		
		if(apiClient == null) {
			return;
		}

		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject activePhysicalInterfaceResponse = apiClient.getActivePhysicalInterfaces(parameters);
		System.out.println("Response = " + activePhysicalInterfaceResponse.toString());
		try {
			String noOfRows = activePhysicalInterfaceResponse.getAsJsonObject("meta").get("total_rows").toString();
			System.out.println("Retrieved " + noOfRows);
			assertTrue("Number of Active Physical Interface retrieved from the Platform", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No Active Physical Interfaces from the Platform", true);
			
		}
		
	}
	

	public void test54RetrieveActiveSchemaDefinitionMetadata() throws IoTFCReSTException {
		System.out.println("\nInside test method test54RetrieveActiveSchemaDefinitionMetadata()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Schema Definition Metadata
		JsonObject activeSchemaDefinitionMetadataReturned = apiClient.getActiveSchemaDefinitionMetadata(physicalSchemaId);
		String activeSchemaDefinitionMetadataIdReturned = activeSchemaDefinitionMetadataReturned.get("id").getAsString();
		assertTrue("Active Schema Definition Id Returned "+ activeSchemaDefinitionMetadataIdReturned + " got retrieved from the Platform", true);
	}

	
	public void test55RetrieveAllActiveSchemas() throws IoTFCReSTException {
		System.out.println("\nInside test method test55RetrieveAllActiveSchemas()");		
		if(apiClient == null) {
			return;
		}

		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject activeSchemasResponse = apiClient.getActiveSchemas(parameters);
		System.out.println("Response = " + activeSchemasResponse.toString());
		try {
			String noOfRows = activeSchemasResponse.getAsJsonObject("meta").get("total_rows").toString();
			System.out.println("Retrieved " + noOfRows);
			assertTrue("Number of Active Schema retrieved from the Platform", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No Active Schemas from the Platform", true);
			
		}
		
	}
	

	public void test56RetrieveActiveSchemaDefinitionFile() throws IoTFCReSTException {
		System.out.println("\nInside test method test56RetrieveActiveSchemaDefinitionFile()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Schema Definition File
		JsonObject activeSchemaDefinitionContentReturned = apiClient.getActiveSchemaDefinitionContents(physicalSchemaId);
		System.out.println("Schema Definition Content = " + activeSchemaDefinitionContentReturned);
		assertTrue("Active Schema Definition Content ", true);
	}


	public void test81DeleteDraftPhysicalInterfaceFromDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test81DeleteDraftPhysicalInterfaceFromDeviceType()");		
		if(apiClient == null) {
			return;
		}
		boolean draftPIFromDeviceType = apiClient.deleteDraftPhysicalInterfaceFromDeviceType(DEVICE_TYPE);
		assertTrue("Draft Physical Interface deleted from "+ DEVICE_TYPE, draftPIFromDeviceType);		
	}


	public void test83DeleteDraftPropertyMappings() throws IoTFCReSTException {
		System.out.println("\nInside test method test94DeleteDraftPropertyMappings()");		
		if(apiClient == null) {
			return;
		}
		boolean draftLIFromDeviceType = apiClient.deleteDraftPropertyMappings(DEVICE_TYPE, logicalInterfaceId);
		assertTrue("Draft Property Mappings for Logical Interface deleted from "+ DEVICE_TYPE, draftLIFromDeviceType);		
	}

	
	public void test85DeleteDraftLogicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test85DeleteDraftLogicalInterface()");		
		if(apiClient == null) {
			return;
		}
		boolean draftLIFromDeviceType = apiClient.deleteDraftLogicalInterface(DEVICE_TYPE, logicalInterfaceId);
		
		assertTrue("Draft Physical Interface deleted from "+ DEVICE_TYPE, draftLIFromDeviceType);		
	}

	
	public void test87DeleteMapping() throws IoTFCReSTException {
		System.out.println("\nInside test method test87DeleteMapping()");		
		if(apiClient == null) {
			return;
		}

		boolean mappingDeletion = apiClient.deleteEventMappingFromPhysicalInterface(physicalInterfaceId, EVT_TOPIC);
		assertTrue("Event Type deletion = ", mappingDeletion);
	}

	
	public void test89TestDeactivateOperation() throws IoTFCReSTException {
		System.out.println("\nInside test method test89TestDeactivateOperation()");		
		if(apiClient == null) {
			return;
		}

		JsonObject deactivateOperation = new JsonObject();
		deactivateOperation.addProperty("operation", SchemaOperation.DEACTIVATE.getOperation());
		System.out.println("id" + logicalInterfaceId + " deactivateOperation.toString()" + deactivateOperation.toString() );

		JsonObject deactivated = apiClient.performOperationAgainstLogicalInterface(logicalInterfaceId, deactivateOperation.toString());
		System.out.println("\n16. Dectivate operation = " + deactivated.toString());
		
		assertTrue("Event Type deletion = ", true);
	}

	
	public void test90DeletePhysicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test90DeletePhysicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Delete Physical Interfaces
		boolean piDeletion = apiClient.deleteDraftPhysicalInterface(physicalInterfaceId);
		assertTrue("Physical Interface "+ physicalInterfaceId + " got deleted from the Platform", piDeletion);		
	}
	
	
	public void test91DeleteEventType() throws IoTFCReSTException {
		System.out.println("\nInside test method test91DeleteEventType()");		
		if(apiClient == null) {
			return;
		}

		boolean eventDeletion = apiClient.deleteDraftEventType(eventId);
		assertTrue("Event Type deletion = ", eventDeletion);
	}
	

	public void test96DeleteLogicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test96DeleteLogicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Delete Logical Interfaces
		boolean liDeletion = apiClient.deleteDraftLogicalInterface(logicalInterfaceId);
		assertTrue("Logical Interface "+ logicalInterfaceId + " got deleted from the Platform", liDeletion);		
	}

	
	public void test99DeleteSchemaDefinitionContent() throws IoTFCReSTException {
		System.out.println("\nInside test method test99DeleteSchemaDefinitionContent()");		
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
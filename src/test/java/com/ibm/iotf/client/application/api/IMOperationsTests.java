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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import com.ibm.iotf.client.IoTFCReSTException;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.api.APIClient.SchemaOperation;
import com.ibm.iotf.test.common.TestEnv;
import com.ibm.iotf.util.LoggerUtility;

/**
 * This test verifies various IM ReST operations that can be performed on Watson IoT Platform.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IMOperationsTests {
	
	private final static String CLASS_NAME = IMOperationsTests.class.getName();
	private final static String APP_ID = "IMOpApp1";
	private final static String DEVICE_TYPE = "IMOpType1";
	private final static String DEVICE_ID = "IMOpDev1";
	
	private static String EVENT_SCHEMA1 = "fahrenhietSchema.json";
	private static String SCHEMA_NAME1 = "fahrenhietSchema";
	private static String SCHEMA_DESCRIPTION1 = "Schema to capture the temperature readings in Fahrenhiet";
	private static String SCHEMA_TYPE1 = "json-schema";
	private static final String jsonStringSchema1 =  "{"
        + "\"$schema\": \"http://json-schema.org/draft-04/schema#\","
        + "\"type\": \"object\","
        + "\"title\": \"EnvSensor4 tempEvent Schema\","
        + "\"description\": \"defines the structure of a temperature event in degrees Fahrenheit \","
        + "\"properties\": {"
        +        "\"temp\": {"
        +                "\"description\": \"temperature in degrees Fahrenheit\","
        +                "\"type\": \"number\","
        +                "\"minimum\": -459.67,"
        +                "\"default\": 75.0"
        +        "}"
        + "},"
        + "\"required\": [\"temp\"] }";
	
	
	private static String EVENT_SCHEMA2 = "celciusSchema.json";
	private static String SCHEMA_NAME2 = "celciusSchema";
	private static String SCHEMA_DESCRIPTION2 = "Schema to capture the temperature readings in Celcius";
	private static String SCHEMA_TYPE2 = "json-schema";
	private static final String jsonStringSchema2 =  "{"
	        + "\"$schema\": \"http://json-schema.org/draft-04/schema#\","
	        + "\"type\": \"object\","
	        + "\"title\": \"Environment Sensor Schema\","
	        + "\"description\": \"temperature in degrees Celsius\","
	        + "\"properties\": {"
	        +        "\"temperature\": {"
	        +                "\"description\": \"temperature in degrees Celsius\","
	        +                "\"type\": \"number\","
	        +                "\"minimum\": -273.15,"
	        +                "\"default\": 0.0"
	        +        "}"
	        + "},"
	        + "\"required\": [\"temperature\"] }";
	
	private static String EVENT_DESCRIPTION1 = "Native event definition for the MyBulbCo light bulb on event";
	private static String EVT_TOPIC = "tempEvent";
	
	
	private static APIClient apiClient = null;
	private static String physicalSchemaId = null;
	private static String physicalInterfaceId = null;
	
	private static String logicalSchemaId = null;
	private static String logicalInterfaceId = null;
	
	private static String eventId = null;
	private static String API_KEY = null;
	
	private final String currentDir = System.getProperty("user.dir");
	
	private static void writeToFile(String filename, String content) {
		final String METHOD = "writeToFile";
		String pathname = System.getProperty("user.dir") + File.pathSeparator + filename;
		LoggerUtility.info(CLASS_NAME, METHOD, "pathname: " + pathname);
		PrintWriter writer;
		try {
			writer = new PrintWriter(pathname, "UTF-8");
			writer.println(content);
			writer.close();		
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			LoggerUtility.severe(CLASS_NAME, METHOD, e.getMessage());
		}
	}
	
	private static void createSchema1() {
		JsonParser parser = new JsonParser();
		JsonElement schema = parser.parse(jsonStringSchema1);
		writeToFile(EVENT_SCHEMA1, schema.getAsString());
	}

	private static void createSchema2() {
		JsonParser parser = new JsonParser();
		JsonElement schema = parser.parse(jsonStringSchema2);
		writeToFile(EVENT_SCHEMA2, schema.getAsString());
	}
	

	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		
		createSchema1();
		createSchema2();
		
		Properties appProps = TestEnv.getAppProperties(APP_ID, false, DEVICE_TYPE, DEVICE_ID);
		apiClient = new APIClient(appProps);

		// Delete device if it was left from the last test run
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		// If the device type does not exist, create it
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE) == false) {
			apiClient.addDeviceType(DEVICE_TYPE, null, null, null);
		}
		
		// Register the test device DEVICE_ID
		apiClient.registerDevice(DEVICE_TYPE, DEVICE_ID, TestEnv.getDeviceToken(), null, null, null);

	}
	
	@AfterClass
	public static void oneTimeCleanup() throws Exception {
		
		if (apiClient.isDeviceExist(DEVICE_TYPE, DEVICE_ID)) {
			apiClient.deleteDevice(DEVICE_TYPE, DEVICE_ID);
		}
		
		if (apiClient.isDeviceTypeExist(DEVICE_TYPE)) {
			apiClient.deleteDeviceType(DEVICE_TYPE);
		}
	}	
	
	/**
	 * This sample verifies the Schema addition in Watson IoT
	 * 
	 * @throws Exception 
	 */
	public void test01AddEventSchema() throws IoTFCReSTException {
		final String METHOD = "test01AddEventSchema";

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

		LoggerUtility.info(CLASS_NAME, METHOD,"Schema created with id = " + physicalSchemaId);
		
		//System.out.println("Schema Object = " + physicalSchemaId);

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
		final String METHOD = "test02RetrieveSchemaDefinitionContent";
		LoggerUtility.info(CLASS_NAME, METHOD, "Get schema ID " + physicalSchemaId);
		JsonObject draftSchemaResponse = apiClient.getDraftSchemaDefinitionContents(physicalSchemaId);
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

	
	public void test11AddDraftEventType() throws IoTFCReSTException {
		System.out.println("\nInside test method test11AddDraftEventType()");		
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

	
	public void test12GetDraftEventType() throws IoTFCReSTException {
		System.out.println("\nInside test method test12GetDraftEventType()");		
		if(apiClient == null) {
			return;
		}
		JsonObject draftSchemaResponse = apiClient.getDraftEventType(eventId);
		assertTrue("Event Type Id = "+ eventId + " got retrieved from the Platform", (! draftSchemaResponse.isJsonNull()));
	}

	
	public void test14GetDraftEventTypes() throws IoTFCReSTException {
		System.out.println("\nInside test method test14GetDraftEventTypes");		
		if(apiClient == null) {
			return;
		}
		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject draftEventResponse = apiClient.getDraftEventTypes(parameters);

		try {
			System.out.println("draftEventResponse = " + draftEventResponse.toString());
			String noOfRows = draftEventResponse.getAsJsonObject("meta").get("total_rows").toString();
			assertTrue("Draft Event Types retrieved from the Platform = ", noOfRows != null);

		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No draft event types retrieved from the Platform", true);			
		}
	}
	
	
	public void test14UpdateDraftEventType() throws IoTFCReSTException {
		System.out.println("\nInside test method test14UpdateDraftEventType()");		
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

	
	public void test22GetDraftPhysicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test22GetDraftPhysicalInterface()");		
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

	public void test32GetDraftLogicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test32GetDraftLogicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		JsonObject liReturned = apiClient.getDraftLogicalInterface(logicalInterfaceId);
		String liIdReturned = liReturned.get("id").getAsString();
		assertTrue("Draft Logical Interface Id "+ logicalInterfaceId + " got retrieved from the Platform", liIdReturned.equals(logicalInterfaceId));
	}

	
	public void test33GetAllDraftLogicalInterfaces() throws IoTFCReSTException {
		System.out.println("\nInside test method test33GetAllDraftLogicalInterfaces()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		//Retrieve Schema Content
		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject draftLIResponse = apiClient.getAllDraftLogicalInterfaces(parameters);
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
		JsonObject updateDraftDTToPIResponse = apiClient.associateDraftPhysicalInterfaceWithDeviceType(DEVICE_TYPE, updateDraftDTToPIRequest.toString());
		
		String version = updateDraftDTToPIResponse.get("version").getAsString();
		System.out.println("Response = " + version);
		assertTrue("Response obtained after mapping deviceType and Physical Interface ", version.equals("draft")) ;
	}

	
	public void test36AssociateDraftLogicalInterfaceToDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test36AssociateDraftLogicalInterfaceToDeviceType()");		
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
		JsonObject draftLIToDTResponse = apiClient.associateDraftLogicalInterfaceToDeviceType(DEVICE_TYPE, draftLIToDTRequest.toString());
		System.out.println("Draft Logical Interface added to device Type with id = " + draftLIToDTResponse.get("id").getAsString());
		System.out.println("Draft Logical Interface to Device Type Object = " + draftLIToDTResponse.toString());
		assertTrue("Mapping of Logical Interface to ", true);
		
	}

	
	public void test37AddDraftPropertyMappingsToDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test37AddDraftPropertyMappingsToDeviceType()");		
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
//		JsonElement element = gson.fromJson(tempMapping.toString(), JsonElement.class);
		mappings.add("propertyMappings", tempMapping);
		JsonObject deviceToLImappings = apiClient.addDraftPropertyMappingsToDeviceType(DEVICE_TYPE, mappings.toString());
		
		assertTrue("Mapping created", deviceToLImappings.get("logicalInterfaceId").getAsString().equals(logicalInterfaceId));
	}


	public void test37UpdateDraftPropertyMappingsToDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test37UpdateDraftPropertyMappingsToDeviceType()");		
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
		mappings.add("propertyMappings", tempMapping);
		JsonObject deviceToLImappings = apiClient.updateDraftPropertyMappingsForSpecificLogicalInterfaceOfDeviceType(DEVICE_TYPE, logicalInterfaceId, mappings.toString());
		
		assertTrue("Mapping modified", deviceToLImappings.get("logicalInterfaceId").getAsString().equals(logicalInterfaceId));
	}

	
	public void test38GetDraftPhysicalInterfaceAssociatedWithDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test38GetDraftPhysicalInterfaceAssociatedWithDeviceType()");		
		if(apiClient == null) {
			return;
		}

		JsonObject piReturned = apiClient.getDraftPhysicalInterfaceAssociatedWithDeviceType(DEVICE_TYPE);
		String piIdReturned = piReturned.get("id").getAsString();
		assertTrue("Draft Physical Interface Id "+ physicalInterfaceId + " got retrieved from the Platform", piIdReturned.equals(physicalInterfaceId));
	}

	
	public void test38GetDraftLogicalInterfacesAssociatedWithDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test38getDraftLogicalInterfacesAssociatedWithDeviceType()");		
		if(apiClient == null) {
			return;
		}

		JsonArray liReturned = apiClient.getDraftLogicalInterfacesAssociatedWithDeviceType(DEVICE_TYPE);
		int liSizeReturned = liReturned.size();
		assertTrue("Total Draft Logical Interfaces retrieved from the Platform = " + liSizeReturned, liSizeReturned != 0);
	}

	
	
	public void test38PerformOperationAgainstDraftDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test38PerformOperationAgainstDraftDeviceType()");		
		if(apiClient == null) {
			return;
		}
		JsonObject validateOperation = new JsonObject();
		try {
			validateOperation.addProperty("operation", SchemaOperation.VALIDATE.getOperation());
			JsonObject validated = apiClient.performOperationAgainstDraftDeviceType(DEVICE_TYPE, validateOperation.toString());
			System.out.println("Validate operation = " + validated.toString());
			try {
				Thread.sleep(20000);			
			} catch (InterruptedException iex) {
				iex.printStackTrace();
			}
			assertTrue("performOperationAgainstDraftDeviceType succeeded", true);

		} catch (Exception ex) {
			assertFalse("performOperationAgainstDraftDeviceType failed", true);			
		}
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
	
	
	public void test42GetDeviceTypesAssociatedWithLogicalOrPhysicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test42GetDeviceTypesAssociatedWithLogicalOrPhysicalInterface()");		
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
				int size = draftDeviceTypesResponse.getAsJsonObject("meta").getAsJsonPrimitive("total_rows").getAsInt();
				assertTrue("Total Logical or Physical Interfaces mapped to device type = " + size, size != 0);
			} else {
				assertFalse("Unable to get retrieve list of interfaces mapped to device type", true);
			}
		} catch (Exception ex) {
			assertFalse("Unable to get retrieve list of interfaces mapped to device type", true);
		}
	}
	

	public void test43GetDraftPropertyMappingsForDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test43GetDraftPropertyMappingsForDeviceType()");		
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
	
	
	public void test43GetDraftPropertyMappingsForSpecificLogicalInterfaceForDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test43GetDraftPropertyMappingsForSpecificLogicalInterfaceForDeviceType()");		
		if(apiClient == null) {
			return;
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException ie) {
			
		}
		try {
			JsonObject propertyMapping = apiClient.getDraftPropertyMappingsForSpecificLogicalInterfaceDeviceType(DEVICE_TYPE, logicalInterfaceId);
			
			String logicalInterfaceIdReturned = propertyMapping.get("logicalInterfaceId").getAsString();
			assertTrue("Property Mappings retrieved with logicalInterfaceId = " + logicalInterfaceIdReturned, logicalInterfaceIdReturned.equals(logicalInterfaceId) );
		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("No property Mapping retrieved ", true);
		}
	}
	
	
	public void test44GetActiveLogicalInterfacesForDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test44GetActiveLogicalInterfacesForDeviceType()");		
		if(apiClient == null) {
			return;
		}
		try {
			JsonArray activeLogicalInterfacesArray = apiClient.getActiveLogicalInterfacesForDeviceType(DEVICE_TYPE);
			int size = activeLogicalInterfacesArray.size();
			assertTrue("Active Logical Interface list has size = " + size, size != 0);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertFalse("Active Logical Interface list not retrieved ", true);
		}
	}
	

	public void test45GetActivePropertyMappingsForDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test45GetActivePropertyMappingsForDeviceType()");		
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
	

	public void test46GetActivePropertyMappingsForLogicalInterfaceOfDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test46GetActivePropertyMappingsForLogicalInterfaceOfDeviceType()");		
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
	

	public void test47GetActivePhysicalInterfaceForDeviceTypee() throws IoTFCReSTException {
		System.out.println("\nInside test method test47GetActivePhysicalInterfaceForDeviceTypee()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		
		JsonObject activePIReturned = apiClient.getActivePhysicalInterfaceForDeviceType(DEVICE_TYPE);
		String activePIIdReturned = activePIReturned.get("id").getAsString();
		assertTrue("Active Physical Interface Id "+ activePIIdReturned + " got retrieved from the Platform", activePIIdReturned != null || activePIIdReturned.equals(""));
	}

	
	public void test48GetActiveEventType() throws IoTFCReSTException {
		System.out.println("\nInside test method test48GetActiveEventType()");		
		if(apiClient == null) {
			return;
		}
		//Retrieve Logical Interfaces
		JsonObject activeEventTypeReturned = apiClient.getActiveEventType(eventId);
		String activeEventTypeIdReturned = activeEventTypeReturned.get("id").getAsString();
		assertTrue("Active Event Type Returned Id "+ activeEventTypeIdReturned + " got retrieved from the Platform", true);
	}

	
	public void test49GetAllActiveEventTypes() throws IoTFCReSTException {
		System.out.println("\nInside test method test49GetAllActiveEventTypes()");		
		if(apiClient == null) {
			return;
		}

		List <NameValuePair> parameters = new ArrayList<NameValuePair>();
		NameValuePair nvp = new BasicNameValuePair("_page", "25");
		parameters.add(nvp);
		JsonObject activeEventTypesResponse = apiClient.getAllActiveEventTypes(parameters);
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

	
	public void test57PerformOperationAgainstDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test57PerformOperationAgainstDeviceType()");		
		if(apiClient == null) {
			return;
		}
		JsonObject validateOperation = new JsonObject();
		try {
			validateOperation.addProperty("operation", SchemaOperation.DEACTIVATE.getOperation());
			JsonObject validated = apiClient.performOperationAgainstDeviceType(DEVICE_TYPE, validateOperation.toString());
			try {
				Thread.sleep(20000);			
			} catch (InterruptedException iex) {
				iex.printStackTrace();
			}
			assertTrue("performOperationAgainstDeviceType succeeded", true);

		} catch (Exception ex) {
			assertFalse("performOperationAgainstDeviceType failed", true);			
		}
	}
	

	public void test81DissociateDraftPhysicalInterfaceFromDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test81DissociateDraftPhysicalInterfaceFromDeviceType()");		
		if(apiClient == null) {
			return;
		}
		boolean draftPIFromDeviceType = apiClient.dissociateDraftPhysicalInterfaceFromDeviceType(DEVICE_TYPE);
		assertTrue("Draft Physical Interface deleted from "+ DEVICE_TYPE, draftPIFromDeviceType);		
	}


	public void test83DeleteDraftPropertyMappings() throws IoTFCReSTException {
		System.out.println("\nInside test method test83DeleteDraftPropertyMappings()");		
		if(apiClient == null) {
			return;
		}
		boolean draftLIFromDeviceType = apiClient.deleteDraftPropertyMappings(DEVICE_TYPE, logicalInterfaceId);
		assertTrue("Draft Property Mappings for Logical Interface deleted from "+ DEVICE_TYPE, draftLIFromDeviceType);		
	}

	
	public void test85DissociateDraftLogicalInterfaceFromDeviceType() throws IoTFCReSTException {
		System.out.println("\nInside test method test85DissociateDraftLogicalInterfaceFromDeviceType()");		
		if(apiClient == null) {
			return;
		}
		boolean draftLIFromDeviceType = apiClient.dissociateDraftLogicalInterfaceFromDeviceType(DEVICE_TYPE, logicalInterfaceId);
		
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

/*	
 * To run this tescase, testcase test57PerformOperationAgainstDeviceType needs to be commented
 * 
	public void test89TestDeactivateOperation() throws IoTFCReSTException {
		System.out.println("\nInside test method test89TestDeactivateOperation()");		
		if(apiClient == null) {
			return;
		}

		JsonObject deactivateOperation = new JsonObject();
		deactivateOperation.addProperty("operation", SchemaOperation.DEACTIVATE.getOperation());
		System.out.println("id" + logicalInterfaceId + " deactivateOperation.toString()" + deactivateOperation.toString() );

		JsonObject deactivated = apiClient.performOperationAgainstLogicalInterface(logicalInterfaceId, deactivateOperation.toString());
		assertTrue("Event Type deletion = ", true);
	}
*/
	
	public void test90DeletePhysicalInterface() throws IoTFCReSTException {
		System.out.println("\nInside test method test90DeletePhysicalInterface()");		
		if(apiClient == null) {
			return;
		}
		//Delete Physical Interfaces
		boolean piDeletion = apiClient.deleteDraftPhysicalInterface(physicalInterfaceId);
		assertTrue("Physical Interface "+ physicalInterfaceId + " got deleted from the Platform", piDeletion);		
	}
	
	
	public void test91DeleteDraftEventType() throws IoTFCReSTException {
		System.out.println("\nInside test method test91DeleteDraftEventType()");		
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
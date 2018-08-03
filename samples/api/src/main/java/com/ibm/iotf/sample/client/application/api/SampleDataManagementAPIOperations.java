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

package com.ibm.iotf.sample.client.application.api;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.iotf.client.api.APIClient;
import com.ibm.iotf.client.api.APIClient.SchemaOperation;

public class SampleDataManagementAPIOperations {
	
	private final static String APPLICATION_PROPERTIES_FILE = "/application.properties";
	private final static String EVENT_SCHEMA1 = "/tempEventSchema.json";
	private final static String SCHEMA_NAME1 = "fahrenhietSchema";
	private final static String SCHEMA_DESCRIPTION1 = "Schema to capture the temperature readings in Fahrenhiet";
	private final static String SCHEMA_TYPE1 = "json-schema";
	
	private final static String EVENT_SCHEMA2 = "/envSensor.json";
	private final static String SCHEMA_NAME2 = "celciusSchema";
	private final static String SCHEMA_DESCRIPTION2 = "Schema to capture the temperature readings in Celcius";
	private final static String SCHEMA_TYPE2 = "json-schema";
	
	private static String EVT_TOPIC = "tempEvent";
	private static String TYPE_ID = "deviceType";
	private static String API_KEY = "<Your API Key>";	

	private final static String NOTIFICATION_STRATEGY = "on-state-change"; // on-every-event
	
//	private APIClient myClient = null;
	
	public static void main(String[] args) {

		Properties props = new Properties();
		try {
			props.load(SampleDataManagementAPIOperations.class.getResourceAsStream(APPLICATION_PROPERTIES_FILE));
		} catch (IOException e1) {
			System.err.println("Not able to read the properties file, exiting..");
			return;
		} 
//		SystemObject object = new SystemObject();
			
		APIClient myClient = null;

		
		try {
			//Instantiate the class by passing the properties file
			myClient = new APIClient(props);
		} catch (Exception e) {
			System.out.println(""+e.getMessage());
			// Looks like the properties file is not updated, just ignore;
			return;
		}
		
		try {
			
			//Create Schema Resource for Draft Physical Interface
			JsonObject draftPhysicalSchemaResponse = myClient.addDraftSchemaDefinition(new File(EVENT_SCHEMA1), SCHEMA_NAME1, SCHEMA_DESCRIPTION1, SCHEMA_TYPE1);			
			JsonElement draftSchemaId = draftPhysicalSchemaResponse.get("id");
			System.out.println("Creating artifacts");
			System.out.println("1. Schema for Draft Physical Interface created with id = " + draftSchemaId.getAsString());
			System.out.println("Schema Object = " + draftPhysicalSchemaResponse.toString());
			
			
			//Create Event Type
			JsonObject draftEventTypeRequest = new JsonObject();
			draftEventTypeRequest.add("schemaId", draftSchemaId);
			draftEventTypeRequest.addProperty("name", EVT_TOPIC);
			JsonObject draftEventTypeResponse = myClient.addDraftEventType(draftEventTypeRequest.toString());
			JsonElement draftEventTypeId = draftEventTypeResponse.get("id");
			System.out.println("\n2. Event Type created with id = " + draftEventTypeId.getAsString());
			System.out.println("Event Type Object = " + draftEventTypeResponse);
			
			
			//Create Draft Physical Interface
			JsonObject draftPhysicalInterfaceRequest = new JsonObject();
			draftPhysicalInterfaceRequest.addProperty("name", "Env sensor physical interface 7");
			JsonObject draftPhysicalInterfaceResponse = myClient.addDraftPhysicalInterface(draftPhysicalInterfaceRequest.toString());
			System.out.println("\n3. Draft Physical Interface created with id = " + draftPhysicalInterfaceResponse.get("id").getAsString());
			System.out.println("Draft Physical Interface Object = " + draftPhysicalInterfaceResponse.toString());
			
			
			//Add Event to Physical Interface
			JsonObject eventTypeToPIRequest = new JsonObject();
			eventTypeToPIRequest.addProperty("eventId", EVT_TOPIC);
			eventTypeToPIRequest.addProperty("eventTypeId", draftEventTypeId.getAsString());
			JsonObject eventTypeToPIResponse = myClient.addEventToPhysicalInterface(draftPhysicalInterfaceResponse.get("id").
					getAsString(), eventTypeToPIRequest.toString());
			System.out.println("\n4. Event Type to Draft Physical Interface added with eventTypeId = " + eventTypeToPIResponse.get("eventTypeId").getAsString());
			System.out.println("Event Type to Physical Interface = " + eventTypeToPIResponse.toString());
			
			
			//Update Draft Device Type to connect the Draft Physical Interface
			JsonObject updateDraftDTToPIRequest = new JsonObject();
			JsonObject refs = new JsonObject();
			refs.addProperty("events", "/api/v0002/draft/physicalinterfaces/" + draftPhysicalInterfaceResponse.get("id").getAsString() + "/events");

			Gson gson = new Gson();
			JsonElement element = gson.fromJson(refs.toString(), JsonElement.class);

			updateDraftDTToPIRequest.add("refs", element);
			updateDraftDTToPIRequest.addProperty("version", "draft");
			updateDraftDTToPIRequest.addProperty("name", "Env sensor physical interface 1");
			updateDraftDTToPIRequest.addProperty("createdBy", API_KEY);
			updateDraftDTToPIRequest.addProperty("updatedBy", API_KEY);
			updateDraftDTToPIRequest.addProperty("id", draftPhysicalInterfaceResponse.get("id").getAsString());
			JsonObject updateDraftDTToPIResponse = myClient.associateDraftPhysicalInterfaceWithDeviceType(TYPE_ID, updateDraftDTToPIRequest.toString());
			System.out.println("\n5. Draft Device Type added to draft Physical Interface with id = " + updateDraftDTToPIResponse.get("id").getAsString());
			System.out.println("Draft Device Type to draft Physical Interface = " + updateDraftDTToPIResponse.toString());
			
			
			//Create schema for draft logical interface
			JsonObject draftLogicalSchemaResponse = myClient.addDraftSchemaDefinition(new File(EVENT_SCHEMA2), SCHEMA_NAME2, SCHEMA_DESCRIPTION2, SCHEMA_TYPE2);			
			JsonElement draftLogicalSchemaId = draftLogicalSchemaResponse.get("id");
			System.out.println("\n6. Schema for Draft Logical Interface created with Id = " + draftLogicalSchemaId.getAsString());
			System.out.println("Schema for Draft Logical Interface Object = " + draftLogicalSchemaResponse.toString());
			
			
			//Create Draft Logical Interface
			JsonObject draftLogicalInterfaceRequest = new JsonObject();
			draftLogicalInterfaceRequest.addProperty("name", "environment sensor interface");
			draftLogicalInterfaceRequest.addProperty("schemaId", draftLogicalSchemaId.getAsString());
			JsonObject draftLogicalInterfaceResponse = myClient.addDraftLogicalInterface(draftLogicalInterfaceRequest.toString());
			System.out.println("\n7. Draft Logical Interface created with Id = " + draftLogicalInterfaceResponse.get("id").
					getAsString() + " and schemaId = " + draftLogicalInterfaceResponse.get("schemaId").getAsString());
			System.out.println("Draft for Logical Interface Object = " + draftLogicalInterfaceResponse.toString());
			
			
			//Add Draft Logical Interface to Device Type
			JsonObject draftLIToDTRequest = new JsonObject();
			refs = new JsonObject();
			refs.addProperty("schema", "/api/v0002/draft/schemas/" + draftLogicalSchemaId.getAsString());

			element = gson.fromJson(refs.toString(), JsonElement.class);

			draftLIToDTRequest.add("refs", element);
			draftLIToDTRequest.addProperty("version", "draft");
			draftLIToDTRequest.addProperty("name", "environment sensor interface");
			draftLIToDTRequest.addProperty("createdBy", API_KEY);
			draftLIToDTRequest.addProperty("updatedBy", API_KEY);
			draftLIToDTRequest.addProperty("schemaId", draftLogicalSchemaId.getAsString());
			draftLIToDTRequest.addProperty("id", draftLogicalInterfaceResponse.get("id").getAsString());
			JsonObject draftLIToDTResponse = myClient.associateDraftLogicalInterfaceToDeviceType(TYPE_ID, draftLIToDTRequest.toString());
			System.out.println("\n8. Draft Logical Interface added to device Type with id = " + draftLIToDTResponse.get("id").getAsString());
			System.out.println("Draft Logical Interface to Device Type Object = " + draftLIToDTResponse.toString());
			
			
			//Create mapping between Device Type and Logical Interface
			JsonObject mappings = new JsonObject();
			mappings.addProperty("logicalInterfaceId", draftLIToDTResponse.get("id").getAsString());
			mappings.addProperty("notificationStrategy", NOTIFICATION_STRATEGY);
			JsonObject propertyMapping = new JsonObject();
			propertyMapping.addProperty("temperature", "($event.temp - 32) / 1.8");
			JsonObject tempMapping = new JsonObject();
			tempMapping.add(EVT_TOPIC, gson.fromJson(propertyMapping.toString(), JsonElement.class));
			element = gson.fromJson(tempMapping.toString(), JsonElement.class);
			mappings.add("propertyMappings", tempMapping);
			JsonObject deviceToLImappings = myClient.addDraftPropertyMappingsToDeviceType(TYPE_ID, mappings.toString());
			System.out.println("\n9. Mapping created between device type and Logical Interface with Id = " + deviceToLImappings.
					get("logicalInterfaceId").getAsString());
			System.out.println("Mapping between device type and logical interface = " + deviceToLImappings.toString());
			
			
			//Validating configuration
			JsonObject validateOperation = new JsonObject();
			validateOperation.addProperty("operation", SchemaOperation.VALIDATE.getOperation());
			System.out.println("id" + draftLogicalInterfaceResponse.get("id").getAsString() + "validateOperation.toString()" + validateOperation.toString() );
			JsonObject validated = myClient.performOperationAgainstDraftLogicalInterface(draftLogicalInterfaceResponse.get("id").getAsString(), validateOperation.toString());
			System.out.println("\n10. Validate operation = " + validated.toString());

			Thread.sleep(20000);
			//Activating configuration
			if(validated.get("failures").getAsJsonArray().size() == 0) {
				System.out.println("No validation failures");
				JsonObject activateOperation = new JsonObject();
				activateOperation.addProperty("operation", SchemaOperation.ACTIVATE.getOperation());
				System.out.println("id" + draftLogicalInterfaceResponse.get("id").getAsString() + " activateOperation.toString()" + activateOperation.toString() );

				JsonObject activated = myClient.performOperationAgainstDraftLogicalInterface(draftLogicalInterfaceResponse.get("id").getAsString(), activateOperation.toString());
				System.out.println("\n11. Activate operation = " + activated.toString());
			}
			Thread.sleep(25000);
			
			System.out.println("\n\nEnter anything to delete the artifacts created......");

			System.out.println("Deleting artifacts");
			
			
			System.out.println("\n12. Dissociating the device type from Physical Interface = " + TYPE_ID + " = " + 
					myClient.dissociateDraftPhysicalInterfaceFromDeviceType(TYPE_ID));
			
			
			System.out.println("\n13. Mappings between device type and LogicalInterface deleted = " + 
					myClient.deleteDraftPropertyMappings(TYPE_ID, deviceToLImappings.get("logicalInterfaceId").getAsString()));

			
			System.out.println("\n14. Dissociating the device type from Logical Interface = " + TYPE_ID + " = " + 
					myClient.dissociateDraftLogicalInterfaceFromDeviceType(TYPE_ID, draftLIToDTResponse.get("id").getAsString()));

			
			System.out.println("\n15. Mapping between Event = " + EVT_TOPIC + " and Physical Interface = " + 
					draftPhysicalInterfaceResponse.get("id").getAsString() + " deleted = " + 
					myClient.deleteEventMappingFromPhysicalInterface(draftPhysicalInterfaceResponse.get("id").getAsString(), EVT_TOPIC));

			
			JsonObject deactivateOperation = new JsonObject();
			deactivateOperation.addProperty("operation", SchemaOperation.DEACTIVATE.getOperation());
			System.out.println("id" + draftLogicalInterfaceResponse.get("id").getAsString() + " deactivateOperation.toString()" + deactivateOperation.toString() );

			JsonObject deactivated = myClient.performOperationAgainstLogicalInterface(draftLogicalInterfaceResponse.get("id").getAsString(), deactivateOperation.toString());
			System.out.println("\n16. Dectivate operation = " + deactivated.toString());
			
			System.out.println("\n17. Draft Physical Interface with Id = " + draftPhysicalInterfaceResponse.get("id").getAsString() + " deleted = " + 
					myClient.deleteDraftPhysicalInterface(draftPhysicalInterfaceResponse.get("id").getAsString()));
			
			
			System.out.println("\n18. Draft Event Type with Id = " + draftEventTypeId.toString() + " deleted = " + 
					myClient.deleteDraftEventType(draftEventTypeId.getAsString()));
			
			
			System.out.println("\n19. Physical Schema with Id = " + draftSchemaId.toString() + " deleted = " + 
					myClient.deleteDraftSchemaDefinition(draftSchemaId.getAsString()));

			
			System.out.println("\n20. Draft Logical Interface with Id = " + draftLogicalInterfaceResponse.get("id").getAsString() + " deleted = " + 
					myClient.deleteDraftLogicalInterface(draftLogicalInterfaceResponse.get("id").getAsString()));

			
			System.out.println("\n21. Logical Schema with Id = " + draftLogicalSchemaId.toString() + " deleted = " + 
					myClient.deleteDraftSchemaDefinition(draftLogicalSchemaId.getAsString()));

		} catch(Exception ioe) {
			ioe.printStackTrace();
		}

	}
}


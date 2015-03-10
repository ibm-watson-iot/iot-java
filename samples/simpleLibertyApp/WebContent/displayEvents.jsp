<%@page import="java.io.PrintWriter"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<%@ page import="java.util.*" %>  

<%@ page import="java.util.concurrent.*" %>
<%@ page import="com.ibm.json.java.*" %>

<%@ page import="org.apache.http.*" %>
<%@ page import="org.apache.http.client.*" %>
<%@ page import="org.apache.http.client.methods.*" %>
<%@ page import="org.apache.http.entity.*" %>
<%@ page import="org.apache.http.impl.client.*" %>

<%@ page import="com.ibm.iotf.client.app.*"  %>
<%@ page import="iotfwebappclient.*"  %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%!
%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Event Dashboard</title>

</head>
<body>
<%

		iotfwebappclient.IoTFAgent agent = (iotfwebappclient.IoTFAgent)application.getAttribute("iotFAgent");
//		response.setIntHeader("Refresh", 20);
        List<String> messages = new LinkedList<String>();
        
        String deviceType = null;
        String deviceId = null;
        String eventType = null;
        String formatType = null;
        String qoS = null;

        String originalDeviceType = null;
        String originalDeviceId = null;
        String originalEventType = null;
        String originalFormatType = null;
        
        boolean unchanged = true;
        
        if(request.getParameter("type") == null) {
			deviceType = agent.getDeviceType() != null ? agent.getDeviceType() : "+";
		} else {
			deviceType = request.getParameter("type") ;
			originalDeviceType = agent.getDeviceType();
			agent.setDeviceType(deviceType);
			unchanged = false;
		}
		
		if(request.getParameter("id") == null){
			deviceId = agent.getDeviceId() != null ? agent.getDeviceId() : "+";
		} else {
			deviceId = request.getParameter("id");
			originalDeviceId = agent.getDeviceId();
			agent.setDeviceId(deviceId);
			unchanged = false;			
		}
		
		if(request.getParameter("event") == null){
			eventType = agent.getEventType() != null ? agent.getEventType() : "+";
		} else {
			eventType = request.getParameter("event");
			originalEventType = agent.getEventType();
			agent.setEventType(eventType);
			unchanged = false;			
		}
		
		if(request.getParameter("format") == null){
			formatType = agent.getFormatType() != null ? agent.getFormatType() : "+";
		} else {
			formatType = request.getParameter("format");
			originalFormatType = agent.getFormatType();
			agent.setFormatType(formatType);
			unchanged = false;			
		}
//        agent.client.subscribeToDeviceCommands(deviceType, deviceId, eventType, "json", 0);
        if(! unchanged) {
			agent.client.unsubscribeFromDeviceEvents(deviceType, deviceId, eventType, formatType, 0);
			try {
				Thread.sleep(10);
			} catch(InterruptedException ex) {
			
			}
			agent.client.subscribeToDeviceEvents(deviceType, deviceId, eventType, formatType, 0);        
			try {
				Thread.sleep(10);
			} catch(InterruptedException ex) {
			
			}
		}
        
%>
		<table class="TFtable" align="center">
			<tr>
				<th align="right" > Property</th>
				<th></th>
				<th align="left" > Value</th>
			</tr>
			<tr>
				<td align="right" > Organization Id </td>
				<td></td>
				<td><%= agent.client.getOrgId() %></td>
			</tr>
			<tr>
				<td align="right">Device Type</td>
				<td></td>
				<td><%= deviceType %></td>
			</tr>
			<tr>
				<td align="right">Device Id</td>
				<td></td>
				<td><%= deviceId%></td>
			</tr>						
			<tr>
				<td align="right">Event Type</td>
				<td></td>
				<td><%= eventType%></td>
			</tr>
			<tr>
				<td align="right">Format Type</td>
				<td></td>
				<td><%= formatType%></td>
			</tr>
									
			<tr>
				<td align="right"  valign="top">Events</td>
				<td></td>	
											
<%
//		out.println("Connected successfully - <br>Your device ID is " + tc.client.getAppId());
//		out.println("<br>Organization: " + tc.client.getOrgId() + " (" + tc.client.getAuthToken() + ")");
		agent.messages.drainTo(messages);
//		out.println("Messages received");
		
		Iterator iterator = messages.iterator();
		
%>
<td>
<% 		
 		while(iterator.hasNext()) {
			out.println(iterator.next() + "<br>");
		} 
%>		
		</td>
		</tr>						

		</table> 
</body>
</html>
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

		IoTFAgent agent = (IoTFAgent)application.getAttribute("iotFAgent");
//		response.setIntHeader("Refresh", 20);
        List<String> messages = new LinkedList<String>();
%>
		<table style="width:60%">
			<tr>
				<th align="right">Property</td>
				<th></th>
				<th align="left">Value</td>
			</tr>
			<tr>
				<td align="right">Organization Id</td>
				<td></td>
				<td><%= agent.client.getOrgId()%></td>
			</tr>
			<tr>
				<td align="right">Device Type</td>
				<td></td>
				<td>All</td>
			</tr>
			<tr>
				<td align="right">Device Id</td>
				<td></td>
				<td>All</td>
			</tr>						
			<tr>
				<td align="right">Event Type</td>
				<td></td>
				<td>status</td>
			</tr>						
			<tr>
				<td align="right" valign="top">Events</td>
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
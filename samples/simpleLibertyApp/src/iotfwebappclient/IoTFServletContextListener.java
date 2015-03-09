package iotfwebappclient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class IoTFServletContextListener implements ServletContextListener {
	private IoTFAgent iotFAgent = null;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		iotFAgent.client.disconnect();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		BlockingQueue<String> messages = new LinkedBlockingQueue<String>();
		iotFAgent = new IoTFAgent(messages);		
		ServletContext context = arg0.getServletContext();
		context.setAttribute("iotFAgent", iotFAgent);
	}
}

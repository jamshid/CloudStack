// Copyright 2012 Citrix Systems, Inc. Licensed under the
// Apache License, Version 2.0 (the "License"); you may not use this
// file except in compliance with the License.  Citrix Systems, Inc.
// reserves all rights not expressly granted by the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 
// Automatically generated by addcopyright.py at 04/03/2012
package com.cloud.consoleproxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import com.cloud.consoleproxy.vnc.FrameBufferCanvas;
import com.cloud.consoleproxy.vnc.RfbConstants;
import com.cloud.consoleproxy.vnc.VncClient;

/**
 * 
 * @author Kelven Yang
 * ConsoleProxyVncClient bridges a VNC engine with the front-end AJAX viewer
 * 
 */
public class ConsoleProxyVncClient extends ConsoleProxyClientBase {
	private static final Logger s_logger = Logger.getLogger(ConsoleProxyVncClient.class);
	
	private VncClient client;
	private Thread worker;
	private boolean workerDone = false;
	
	public ConsoleProxyVncClient() {
	}
	
	public boolean isHostConnected() {
		if(client != null)
			return client.isHostConnected();
		
		return false;
	}
	
	@Override
	public boolean isFrontEndAlive() {
		if(workerDone || System.currentTimeMillis() - getClientLastFrontEndActivityTime() > ConsoleProxy.VIEWER_LINGER_SECONDS*1000) {
			s_logger.info("Front end has been idle for too long");
			return false;
		}
		return true;
	}

	@Override
	public void initClient(ConsoleProxyClientParam param) {
		setClientParam(param);
		
		client = new VncClient(this);
		worker = new Thread(new Runnable() {
			public void run() {
				String tunnelUrl = getClientParam().getClientTunnelUrl();
				String tunnelSession = getClientParam().getClientTunnelSession();
				
				for(int i = 0; i < 15; i++) {
					try {
						if(tunnelUrl != null && !tunnelUrl.isEmpty() && tunnelSession != null && !tunnelSession.isEmpty()) {
							URI uri = new URI(tunnelUrl);
							s_logger.info("Connect to VNC server via tunnel. url: " + tunnelUrl + ", session: " + tunnelSession);
							client.connectTo(
								uri.getHost(), uri.getPort(), 
								uri.getPath() + "?" + uri.getQuery(), 
								tunnelSession, "https".equalsIgnoreCase(uri.getScheme()),
								getClientHostPassword());
						} else {
							s_logger.info("Connect to VNC server directly. host: " + getClientHostAddress() + ", port: " + getClientHostPort());
							client.connectTo(getClientHostAddress(), getClientHostPort(), getClientHostPassword());
						}
					} catch (UnknownHostException e) {
						s_logger.error("Unexpected exception (will retry until timeout)", e);
					} catch (IOException e) {
						s_logger.error("Unexpected exception (will retry until timeout) ", e);
					} catch (Throwable e) {
						s_logger.error("Unexpected exception (will retry until timeout) ", e);
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}

					if(tunnelUrl != null && !tunnelUrl.isEmpty() && tunnelSession != null && !tunnelSession.isEmpty()) {
						ConsoleProxyAuthenticationResult authResult = ConsoleProxy.reAuthenticationExternally(getClientParam());
						if(authResult != null && authResult.isSuccess()) {
							if(authResult.getTunnelUrl() != null && !authResult.getTunnelUrl().isEmpty() && 
								authResult.getTunnelSession() != null && !authResult.getTunnelSession().isEmpty()) {
								tunnelUrl = authResult.getTunnelUrl();
								tunnelSession = authResult.getTunnelSession();
								
								s_logger.info("Reset XAPI session. url: " + tunnelUrl + ", session: " + tunnelSession);
							}
						}
					}
				}

				s_logger.info("Receiver thread stopped.");
				workerDone = true;
			    client.getClientListener().onClientClose();
			}
		});
		
		worker.setDaemon(true);
		worker.start();
	}
	
	@Override
	public void closeClient() {
		if(client != null)
			client.shutdown();
	}
	
	@Override
	public void onClientConnected() {
	}
	
	public void onClientClose() {
		s_logger.info("Received client close indication. remove viewer from map.");
		
		ConsoleProxy.removeViewer(this);
	}
	
	@Override
	public void onFramebufferUpdate(int x, int y, int w, int h) {
		super.onFramebufferUpdate(x, y, w, h);
		client.requestUpdate(false);
	}

	public void sendClientRawKeyboardEvent(InputEventType event, int code, int modifiers) {
		if(client == null)
			return;
		
		updateFrontEndActivityTime();
		
		switch(event) {
		case KEY_DOWN :
			client.sendClientKeyboardEvent(RfbConstants.KEY_DOWN, code, 0);
			break;
			
		case KEY_UP :
			client.sendClientKeyboardEvent(RfbConstants.KEY_UP, code, 0);
			break;
			
		case KEY_PRESS :
			break;
			
		default :
			assert(false);
			break;
		}
	}
	
	public void sendClientMouseEvent(InputEventType event, int x, int y, int code, int modifiers) {
		if(client == null)
			return;
		
		updateFrontEndActivityTime();

	    int pointerMask = 0;
	    int mask = 1;
	    if(code == 2)
	    	mask = 4;
		if(event == InputEventType.MOUSE_DOWN) {
			pointerMask = mask;
		}
		
		client.sendClientMouseEvent(pointerMask, x, y, code, modifiers);
	}
	
	@Override
	protected FrameBufferCanvas getFrameBufferCavas() {
		if(client != null)
			return client.getFrameBufferCanvas();
		return null;
	}
}

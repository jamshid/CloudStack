package com.cloud.hypervisor;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Local;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.api.Command;
import com.cloud.host.Host;
import com.cloud.host.HostVO;
import com.cloud.host.dao.HostDao;
import com.cloud.hypervisor.Hypervisor.HypervisorType;
import com.cloud.utils.component.Adapters;
import com.cloud.utils.component.ComponentLocator;
import com.cloud.utils.component.Inject;

@Local(value = { HypervisorGuruManager.class } )
public class HypervisorGuruManagerImpl implements HypervisorGuruManager {
    public static final Logger s_logger = Logger.getLogger(HypervisorGuruManagerImpl.class.getName());

    @Inject HostDao _hostDao;
    
	String _name;
    Map<HypervisorType, HypervisorGuru> _hvGurus = new HashMap<HypervisorType, HypervisorGuru>();
	
	@Override
	public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {
		_name = name;
        ComponentLocator locator = ComponentLocator.getCurrentLocator();

        Adapters<HypervisorGuru> hvGurus = locator.getAdapters(HypervisorGuru.class);
        for (HypervisorGuru guru : hvGurus) {
            _hvGurus.put(guru.getHypervisorType(), guru);
        }
		
		return true;
	}

	@Override
	public boolean start() {
		return true;
	}

	@Override
	public boolean stop() {
		return true;
	}

	@Override
	public String getName() {
		return _name;
	}

	public HypervisorGuru getGuru(HypervisorType hypervisorType) {
		return _hvGurus.get(hypervisorType);
	}
	
    public long getGuruProcessedCommandTargetHost(long hostId, Command cmd) {
    	HostVO hostVo = _hostDao.findById(hostId);
    	HypervisorGuru hvGuru = null;
    	if(hostVo.getType() == Host.Type.Routing) {
    		hvGuru = _hvGurus.get(hostVo.getHypervisorType());
    	}
    	
    	if(hvGuru != null)
    		return hvGuru.getCommandHostDelegation(hostId, cmd);
    	
    	return hostId;
    }
}

package org.jboss.bpm.console.server;

import org.jboss.bpm.console.server.integration.ManagementFactory;
import org.jbpm.integration.console.ProcessManagement;
import org.jbpm.integration.console.UserManagement;

public class ManagementFactoryImpl extends ManagementFactory {
	@Override
	public ProcessManagement createProcessManagement() {
		return new ProcessManagement();
	}

	@Override
	public TaskManagementExt createTaskManagement() {
		return new TaskManagementImpl();
	}

	@Override
	public UserManagement createUserManagement() {
		return new UserManagement();
	}
}

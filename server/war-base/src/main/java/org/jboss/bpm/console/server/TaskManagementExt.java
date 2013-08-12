package org.jboss.bpm.console.server;

import java.util.List;

import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.bpm.console.server.integration.TaskManagement;

public interface TaskManagementExt extends TaskManagement {
	public TaskRef getTaskById(long taskId, String locale);

	public List<TaskRef> getAssignedTasks(String idRef, String locale);

	public List<TaskRef> getUnassignedTasks(String idRef, String participationType, String locale);
}

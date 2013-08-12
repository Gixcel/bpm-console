package org.jboss.bpm.console.server;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.drools.command.CommandService;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.persistence.SingleSessionCommandService;
import org.drools.runtime.Environment;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jbpm.integration.console.StatefulKnowledgeSessionUtil;
import org.jbpm.integration.console.TaskManagement;
import org.jbpm.integration.console.Transform;
import org.jbpm.process.workitem.wsht.GenericHTWorkItemHandler;
import org.jbpm.task.Content;
import org.jbpm.task.I18NText;
import org.jbpm.task.Status;
import org.jbpm.task.Task;
import org.jbpm.task.TaskService;
import org.jbpm.task.query.TaskSummary;
import org.jbpm.task.utils.ContentMarshallerHelper;

/**
 * Based on org.jbpm.integration.console.TaskManagement. Code is copied, since inheriting brings no benefits.
 * 
 * @author Julio
 * 
 */
public class TaskManagementImpl extends TaskManagement implements TaskManagementExt {
	private TaskService service;
	private String defaultLocale;

	public TaskManagementImpl() {
		super();
	}

	private Object getPrivateField(String fieldName) {
		Object result = null;
		try {
			Field field = this.getClass().getSuperclass().getDeclaredField(fieldName);
			field.setAccessible(true);
			result = field.get(this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
		return result;
	}

	// In the default implementation this method created a new LocalTaskService in the first call. This leads that there
	// where more than one LocalTaskService running at the same time instead of a singleton. In turn this raises a
	// problem when several services worked at the same time with a task, like when completing one. This is the reason
	// for the hack. Instead we take the one already registered in the human task handler.
	public void connect() {
		super.connect();
		if (service == null) {
			// Awful hack to get the fields, but there is no much of a choice here
			StatefulKnowledgeSession session = StatefulKnowledgeSessionUtil.getStatefulKnowledgeSession();
			if (session instanceof CommandBasedStatefulKnowledgeSession){
				CommandService commandService = ((CommandBasedStatefulKnowledgeSession)session).getCommandService();
				if (commandService instanceof SingleSessionCommandService){
					session = ((SingleSessionCommandService)commandService).getStatefulKnowledgeSession();
				}
			}
			WorkItemManager manager = session.getWorkItemManager();
			try {
				// Getting the task client
				Field field = manager.getClass().getDeclaredField("workItemHandlers");
				field.setAccessible(true);
				Map<String, WorkItemHandler> handlers = (Map<String, WorkItemHandler>) field.get(manager);
				GenericHTWorkItemHandler handler = (GenericHTWorkItemHandler) handlers.get("Human Task");
				service = handler.getClient();

				// Setting in parent
				field = this.getClass().getSuperclass().getDeclaredField("service");
				field.setAccessible(true);
				field.set(this, service);

				// Getting locale field
				field = this.getClass().getSuperclass().getDeclaredField("locale");
				field.setAccessible(true);
				defaultLocale = (String) field.get(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
			}
		}
	}

	protected TaskRef transform(Task task, String locale) {
		TaskRefExt taskRef = new TaskRefExt();
		try {
			BeanUtils.copyProperties(taskRef, Transform.task(task));
		} catch (Exception e1) {
			// TODO Auto-generated catch block
		}
		if (StringUtils.isBlank(locale)) {
			locale = defaultLocale;
		}
		taskRef.setDescription(I18NText.getLocalText(task.getDescriptions(), locale, null));

		// Prepare content of task
		Map<String, Object> contentObject = new HashMap<String, Object>();
		long contentId = task.getTaskData().getDocumentContentId();
		if (contentId != -1) {
			Content content = service.getContent(contentId);
			Object result = ContentMarshallerHelper.unmarshall(content.getContent(), null);
			if (result instanceof Map) {
				Map<?, ?> map = (Map<?, ?>) result;
				for (Map.Entry<?, ?> entry : map.entrySet()) {
					if (entry.getKey() instanceof String) {
						contentObject.put((String) entry.getKey(), entry.getValue());
					}
				}
			} else {
				contentObject.put("Result", result);
			}
		}
		taskRef.setContent(contentObject);

		return taskRef;
	}

	protected TaskRef transform(TaskSummary task) {
		TaskRef taskRef = Transform.task(task);
		taskRef.setDescription(task.getDescription());
		return taskRef;
	}

	public TaskRef getTaskById(long taskId, String locale) {
		connect();
		Task task = service.getTask(taskId);
		return transform(task, locale);
	}

	public List<TaskRef> getAssignedTasks(String idRef, String locale) {
		connect();
		List<TaskRef> result = new ArrayList<TaskRef>();
		try {
			List<Status> onlyReserved = Collections.singletonList(Status.Reserved);
			if (StringUtils.isBlank(locale)) {
				locale = defaultLocale;
			}
			List<TaskSummary> tasks = service.getTasksOwned(idRef, onlyReserved, locale);

			for (TaskSummary task : tasks) {
				result.add(transform(task));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return result;
	}

	public List<TaskRef> getUnassignedTasks(String idRef, String participationType, String locale) {
		// TODO participationType ?
		connect();
		List<TaskRef> result = new ArrayList<TaskRef>();
		try {

			List<TaskSummary> tasks = null;
			List<Status> onlyReady = Collections.singletonList(Status.Ready);

			if (StringUtils.isBlank(locale)) {
				locale = defaultLocale;
			}
			tasks = service.getTasksAssignedAsPotentialOwnerByStatus(idRef, onlyReady, locale);

			for (TaskSummary task : tasks) {
				result.add(transform(task));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return result;
	}
}
package org.jboss.bpm.console.server;

import java.util.Map;

import org.jboss.bpm.console.client.model.TaskRef;

public class TaskRefExt extends TaskRef {
	private Map<String, Object> content;

	public Map<String, Object> getContent() {
		return content;
	}

	public void setContent(Map<String, Object> content) {
		this.content = content;
	}
}

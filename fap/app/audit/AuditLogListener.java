package audit;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embedded;

import models.TableKeyValue;

import org.apache.log4j.Logger;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.event.*;

import aed.AedClient;

import play.db.jpa.Model;
import validation.ValueFromTable;

public class AuditLogListener implements
	PostInsertEventListener,
	PostUpdateEventListener,
	PostDeleteEventListener,
	PostCollectionUpdateEventListener,
	PostCollectionRecreateEventListener,
	PostCollectionRemoveEventListener {

	private static Logger log = Logger.getLogger(AuditLogListener.class);
	
	private String evento;
	private String entity;
	private String entityId;
	private String property;
	private String value;
	private String oldValue;
	private String añadidos; 
	private String borrados; 


	public void onPostInsert(PostInsertEvent event) {
		Model entity = (Model) event.getEntity();
		if (!entity.getClass().isAnnotationPresent(Auditable.class)) {
			return;
		}
		evento = "entidad creada";
		this.entity = entity.getClass().getName().replaceFirst("models.", "");
		this.entityId = entity.getId().toString();
		logEntity();
		
		evento = "campo inicializado";
		String[] properties = event.getPersister().getPropertyNames();
		Object[] values = event.getState();
		for (int i = 0; i < properties.length; i++) {
			try {
				if (entity.getClass().getField(properties[i]).getAnnotation(Embedded.class) != null) {
					onPostInsertEmbeddable(properties[i], values[i]);
					continue;
				}
			} catch (Exception e) {
			}
			if ((values[i] instanceof Collection)) {
				continue;
			}
			property = properties[i];
			value = getValueAsString(entity, property, values[i]);
			logProperty();
		}
	}

	
	public void onPostUpdate(PostUpdateEvent event) {
		Model entity = (Model) event.getEntity();
		if (!entity.getClass().isAnnotationPresent(Auditable.class)) {
			return;
		}
		evento = "campo modificado";
		this.entity = entity.getClass().getName().replaceFirst("models.", "");
		this.entityId = entity.getId().toString();
		String[] properties = event.getPersister().getPropertyNames();
		Object[] oldValues = event.getOldState();
		Object[] values = event.getState();
		
		for(int dirtyIndex : event.getDirtyProperties()){
			property = properties[dirtyIndex];
			value = getValueAsString(entity, property, values[dirtyIndex]);
			oldValue = getValueAsString(entity, property, oldValues[dirtyIndex]);
			logUpdateProperty();
		}
	}
	

	public void onPostDelete(PostDeleteEvent event) {
		Model entity = (Model) event.getEntity();
		if (!entity.getClass().isAnnotationPresent(Auditable.class)) {
			return;
		}
		evento = "entidad borrada";
		this.entity = entity.getClass().getName().replaceFirst("models.", "");
		this.entityId = entity.getId().toString();
		logEntity();
	}

	
	public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
		Model entity = (Model) event.getAffectedOwnerOrNull();
		if (!entity.getClass().isAnnotationPresent(Auditable.class)) {
			return;
		}
		PersistentCollection collection = event.getCollection();
		evento = "lista modificada";
		this.entity = entity.getClass().getName().replaceFirst("models.", "");
		entityId = entity.id.toString();
		Field[] fields = entity.getClass().getFields();
		property = null;
		for (Field field : fields) {
			try {
				if (field.get(entity) == collection) {
					property = field.getName();
					Collection oldCollection = getPreviousContents(collection);
					Collection newCollection = (Collection) collection;
					Collection added = new ArrayList<Object>();
					Collection deleted = new ArrayList<Object>();
					
					for (Object o: oldCollection){
						if (!newCollection.contains(o)){
							deleted.add(o);
						}
					}
					for (Object o: newCollection){
						if (!oldCollection.contains(o)){
							added.add(o);
						}
					}
					añadidos = getValueAsString(field, added);
					borrados = getValueAsString(field, deleted);
					logUpdateCollection();
					return;
				}
			} catch (Exception e) {}
		}
	}

	
	public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
		Model entity = (Model) event.getAffectedOwnerOrNull();
		if (!entity.getClass().isAnnotationPresent(Auditable.class)) {
			return;
		}
		PersistentCollection collection = event.getCollection();
		evento = "lista creada";
		this.entity = entity.getClass().getName().replaceFirst("models.", "");
		entityId = entity.id.toString();
		Field[] fields = entity.getClass().getFields();
		property = null;
		for (Field field : fields) {
			try {
				if (field.get(entity) == collection) {
					property = field.getName();
					value = getValueAsString(field, collection);
					logProperty();
					return;
				}
			} catch (Exception e) {}
		}
	}
	
	
	public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
		Model entity = (Model) event.getAffectedOwnerOrNull();
		if (!entity.getClass().isAnnotationPresent(Auditable.class)) {
			return;
		}
		PersistentCollection collection = event.getCollection();
		evento = "lista borrada";
		this.entity = entity.getClass().getName().replaceFirst("models.", "");
		entityId = entity.id.toString();
		Field[] fields = entity.getClass().getFields();
		property = null;
		for (Field field : fields) {
			try {
				if (field.get(entity) == collection) {
					property = field.getName();
					logDelete();
					return;
				}
			} catch (Exception e) {}
		}
		
	}

	
	private String getValueAsString(Object entity, String property, Object value) {
		Field field = null;
		try {
			field = entity.getClass().getField(property);
		} catch (Exception e) {
		}
		return getValueAsString(field, value);
	}

	
	private String getValueAsString(Field field, Object value) {
		if (value == null) {
			return "null";
		}
		ValueFromTable annotation = field.getAnnotation(ValueFromTable.class);
		if (annotation != null) {
			if (value instanceof String) {
				return value + ":\"" + TableKeyValue.getValue(annotation.value(), (String) value) + "\"";
			}
			List<String> values = new ArrayList<String>();
			for (String val : (Collection<String>) value) {
				values.add(val + ":\"" + TableKeyValue.getValue(annotation.value(), val) + "\"");
			}
			return values.toString();
		}
		if (value instanceof String) {
			return "\"" + value + "\"";
		}
		return value.toString();
	}
	
	
	private Collection getPreviousContents(PersistentCollection pc) {
		Collection previousContents = null;
		Object snapshot = pc.getStoredSnapshot();
		if (snapshot == null)
			return null;
		if (pc instanceof List) {
			previousContents = Collections.unmodifiableList((List) snapshot);
		} else if (pc instanceof Set) {
			Map snapshotMap = (Map) snapshot;
			previousContents = Collections.unmodifiableSet(new HashSet(snapshotMap.values()));
		} else
			previousContents = (Collection) pc;
		return previousContents;
	}
	
	
	private void onPostUpdateEmbeddable(String property, Object obj, Object oldObj) {
		for (Field field : obj.getClass().getFields()) {
			Object value = null;
			Object oldValue = null;
			try {
				value = field.get(obj);
				oldValue = field.get(oldObj);
			} catch (Exception e) {
			}
			if ((value instanceof Collection)) {
				continue;
			}
			boolean updated = false;
			if (oldValue == null) {
				if (value != null) {
					updated = true;
				}
			} else if (!oldValue.equals(value)) {
				updated = true;
			}
			if (updated) {
				this.property = property + "." + field.getName();
				this.value = getValueAsString(field, value);
				this.oldValue = getValueAsString(field, oldValue);
				logUpdateProperty();
			}
		}
	}

	
	private void onPostInsertEmbeddable(String property, Object obj) {
		for (Field field : obj.getClass().getFields()) {
			Object value = null;
			try {
				value = field.get(obj);
			} catch (Exception e) {
			}
			if ((value instanceof Collection)) {
				continue;
			}
			this.property = property + "." + field.getName();
			this.value = getValueAsString(field, value);
			logProperty();
		}
	}
	
	
	private void logProperty() {
		String msg = "%s ~ %s[%s].%s = %s ";
		log.debug(String.format(msg, evento, entity, entityId, property, value));
	}
	
	
	private void logUpdateProperty() {
		String msg = "%s ~ %s[%s].%s = %s (valor anterior: %s)";
		log.debug(String.format(msg, evento, entity, entityId, property, value, oldValue));
	}
	
	
	private void logUpdateCollection() {
		String msg = "%s ~ %s[%s].%s, añadidos: %s, borrados: %s";
		log.debug(String.format(msg, evento, entity, entityId, property, añadidos, borrados));
	}

	
	private void logEntity() {
		String msg = "%s ~ %s[%s]";
		log.debug(String.format(msg, evento, entity, entityId));
	}
	
	
	private void logDelete() {
		String msg = "%s ~ %s[%s].%s";
		log.debug(String.format(msg, evento, entity, entityId, property));
	}

}
package controllers.popups;

import java.util.Map;

import org.joda.time.DateTime;

import messages.Messages;
import models.Registro;
import models.RegistroModificacion;
import models.SolicitudGenerica;
import play.mvc.Util;
import validation.CustomValidation;
import controllers.gen.popups.PopupCrearModificacionSolicitudControllerGen;
import enumerado.fap.gen.EstadosSolicitudEnum;

public class PopupCrearModificacionSolicitudController extends PopupCrearModificacionSolicitudControllerGen {
	
	@Util
	public static Long crearLogica(Long idSolicitud, RegistroModificacion registroModificacion) {
		checkAuthenticity();
		if (!permiso("crear")) {
			Messages.error("No tiene permisos suficientes para realizar la acción");
		}
		RegistroModificacion dbRegistroModificacion = PopupCrearModificacionSolicitudController.getRegistroModificacion();
		SolicitudGenerica dbSolicitud = PopupCrearModificacionSolicitudController.getSolicitudGenerica(idSolicitud);

		PopupCrearModificacionSolicitudController.PopupCrearModificacionSolicitudBindReferences(registroModificacion);

		if (!Messages.hasErrors()) {

			PopupCrearModificacionSolicitudController.popupCrearModificacionSolicitudValidateCopy("crear", dbRegistroModificacion, registroModificacion);

		}

		if (!Messages.hasErrors()) {
			PopupCrearModificacionSolicitudController.crearValidateRules(dbRegistroModificacion, registroModificacion);
		}
		Long idRegistroModificacion = null;
		if (!Messages.hasErrors()) {
			dbRegistroModificacion.fechaCreacion = new DateTime();
			dbRegistroModificacion.registro =  new Registro();
			dbRegistroModificacion.save();
			idRegistroModificacion = dbRegistroModificacion.id;
			dbSolicitud.estadoAntesModificacion = dbSolicitud.estado;
			dbSolicitud.registroModificacion.add(dbRegistroModificacion);
			dbSolicitud.estado = EstadosSolicitudEnum.modificacion.name();
			dbSolicitud.save();

			log.info("Acción Crear de página: " + "gen/popups/popupCrearModificacionSolicitud.html" + " , intentada con éxito");
		} else {
			log.info("Acción Crear de página: " + "gen/popups/popupCrearModificacionSolicitud.html" + " , intentada sin éxito (Problemas de Validación)");
		}
		return idRegistroModificacion;
	}
	
	@Util
	public static void popupCrearModificacionSolicitudValidateCopy(String accion, RegistroModificacion dbRegistroModificacion, RegistroModificacion registroModificacion) {
		CustomValidation.clearValidadas();

		if (secure.checkGrafico("crearModificacionSolicitud", "editable", accion, (Map<String, Long>) tags.TagMapStack.top("idParams"), null)) {
			CustomValidation.valid("registroModificacion", registroModificacion);
			CustomValidation.required("registroModificacion.fechaLimite", registroModificacion.fechaLimite);
			if (!Messages.hasErrors()){
				DateTime fechaHora = registroModificacion.fechaLimite;
				fechaHora = fechaHora.withMinuteOfHour(59);
				fechaHora = fechaHora.withSecondOfMinute(59);
				fechaHora = fechaHora.withHourOfDay(23);
				
				if (fechaHora.isAfter(registroModificacion.fechaCreacion)){
					dbRegistroModificacion.fechaLimite = fechaHora;
				}
				else{ //Error de asignacion de fechas
					Messages.error("La fecha límite no puede ser anterior a la fecha de creación");
					log.error("La fecha límite no puede ser anterior a la fecha de creación");
				}
			}
		}

	}
	
}

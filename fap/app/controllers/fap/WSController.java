package controllers.fap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.DateTime;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import controllers.GraficasWSController;

import messages.Messages;
import models.Aplicacion;
import models.ListaResultadosPeticion;
import models.Peticion;
import models.ResultadoPeticion;
import models.ResultadosPeticion;
import models.ServicioWebInfo;
import models.ServiciosWeb;
import models.TableKeyValue;

import play.Play;
import play.libs.WS;
import play.libs.WS.WSRequest;
import play.mvc.Http;
import play.mvc.With;
import play.utils.Java;
import properties.FapProperties;

public class WSController extends GenericController {

	/**
	 * Función que es sobreescrita por todos los servicios web
	 * hijos.
	 * @return La información de cada servicio web.
	 */
	public static ServicioWebInfo getInfoWS() {
		return null;
	}
	
	/**
	 * Función que será llamada vía Ajax y que devuelve un array de pares,
	 * el cual será la entrada para las gráficas.
	 * @param listaResultadosId
	 * @param nameVariable
	 * @param rango
	 * @param activo
	 */
	public static void getData(Long idWS, String fecha, String nameVariable, int rango, boolean activo) {
		
		String tituloVariable = nameVariable.split(" \\(")[0];
		String day = fecha.split("-")[0];
		String time = fecha.split("-")[1];
		DateTime fechaComparar = new DateTime(Integer.parseInt(day.split("/")[2]),
								Integer.parseInt(day.split("/")[1]), 
								Integer.parseInt(day.split("/")[0]),
								Integer.parseInt(time.split(":")[0]), 
								Integer.parseInt(time.split(":")[1]), 
								Integer.parseInt(time.split(":")[2]));
		
		Gson gson = new Gson();
		ServiciosWeb servicioWeb = ServiciosWeb.findById(idWS);
		ListaResultadosPeticion listaResultados = new ListaResultadosPeticion();
		for (int i = 0; i < servicioWeb.peticion.size(); i++) {
			
			String a = servicioWeb.peticion.get(i).fechaPeticion.split("\\.")[0];
			String b = fechaComparar.toString().split("\\.")[0];
	
			if (a.equals(b)) {
				listaResultados = gson.fromJson(servicioWeb.peticion.get(i).stringJson, ListaResultadosPeticion.class);
			}
		}
		
		Map<String, Object> mapa = new HashMap<String, Object>();
		boolean type1 = false;
		boolean type2 = false;
		boolean type3 = false;
		for (int i = 0; i < listaResultados.resultadosPeticion.size(); i++) {
			ResultadosPeticion resultados = listaResultados.resultadosPeticion.get(i);
			
			for (int j = 0; j < resultados.resultadoPeticion.size(); j++) {
				ResultadoPeticion resultado = resultados.resultadoPeticion.get(j);
				String nombreVariable = resultado.nombre;
				String valor = null;
				
				if (tituloVariable.equals(nombreVariable)) {
					if (resultado.getType() != null) {
						if (resultado.getType().equals("String"))
							valor = resultado.valorString;
						else if (resultado.getType().equals("Boolean")) {
							if (resultado.valorBoolean.toString().toLowerCase().equals("true"))
								valor = "Sí";
							else
								valor = "No";
						}
						else if (resultado.getType().equals("DateTime")) {
							valor = resultado.valorDateTime;
							String fechaResultado = valor.split("T")[0];
							String dia = fechaResultado.split("-")[2];
							String mes = fechaResultado.split("-")[1];
							String agno = fechaResultado.split("-")[0];
							if (rango == 0)
								valor = "Día "+dia;
							else if (rango == 1) {
								valor = dia+"-"+getMes(mes);
								type1 = true;
							}
							else if (rango == 2) {
								DateTime date = new DateTime(fechaResultado);
								valor = "Semana " + date.getWeekOfWeekyear();
							}
							else if (rango == 3) {
								valor = getMes(mes);
								type2 = true;
							}
							else if (rango == 4) {
								valor = getMes(mes)+"-"+agno;
								type3 = true;
							}
							else if (rango == 5)
								valor = agno;
						}
						
						if (mapa.containsKey(valor)) {
							int numAnterior = (Integer) mapa.get(valor);
							mapa.remove(valor);
							mapa.put(valor, numAnterior + 1);
						} else {
							mapa.put(valor, 1);
						}
					}
				}
				
			}
		}
		Map<String, Object> mapaOrdenado = sortByComparator(mapa, type1, type2, type3);
		type1 = false;
		type2 = false;
		type3 = false;
		// Teniendo en el mapa la información que se va a representar,
		// se crea un array de pares con esa información.
		String jsData = "[";
		Iterator it = mapaOrdenado.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry)it.next();
			jsData += "[ '" + e.getKey() + "', "+ e.getValue()+"]";
			if (it.hasNext())
				jsData += ", ";
		}
		jsData += "]";
		renderText(jsData);
	}
	
	/**
	 * Función que me permite ordenar el mapa con la información que 
	 * se va a mostrar en la gráfica.
	 * @param mapa
	 * @return mapaOrdenado
	 */
	private static Map sortByComparator(Map mapa, final boolean type1, final boolean type2, final boolean type3) {		 
        List lista = new LinkedList(mapa.entrySet());
        Collections.sort(lista, new Comparator() {
             public int compare(Object o1, Object o2) {
            	if (type1) {
            		String clave1 = getNumMes(((String) ((Map.Entry) (o1)).getKey()).split("-")[1]) + "-" +  ((String) ((Map.Entry) (o1)).getKey()).split("-")[0];
            		String clave2 = getNumMes(((String) ((Map.Entry) (o2)).getKey()).split("-")[1]) + "-" + ((String) ((Map.Entry) (o2)).getKey()).split("-")[0];
            		return ((Comparable) clave1).compareTo(clave2);
            	} else if (type2) {
            		String clave1 = ((String) ((Map.Entry) (o1)).getKey());
            		String clave2 = ((String) ((Map.Entry) (o2)).getKey());
            		return ((Comparable) getNumMes(clave1)).compareTo(getNumMes(clave2));
            	} else if (type3) {
            		String clave1 = ((String) ((Map.Entry) (o1)).getKey()).split("-")[0];
            		String clave2 = ((String) ((Map.Entry) (o2)).getKey()).split("-")[0];
            		return ((Comparable) getNumMes(clave1)).compareTo(getNumMes(clave2));
            	}
            	return ((Comparable) ((Map.Entry) (o1)).getKey()).compareTo(((Map.Entry) (o2)).getKey());
             }
		});

		Map mapaOrdenado = new LinkedHashMap();
		for (Iterator it = lista.iterator(); it.hasNext();) {
		     Map.Entry entry = (Map.Entry)it.next();
		     mapaOrdenado.put(entry.getKey(), entry.getValue());
		}
		return mapaOrdenado;
	}

	/**
	 * Función que permite obtener la información de cada servicio web hijo.
	 * @param nombreFuncion
	 * @param args
	 * @return Lista con el ServicioWebInfo de cada servicio web hijo.
	 * @throws Throwable
	 */
	public static <T> List<ServicioWebInfo> invoke(String nombreFuncion, Object... args) throws Throwable {
		Class claseDelMetodoALlamar = null;
        List<Class> classes = Play.classloader.getAssignableClasses(WSController.class);
        ServicioWebInfo swi = new ServicioWebInfo();
        List<ServicioWebInfo> listaInfoWS = new ArrayList<ServicioWebInfo>();
        
        if(classes.size() != 0) {
        	for (int i = 0; i < classes.size(); i++) {
	        	claseDelMetodoALlamar = classes.get(i);
	        	if (!claseDelMetodoALlamar.getName().endsWith("Gen")) {
		        	try {
		            	swi = (ServicioWebInfo)Java.invokeStaticOrParent(claseDelMetodoALlamar, nombreFuncion, args);
		            	listaInfoWS.add(swi);
		            	
		            } catch(InvocationTargetException e) {
		            	throw e.getTargetException();
		            }
	        	}
        	}
        	return listaInfoWS;
        } else {
        	swi = (ServicioWebInfo)Java.invokeStatic(WSController.class, nombreFuncion, args);
        	listaInfoWS.add(swi);
        	return listaInfoWS;
        }
	}
	
	private static String getMes(String mes) {
		switch (Integer.parseInt(mes)) {
		case 1:
			return "Ene";
		case 2:
			return "Feb";
		case 3:
			return "Mar";
		case 4:
			return "Abr";
		case 5:
			return "May";
		case 6:
			return "Jun";
		case 7:
			return "Jul";
		case 8:
			return "Ago";
		case 9:
			return "Sept";
		case 10:
			return "Oct";
		case 11:
			return "Nov";
		case 12:
			return "Dic";
		default:
			break;
		}
		return null;
	}
	
	private static String getNumMes(String mes) {
		if (mes.equals("Ene"))
			return "01";
		else if (mes.equals("Feb"))
			return "02";
		else if (mes.equals("Mar"))
			return "03";
		else if (mes.equals("Abr"))
			return "04";
		else if (mes.equals("May"))
			return "05";
		else if (mes.equals("Jun"))
			return "06";
		else if (mes.equals("Jul"))
			return "07";
		else if (mes.equals("Ago"))
			return "08";
		else if (mes.equals("Sept"))
			return "09";
		else if (mes.equals("Oct"))
			return "10";
		else if (mes.equals("Nov"))
			return "11";
		else if (mes.equals("Dic"))
			return "12";
		return "0";
	}
	
	public static void getFechasPeticiones(Long idWS) {
		ServiciosWeb servicioWeb = ServiciosWeb.findById(idWS);
		List<Peticion> peticiones = servicioWeb.peticion;
		String jsData = "[";
		
		for (int i = 0; i < peticiones.size(); i++) {
			String fecha = peticiones.get(i).fechaPeticion.split("T")[0];
			String hora = peticiones.get(i).fechaPeticion.split("T")[1].split("\\.")[0];
			String fechaBien = fecha.split("-")[2] + "/" + fecha.split("-")[1] + "/" + fecha.split("-")[0];
			jsData += "['" + fechaBien + "-" + hora + "']";
			if (i != peticiones.size()-1)
				jsData += ", ";
		}
		
		jsData += "]";
		renderText(jsData);
	}
	
	public static void getDatosPeticiones(Long idWS, String fecha) {
		
		String day = fecha.split("-")[0];
		String time = fecha.split("-")[1];
		DateTime date = new DateTime(Integer.parseInt(day.split("/")[2]),
								Integer.parseInt(day.split("/")[1]), 
								Integer.parseInt(day.split("/")[0]),
								Integer.parseInt(time.split(":")[0]), 
								Integer.parseInt(time.split(":")[1]), 
								Integer.parseInt(time.split(":")[2]));

		Gson gson = new Gson();
		ServiciosWeb servicioWeb = ServiciosWeb.findById(idWS);
		ListaResultadosPeticion listaResultados = new ListaResultadosPeticion();
		for (int i = 0; i < servicioWeb.peticion.size(); i++) {
			
			String a = servicioWeb.peticion.get(i).fechaPeticion.split("\\.")[0];
			String b = date.toString().split("\\.")[0];

			if (a.equals(b)) {
				listaResultados = gson.fromJson(servicioWeb.peticion.get(i).stringJson, ListaResultadosPeticion.class);
			}
		}
		
		Map<String, String> mapVars = new HashMap<String, String>();
		String jsData = "";
		if (listaResultados.resultadosPeticion.size() != 0) {
			List<String> lista = new ArrayList<String>();
			
			for (int j = 0; j < servicioWeb.servicioWebInfo.infoParams.size(); j++) {
				lista.add(servicioWeb.servicioWebInfo.infoParams.get(j).tipo);
			}
			
			int k = 0;
			for (int m = 0; m < listaResultados.resultadosPeticion.size(); m++) {
				for (int i = 0; i < listaResultados.resultadosPeticion.get(m).resultadoPeticion.size(); i++) {
					List<ResultadoPeticion> x = listaResultados.resultadosPeticion.get(m).resultadoPeticion;
					if (!x.get(i).nombre.startsWith("id") && !mapVars.containsKey(x.get(i).nombre)) {
						mapVars.put(x.get(i).nombre, lista.get(k));
						k++;
					}
				}
			}
			
			jsData = "[";
			Iterator it = mapVars.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry)it.next();
				jsData += "['" + e.getKey() + "', '"+ e.getValue()+"']";
				if (it.hasNext())
					jsData += ", ";
			}
			jsData += "]";

			renderText(jsData);
			
		} else {
			Messages.warning("No existen datos para ese servicio web.");
			play.Logger.error("No existen datos para ese servicio web.");
			Messages.keep();
			renderText("");
		}
		
	}
}

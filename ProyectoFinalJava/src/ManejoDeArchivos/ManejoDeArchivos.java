package ManejoDeArchivos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import ConexionDB.Conexion;

public class ManejoDeArchivos {
	
	Scanner sc = new Scanner(System.in);
	String nombreArchivo = null;
	String nombreCarpeta = null;
	String carpetaDestino = null;
	
	//ATRIBUTOS DB:
	private String medioDePago;
	private int montoTotal;
	private String destino;
	private Date fechaDeTransaccion = null;	
	private int idTransaccion = 0;
	private String idEmpleado = null;
	
	//ATRIBUTOS PARA LA CONEXIÓN: 
	Conexion conexion = new Conexion();
	private Connection cn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	
	public ManejoDeArchivos() {
		
	}
	public void crearYEscribirArchivo() {
		
		cn = conexion.conectar();
		String carpetaDestino = "facturas";
		
		File carpeta = new File(carpetaDestino);
			
		if(!carpeta.exists()) {
				carpeta.mkdirs();
			}
	
		
		System.out.println("Nombre de su factura:");

		nombreArchivo = sc.nextLine().trim().replace(" ", "_").concat(".txt");
		
		String rutaCompletaDelArchivo = carpetaDestino + "/" + nombreArchivo;
		
		String rutaAbsoluta = carpeta.getAbsolutePath();
		
		File file = new File(rutaCompletaDelArchivo);	

		
		String query1 = "SELECT transaccion.idTransaccion,"
				+ "transaccion.fechaDeTransaccion,"
				+ "transaccion.medioDePago,"
				+ "transaccion.montoTotal,"
				+ "ventas.idVentas,"
				+ "ventas.empleado_id "
				+ "FROM transaccion "
				+ "INNER JOIN ventas "
				+ "ON transaccion.idTransaccion = ventas.transaccion_id";
		String query2 = "SELECT transaccion.idTransaccion,"
				+ "transaccion.fechaDeTransaccion,"
				+ "transaccion.medioDePago,"
				+ "transaccion.montoTotal,"
				+ "gastos.destino,"
				+ "gastos.transaccion_id "
				+ "FROM transaccion "
				+ "INNER JOIN gastos "
				+ "ON transaccion.idTransaccion = gastos.transaccion_id";
		
		try {
			ps = cn.prepareStatement(query1);
			rs = ps.executeQuery();

		ArrayList<String>datos = new ArrayList<>();
		String datosStringParaMostrar = null;
		String datosStringParaImprimir = null;
		
		// OBTENER DATOS DE VENTAS:
		while(rs.next()) {
			idTransaccion = rs.getInt("idTransaccion");
			idEmpleado = rs.getString("empleado_id");
			fechaDeTransaccion = rs.getDate("fechaDeTransaccion");
			medioDePago = rs.getString("medioDePago");
			montoTotal = rs.getInt("montoTotal");
			
					datosStringParaMostrar = "Datos de la transacción:[" 
					+ "ID transaccion: " + idTransaccion
					+ ", monto total: " + montoTotal
					+ ", medio de pago: " + medioDePago
					+ ", fecha de transaccion: " + fechaDeTransaccion
					+ ", ID empleado: " + idEmpleado
					+ "Tipo de transacción: Venta]";
					
					datosStringParaImprimir = "Datos de la transacción: \n"
							+ "- ID transaccion: " + idTransaccion + "\n"
							+ "- Monto total: " + "$" + montoTotal  + "\n"
							+ "- Medio de pago: " + medioDePago + "\n" 
							+ "- Fecha de transaccion: " + fechaDeTransaccion + "\n"
							+ "- ID empleado: " + idEmpleado + "\n"
							+ "- Tipo de transaccion: Venta";
			
			datos.add(datosStringParaMostrar);
		}
		
		ps = cn.prepareStatement(query2);
		rs = ps.executeQuery();
		
		// OBTENER DATOS DE GASTOS:
		while(rs.next()) {
			idTransaccion = rs.getInt("idTransaccion");
			fechaDeTransaccion = rs.getDate("fechaDeTransaccion");
			destino = rs.getString("destino");
			
			datosStringParaMostrar = "Datos de la transaccion:["
					+ "ID transaccion: " + idTransaccion 
					+ ", destino: " + destino 
					+ ", fecha de transaccion: " + fechaDeTransaccion
					+ ", tipo de transaccion: Gasto]";
			
			datosStringParaImprimir = "Datos de la transacción: \n"
					+ "- ID transaccion: " + idTransaccion + "\n"
					+ "- Destino: " + destino  + "\n"
					+ "- Fecha de transaccion: " + fechaDeTransaccion + "\n"
					+ "- Tipo de transaccion: Gasto";
			
			datos.add(datosStringParaMostrar);
		}
		
		System.out.println("Seleccione la transacción para generar factura por ID: ");
		
		for(String datoTransaccion : datos) {
			
			System.out.println(datoTransaccion);
			
		}
		
		int idSeleccionado = sc.nextInt();
		
		try {
			
			String query = "SELECT * FROM transaccion WHERE idTransaccion = ?";
			ps = cn.prepareStatement(query);
			ps.setInt(1, idSeleccionado);
			
			if(!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter escritor = new FileWriter(file);
			BufferedWriter buffer = new BufferedWriter(escritor);
			
			buffer.write(datosStringParaImprimir);
			
			buffer.close();
			
			System.out.println("Su factura se ha generado exitosamente en la carpeta con la siguiente direccion: " + rutaAbsoluta);
			
			
		} catch(IOException e) {
			e.printStackTrace();
		}
			
			} catch(SQLException e) {
			e.printStackTrace();
			}	

	}
	
	public void leerArchivo() {
		boolean realizarOtraAccion = true;
		
		while(realizarOtraAccion) {
			System.out.println("Seleccione la factura que desea leer: ");
			String carpetaDestino = "facturas";

			File carpeta = new File(carpetaDestino);
			ArrayList<File>listaDeArchivos = new ArrayList<>();
			
			try {
				if(carpeta.exists() && carpeta.isDirectory()) {
					
					/* Creo una lista de tipo File que va a contener un listado de todos los archivos dentro de la carpeta*/
					File[]archivos = carpeta.listFiles();
					
					/* Si la lista contiene algo, mostraremos su contenido, específicando un índice para que pueda ser seleccionado. */
					if(archivos != null && archivos.length > 0) {
						System.out.println("Facturas disponibles: ");
						
						for(File archivo : archivos) {
							listaDeArchivos.add(archivo);
							System.out.println(listaDeArchivos.size() - 1 + ". " + archivo.getName());
						}
						
						
					}
					
					if(!listaDeArchivos.isEmpty()) {
					
					int facturaSeleccionada = sc.nextInt();
					
					/* Obtenemos, si es que existe una facturta con ese índice, los datos de esa factura, asignandole a un objeto de 
					 * tipo File el nombre de la factura seleccionada (la cuál se buscará según el índice insertado). */
					if(facturaSeleccionada >=0 && facturaSeleccionada <= listaDeArchivos.size()) {
						File archivoSeleccionado = listaDeArchivos.get(facturaSeleccionada);
						
						FileReader lector = new FileReader(archivoSeleccionado);
						
						BufferedReader buffer = new BufferedReader(lector);
						
						String linea = buffer.readLine();
						while (linea != null) {
								System.out.println(linea);
								
								linea = buffer.readLine();
						}
						buffer.close();

					} else {
						System.out.println("Índice de factura no válido.");
					}
					
					} else {
						System.out.println("No hay facturas en la carpeta contenedora!");
					}
				} else {
					System.out.println("La carpeta no existe.");
				}

			} catch(IOException e) {
				e.printStackTrace();
			}
		
			System.out.println("Desea leer otra factura?");
			System.out.println("1. SI");
			System.out.println("0. NO");
			int opcionAccion = sc.nextInt();
			
			if(opcionAccion == 1) {
				realizarOtraAccion = true;
			} else if(opcionAccion == 0) {
				realizarOtraAccion = false;
			} else {
				System.out.println("Opción no válida. Se asume que no desea leer otra factura.");
				realizarOtraAccion = false;
			}
			
		}
		
	}
	
	public void eliminarArchivo() {
		System.out.println("Seleccione la factura que desea eliminar: ");
		String carpetaDestino = "facturas";

		File carpeta = new File(carpetaDestino);
		ArrayList<File>listaDeArchivos = new ArrayList<>();
		
		if(carpeta.exists() && carpeta.isDirectory()) {
			
			/* Creo una lista de tipo File que va a contener un listado de todos los archivos dentro de la carpeta*/
			File[]archivos = carpeta.listFiles();
			
			/* Si la lista contiene algo, mostraremos su contenido, específicando un índice para que pueda ser seleccionado. */
			if(archivos != null && archivos.length > 0) {
				System.out.println("Facturas disponibles: ");
				
				for(File archivo : archivos) {
					listaDeArchivos.add(archivo);
					System.out.println(listaDeArchivos.size() - 1 + ". " + archivo.getName());
				}
				
				
			}
			
			if(!listaDeArchivos.isEmpty()) {
			
			int facturaSeleccionada = sc.nextInt();
			
			/*  Si es que existe un índice igual al ingresado, se eliminará ese archivo de la carpeta. */
			if(facturaSeleccionada >=0 && facturaSeleccionada <= listaDeArchivos.size()) {
				
				
				File archivoSeleccionado = listaDeArchivos.get(facturaSeleccionada);
				AtomicBoolean bandera = new AtomicBoolean(true);
				
				while(bandera.get()) {
					System.out.println("Usted desea eliminar el siguiente archivo?: " + archivoSeleccionado);
					System.out.println("1. SI");
					System.out.println("0. NO");
					int opcionEliminar = sc.nextInt();
					
					if(opcionEliminar == 1) {
						if(archivoSeleccionado.delete()) {
							System.out.println("El archivo seleccionado ha sido eliminado exitosamente!");
							bandera.set(false);
						} else {
							System.err.println("No se ha podido eliminar el archivo.");
							bandera.set(false);
						}
					} else if(opcionEliminar == 0) {
						System.out.println("Okay!");
						bandera.set(false);
					} else {
						System.out.println("Por favor, ingrese una opción válida.");
						bandera.set(true);
					}
				} 
	
			} else {
				System.out.println("Índice de factura no válido.");
			}
			
		} else {
			System.out.println("No hay facturas en la carpeta contenedora!");
		}

		} else {
			System.out.println("La carpeta no existe.");
		}
		
	}
	
	public void actualizarArchivo() {
		boolean realizarOtraAccion = true;
		
		while(realizarOtraAccion) {
			System.out.println("Seleccione la factura que desea actualizar: ");
			String carpetaDestino = "facturas";

			File carpeta = new File(carpetaDestino);
			ArrayList<File>listaDeArchivos = new ArrayList<>();
			
			try {
				if(carpeta.exists() && carpeta.isDirectory()) {
					
					/* Creo una lista de tipo File que va a contener un listado de todos los archivos dentro de la carpeta*/
					File[]archivos = carpeta.listFiles();
					
					/* Si la lista contiene algo, mostraremos su contenido, específicando un índice para que pueda ser seleccionado. */
					if(archivos != null && archivos.length > 0) {
						System.out.println("Facturas disponibles: ");
						
						for(File archivo : archivos) {
							listaDeArchivos.add(archivo);
							System.out.println(listaDeArchivos.size() - 1 + ". " + archivo.getName());
						}
						
					}
					
					if(!listaDeArchivos.isEmpty()) {
					
					int facturaSeleccionada = sc.nextInt();
					
					/* Obtenemos, si es que existe una facturta con ese índice, los datos de esa factura, asignandole a un objeto de 
					 * tipo File el nombre de la factura seleccionada (la cuál se buscará según el índice insertado). */
					if(facturaSeleccionada >=0 && facturaSeleccionada <= listaDeArchivos.size()) {
						File archivoSeleccionado = listaDeArchivos.get(facturaSeleccionada);
						
						FileWriter escritor = new FileWriter(archivoSeleccionado);
						BufferedWriter buffer = new BufferedWriter(escritor);
						System.out.println("ID transaccion: ");
						idTransaccion = sc.nextInt();
						System.out.println("Destino: ");
						destino = sc.nextLine();
						System.out.println("Tipo de transaccion: ");
						String tipoDeTransaccion = sc.nextLine();
						System.out.println("Fecha de transaccion: ");
						System.out.println("Dia: ");
						int dia = sc.nextInt();
						System.out.println("Mes: ");
						int mes = sc.nextInt();
						System.out.println("Año: ");
						int año = sc.nextInt();
						LocalDate fechaDeTransaccion = LocalDate.of(año, mes, dia);
						Date fechaDeTransaccionDate = Date.valueOf(fechaDeTransaccion);

						
						
						String datosStringParaActualizar = "Datos de la transaccion: \n"
								+ "- ID transaccion: " + idTransaccion + "\n"
								+ "- Destino: " + destino + "\n"
								+ "- Fecha de transaccion: " + fechaDeTransaccionDate + "\n"
								+ "- Tipo de transaccion: " + tipoDeTransaccion;
						
						buffer.write(datosStringParaActualizar);
						
						buffer.close();
						
						System.out.println("Archivo actualizado exitosamente!");

					} else {
						System.out.println("Índice de factura no válido.");
					}
					
					} else {
						System.out.println("No hay facturas en la carpeta contenedora!");
					}
				} else {
					System.out.println("La carpeta no existe.");
				}

			} catch(IOException e) {
				e.printStackTrace();
			}
		
			System.out.println("Desea leer otra factura?");
			System.out.println("1. SI");
			System.out.println("0. NO");
			int opcionAccion = sc.nextInt();
			
			if(opcionAccion == 1) {
				realizarOtraAccion = true;
			} else if(opcionAccion == 0) {
				realizarOtraAccion = false;
			} else {
				System.out.println("Opción no válida. Se asume que no desea leer otra factura.");
				realizarOtraAccion = false;
			}
			
		}
		
		
	}

}

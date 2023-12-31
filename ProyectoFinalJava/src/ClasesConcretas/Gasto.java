package ClasesConcretas;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

import ClasesAbstractas.Transaccion;
import ConexionDB.Conexion;
import Interfaces.GestionDeFacturas;

public class Gasto extends Transaccion implements GestionDeFacturas<Gasto>{

	// ATRIBUTOS
	private String destino;
	
	
	//ATRIBUTOS PARA LA CONEXIÓN: 
	Conexion conexion = new Conexion();
	private Connection cn = null;
	private PreparedStatement ps = null;
	private ResultSet rs = null;
	
	//ATRIBUTOS AUXILIARES: 
	Scanner sc = new Scanner(System.in);
	
	/* Inicializo las variables globalmente. Estas van a ser rellenadas con lo que se obtenga 
	 * de la base de datos.*/
	int idTransaccion = 0;
	Date fechaDeTransaccion = null;
	String medioDePago = null;
	float montoTotal = 0;
	int idGastos = 0;
	
	
	// CONSTRUCTOR
	public Gasto(String medioDePago, int montoTotal, String destino) {
		super(medioDePago, montoTotal);
		this.destino = destino;
	}

	
	// MÉTODOS: 
	@Override
	public String toString() {
		return "Gastos [destino=" + destino + "]";
	}


	public String getDestino() {
		return destino;
	}



	public void setDestino(String destino) {
		this.destino = destino;
	}
	
	/* Al intentar overridear con el tipo de dato "Factura" únicamente, hay ciertos parámetros pertenecientes a Gasto
	 * como "getDestino" que no se podrían llamar. Por lo tanto, el tipo de dato que recibe generarFactura en este caso sería
	 * Gasto, para poder traer consigo todo lo de "Factura" y a su vez incorporar los atributos de "Gasto".
	 * Si yo quisiera implementar la interfaz directamente en esta clase "Gasto" se podría hacer únicamente si saco la implementación
	 * del objeto "Transaccion". 
	 * La implementación del método "generarFactura" en el objeto "Transaccion" tal vez no sería necesaria ya que este se va a aplicar 
	 * desde las clases hijas (Gasto y Venta), trayendo consigo todo lo que pertenezca a Transaccion. 
	 * Otra opción podría ser crear métodos abstractos desde la clase "Transaccion" e incorporarlos en estas clases hijas. */
	
	@Override
	public void generarFactura() {
		try {
			// Establecemos conexion con la base de datos.
			cn = conexion.conectar();
			
			/* Pasamos la fecha que se registro al momento de instanciar el objeto a uno de tipo Date 
			 * para que se pueda registrar en la DB. */
			LocalDate fecha = LocalDate.now();
			Date fechaSQL = Date.valueOf(fecha);
			
			System.out.println("Ingrese el medio de pago: ");
			medioDePago = sc.nextLine();
			System.out.println("Ingrese el monto total: ");
			
			montoTotal = sc.nextFloat();
		

			sc.nextLine();
			System.out.println("Ingrese el destino: ");
			destino = sc.nextLine();

			/* Insertamos los datos que vienen con el objeto "gasto" que recibe la función por parámetro. */
			String query = "INSERT INTO transaccion (fechaDeTransaccion, medioDePago, montoTotal) VALUES (?, ?, ?)";
			ps = cn.prepareStatement(query);
			
			ps.setDate(1, fechaSQL);
			ps.setString(2, medioDePago);
			ps.setFloat(3, montoTotal);
			ps.executeUpdate();
			
			
			/* Creo una variable "idTransaccion" para asignarle el valor de la idTransaccion al llamar a la tabla
			 * "transaccion" en la base de datos. Esto con el fin de poder asignarle el valor "transaccion_id" a la 
			 * tabla "gastos" y poder generar el enlazamiento de los generales de la Transaccion con el Gasto que le pertenece. */
			int idTransaccion = 0;
			
			/* Llamo al idTransaccion */
			String query2 = "SELECT idTransaccion FROM transaccion";
			rs = ps.executeQuery(query2);
			while(rs.next()) {
				idTransaccion = rs.getInt(1);
			}

			/* Inserto los datos del objeto Gasto en la tabla "gastos" con su respectivo transaccion_id que va a conectarlo con la
			 * Transaccion a la que pertenece. */
			String query3 = "INSERT INTO gastos(destino, transaccion_id) VALUES (?, ?)";
			ps = cn.prepareStatement(query3);
			ps.setString(1, destino);
			ps.setInt(2, idTransaccion);
			ps.executeUpdate();

			
			System.out.println("Se han insertado los datos correctamente");

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Override 
	public void buscarFactura() {		
		try {
			
			cn = conexion.conectar();
			
			/* Obtengo el ArrayList que retorna el método mostrarGastos, para comprobar si tiene contenido o no. */
			ArrayList<String> datos = mostrarGastos();

			
			if(datos.isEmpty() == false) {
			System.out.println("Ingrese el id del gasto que desea ver: ");
			int id = sc.nextInt();
			
			/* Con el siguiente QUERY llamamos a la tabla de transaccion y la tabla de gastos correspondiente al id ingresado. El id ingresado será el id
			 * perteneciente a la transaccion, el cual a su vez es el "transaccion_id" correspondiente al gasto. */
			String query = "SELECT transaccion.idTransaccion,"
					+ "transaccion.fechaDeTransaccion,"
					+ "transaccion.medioDePago,"
					+ "transaccion.montoTotal,"
					+ "gastos.idGastos,"
					+ "gastos.destino "
					+ "FROM transaccion "
					+ "INNER JOIN gastos "
					+ "ON transaccion.idTransaccion = gastos.transaccion_id "
					+ "WHERE transaccion.idTransaccion = " + id; // Concateno el id ya que al poner "?" y luego ingresarlo con el ps.setInt(x, x) me daba error.
			ps = cn.prepareStatement(query);
			ps.executeQuery();
		
			rs = ps.executeQuery(query);
			/* Obtenemos los datos desde la DB y los almacenamos en sus correspondientes variables.*/
			while(rs.next()) {
				idTransaccion = rs.getInt("idTransaccion");
				fechaDeTransaccion = rs.getDate("fechaDeTransaccion");
				medioDePago = rs.getString("medioDePago");
				montoTotal = rs.getFloat("montoTotal");
				idGastos = rs.getInt("idGastos");
				destino = rs.getString("destino");
			}
			
			/* Luego, mostramos los datos obtenidos por pantalla. */
			System.out.println("Datos solicitados: " + "[ID de transaccion= " + idTransaccion
					+ ", ID de gasto= " + idGastos 
					+", fecha= " + fechaDeTransaccion 
					+ ", medio de pago= " + medioDePago 
					+ ", monto total= " + montoTotal 
					+ ", destino= " + destino + "]");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	/* Método axuliar para mostrar Gastos. */
	public ArrayList<String> mostrarGastos() {
		/* Creo un ArrayList en donde se registrarán todos los Gastos existentes en la DB. */
		ArrayList<String>datos = new ArrayList<>();
		
		try {
			/* MOSTRAR DATOS */
			
			cn = conexion.conectar();

			
			/* Llamo a los datos de los Gastos.*/
			String query1 = "SELECT transaccion.idTransaccion,"
					+ "transaccion.fechaDeTransaccion,"
					+ "transaccion.medioDePago,"
					+ "transaccion.montoTotal,"
					+ "gastos.idGastos,"
					+ "gastos.destino "
					+ "FROM transaccion "
					+ "INNER JOIN gastos "
					+ "ON transaccion.idTransaccion = gastos.transaccion_id ";
			
			ps = cn.prepareStatement(query1);
			rs = ps.executeQuery(query1);
			
			/* Leo los datos */
			while (rs.next()) {
				idTransaccion = rs.getInt("idTransaccion");
				fechaDeTransaccion = rs.getDate("fechaDeTransaccion");
				medioDePago = rs.getString("medioDePago");
				montoTotal = rs.getFloat("montoTotal");
				idGastos = rs.getInt("idGastos");
				destino = rs.getString("destino");
				
				/* Registro los datos en una variable de tipo String para despuyés insertarla en el ArrayList de tipo String.*/
				String cadenaDeDatos = "[ID de transaccion= " + idTransaccion
						+ ", ID de gasto= " + idGastos 
						+", fecha= " + fechaDeTransaccion 
						+ ", medio de pago= " + medioDePago 
						+ ", monto total= " + montoTotal 
						+ ", destino= " + destino + "]";
				
				/* Inserto en el ArrayList los datos que se obtuvieron de la base de datos. */
					datos.add(cadenaDeDatos);
				
			}
			
			/* Muestro en pantalla los datos obtenidos desde la base de datos. */
			if(datos.isEmpty() == false) {
				System.out.println("Gastos registrados en el sistema: ");
				for (String dato : datos) {
					System.out.println(dato);
				}
			} else {
								
				System.out.println("No hay gastos registrados en el sistema!");
				
				
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return datos;
		
	}
	

	/* Este método muestra los datos existentes y nos da la opción de seleccionar por ID. */
	@Override
	public void eliminarFactura() {
		try {
			/* MOSTRAR DATOS */

			cn = conexion.conectar();

			/* Creo un ArrayList en donde se registrarán todos los Gastos existentes en la DB. */
			
			ArrayList<String> datos = mostrarGastos();
			
			int idSeleccionador = 0;
			
			/* BORRAR DATOS (esto se puede hacer en un método aparte en donde llamemos un método para mostrar los datos. */
			
			if(datos.isEmpty() == false) {
				System.out.println("Ingrese el ID de la factura que desea eliminar: ");
				idSeleccionador = sc.nextInt();
				
				/* Creamos dos QUERY, uno para borrar en primera instancia los datos contenidos en "gastos". En segunda instancia
				 * borramos los datos en "transaccion". Esto se hace ya que no se puede borrar la "transaccion" primero debido a que 
				 * tiene una llave foranea de la cual depende "gastos". Hacer esto es un modo de simular el borrado simultáneo. */
				String query1 = "DELETE from gastos WHERE transaccion_id ="+idSeleccionador;
				String query2 = "DELETE from transaccion WHERE idTransaccion="+idSeleccionador;
				
				ps = cn.prepareStatement(query1);
				ps.executeUpdate();
				
				ps = cn.prepareStatement(query2);
				ps.executeUpdate();
				
				System.out.println("El gasto seleccionado ha sido eliminada con éxito!");
				
				/* Si no hay ningún dato dentro de la base de datos, lo que hacen los siguientes QUERIES es
				 * actualizar el incremento del ID. Sin esto, los id's de los siguientes registros empezarán
				 * con el número siguiente al último id borrado. Ahora, los id's, al no tener contenido la tabla, comenzarán 
				 * su auto-incremento desde cero nuevamente. */

				/* Verificamos si no hay registros en la tabla antes de restablecer sus valores de ID a 0*/
				
				/* Sumamos las ventas en un atributo propio de SQL al que llamamos gastos, para verificar si existen gastos o no. */
				String verificarRegistrosQueryGastos = "SELECT COUNT(*) as total FROM gastos";
				ps = cn.prepareStatement(verificarRegistrosQueryGastos);
				rs = ps.executeQuery();
				
				/* Ejecutamos un condicional para verificar si hay resultados de la consulta. */
				if(rs.next()) {
					
					/* Registramos el total de gastos que existen en la DB */
					int totalRegistrosGastos = rs.getInt("total");
					
					/* Por lo tanto, si ese total nos da igual a 0, significa que no hay registros de gastos por lo que actualizará el valor 
					 * del auto incremento del ID a 0. */
					if(totalRegistrosGastos == 0) {
						String actualizarIDGastos = "ALTER TABLE gastos AUTO_INCREMENT = 1";
						
						ps = cn.prepareStatement(actualizarIDGastos);
						ps.executeUpdate();
					}
				}
				
				String verificarRegistrosQueryTransaccion = "SELECT COUNT(*) as total FROM transaccion";
				ps = cn.prepareStatement(verificarRegistrosQueryTransaccion);
				rs = ps.executeQuery();
				
				if(rs.next()) {
					int totalRegistrosTransaccion = rs.getInt("total");
					
					if(totalRegistrosTransaccion == 0) {
						String actualizarIDTransaccion = "ALTER TABLE transaccion AUTO_INCREMENT = 1";
						
						ps = cn.prepareStatement(actualizarIDTransaccion);
						ps.executeUpdate();
					}
				}
				
	
				
			}


			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}




	
	

	
	
}

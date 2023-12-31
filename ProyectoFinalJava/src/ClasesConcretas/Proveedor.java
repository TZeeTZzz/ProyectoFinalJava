package ClasesConcretas;
import java.sql.*;

import java.util.ArrayList;

import Interfaces.GestionDeDatos;
import ConexionDB.Conexion;
import java.util.Scanner;
public class Proveedor implements GestionDeDatos<Proveedor>{


	//ATRIBUTOS PARA CONEXION
	Conexion conexion = new Conexion();
	private Connection cn = null;
	
	//SCANNER
	Scanner scanner = new Scanner (System.in);
	
	//CONSTRUCTOR
	public Proveedor() {
	}
	
	
	public void verProductos (int id) {
	
	try{	
		cn = conexion.conectar();
		
		String query = "SELECT *, proveedor.idProveedor, proveedor.nombreProveedor, proveedor.telefonoProveedor FROM producto INNER JOIN proveedor ON producto.proveedor_id = proveedor.idProveedor WHERE proveedor.idProveedor = ?";
		
		PreparedStatement declaracion  = cn.prepareStatement(query);
		declaracion.setInt(1, id);
		ResultSet resultados = declaracion.executeQuery();
		
		boolean proveedorEncontrado = false;
			
			while(resultados.next()) {
			 // Necesito reemplazar el if por un while, ya que este recorrerá todos los registros devueltos, y no solo el primero
				
				proveedorEncontrado = true;//si hubo resultados por recorrer quiere decir que hay un proveedor para ese ID
				
			System.out.println(
					"ID: " + resultados.getInt("idProducto") + ". Nombre: " +resultados.getString("nombreProducto") + 
					". Categoria: " + resultados.getString("categoria") + ". Stock: " + resultados.getInt("stockDisponible") + 
					". Precio: " + resultados.getInt("precioxUnidad"));
			}
			
			if (!proveedorEncontrado) { //si no se encontro proveedor con ese id, entonces la variable booleana se mantendra falsa
			    System.out.println("ID inválido! Vuelva a intentarlo.");
			}
		
		}catch(SQLException e){
		e.printStackTrace();
	}
	}
	

	//OVERRIDES
	
	@Override
	public void Ver() {
		
		try{
			cn = conexion.conectar();
			
			String query = "SELECT * FROM proveedor";
			
			Statement declaracion = cn.createStatement();
			ResultSet resultados = declaracion.executeQuery(query);
			
			while(resultados.next()) { //mientras haya datos por leer
			System.out.println(
						"ID: " + resultados.getInt("idProveedor") + ". Nombre: " +resultados.getString("nombreProveedor") + 
						 ". Telefono: " + resultados.getLong("telefonoProveedor"));
			}
		} catch(SQLException e){
			e.printStackTrace();
		} 
	}

	@Override
	public void Buscar(int ID) {
		
			try{
				cn = conexion.conectar();
				
				String query = "SELECT * FROM proveedor WHERE  idProveedor = ?";
				
				PreparedStatement declaracion  = cn.prepareStatement(query);
			
					declaracion.setInt(1, ID);
					ResultSet resultados = declaracion.executeQuery();
					
				if(resultados.next()) { //mientras haya datos por leer
					//Puede ser in if ya que solo se va a leer un registro
						System.out.println(
									"ID: " + resultados.getInt("idProveedor") + ". Nombre: " +resultados.getString("nombreProveedor") + 
									 ". Telefono: " + resultados.getLong("telefonoProveedor"));
				}else {
					System.out.println("ID inválido! Vuelva a intentarlo.");
				}
				
			} catch(SQLException e){
				e.printStackTrace();
			} 
	}
	
	@Override
	public void Actualizar(int id) {
		
		try{
			cn = conexion.conectar();
			
			String querySelect = "SELECT * FROM proveedor WHERE  idProveedor = ?";
			
			PreparedStatement declaracionSelect  = cn.prepareStatement(querySelect);
		
			declaracionSelect.setInt(1, id);
			ResultSet resultados = declaracionSelect.executeQuery();
			
			if(resultados.next()) {
				
			boolean salir = false;
				
			  while (!salir) {
				  
				System.out.println("Ingrese el atributo que quisiera actualizar");
				System.out.println("1. Nombre.");
				System.out.println("2. Telefono.");
				int opcion = scanner.nextInt();
				
					switch(opcion) {
					
					case 1:
						scanner.nextLine();
						System.out.println("Nombre:");
						String nombreProveedor = (scanner.nextLine()).trim().replace(" ", "_");
						
						String queryUpdate1 = "UPDATE proveedor SET nombreProveedor = ? WHERE  idProveedor = ?";
						
						PreparedStatement declaracionUpdate1  = cn.prepareStatement(queryUpdate1);
					
						declaracionUpdate1.setString(1, nombreProveedor);
						declaracionUpdate1.setInt(2, id);
							
						declaracionUpdate1.executeUpdate();
						
					break;
						
					case 2:
						scanner.nextLine();
						System.out.println("Telefono:");
						long  telefono = scanner.nextLong();
						
						String queryUpdate = "UPDATE proveedor SET telefonoProveedor = ? WHERE  idProveedor = ?";
						
						PreparedStatement declaracionUpdate  = cn.prepareStatement(queryUpdate);
					
						declaracionUpdate.setLong(1, telefono);
						declaracionUpdate.setInt(2, id);
							
						declaracionUpdate.executeUpdate();
					break;
					
					default:
						System.out.println("Opcion inválida! Vuelva a intentarlo.");
						break;
					}
					
					System.out.println("Desea seguir actualizando? (si/no)");
					String seguir = scanner.next();
					  
					  if(seguir.equalsIgnoreCase("si")) {
						  
						  salir = false;
						  
					  }else {
						  salir = true;
					  }
				}
				
				//VER DATOS ACTUALIZADOS
				ResultSet resultados2 = declaracionSelect.executeQuery();
					
				while(resultados2.next()) { //mientras haya datos por leer
						System.out.println("Proceso exitoso! Datos actualizados: ");
						System.out.println(
							"ID: " + resultados2.getInt("idProveedor") + ". Nombre: " +resultados2.getString("nombreProveedor") + 
							". Telefono: " + resultados2.getLong("telefonoProveedor"));
					}
			}else {
				System.out.println("ID inválido! Vuelva a intentarlo.");
			}
		} catch(SQLException e){
			e.printStackTrace();
		} 
		
	}
	
	@Override
	public void Agregar() {
		
		System.out.println("Por favor, ingrese los datos correspondientes");
		System.out.println("Nombre: ");
		String nombreProveedor = (scanner.nextLine()).trim().replace(" ", "_");
		System.out.println("Telefono: ");
		long telefono = scanner.nextLong();

		try{
			cn = conexion.conectar();
			
			String query = "INSERT INTO proveedor (nombreProveedor, telefonoProveedor) VALUES (?, ?)";
			//excluyo el id ya que es autoincremental
			
			PreparedStatement declaracion  = cn.prepareStatement(query);
			
				declaracion.setString(1, nombreProveedor);
				declaracion.setLong(2, telefono);
				
				declaracion.executeUpdate();
				
				System.out.println("Datos cargados exitosamente!");
			
		} catch(SQLException e){
			e.printStackTrace();
		} 
	}
	
	
	@Override
	public void Eliminar(int id) {
		
		try{
			cn = conexion.conectar();
			
			String querySelect = "SELECT * FROM proveedor WHERE  idProveedor = ?";
			
			PreparedStatement declaracionSelect  = cn.prepareStatement(querySelect);
		
			declaracionSelect.setInt(1, id);
			ResultSet resultados = declaracionSelect.executeQuery();
				
			if(resultados.next()) { //mientras haya datos por leer
				System.out.println("Seguro que desea eliminar el proveedor: ");
					System.out.println(
								"ID: " + resultados.getInt("idProveedor") + ". Nombre: " +resultados.getString("nombreProveedor") + 
								 ". Telefono: " + resultados.getInt("telefonoProveedor"));
					System.out.println("Si eliminas el proveedor, ten en cuenta que tambien seran eliminados todos los productos que provee");
					System.out.println("Ingrese si/no segun desee.");
					String confirmacion = scanner.nextLine();
					
					 if(confirmacion.equalsIgnoreCase("si")) {
						 
						 try{
							 cn = conexion.conectar();
						
							 String queryDelete = "DELETE FROM proveedor WHERE idProveedor = ?";
							 //excluyo el id ya que es autoincremental
						
								PreparedStatement declaracionDelete  = cn.prepareStatement(queryDelete);
								
								declaracionDelete.setInt(1, id);
								declaracionDelete.executeUpdate();
							
							 	System.out.println("Proveedor eliminado del sistema!");
						
						 } catch(SQLException e){
							 e.printStackTrace();
						 }
						 
					  }else {
						  System.out.println("Perfecto! El proveedor no será eliminado");
					  }
			}else {
				System.out.println("ID inválido! Vuelva a intentarlo.");
			}
		}catch(SQLException e){
				e.printStackTrace();
			} 
	}
}

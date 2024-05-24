package tarjetaAlmacen;
import java.io.*;
/*
 * Estructura de la tarjeta de almacen
 * 
 * Concepto String 2+2
 * Entrada/Salida	int 4
 * Existencia int 4
 * Unitario double 8
 * Precio promedio double 8
 * Debe/Haber	double 8
 * Saldo double 8
 * 
 * Longitud x registro con precio promedio = 44 
 * Longitud x registro sin precio promedio = 36
 * 
 * Glosario
 * 
 * SI = Saldo Incial
 * C = Compra
 * V = Venta
 * DC = Devolucion sobre compra
 * DV = Devolucion sobre venta
 * */
public class TarjetaAlmacen {
	private RandomAccessFile file = null;
	private String metodoInventario, msg;
	final private int longitudXRegistro = 44;
	//Sin inventario incial
	public TarjetaAlmacen(String metodoInventario) {
		this.metodoInventario = metodoInventario.toUpperCase();
		msg = "";
		AbrirFile();
	}
	//Con inventario incial
	public TarjetaAlmacen(String metodoInventario, int cantidad, double precioXCantidad) {
		AbrirFile();
		msg = "";
		this.metodoInventario = metodoInventario.toUpperCase();
		AdquisicionInventario("SI", cantidad, precioXCantidad);
	}
	private boolean AbrirFile() {
		try {
			file = new RandomAccessFile("Tarjeta_Almacen.DAT","rw");
		}catch(Exception obj) {
			msg = "No se pudo Abrir el archivo";
			return false;
		}
		return true;
	}
	public boolean AdquisicionInventario(String concepto, int cantidad, double precioXCantidad) {
		try {
			int numRegistro = (int)file.length()/longitudXRegistro;
			int existencia = 0;
			double saldo = 0, precioPromedio;
			if(numRegistro > 0) {
				//La obtencion del dato de produtos existentes
				file.seek(file.length()- (metodoInventario.equals("PRECIO PROMEDIO") ? 36: 28));
				existencia = file.readInt();
				file.seek(file.getFilePointer() + ((metodoInventario.equals("PRECIO PROMEDIO"))?24:16));
				saldo = file.readDouble();
				file.seek(file.length());
			}
			//Concepto
			file.writeUTF(concepto); 
			//Entrada
			file.writeInt(cantidad);
			//Existencia
			file.writeInt(cantidad + existencia);
			//Unitario
			file.writeDouble(precioXCantidad);
			//Precio promedio si en dado caso estamos por el metodo dicho
			if(metodoInventario.equals("PRECIO PROMEDIO")) {
				file.writeDouble((saldo + cantidad*precioXCantidad)/(existencia +cantidad));
			}
			//Debe
			file.writeDouble(cantidad*precioXCantidad);
			//Saldo
			file.writeDouble(cantidad*precioXCantidad + saldo);
		}catch(Exception obj) {
			msg = "Hubo un error al anexar la adquisicion al inventario";
			return false;
		}
		return true;
	}
	public boolean VentaArticulo(int cantidad){
		try {
			//Poder evaluar si disponemos de inventario para realizar las acciones correspondientes
			file.seek(file.length());
			int numRegistro = (int)file.length()/(metodoInventario.equals("PRECIO PROMEDIO")? longitudXRegistro: (longitudXRegistro-8));
			int existencia = 0;
			double precioPromedio;
			if(numRegistro == 0) {
				msg = "No dispone de ninguna accion";
				return false;
			}
			file.seek(file.getFilePointer()-(metodoInventario.equals("PRECIO PROMEDIO")? 36: 28));
			existencia = file.readInt();
			if(cantidad > existencia) {
				msg = "No dispone de mercancia";
				return false;
			}
			switch(metodoInventario) {
			case "UEPS":
				break;
			case "PEPS":
				VentaXPeps(cantidad);
				break;
			case "PRECIO PROMEDIO":
				VentaXPrecioPromedio(cantidad);
				break;
			}
		}catch(Exception obj) {
			msg = "No se pudo vender el articulo por => "+obj.getMessage();
			return false;
		}
		return true;
	}
	//Metodo PEPS 
	private boolean VentaXPeps(int cantidad) {
		try {
			//La variable apuntador me indica hasta que registro tuvo que llegar para poder saciar la cantidad deseada y la variable residuo me indica lo que sobro del ultimo registro consumido
			int auxCantidad = cantidad, apuntador = -1, residuo = 0, entradaXRegistro, existencia = 0;
			String concepto = "";
			file.seek(file.length()-8);
			double saldo = file.readDouble();
			file.seek(file.length());
			//Para referenciar la cantidad de productos que saldran
			file.writeUTF(String.format("%-2s", "V"));
			file.writeInt(cantidad);
			file.writeInt(0);
			file.writeDouble(0);
			file.writeDouble(0);
			file.writeDouble(0);
			//Se resta -8 para descartar que el documento dispone de la longitud de bytes del precio promedio
			for(int i = 0; i <file.length()/(longitudXRegistro-8);i++) {
				file.seek(i*(longitudXRegistro-8));
				concepto = file.readUTF();
				if(concepto.contains("SI") || concepto.contains("C")) {
					apuntador = i;
					entradaXRegistro = file.readInt();
					if((auxCantidad - entradaXRegistro) <= 0) {
						residuo = Math.abs(auxCantidad - entradaXRegistro);
						existencia = file.readInt();
						break;
					}
					auxCantidad -= entradaXRegistro;
				}
			}
			//Procedemos a realizar la lectura hasta al apuntador y a la par la escritura de los registros consumidos procediendo al ultimo dato sumarle el residuo
			int accion = 0;
			double precioUnitario = 0, movimiento = 0;
			for(int i = 0; i < apuntador+1; i++) {
				file.seek(i*(longitudXRegistro-8));
				concepto = file.readUTF();
				if(concepto.contains("SI") || concepto.contains("C")) {
					accion = file.readInt();
					file.seek(file.getFilePointer()+4);
					precioUnitario = file.readDouble();
					movimiento = file.readDouble();
				}
				//Realizar la escritura al final de todos los datos ya establecidos
				file.seek(file.length());
				//Concepto
				file.writeUTF(String.format("%-2s", "V"));
				//Salida
				file.writeInt(accion);
				//Existencia actualizada
				file.writeInt(existencia - accion);
				//Precio dependiendo del registro
				file.writeDouble(precioUnitario);
				//Haber
				file.writeDouble(movimiento);
				//Saldo
				file.writeDouble(saldo - movimiento);
			}
			file.seek(file.length()-32);
			file.writeInt(accion -residuo);
			file.writeInt(existencia -(accion-residuo));
			file.seek(file.getFilePointer()+8);
			movimiento = (accion-residuo) * precioUnitario;
			file.writeDouble(movimiento);
			file.writeDouble(saldo - movimiento);
			
		}catch(Exception obj) {
			msg = "Hubo un error al realizar la venta";
			return false;
		}
		return true;
	}
	//Metodo PRECIO PROMEDIO
	private boolean VentaXPrecioPromedio(int cantidad) {
		try {
			file.seek(file.length());
			int numRegistro = (int)file.length()/longitudXRegistro;
			int existencia = 0;
			double saldo = 0, precioPromedio;
			if(numRegistro == 0) {
				msg = "No dispone de mercancia";
				return false;
			}
			file.seek(file.getFilePointer()-36);
			existencia = file.readInt();
			if(cantidad > existencia) {
				msg = "No dispone de mercancia";
				return false;
			}
			file.seek(file.getFilePointer()+8);
			precioPromedio = file.readDouble();
			file.seek(file.getFilePointer()+8);
			saldo = file.readDouble();
			//Concepto
			file.writeUTF("V");
			//Salida
			file.writeInt(cantidad);
			//Existencia
			file.writeInt(existencia - cantidad);
			//Unitario
			file.writeDouble(precioPromedio);
			//Precio promedio actualizado
			file.writeDouble((saldo + cantidad*precioPromedio)/(existencia +cantidad));
			//Haber
			file.writeDouble(cantidad*precioPromedio);
			//Saldo
			file.writeDouble(saldo - cantidad*precioPromedio);
		}catch(Exception obj) {
			msg = "Surgio un error durante la venta";
			return false;
		}
		return true;
	}
	public boolean lecturaFile() {
		try {
			file.seek(0);
			String concepto = "";
			int accion, existencia;
			double unitario, movimiento, saldo, precioPromedio = 0;
			System.out.printf("%-18s %-18s %-18s %-18s %-18s %-18s %-18s %-18s %-18s\n", "concepto", "Entrada","Salida", "Existencia"," Precio unitario", "Precio promedio", "Debe", "Haber", "Saldo" );
			for(int i = 0; i < file.length()/((metodoInventario.equals("PRECIO PROMEDIO")?longitudXRegistro:longitudXRegistro-8)); i++) {
				concepto = file.readUTF();
				accion = file.readInt();
				existencia = file.readInt();
				unitario = file.readDouble();
				if(metodoInventario.equals("PRECIO PROMEDIO")) {
					precioPromedio = file.readDouble();
				}
				movimiento = file.readDouble();
				saldo = file.readDouble();
				if(concepto.contains("V") || concepto.contains("DC")) {
					System.out.printf("%-18s %-18s %-18d %-18d %-18.2f %-18.2f %-18s %-18.2f %-18.2f\n", concepto, "",accion, existencia,unitario, precioPromedio, "", movimiento, saldo);
				}else {
					System.out.printf("%-18s %-18d %-18s %-18d %-18.2f %-18.2f %-18.2f %-18s %-18.2f\n", concepto, accion,"", existencia,unitario, precioPromedio, movimiento, "", saldo);
				}
			}
		}catch(Exception obj) {
			msg = "No se pudo realizar la lectura =>"+obj.getMessage();
			return false;
		}
		return true;
	}
	public boolean CerrarFile() {
		try {
			file.close();
		}catch(Exception obj) {
			msg = "El archivo no se pudo cerrar";
			return false;
		}
		return true;
	}
	public String getMsg() {
		return msg;
	}
	
}

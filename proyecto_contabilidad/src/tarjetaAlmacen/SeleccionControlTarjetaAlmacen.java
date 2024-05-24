package tarjetaAlmacen;
import java.util.Scanner;
public class SeleccionControlTarjetaAlmacen {
	/*
	 * Orden de mensajes a llamar de forma correcta
	 * 1.-SeleccionMetodoTarjeta();
	 * 2.-InventarioIncial(Variable del mensaje llamado anteriormente);
	 * 3.-Menu();
	 * */
	
	final Scanner sc = new Scanner(System.in);
	TarjetaAlmacen tj;
	//Asignacion del metodo de inventario
	public String SeleccionMetodoTarjeta() {
		int inputSeleccion;
		String metodoInventario = "";
		do {
			System.out.println(String.format("Escriba el metodo de inventario:\n1) UEPS\n2) PEPS\n3) Precio promedio"));
			inputSeleccion = sc.nextInt();
			switch(inputSeleccion) {
			case 1: 
				metodoInventario = "EUPS";
				break;
			case 2:
				metodoInventario = "PEPS";
				break;
			case 3: 
				metodoInventario = "PRECIO PROMEDIO";
				break;
			default:
				System.out.println("Ingrese un valor entre el 1 y 3");
			}
		}while(inputSeleccion > 3 || inputSeleccion < 0);
		return metodoInventario;
	}
	//Seleccion de asignacion de inventario incial
	public void InventarioIncial(String metodoInventario) {
		System.out.println("Desea anexar articulos de saldo incial, digite la opcion:\nS)Si\nN)No");
		char input = sc.next().charAt(0);
		//Sí requiere saldo incial
		if(input == 'S') {
			System.out.println("Ingrese la cantidad de articulos: ");
			int cantidad = sc.nextInt();
			System.out.println("\nIngrese el precio por articulo: ");
			Double precioXCantidad = sc.nextDouble();
			tj = new TarjetaAlmacen(metodoInventario, cantidad, precioXCantidad);
		}else {
			tj = new TarjetaAlmacen(metodoInventario);
		}
	}
	//Seleccion del menu
	public void Menu() {
		char input = ' ';
		while(input != 'E') {
			System.out.println(ImpresionOpciones());
			input = sc.next().charAt(0);
			int cantidad = 0;
			switch(input){
			case 'C':
				//Adquisicion de articulos
				System.out.println("Ingrese la cantidad de articulos: ");
				cantidad = sc.nextInt();
				System.out.println("\nIngrese el precio por articulo: ");
				Double precioXCantidad = sc.nextDouble();
				tj.AdquisicionInventario(String.format("%-2s", "C"), cantidad, precioXCantidad);
				System.out.println(tj.getMsg());
				break;
			case 'M':
				//Impresion de la tarjeta de almacen
				System.out.println(tj.lecturaFile());
				System.out.println(tj.getMsg());
				break;
			case 'V':
				//Venta de articulos
				System.out.println("Ingrese la cantidad a vender: ");
				cantidad = sc.nextInt();
				tj.VentaArticulo(cantidad);
				System.out.println(tj.getMsg());
				break;
			case 'E': 
				break;
			default:
				System.out.println("Indique la opcion correcta");
			}
		}
		tj.CerrarFile();
	}
	//Impresion menu
	public String ImpresionOpciones() {
		return String.format("Digite la opcion que desee; \nC - Compra mercancia\nV- Venta mercancia\nD - Devolucion\nE - Salir\nG - Generar .CVS\nM - Mostrar tarjeta de almacen", "");		}
}

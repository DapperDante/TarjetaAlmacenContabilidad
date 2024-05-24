package tarjetaAlmacen;
import java.io.*;
import java.util.Scanner;
public class AplicacionTarjetaAlmacen {
	public static void main(String [] args) {
		new File("Tarjeta_Almacen.DAT").delete();
		SeleccionControlTarjetaAlmacen control = new SeleccionControlTarjetaAlmacen();
		//Seleccion del metodo de gestion de inventario
		String metodoInventario = control.SeleccionMetodoTarjeta();
		//Seleccion para saldo incial
		control.InventarioIncial(metodoInventario);
		//Menu
		control.Menu();
	}
}
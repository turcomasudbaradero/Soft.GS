package model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FacturaTest {
    
    @Test
    void calculaTotalPermiteQuitarDetalleEnBorrador() {
        Cliente cliente = new Cliente("C-FACT", "Perez", "Ana", "12345678",
                "Calle 2", "222", "ana@test.com");
        Proveedor proveedor = new Proveedor("P-FACT", "20-22222222-2",
                "Proveedor Demo", "Calle 1", "111", "proveedor@test.com");
        Rubro rubro = new Rubro("R-FACT", "Perifericos", "Demo");
        Articulo articulo = new Articulo("A-FACT", "Mouse", rubro,
                proveedor, 10, 2, new BigDecimal("150.00"));

        Factura factura = new Factura("A-0001-00000001", cliente, "Prueba unitaria");
        DetalleFactura detalle = new DetalleFactura(articulo, 2, articulo.getPrecioUnitario());
        factura.agregarDetalle(detalle);
        assertEquals(new BigDecimal("300.00"), factura.calcularTotal());

        factura.quitarDetalle(detalle);
        assertEquals(BigDecimal.ZERO.setScale(2), factura.calcularTotal());
        assertTrue(factura.getDetalles().isEmpty());
    }
}
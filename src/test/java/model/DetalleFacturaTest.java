package model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DetalleFacturaTest {
    
    @Test
    void calculaSubtotalConPrecioHistorico() {
        Rubro rubro = new Rubro("R-TEST", "Perifericos", "Prueba");
        Proveedor proveedor = new Proveedor("P-TEST", "20-12345678-9",
                "Proveedor de prueba", "Direccion", "123", "test@example.com");
        Articulo articulo = new Articulo("A-TEST", "Teclado", rubro, proveedor,
                10, 2, new BigDecimal("200.00"));
        DetalleFactura detalle = new DetalleFactura(articulo, 3,
                new BigDecimal("200.00"));
        articulo.actualizarPrecio(new BigDecimal("250.00"));
        assertEquals(new BigDecimal("600.00"), detalle.calcularSubtotal());
    }
}
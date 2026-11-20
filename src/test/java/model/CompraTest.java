package model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CompraTest {
    
    @Test
    void sumaSubtotalesYNoPermiteConfirmarSinDetalles() {
        Proveedor proveedor = new Proveedor("P-COMPRA", "20-11111111-1",
                "Proveedor Demo", "Calle 1", "111", "proveedor@test.com");
        Compra compra = new Compra("FC-0001", proveedor, "Prueba unitaria");
        assertThrows(IllegalStateException.class, compra::marcarConfirmada);

        Rubro rubro = new Rubro("R-COMPRA", "Perifericos", "Demo");
        Articulo articulo = new Articulo("A-COMPRA", "Teclado", rubro,
                proveedor, 4, 1, new BigDecimal("100.00"));
        compra.agregarDetalle(new DetalleCompra(articulo, 3, new BigDecimal("80.00")));
        assertEquals(new BigDecimal("240.00"), compra.calcularTotal());
        assertEquals(1, compra.getDetalles().size());
    }
}
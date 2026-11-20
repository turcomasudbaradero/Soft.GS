package model;

import exception.StockInsuficienteException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArticuloTest {
    
    private Articulo crearArticulo(int stock, int minimo) {
        Rubro rubro = new Rubro("R-TEST", "Perifericos", "Prueba");
        Proveedor proveedor = new Proveedor("P-TEST", "20-12345678-9",
                "Proveedor de prueba", "Direccion", "123", "test@example.com");
        return new Articulo("A-TEST", "Mouse", rubro, proveedor,
                stock, minimo, new BigDecimal("1000.00"));
    }

    @Test
    void incrementaStock() {
        Articulo a = crearArticulo(5, 2);
        a.incrementarStock(3);
        assertEquals(8, a.getStockActual());
    }

    @Test
    void noPermiteDescontarMasQueElStock() {
        Articulo a = crearArticulo(2, 1);
        assertThrows(StockInsuficienteException.class, () -> a.descontarStock(3));
        assertEquals(2, a.getStockActual());
    }

    @Test
    void detectaStockBajoAlLlegarAlMinimo() {
        Articulo a = crearArticulo(3, 2);
        a.descontarStock(1);
        assertTrue(a.estaEnStockBajo());
    }
}
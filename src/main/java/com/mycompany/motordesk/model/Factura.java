package com.mycompany.motordesk.model;

import java.sql.Timestamp;

/**
 * Clase Factura: representa la entidad de facturación en el sistema.
 * Contiene la información económica y legal sobre el cobro de una orden de trabajo finalizada.
 */
public class Factura {
    // Identificador único de la factura generada (llave primaria)
    private int idFactura;
    // Identificador de la orden de trabajo que se está facturando (llave foránea)
    private int idOrdenFk;
    // Documento del empleado (cajero o administrador) que genera la factura (llave foránea)
    private String docEmpleFk;
    // Número o código oficial de la factura entregada al cliente
    private String numeroFactura;
    // Fecha y hora exacta de emisión de la factura
    private java.sql.Timestamp fechaEmision;
    // Subtotal a cobrar antes de aplicar los impuestos
    private double subtotal;
    // Valor del impuesto (IVA) aplicado sobre el subtotal
    private double iva;
    // Monto total final que debe pagar el cliente (subtotal + iva)
    private double total;
    // Método por el cual el cliente realizó el pago (ej. 'Efectivo', 'Tarjeta', 'Transferencia')
    private String metodoPago;
    // Estado actual de la factura (ej. 'Pagada', 'Pendiente', 'Anulada')
    private String estado;

    public Factura() {}

    public Factura(int idFactura, int idOrdenFk, String docEmpleFk, String numeroFactura, java.sql.Timestamp fechaEmision, double subtotal, double iva, double total, String metodoPago, String estado) {
        this.idFactura = idFactura;
        this.idOrdenFk = idOrdenFk;
        this.docEmpleFk = docEmpleFk;
        this.numeroFactura = numeroFactura;
        this.fechaEmision = fechaEmision;
        this.subtotal = subtotal;
        this.iva = iva;
        this.total = total;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    public int getIdFactura() { return idFactura; }
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; }

    public int getIdOrdenFk() { return idOrdenFk; }
    public void setIdOrdenFk(int idOrdenFk) { this.idOrdenFk = idOrdenFk; }

    public String getDocEmpleFk() { return docEmpleFk; }
    public void setDocEmpleFk(String docEmpleFk) { this.docEmpleFk = docEmpleFk; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public java.sql.Timestamp getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(java.sql.Timestamp fechaEmision) { this.fechaEmision = fechaEmision; }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}

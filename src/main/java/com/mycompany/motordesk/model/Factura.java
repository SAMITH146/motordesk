package com.mycompany.motordesk.model; // Paquete de la capa de modelos (entidades del negocio)

import java.sql.Timestamp; // Tipo de dato para representar la fecha y hora exactas de emision

/**
 * Clase Factura: representa la entidad de facturacion en el sistema.
 * Contiene la informacion economica y legal sobre el cobro de una orden de trabajo finalizada.
 */
public class Factura { // Entidad que representa una factura generada por el cobro de una orden de trabajo
    // Identificador unico de la factura generada (llave primaria)
    private int idFactura; // ID autoincremental asignado por la base de datos

    // Identificador de la orden de trabajo que se esta facturando (llave foranea)
    private int idOrdenFk; // Vincula la factura con su orden de trabajo correspondiente

    // Documento del empleado (cajero o administrador) que genera la factura (llave foranea)
    private String docEmpleFk; // Identifica al empleado responsable de emitir la factura

    // Numero o codigo oficial de la factura entregada al cliente
    private String numeroFactura; // Numero unico de factura para registro contable

    // Fecha y hora exacta de emision de la factura
    private java.sql.Timestamp fechaEmision; // Momento preciso en que se genero la factura

    // Subtotal a cobrar antes de aplicar los impuestos
    private double subtotal; // Suma de servicios y repuestos antes del IVA

    // Valor del impuesto (IVA) aplicado sobre el subtotal
    private double iva; // Porcentaje de impuesto calculado sobre el subtotal

    // Monto total final que debe pagar el cliente (subtotal + iva)
    private double total; // Valor definitivo que cancela el cliente

    // Metodo por el cual el cliente realizo el pago (ej. 'Efectivo', 'Tarjeta', 'Transferencia')
    private String metodoPago; // Forma de pago registrada para esta factura

    // Estado actual de la factura (ej. 'Pagada', 'Pendiente', 'Anulada')
    private String estado; // Indica si la factura ha sido pagada, esta pendiente o fue anulada

    public Factura() {} // Constructor vacio, necesario para instanciar el objeto sin datos iniciales

    public Factura(int idFactura, int idOrdenFk, String docEmpleFk, String numeroFactura, java.sql.Timestamp fechaEmision, double subtotal, double iva, double total, String metodoPago, String estado) { // Constructor completo para crear una factura con todos sus datos desde la BD
        this.idFactura = idFactura; // Asigna el ID de la factura
        this.idOrdenFk = idOrdenFk; // Asigna la referencia a la orden de trabajo
        this.docEmpleFk = docEmpleFk; // Asigna el documento del empleado que emitio la factura
        this.numeroFactura = numeroFactura; // Asigna el numero de factura
        this.fechaEmision = fechaEmision; // Asigna la fecha y hora de emision
        this.subtotal = subtotal; // Asigna el subtotal sin impuestos
        this.iva = iva; // Asigna el valor del IVA
        this.total = total; // Asigna el total con impuestos incluidos
        this.metodoPago = metodoPago; // Asigna el metodo de pago utilizado
        this.estado = estado; // Asigna el estado actual de la factura
    }

    public int getIdFactura() { return idFactura; } // Retorna el ID de la factura
    public void setIdFactura(int idFactura) { this.idFactura = idFactura; } // Establece el ID de la factura

    public int getIdOrdenFk() { return idOrdenFk; } // Retorna el ID de la orden vinculada
    public void setIdOrdenFk(int idOrdenFk) { this.idOrdenFk = idOrdenFk; } // Vincula la factura a una orden de trabajo

    public String getDocEmpleFk() { return docEmpleFk; } // Retorna el documento del empleado emisor
    public void setDocEmpleFk(String docEmpleFk) { this.docEmpleFk = docEmpleFk; } // Asigna el empleado que emitio la factura

    public String getNumeroFactura() { return numeroFactura; } // Retorna el numero de factura
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; } // Establece el numero de factura

    public java.sql.Timestamp getFechaEmision() { return fechaEmision; } // Retorna la fecha y hora de emision
    public void setFechaEmision(java.sql.Timestamp fechaEmision) { this.fechaEmision = fechaEmision; } // Establece la fecha de emision

    public double getSubtotal() { return subtotal; } // Retorna el subtotal antes de impuestos
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; } // Establece el subtotal

    public double getIva() { return iva; } // Retorna el valor del IVA
    public void setIva(double iva) { this.iva = iva; } // Establece el IVA calculado

    public double getTotal() { return total; } // Retorna el total final a pagar
    public void setTotal(double total) { this.total = total; } // Establece el total incluyendo impuestos

    public String getMetodoPago() { return metodoPago; } // Retorna el metodo de pago registrado
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; } // Establece el metodo de pago

    public String getEstado() { return estado; } // Retorna el estado actual de la factura
    public void setEstado(String estado) { this.estado = estado; } // Actualiza el estado de la factura
}

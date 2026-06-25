package com.mycompany.motordesk.model;

import java.sql.Timestamp;

/**
 * Clase que representa la entidad Bitacora.
 * Se utiliza para registrar o auditar las acciones y eventos importantes que realizan los usuarios dentro del sistema.
 */
public class Bitacora {
    // Identificador único de la bitácora (llave primaria)
    private int idBitacora;
    // Documento del empleado que realizó la acción (llave foránea relacionada con el empleado)
    private String docEmpleFk;
    // Nombre del usuario que ejecutó la acción en el sistema
    private String nombreUsuario;
    // Descripción de la acción realizada (por ejemplo: 'INSERTAR', 'ACTUALIZAR', 'ELIMINAR')
    private String accion;
    // Detalles adicionales o descripción profunda sobre el evento ocurrido
    private String detalle;
    // Fecha y hora exacta en la que se registró el evento en el sistema
    private Timestamp fechaHora;

    public Bitacora() {}

    public Bitacora(int idBitacora, String docEmpleFk, String nombreUsuario, String accion, String detalle, Timestamp fechaHora) {
        this.idBitacora = idBitacora;
        this.docEmpleFk = docEmpleFk;
        this.nombreUsuario = nombreUsuario;
        this.accion = accion;
        this.detalle = detalle;
        this.fechaHora = fechaHora;
    }

    public int getIdBitacora() { return idBitacora; }
    public void setIdBitacora(int idBitacora) { this.idBitacora = idBitacora; }

    public String getDocEmpleFk() { return docEmpleFk; }
    public void setDocEmpleFk(String docEmpleFk) { this.docEmpleFk = docEmpleFk; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public Timestamp getFechaHora() { return fechaHora; }
    public void setFechaHora(Timestamp fechaHora) { this.fechaHora = fechaHora; }
}

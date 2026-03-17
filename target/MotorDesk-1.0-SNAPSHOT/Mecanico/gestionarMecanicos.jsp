<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Gestionar Mecánicos</title>
    </head>
    <body>

        <h2>Registrar Mecánico</h2>

        <form action="${pageContext.request.contextPath}/MecanicoController" method="post">

            <label>Documento:</label><br>
            <input type="number" name="doc_emple" required step="1" min="0">

            <label>Nombre:</label><br>
            <input type="text" name="nom_empleado" required><br><br>

            <label>PIN:</label><br>
            <input type="password" name="pin_acceso" maxlength="10" required><br><br>

            <!-- ocultos porque el admin crea mecánicos -->
            <input type="hidden" name="id_rol_fk" value="2">
            <input type="hidden" name="id_cargo_fk" value="2">

            <button type="submit">Guardar Mecánico</button>

        </form>




    </body>
</html>
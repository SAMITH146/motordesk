<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css" />
    <title>Historial de Usuario (Bitácora) | MotorDesk</title>
    <style>
        .filter-container {
            display: flex;
            gap: 15px;
            margin-bottom: 20px;
            flex-wrap: wrap;
        }
        .filter-input {
            padding: 10px 15px;
            border-radius: 8px;
            border: 1px solid rgba(255, 255, 255, 0.1);
            background: rgba(0, 0, 0, 0.2);
            color: #fff;
            font-size: 0.9rem;
            width: 250px;
        }
        .filter-input::placeholder {
            color: #94a3b8;
        }
        .badge {
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 0.75rem;
            font-weight: 600;
        }
        .badge-login { background: rgba(59, 130, 246, 0.2); border: 1px solid #3b82f6; color: #3b82f6; }
        .badge-create { background: rgba(16, 185, 129, 0.2); border: 1px solid #10b981; color: #10b981; }
        .badge-update { background: rgba(245, 158, 11, 0.2); border: 1px solid #f59e0b; color: #f59e0b; }
        .badge-delete { background: rgba(239, 68, 68, 0.2); border: 1px solid #ef4444; color: #ef4444; }
        .badge-billing { background: rgba(168, 85, 247, 0.2); border: 1px solid #a855f7; color: #a855f7; }
    </style>
</head>
<body>
    <header class="navbar">
        <div class="navbar__logo">
            <img src="${pageContext.request.contextPath}/LogoI_mg/Logo_blanco.png" alt="Logo MotorDesk" class="navbar__logo-img" />
        </div>
        <nav class="navbar__menu" aria-label="Menu principal">
            <a href="${pageContext.request.contextPath}/AdminDashboard" class="navbar__menu-item">Dashboard</a>
            <a href="${pageContext.request.contextPath}/MecanicoController" class="navbar__menu-item">Mecánicos</a>
            <a href="${pageContext.request.contextPath}/ClienteController" class="navbar__menu-item">Clientes</a>
            <a href="${pageContext.request.contextPath}/ProveedorController" class="navbar__menu-item">Proveedores</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>

            <a href="${pageContext.request.contextPath}/BitacoraController" class="navbar__menu-item active">Auditoría</a>
        </nav>
        <div class="navbar__session">
            <div class="navbar__user-info">
                <span class="navbar__user-name">${sessionScope.usuarioLogueado.nombre}</span>
            </div>
            <a href="#logoutModal" class="navbar__session-btn">
                <img src="${pageContext.request.contextPath}/LogoI_mg/cerrarseccion_blanco.png" alt="Cerrar sesión" class="navbar__session-icon" />
            </a>
        </div>
    </header>

    <main class="admin-main fade-in">
        <header class="admin-section__header">
            <h1 class="admin-section__title">Auditoría / Bitácora de Acciones</h1>
            <p class="admin-section__subtitle">Registro de operaciones críticas y accesos al sistema.</p>
        </header>

        <div class="filter-container">
            <input type="text" id="searchInput" class="filter-input" placeholder="Buscar por usuario o detalle..." onkeyup="filtrarTabla()" />
            <select id="actionFilter" class="filter-input" onchange="filtrarTabla()" style="width: 200px;">
                <option value="">Todas las acciones</option>
                <option value="LOGIN">LOGIN</option>
                <option value="CREAR">CREAR</option>
                <option value="MODIFICAR">MODIFICAR</option>
                <option value="ELIMINAR">ELIMINAR</option>
                <option value="FACTURACION">FACTURACION</option>
            </select>
        </div>

        <section class="admin-card">
            <div style="overflow-x: auto;">
                <!-- Tabla que despliega el registro histórico de todas las acciones (Bitácora) -->
                <table class="admin-table" id="bitacoraTable">
                    <thead>
                        <tr>
                            <th style="width: 80px;">ID</th>
                            <th style="width: 150px;">Documento</th>
                            <th style="width: 200px;">Usuario</th>
                            <th style="width: 150px;">Acción</th>
                            <th>Detalle</th>
                            <th style="width: 200px;">Fecha y Hora</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <%-- Bucle que recorre la listaBitacora y dibuja una fila por cada evento auditado, asignando un estilo visual según la acción --%>
                            <c:when test="${not empty listaBitacora}">
                                <c:forEach var="log" items="${listaBitacora}">
                                    <tr>
                                        <td>#${log.idBitacora}</td>
                                        <td><strong>${not empty log.docEmpleFk ? log.docEmpleFk : 'N/A'}</strong></td>
                                        <td>${not empty log.nombreUsuario ? log.nombreUsuario : 'Sistema'}</td>
                                        <td>
                                            <c:set var="actionUpper" value="${log.accion.toUpperCase()}" />
                                            <c:choose>
                                                <c:when test="${actionUpper == 'LOGIN'}">
                                                    <span class="badge badge-login">${log.accion}</span>
                                                </c:when>
                                                <c:when test="${actionUpper.contains('CREAR')}">
                                                    <span class="badge badge-create">${log.accion}</span>
                                                </c:when>
                                                <c:when test="${actionUpper.contains('MODIFICAR') || actionUpper.contains('ACTUALIZAR')}">
                                                    <span class="badge badge-update">${log.accion}</span>
                                                </c:when>
                                                <c:when test="${actionUpper.contains('ELIMINAR') || actionUpper.contains('BORRAR')}">
                                                    <span class="badge badge-delete">${log.accion}</span>
                                                </c:when>
                                                <c:when test="${actionUpper.contains('FACTURA')}">
                                                    <span class="badge badge-billing">${log.accion}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge" style="background: rgba(255, 255, 255, 0.1); color: #fff;">${log.accion}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>${log.detalle}</td>
                                        <td style="color: #94a3b8; font-size: 0.85rem;">
                                            <fmt:formatDate value="${log.fechaHora}" pattern="dd/MM/yyyy hh:mm:ss a" />
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" style="text-align: center; font-style: italic; opacity: 0.6; padding: 2rem;">
                                        No se han registrado acciones en la bitácora todavía.
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>
    </main>

    <div id="logoutModal" class="modal-css">
        <div class="modal-content-css">
            <h2>¿Cerrar Sesión?</h2>
            <p>Estás a punto de salir del sistema.<br>¿Estás seguro?</p>
            <div class="modal-buttons-css">
                <a href="#" class="btn-modal-css btn-modal-css--cancel">No, quedarme</a>
                <a href="${pageContext.request.contextPath}/LogoutController" class="btn-modal-css btn-modal-css--confirm">Sí, salir</a>
            </div>
        </div>
    </div>

    <script>
        function filtrarTabla() {
            var searchVal = document.getElementById("searchInput").value.toLowerCase();
            var actionVal = document.getElementById("actionFilter").value.toUpperCase();
            var table = document.getElementById("bitacoraTable");
            var rows = table.getElementsByTagName("tbody")[0].getElementsByTagName("tr");

            for (var i = 0; i < rows.length; i++) {
                var row = rows[i];
                if (row.getElementsByTagName("td").length < 6) continue;

                var doc = row.getElementsByTagName("td")[1].textContent.toLowerCase();
                var user = row.getElementsByTagName("td")[2].textContent.toLowerCase();
                var action = row.getElementsByTagName("td")[3].textContent.toUpperCase();
                var detail = row.getElementsByTagName("td")[4].textContent.toLowerCase();

                var matchesSearch = doc.includes(searchVal) || user.includes(searchVal) || detail.includes(searchVal);
                var matchesAction = actionVal === "" || action.includes(actionVal);

                if (matchesSearch && matchesAction) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            }
        }
    </script>
</body>
</html>

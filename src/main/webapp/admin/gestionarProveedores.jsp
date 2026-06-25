<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.1" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css" />

    <title>Gestión de Proveedores | MotorDesk</title>
    <style>
        .admin-alert {
            padding: 1rem;
            margin-bottom: 1.5rem;
            border-radius: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            animation: slideIn 0.3s ease-out;
        }
        .admin-alert--success { background-color: rgba(46, 204, 113, 0.2); border: 1px solid #2ecc71; color: #2ecc71; }
        .admin-alert--error { background-color: rgba(231, 76, 60, 0.2); border: 1px solid #e74c3c; color: #e74c3c; }
        .admin-alert--info { background-color: rgba(52, 152, 219, 0.2); border: 1px solid #3498db; color: #3498db; }
        .admin-alert__close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; opacity: 0.7; }
        .admin-alert__close:hover { opacity: 1; }
        @keyframes slideIn { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
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
            <a href="${pageContext.request.contextPath}/ProveedorController" class="navbar__menu-item active">Proveedores</a>
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item">Productos</a>
            <a href="${pageContext.request.contextPath}/admin/ingreso" class="navbar__menu-item">Ingresar Pedido</a>
            <a href="${pageContext.request.contextPath}/admin/historialCompras" class="navbar__menu-item">Historial Compras</a>
            <a href="${pageContext.request.contextPath}/OrdenController?action=listAll" class="navbar__menu-item">Órdenes</a>

            <a href="${pageContext.request.contextPath}/BitacoraController" class="navbar__menu-item">Auditoría</a>
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
        <!-- Alertas de feedback -->
        <c:if test="${not empty sessionScope.mensaje}">
            <div class="admin-alert admin-alert--${sessionScope.tipoMensaje}">
                <span class="admin-alert__text">${sessionScope.mensaje}</span>
                <button class="admin-alert__close" onclick="this.parentElement.remove();">×</button>
            </div>
            <% session.removeAttribute("mensaje"); session.removeAttribute("tipoMensaje"); %>
        </c:if>


                    <div class="admin-form__group">
                        <label class="admin-form__label" for="nombre_proveedor">Empresa / Nombre (*):</label>
                        <!-- Aquí renderizamos el input para capturar el nombre de la empresa proveedora -->
                        <input class="admin-form__input" type="text" id="nombre_proveedor" name="nombre_proveedor" required
                            placeholder="Ej: Repuestos El Chamo" value="${requestScope.proveedorEditar.nombreProveedor}">
                    </div>

                    <div class="admin-form__group">
                        <label class="admin-form__label" for="contacto">Nombre del Contacto:</label>
                        <!-- Aquí renderizamos el input para el nombre del contacto principal del proveedor -->
                        <input class="admin-form__input" type="text" id="contacto" name="contacto"
                            placeholder="Ej: Juan Pérez" value="${requestScope.proveedorEditar.contacto}">
                    </div>

                    <div class="admin-form__group">
                        <label class="admin-form__label" for="telefono">Teléfono:</label>
                        <input class="admin-form__input" type="text" id="telefono" name="telefono"
                            placeholder="Ej: 3001234567" value="${requestScope.proveedorEditar.telefono}"
                            pattern="[0-9]{7,15}" title="Ingrese un número de teléfono válido">
                    </div>

                    <div class="admin-form__group">
                        <label class="admin-form__label" for="correo">Correo Electrónico:</label>
                        <input class="admin-form__input" type="email" id="correo" name="correo"
                            placeholder="Ej: ventas@empresa.com" value="${requestScope.proveedorEditar.correo}">
                    </div>

                    <div class="admin-form__actions">
                        <c:choose>
                            <c:when test="${not empty requestScope.proveedorEditar}">
                                <a href="${pageContext.request.contextPath}/ProveedorController" class="admin-btn admin-btn--danger" style="text-decoration: none; display: flex; align-items: center; justify-content: center;">Cancelar</a>
                                <button type="submit" class="admin-btn">Actualizar Cambios</button>
                            </c:when>
                            <c:otherwise>
                                <button type="reset" class="admin-btn admin-btn--danger">Limpiar</button>
                                <button type="submit" class="admin-btn">Registrar Proveedor</button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </form>
            </article>
        </section>
    </main>

    <!-- Modales de Eliminación -->
    <%-- Aquí iteramos a través de la lista de proveedores para generar los modales de eliminación correspondientes --%>
    <c:forEach var="prov" items="${requestScope.listaProveedores}">
        <div id="deleteModal${prov.idProveedor}" class="modal-css">
            <div class="modal-content-css">
                <h2>¿Eliminar Proveedor?</h2>
                <p>Estás a punto de eliminar a <strong>${prov.nombreProveedor}</strong>.<br>Esta acción no se puede deshacer.</p>
                <%-- Aquí renderizamos el formulario que ejecuta un POST para eliminar a este proveedor específico --%>
                <form action="${pageContext.request.contextPath}/ProveedorController" method="post" class="modal-buttons-css">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="id_proveedor" value="${prov.idProveedor}">
                    <a href="#" class="btn-modal-css btn-modal-css--cancel">Cancelar</a>
                    <button type="submit" class="btn-modal-css btn-modal-css--confirm" style="border:none; font-family:inherit; font-size:inherit; cursor:pointer;">Eliminar</button>
                </form>
            </div>
        </div>
    </c:forEach>

    <!-- Log out Modal -->
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
</body>
</html>


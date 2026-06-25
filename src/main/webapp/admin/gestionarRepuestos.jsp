<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/variables.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/navbar.css?v=1.2" />
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-panel.css?v=1.2" />
    <style>
        .admin-alert { padding: 1rem; margin-bottom: 1.5rem; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; animation: slideIn 0.3s ease-out; }
        .admin-alert--success { background-color: rgba(46, 204, 113, 0.2); border: 1px solid #2ecc71; color: #2ecc71; }
        .admin-alert--error { background-color: rgba(231, 76, 60, 0.2); border: 1px solid #e74c3c; color: #e74c3c; }
        .admin-alert__close { background: none; border: none; font-size: 1.2rem; cursor: pointer; color: inherit; opacity: 0.7; }
        @keyframes slideIn { from { transform: translateY(-20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
    </style>
    <title>Gestion de Productos | MotorDesk</title>
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
            <a href="${pageContext.request.contextPath}/ProductoController" class="navbar__menu-item active">Productos</a>
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
        <header class="admin-section__header">
            <h1 class="admin-section__title">Inventario de Productos</h1>
            <p class="admin-section__subtitle">Gestion centralizada de productos y herramientas del taller.</p>
        </header>

        <!-- Feedback Alert -->
        <c:if test="${not empty sessionScope.mensaje}">
            <div class="admin-alert admin-alert--${sessionScope.tipoMensaje}">
                <span class="admin-alert__text">${sessionScope.mensaje}</span>
                <button class="admin-alert__close" onclick="this.parentElement.remove();">x</button>
                                            <span style="font-weight: bold; color: #3498db;">${p.seccion != null ? p.seccion : 'General'}</span>
                                        </td>
                                        <td class="admin-table__td">
                                            <span class="admin-badge ${p.stock <= 5 ? 'admin-badge--inactive' : 'admin-badge--active'}">
                                                ${p.stock} uds.
                                            </span>
                                        </td>
                                        <td class="admin-table__td"><fmt:formatNumber value="${p.precioUnitario}" type="currency" currencySymbol="$" /></td>
                                        <td class="admin-table__td">
                                            <div class="admin-table__actions">
                                                <a href="${pageContext.request.contextPath}/ProductoController?action=edit&id=${p.idProducto}" class="admin-action-btn admin-action-btn--edit">Editar</a>
                                                <a href="#deleteModalProd-${p.idProducto}" class="admin-action-btn admin-action-btn--delete">Borrar</a>

                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr class="admin-table__row">
                                    <td class="admin-table__td" colspan="6" style="text-align: center;">No hay productos registrados.</td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </section>

        <!-- Delete Modals (Renderizados fuera de la tabla para evitar problemas de z-index) -->
        <c:if test="${not empty requestScope.listaProductos}">
            <%-- Aquí iteramos la lista de productos (repuestos) para crear dinámicamente cada modal de eliminación --%>
            <c:forEach var="p" items="${requestScope.listaProductos}">
                <div id="deleteModalProd-${p.idProducto}" class="modal-css">
                    <div class="modal-content-css">
                        <h2>¿Eliminar Producto?</h2>
                        <p>Estás a punto de eliminar <strong>${p.nombreProducto}</strong>.<br>Esta acción no se puede deshacer.</p>
                        <div class="modal-buttons-css">
                            <a href="#" class="btn-modal-css btn-modal-css--cancel">Cancelar</a>
                            <%-- Este formulario envía el POST que confirma la eliminación del producto seleccionado en el modal --%>
                            <form action="${pageContext.request.contextPath}/ProductoController" method="post" style="display:inline;">
                                <input type="hidden" name="action" value="delete" />
                                <input type="hidden" name="id" value="${p.idProducto}" />
                                <button type="submit" class="btn-modal-css btn-modal-css--confirm">Sí, Eliminar</button>
                            </form>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </c:if>

    </main>

    <!-- Log out Modal (CSS Only) -->
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

